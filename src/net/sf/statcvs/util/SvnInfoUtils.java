package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SvnInfoUtils {

	protected static class SvnInfoHandler extends DefaultHandler {

		protected boolean isRootFolder = false;
		protected String sCurrentKind;
		protected String sCurrentRevision;
		protected String sCurrentUrl;
		protected String stringData = "";

		public void characters(char[] ch, int start, int length) throws SAXException {
			stringData += new String(ch, start, length);
		}

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
			}
		}

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

	// Any path given in svn log.
	protected static String sSeedPath = null;

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
	 * Converts an absolute path in the repository to a URL, using the
	 * repository URL
	 * 
	 * @param absolute
	 *            Example: /trunk/statsvn/package.html
	 * @return Example: svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
	 */
	public static String absolutePathToUrl(String absolute) {
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
		if (relative.equals(".") || relative.length() == 0)
			return getRootUrl();
		else
			return getRootUrl() + "/" + (relative.endsWith("/") ? relative.substring(0, relative.length() - 1) : relative);
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
	public static String relativeToAbsolutePath(String relative) {
		return urlToAbsolutePath(relativePathToUrl(relative));
	}

	public static boolean existsInWorkingCopy(String relativePath) {
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
			// Need sSeedPath and sRootUrl
			// sSeedPath: /trunk/statsvn/package.html
			// sRootUrl: svn://svn.statsvn.org/statsvn/trunk/statsvn

			String tmp = sSeedPath;
			if (tmp.endsWith("/"))
				tmp = tmp.substring(0, tmp.length() - 1);

			while (!sRootUrl.endsWith(tmp)) {
				if (!tmp.endsWith("/"))
					tmp += "/";

				// tricking method to think it is receiving a directory.
				tmp = FileUtils.getParentDirectoryPath(tmp);

				if (tmp.endsWith("/"))
					tmp = tmp.substring(0, tmp.length() - 1);
			}

			sModuleName = tmp;
		}
		return sModuleName;
	}

	public static String getRevisionNumber(String relativePath) {
		if (hmRevisions.containsKey(relativePath))
			return hmRevisions.get(relativePath).toString();
		else
			return null;
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
	 * @return the repository url (example: svn://svn.statsvn.org/statsvn)
	 */
	public static String getRepositoryUrl() {
		return sRootUrl.substring(0, sRootUrl.lastIndexOf(getModuleName()));
	}

	protected static String getSeedPath() {
		return sSeedPath;
	}

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

	public static boolean isDirectory(String relativePath) {
		return hsDirectories.contains(relativePath);
	}

	public static List getDirectories() {
		ArrayList list = new ArrayList();
		Object[] dirs = hsDirectories.toArray();
		for (int i = 0; i < dirs.length; i++) {
			list.add(dirs[i]);
		}
		Collections.sort(list);
		return list;
	}

	public static void addDirectory(String relativePath) {
		if (!hsDirectories.contains(relativePath))
			hsDirectories.add(relativePath);
	}

	protected static boolean isQueryNeeded(boolean bRootOnly) {
		return !ENABLE_CACHING || (bRootOnly && sRootUrl == null) || (!bRootOnly && hmRevisions == null);
	}

	private static boolean isRootFolder(Attributes attributes) {
		return attributes.getValue("path").equals(".") && attributes.getValue("kind").equals("dir");
	}

	private static boolean isValidCommit(Attributes attributes) {
		return attributes != null && attributes.getValue("revision") != null;
	}

	private static boolean isValidInfoEntry(Attributes attributes) {
		return attributes != null && attributes.getValue("path") != null && attributes.getValue("kind") != null && attributes.getValue("revision") != null;
	}

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
	 * 
	 * @param seedPath
	 *            any path found in an svn log (ex: /trunk/statsvn/package.html)
	 * @throws LogSyntaxException
	 *             if the svn info --xml is malformed
	 * @throws IOException
	 *             if there is an error reading from the stream
	 */
	public static void loadInfo(String seedPath) throws LogSyntaxException, IOException {
		setSeedPath(seedPath);
		loadInfo(false);
	}

	protected static void setSeedPath(String seedPath) {
		sSeedPath = seedPath;
		hmRevisions = null;
		hsDirectories = null;
		sRootUrl = null;
		sRootRevisionNumber = null;
		sModuleName = null;

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
	 *            Examples: svn://svn.statsvn.org/statsvn/trunk/statsvn,
	 *            svn://svn.statsvn.org/statsvn/trunk/statsvn/package.html
	 * @return Example: ".", package.html
	 */
	public static String urlToRelativePath(String url) {
		return absoluteToRelativePath(urlToAbsolutePath(url));
	}

	protected static void setRootUrl(String rootUrl) {
		if (rootUrl.endsWith("/"))
			sRootUrl = rootUrl.substring(0, rootUrl.length() - 1);
		else
			sRootUrl = rootUrl;

	}
}
