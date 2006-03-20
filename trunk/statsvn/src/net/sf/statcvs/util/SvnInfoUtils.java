package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utilities class that manages calls to svn info. Used to find repository information, latest revision numbers, and directories.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * 
 * @version $Id$
 */
public class SvnInfoUtils {

    /**
     * SAX parser for the svn info --xml command.
     * 
     * @author jkealey
     */
    protected static class SvnInfoHandler extends DefaultHandler {

        protected boolean isRootFolder = false;
        protected String sCurrentKind;
        protected String sCurrentRevision;
        protected String sCurrentUrl;
        protected String stringData = "";

        /**
         * Builds the string that was read; default implementation can invoke this function multiple times while reading the data.
         */
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringData += new String(ch, start, length);
        }

        /**
         * End of xml element.
         */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String eName = localName; // element name
            if ("".equals(eName))
                eName = qName; // namespaceAware = false

            if (isRootFolder && eName.equals("url")) {
                isRootFolder = false;
                setRootUrl(stringData);
                sCurrentUrl = stringData;
            } else if (eName.equals("url")) {
                sCurrentUrl = stringData;
            } else if (eName.equals("entry")) {
                if (sCurrentRevision == null || sCurrentUrl == null || sCurrentKind == null)
                    throw new SAXException("Invalid svn info xml; unable to find revision or url");

                hmRevisions.put(urlToRelativePath(sCurrentUrl), sCurrentRevision);
                if (sCurrentKind.equals("dir"))
                    hsDirectories.add(urlToRelativePath(sCurrentUrl));
            } else if (eName.equals("uuid")) {
            	sRepositoryUuid = stringData;
            } else if (eName.equals("root"))
            	setRepositoryUrl(stringData);
        }

        /**
         * Start of XML element.
         */
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String eName = localName; // element name
            if ("".equals(eName))
                eName = qName; // namespaceAware = false

            if (eName.equals("entry")) {
                if (!isValidInfoEntry(attributes))
                    throw new SAXException("Invalid svn info xml for entry element.");

                if (sRootUrl == null && isRootFolder(attributes)) {
                    isRootFolder = true;
                    sRootRevisionNumber = attributes.getValue("revision");
                }

                sCurrentRevision = null;
                sCurrentUrl = null;
                sCurrentKind = attributes.getValue("kind");
            } else if (eName.equals("commit")) {
                if (!isValidCommit(attributes))
                    throw new SAXException("Invalid svn info xml for commit element.");
                sCurrentRevision = attributes.getValue("revision");
            } 

            stringData = "";
        }

        /**
         * Is this the root of the workspace?
         * 
         * @param attributes
         *            the xml attributes
         * @return true if is the root folder.
         */
        private static boolean isRootFolder(Attributes attributes) {
            return attributes.getValue("path").equals(".") && attributes.getValue("kind").equals("dir");
        }

        /**
         * Is this a valid commit? Check to see if wec an read the revision number.
         * 
         * @param attributes
         *            the xml attributes
         * @return true if is a valid commit.
         */
        private static boolean isValidCommit(Attributes attributes) {
            return attributes != null && attributes.getValue("revision") != null;
        }

        /**
         * Is this a valid info entry? Check to see if we can read path, kind and revision.
         * 
         * @param attributes
         *            the xml attributes.
         * @return true if is a valid info entry.
         */
        private static boolean isValidInfoEntry(Attributes attributes) {
            return attributes != null && attributes.getValue("path") != null && attributes.getValue("kind") != null && attributes.getValue("revision") != null;
        }
    }

    // enable caching to speed up calculations
    protected static final boolean ENABLE_CACHING = true;

    // relative path -> Revision Number
    protected static HashMap hmRevisions;

    // if HashSet contains relative path, path is a directory.
    protected static HashSet hsDirectories;

    // Path of . in repository. Can only be calculated if given an element from
    // the SVN log.
    protected static String sModuleName = null;

    // Revision number of root folder (.)
    protected static String sRootRevisionNumber = null;

    // URL of root (.)
    protected static String sRootUrl = null;
    
    // UUID of repository
    protected static String sRepositoryUuid = null;
    
    // URL of repository
    protected static String sRepositoryUrl = null;

    /**
     * Converts an absolute path in the repository to a path relative to the working folder root.
     * 
     * Will return null if absolute path does not start with getModuleName();
     * 
     * @param absolute
     *            Example (assume getModuleName() returns /trunk/statsvn) /trunk/statsvn/package.html
     * @return Example: package.html
     */
    public static String absoluteToRelativePath(String absolute) {
        if (absolute.endsWith("/"))
            absolute = absolute.substring(0, absolute.length() - 1);

        if (absolute.equals(getModuleName()))
            return ".";
        else if (!absolute.startsWith(getModuleName()))
            return null;
        else
            return absolute.substring(getModuleName().length() + 1);
    }

    /**
     * Converts an absolute path in the repository to a URL, using the repository URL
     * 
     * @param absolute
     *            Example: /trunk/statsvn/package.html
     * @return Example: svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
     */
    public static String absolutePathToUrl(String absolute) {
        return getRepositoryUrl() + (absolute.endsWith("/") ? absolute.substring(0, absolute.length() - 1) : absolute);
    }

    /**
     * Converts a relative path in the working folder to a URL, using the working folder's root URL
     * 
     * @param relative
     *            Example: src/Messages.java
     * @return Example: svn://svn.statsvn.org/statsvn/trunk/statsvn/src/Messages.java
     * 
     */
    public static String relativePathToUrl(String relative) {
        relative = relative.replace('\\', '/');
        if (relative.equals(".") || relative.length() == 0)
            return getRootUrl();
        else
            return getRootUrl() + "/" + (relative.endsWith("/") ? relative.substring(0, relative.length() - 1) : relative);
    }

    /**
     * Converts a relative path in the working folder to an absolute path in the repository.
     * 
     * @param relative
     *            Example: src/Messages.java
     * @return Example: /trunk/statsvn/src/Messages.java
     * 
     */
    public static String relativeToAbsolutePath(String relative) {
        return urlToAbsolutePath(relativePathToUrl(relative));
    }

    /**
     * Returns true if the file exists in the working copy (according to the svn metadata, and not file system checks).
     * 
     * @param relativePath
     *            the path
     * @return <tt>true</tt> if it exists
     */
    public static boolean existsInWorkingCopy(String relativePath) {
        return getRevisionNumber(relativePath) != null;
    }

    /**
     * Assumes #loadInfo(String) has been called. Never ends with /, might be empty.
     * 
     * @return The absolute path of the root of the working folder in the repository.
     */
    public static String getModuleName() {

        if (sModuleName == null) {
        	
        	if (getRootUrl().length()<getRepositoryUrl().length() || getRepositoryUrl().length()==0 ){
        		Logger logger = Logger.getLogger(SvnInfoUtils.class.getName());
        		logger.warning("Unable to process module name.");
        		sModuleName= "";
        	}
        	else
        		sModuleName = getRootUrl().substring(getRepositoryUrl().length());
        	
        }
        return sModuleName;
    }

    /**
     * Returns the revision number of the file in the working copy.
     * 
     * @param relativePath
     *            the filename
     * @return the revision number if it exists in the working copy, null otherwise.
     */
    public static String getRevisionNumber(String relativePath) {
        if (hmRevisions.containsKey(relativePath))
            return hmRevisions.get(relativePath).toString();
        else
            return null;
    }

    /**
     * Assumes #loadInfo() has been invoked.
     * 
     * @return the root of the working folder's revision number (last checked out revision number)
     */
    public static String getRootRevisionNumber() {
        return sRootRevisionNumber;
    }

    /**
     * Assumes #loadInfo() has been invoked.
     * 
     * @return the root of the working folder's url (example: svn://svn.statsvn.org/statsvn/trunk/statsvn)
     */
    public static String getRootUrl() {
        return sRootUrl;
    }

    /**
     * Assumes #loadInfo() has been invoked.
     * 
     * @return the uuid of the repository
     */
    public static String getRepositoryUuid() {
        return sRepositoryUuid;
    }
    
    /**
     * Assumes #loadInfo() has been invoked.
     * 
     * @return the repository url (example: svn://svn.statsvn.org/statsvn)
     */
    public static String getRepositoryUrl() {
    	return sRepositoryUrl;
    }

    /**
     * Invokes svn info.
     * 
     * @param bRootOnly
     *            true if should we check for the root only or false otherwise (recurse for all files)
     * @return the response.
     */
    protected synchronized static InputStream getSvnInfo(boolean bRootOnly) {
        InputStream istream = null;

        String svnInfoCommand = "svn info --xml";
        if (!bRootOnly)
            svnInfoCommand += " -R";

        try {
            hmRevisions = new HashMap();
            hsDirectories = new HashSet();
            Process p = Runtime.getRuntime().exec(svnInfoCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
            istream = new BufferedInputStream(p.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return istream;
    }

    /**
     * Returns true if the path has been identified as a directory.
     * 
     * @param relativePath
     *            the path
     * @return true if it is a known directory.
     */
    public static boolean isDirectory(String relativePath) {
        return hsDirectories.contains(relativePath);
    }


    /**
     * Adds a directory to the list of known directories. Used when inferring implicit actions on deleted paths.
     * 
     * @param relativePath
     *            the relative path.
     */
    public static void addDirectory(String relativePath) {
        if (!hsDirectories.contains(relativePath))
            hsDirectories.add(relativePath);
    }

    /**
     * Do we need to re-invoke svn info?
     * 
     * @param bRootOnly
     *            true if we need the root only
     * @return true if we it needs to be re-invoked.
     */
    protected static boolean isQueryNeeded(boolean bRootOnly) {
        return !ENABLE_CACHING || (bRootOnly && sRootUrl == null) || (!bRootOnly && hmRevisions == null);
    }

    /**
     * Loads the information from svn info if needed.
     * 
     * @param bRootOnly
     *            load only the root?
     * @throws LogSyntaxException
     *             if the format of the svn info is invalid
     * @throws IOException
     *             if we can't read from the response stream.
     */
    protected static void loadInfo(boolean bRootOnly) throws LogSyntaxException, IOException {
        if (isQueryNeeded(true)) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                parser.parse(getSvnInfo(bRootOnly), new SvnInfoHandler());
            } catch (ParserConfigurationException e) {
                throw new LogSyntaxException(e.getMessage());
            } catch (SAXException e) {
                throw new LogSyntaxException(e.getMessage());
            }

        }
    }

    /**
     * Initializes our representation of the repository. 
     * 
     * @throws LogSyntaxException
     *             if the svn info --xml is malformed
     * @throws IOException
     *             if there is an error reading from the stream
     */
    public static void loadInfo() throws LogSyntaxException, IOException {
        loadInfo(false);
    }
    

    /**
     * Converts a url to an absolute path in the repository.
     * 
     * @param url
     *            Examples: svn://svn.statsvn.org/statsvn/trunk/statsvn, svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
     * @return Example: /trunk/statsvn, /trunk/statsvn/package.html
     */
    public static String urlToAbsolutePath(String url) {
        if (url.endsWith("/"))
            url = url.substring(0, url.length() - 1);
        if (getModuleName().length() <= 1) {
            if (getRootUrl().equals(url))
                return "/";
            else
                return url.substring(getRootUrl().length());
        } else
            return url.substring(url.lastIndexOf(getModuleName()));
    }

    /**
     * Converts a url to a relative path in the repository.
     * 
     * @param url
     *            Examples: svn://svn.statsvn.org/statsvn/trunk/statsvn, svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
     * @return Example: ".", package.html
     */
    public static String urlToRelativePath(String url) {
        return absoluteToRelativePath(urlToAbsolutePath(url));
    }

    /**
     * Sets the project's root URL.
     * 
     * @param rootUrl
     */
    protected static void setRootUrl(String rootUrl) {
        if (rootUrl.endsWith("/"))
            sRootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        else
            sRootUrl = rootUrl;
        
        sModuleName=null;
    }
    
    /**
     * Sets the project's repository URL.
     * 
     * @param repositoryUrl
     */
    protected static void setRepositoryUrl(String repositoryUrl) {
    	if (repositoryUrl.endsWith("/"))
    		sRepositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 1);
        else
        	sRepositoryUrl = repositoryUrl;
    	
    	sModuleName=null;
    }    
}
