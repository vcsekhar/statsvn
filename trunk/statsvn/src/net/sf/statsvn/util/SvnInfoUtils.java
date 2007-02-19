package net.sf.statsvn.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statsvn.output.SvnConfigurationOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utilities class that manages calls to svn info. Used to find repository
 * information, latest revision numbers, and directories.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * 
 * @version $Id$
 */
public final class SvnInfoUtils {
	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private SvnInfoUtils() {
	}
	
	/**
	 * SAX parser for the svn info --xml command.
	 * 
	 * @author jkealey
	 */
	protected static class SvnInfoHandler extends DefaultHandler {

		private boolean isRootFolder = false;
		private String sCurrentKind;
		private String sCurrentRevision;
		private String sCurrentUrl;
		private String stringData = "";
		private String sCurrentPath;

		/**
		 * Builds the string that was read; default implementation can invoke
		 * this function multiple times while reading the data.
		 */
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			stringData += new String(ch, start, length);
		}

		/**
		 * End of xml element.
		 */
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			String eName = localName; // element name
			if ("".equals(eName)) {
				eName = qName; // namespaceAware = false
			}

			if (isRootFolder && eName.equals("url")) {
				isRootFolder = false;
				setRootUrl(stringData);
				sCurrentUrl = stringData;
			} else if (eName.equals("url")) {
				sCurrentUrl = stringData;
			} else if (eName.equals("entry")) {
				if (sCurrentRevision == null || sCurrentUrl == null || sCurrentKind == null) {
					throw new SAXException("Invalid svn info xml; unable to find revision or url for path [" 
							+ sCurrentPath + "]" + " revision="
							+ sCurrentRevision + " url:" + sCurrentUrl + " kind:" + sCurrentKind);
				}

				HM_REVISIONS.put(urlToRelativePath(sCurrentUrl), sCurrentRevision);
				if (sCurrentKind.equals("dir")) {
					HS_DIRECTORIES.add(urlToRelativePath(sCurrentUrl));
				}
			} else if (eName.equals("uuid")) {
				sRepositoryUuid = stringData;
			} else if (eName.equals("root")) {
				setRepositoryUrl(stringData);
			}
		}

		/**
		 * Start of XML element.
		 */
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
			String eName = localName; // element name
			if ("".equals(eName)) {
				eName = qName; // namespaceAware = false
			}

			if (eName.equals("entry")) {
				sCurrentPath = attributes.getValue("path");
				if (!isValidInfoEntry(attributes)) {
					throw new SAXException("Invalid svn info xml for entry element. Please verify that you have checked out this project using "
							+ "Subversion 1.3 or above, not only that you are currently using this version.");
				}

				if (sRootUrl == null && isRootFolder(attributes)) {
					isRootFolder = true;
					sRootRevisionNumber = attributes.getValue("revision");
				}

				sCurrentRevision = null;
				sCurrentUrl = null;
				sCurrentKind = attributes.getValue("kind");
			} else if (eName.equals("commit")) {
				if (!isValidCommit(attributes)) {
					throw new SAXException("Invalid svn info xml for commit element. Please verify that you have checked out this project using "
							+ "Subversion 1.3 or above, not only that you are currently using this version.");
				}
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
		private static boolean isRootFolder(final Attributes attributes) {
			return attributes.getValue("path").equals(".") && attributes.getValue("kind").equals("dir");
		}

		/**
		 * Is this a valid commit? Check to see if wec an read the revision
		 * number.
		 * 
		 * @param attributes
		 *            the xml attributes
		 * @return true if is a valid commit.
		 */
		private static boolean isValidCommit(final Attributes attributes) {
			return attributes != null && attributes.getValue("revision") != null;
		}

		/**
		 * Is this a valid info entry? Check to see if we can read path, kind
		 * and revision.
		 * 
		 * @param attributes
		 *            the xml attributes.
		 * @return true if is a valid info entry.
		 */
		private static boolean isValidInfoEntry(final Attributes attributes) {
			return attributes != null && attributes.getValue("path") != null && attributes.getValue("kind") != null 
				&& attributes.getValue("revision") != null;
		}
	}

	// enable caching to speed up calculations
	private static final boolean ENABLE_CACHING = true;

	// relative path -> Revision Number
	private static final HashMap HM_REVISIONS = new HashMap();

	// if HashSet contains relative path, path is a directory.
	private static final HashSet HS_DIRECTORIES = new HashSet();

	// Path of . in repository. Can only be calculated if given an element from
	// the SVN log.
	private static String sModuleName = null;

	// Revision number of root folder (.)
	private static String sRootRevisionNumber = null;

	// URL of root (.)
	private static String sRootUrl = null;

	// UUID of repository
	private static String sRepositoryUuid = null;

	// URL of repository
	private static String sRepositoryUrl = null;

	/**
	 * Converts an absolute path in the repository to a path relative to the
	 * working folder root.
	 * 
	 * Will return null if absolute path does not start with getModuleName();
	 * 
	 * @param absolute
	 *            Example (assume getModuleName() returns /trunk/statsvn)
	 *            /trunk/statsvn/package.html
	 * @return Example: package.html
	 */
	public static String absoluteToRelativePath(String absolute) {
		if (absolute.endsWith("/")) {
			absolute = absolute.substring(0, absolute.length() - 1);
		}

		if (absolute.equals(getModuleName())) {
			return ".";
		} else if (!absolute.startsWith(getModuleName())) {
			return null;
		} else {
			return absolute.substring(getModuleName().length() + 1);
		}
	}

	/**
	 * Converts an absolute path in the repository to a URL, using the
	 * repository URL
	 * 
	 * @param absolute
	 *            Example: /trunk/statsvn/package.html
	 * @return Example: svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
	 */
	public static String absolutePathToUrl(final String absolute) {
		return getRepositoryUrl() + (absolute.endsWith("/") ? absolute.substring(0, absolute.length() - 1) : absolute);
	}

	/**
	 * Converts a relative path in the working folder to a URL, using the
	 * working folder's root URL
	 * 
	 * @param relative
	 *            Example: src/Messages.java
	 * @return Example:
	 *         svn://svn.statsvn.org/statsvn/trunk/statsvn/src/Messages.java
	 * 
	 */
	public static String relativePathToUrl(String relative) {
		relative = relative.replace('\\', '/');
		if (relative.equals(".") || relative.length() == 0) {
			return getRootUrl();
		} else {
			return getRootUrl() + "/" + (relative.endsWith("/") ? relative.substring(0, relative.length() - 1) : relative);
		}
	}

	/**
	 * Converts a relative path in the working folder to an absolute path in the
	 * repository.
	 * 
	 * @param relative
	 *            Example: src/Messages.java
	 * @return Example: /trunk/statsvn/src/Messages.java
	 * 
	 */
	public static String relativeToAbsolutePath(final String relative) {
		return urlToAbsolutePath(relativePathToUrl(relative));
	}

	/**
	 * Returns true if the file exists in the working copy (according to the svn
	 * metadata, and not file system checks).
	 * 
	 * @param relativePath
	 *            the path
	 * @return <tt>true</tt> if it exists
	 */
	public static boolean existsInWorkingCopy(final String relativePath) {
		return getRevisionNumber(relativePath) != null;
	}

	/**
	 * Assumes #loadInfo(String) has been called. Never ends with /, might be
	 * empty.
	 * 
	 * @return The absolute path of the root of the working folder in the
	 *         repository.
	 */
	public static String getModuleName() {

		if (sModuleName == null) {

			if (getRootUrl().length() < getRepositoryUrl().length() || getRepositoryUrl().length() == 0) {
				SvnConfigurationOptions.getTaskLogger().info("Unable to process module name.");
				sModuleName = "";
			} else {
				sModuleName = getRootUrl().substring(getRepositoryUrl().length());
			}

		}
		return sModuleName;
	}

	/**
	 * Returns the revision number of the file in the working copy.
	 * 
	 * @param relativePath
	 *            the filename
	 * @return the revision number if it exists in the working copy, null
	 *         otherwise.
	 */
	public static String getRevisionNumber(final String relativePath) {
		if (HM_REVISIONS.containsKey(relativePath)) {
			return HM_REVISIONS.get(relativePath).toString();
		} else {
			return null;
		}
	}

	/**
	 * Assumes #loadInfo() has been invoked.
	 * 
	 * @return the root of the working folder's revision number (last checked
	 *         out revision number)
	 */
	public static String getRootRevisionNumber() {
		return sRootRevisionNumber;
	}

	/**
	 * Assumes #loadInfo() has been invoked.
	 * 
	 * @return the root of the working folder's url (example:
	 *         svn://svn.statsvn.org/statsvn/trunk/statsvn)
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
	 *            true if should we check for the root only or false otherwise
	 *            (recurse for all files)
	 * @return the response.
	 */
	protected static synchronized ProcessUtils getSvnInfo(boolean bRootOnly) {
		String svnInfoCommand = "svn info --xml";
		if (!bRootOnly) {
			svnInfoCommand += " -R";
		}
		svnInfoCommand += SvnCommandHelper.getAuthString();

		try {
			return ProcessUtils.call(svnInfoCommand);
		} catch (final Exception e) {
			SvnConfigurationOptions.getTaskLogger().error(e.toString());
			return null;
		}
	}

	/**
	 * Returns true if the path has been identified as a directory.
	 * 
	 * @param relativePath
	 *            the path
	 * @return true if it is a known directory.
	 */
	public static boolean isDirectory(final String relativePath) {
		return HS_DIRECTORIES.contains(relativePath);
	}

	/**
	 * Adds a directory to the list of known directories. Used when inferring
	 * implicit actions on deleted paths.
	 * 
	 * @param relativePath
	 *            the relative path.
	 */
	public static void addDirectory(final String relativePath) {
		if (!HS_DIRECTORIES.contains(relativePath)) {
			HS_DIRECTORIES.add(relativePath);
		}
	}

	/**
	 * Do we need to re-invoke svn info?
	 * 
	 * @param bRootOnly
	 *            true if we need the root only
	 * @return true if we it needs to be re-invoked.
	 */
	protected static boolean isQueryNeeded(boolean bRootOnly) {
		return !ENABLE_CACHING || (bRootOnly && sRootUrl == null) || (!bRootOnly && HM_REVISIONS == null);
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
	protected static void loadInfo(final boolean bRootOnly) throws LogSyntaxException, IOException {
		ProcessUtils pUtils = null;
		try {
			pUtils = getSvnInfo(bRootOnly);
			loadInfo(pUtils);
		} finally {
			if (pUtils != null) {
				pUtils.close();
			}
		}
	}

	/**
	 * Loads the information from svn info if needed.
	 * 
	 * @param pUtils
	 *            the process util that contains the input stream representing 
	 *            an svn info command.
	 * @throws LogSyntaxException
	 *             if the format of the svn info is invalid
	 * @throws IOException
	 *             if we can't read from the response stream.
	 */
	public static void loadInfo(final ProcessUtils pUtils) throws LogSyntaxException, IOException {
		// is public for tests
		if (isQueryNeeded(true)) {
			try {
				HM_REVISIONS.clear();
				HS_DIRECTORIES.clear();

				final SAXParserFactory factory = SAXParserFactory.newInstance();
				final SAXParser parser = factory.newSAXParser();
				parser.parse(pUtils.getInputStream(), new SvnInfoHandler());

				if (pUtils.hasErrorOccured()) {
					throw new IOException("svn info: " + pUtils.getErrorMessage());
				}

			} catch (final ParserConfigurationException e) {
				throw new LogSyntaxException("svn info: " + e.getMessage());
			} catch (final SAXException e) {
				throw new LogSyntaxException("svn info: " + e.getMessage());
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
	 *            Examples: svn://svn.statsvn.org/statsvn/trunk/statsvn,
	 *            svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
	 * @return Example: /trunk/statsvn, /trunk/statsvn/package.html
	 */
	public static String urlToAbsolutePath(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		if (getModuleName().length() <= 1) {
			if (getRootUrl().equals(url)) {
				return "/";
			} else {
				return url.substring(getRootUrl().length());
			}
		} else {
			// chop off the repo root from the url
			return url.substring(getRepositoryUrl().length());
		}
	}

	/**
	 * Converts a url to a relative path in the repository.
	 * 
	 * @param url
	 *            Examples: svn://svn.statsvn.org/statsvn/trunk/statsvn,
	 *            svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
	 * @return Example: ".", package.html
	 */
	public static String urlToRelativePath(final String url) {
		return absoluteToRelativePath(urlToAbsolutePath(url));
	}

	/**
	 * Sets the project's root URL.
	 * 
	 * @param rootUrl
	 */
	protected static void setRootUrl(final String rootUrl) {
		if (rootUrl.endsWith("/")) {
			sRootUrl = rootUrl.substring(0, rootUrl.length() - 1);
		} else {
			sRootUrl = rootUrl;
		}

		sModuleName = null;
	}

	/**
	 * Sets the project's repository URL.
	 * 
	 * @param repositoryUrl
	 */
	protected static void setRepositoryUrl(final String repositoryUrl) {
		if (repositoryUrl.endsWith("/")) {
			sRepositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 1);
		} else {
			sRepositoryUrl = repositoryUrl;
		}

		sModuleName = null;
	}

	/**
	 * This method is a 1.4 replacement of the String.replace(CharSequence, CharSequence) found in 1.5.
	 * @param originalPattern
	 * @param newPattern
	 * @param originalString
	 * @return
	 */
    public static String replace(final String originalPattern, final String newPattern, final String originalString) {
        if ((originalPattern == null) || (originalPattern.length() == 0) || (originalString == null)) {
            return originalString;
        }

        final StringBuffer newString = new StringBuffer(originalString.length());
        int index = 0;
        final int originalLength = originalPattern.length();
        int previousIndex = 0;

        while ((index = originalString.indexOf(originalPattern, index)) != -1) {
            newString.append(originalString.substring(previousIndex, index)).append(newPattern);
            index += originalLength;
            previousIndex = index;
        }

        if (previousIndex == 0) {
            newString.append(originalString);
        } else if (previousIndex != originalString.length()) {
            newString.append(originalString.substring(previousIndex));
        }

        return newString.toString();
    }

}
