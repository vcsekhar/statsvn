package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.statcvs.output.ConfigurationOptions;

/**
 * Utilities class that manages calls to svn propget. Used to find binary files.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * 
 * @version $Id$
 */
public class SvnPropgetUtils {

	protected static List binaryFiles;

	/**
	 * Get the svn:mime-types for all files, latest revision.
	 * 
	 * @return the inputstream from which to read the information.
	 */
	protected synchronized static InputStream getFileMimeTypes() {
		return getFileMimeTypes(null, null);
	}

	/**
	 * Get the svn:mime-type for a certain file (leave null for all files).
	 * 
	 * It was first thought that a the mime-type of a file's previous revision
	 * could be found. This is not the case. Leave revision null until future
	 * upgrade of svn propget command line.
	 * 
	 * @param revision
	 *            revision for which to query; LEAVE NULL
	 * @param filename
	 *            the filename (or null for all files)
	 * @return the inputstream from which to read the information.
	 */
	protected synchronized static InputStream getFileMimeTypes(String revision, String filename) {
		InputStream istream = null;

		String svnPropgetCommand = "svn propget svn:mime-type";
		if (revision != null && revision.length() > 0)
			svnPropgetCommand += " -r " + revision; // won't work.

		if (filename != null && filename.length() > 0)
			svnPropgetCommand += " " + SvnInfoUtils.relativePathToUrl(filename);
		else
			svnPropgetCommand += " -R ";

		svnPropgetCommand += SvnCommandHelper.getAuthString();

		try {
			Process p = Runtime.getRuntime().exec(svnPropgetCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
			istream = new BufferedInputStream(p.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return istream;
	}

	/**
	 * Returns the list of binary files in the working directory.
	 * 
	 * @return the list of binary files
	 */
	public static List getBinaryFiles() {
		if (binaryFiles == null) {
			loadBinaryFiles(getFileMimeTypes());
		}

		return binaryFiles;
	}

	/**
	 * Loads the list of binary files from the input stream equivalent to an svn
	 * propget command.
	 * 
	 * @param stream
	 *            stream equivalent to an svn propget command
	 */
	public static void loadBinaryFiles(InputStream stream) {
		// public for tests
		binaryFiles = new ArrayList();
		LookaheadReader mimeReader = new LookaheadReader(new InputStreamReader(stream));
		try {
			while (mimeReader.hasNextLine()) {
				mimeReader.nextLine();
				String file = getBinaryFilename(mimeReader.getCurrentLine(), false);
				if (file != null)
					binaryFiles.add(file);
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Does not currently work.
	 * 
	 * It was first thought that a the mime-type of a file's previous revision
	 * could be found. This is not the case. Leave revision null until future
	 * upgrade of svn propget command line.
	 * 
	 * @param revision
	 *            the revision to query
	 * @param filename
	 *            the filename
	 * @return if that version of a file is binary
	 */
	public static boolean isBinaryFile(String revision, String filename) {
		LookaheadReader mimeReader = new LookaheadReader(new InputStreamReader(getFileMimeTypes(revision, filename)));
		try {
			while (mimeReader.hasNextLine()) {
				mimeReader.nextLine();
				String file = getBinaryFilename(mimeReader.getCurrentLine(), true);
				if (file != null && file.equals(filename))
					return true;
			}
		} catch (IOException e) {
		}

		return false;
	}

	/**
	 * Given a string such as: "lib\junit.jar - application/octet-stream" or
	 * "svn:\\host\repo\lib\junit.jar - application/octet-stream" will return
	 * the filename if the mime type is binary (doesn't end with text/*)
	 * 
	 * Will return the filename with / was a directory seperator.
	 * 
	 * @param currentLine
	 *            the line obtained from svn propget svn:mime-type
	 * @param removeRoot
	 *            if true, will remove any repository prefix
	 * @return should return lib\junit.jar in both cases, given that
	 *         removeRoot==true in the second case.
	 */
	private static String getBinaryFilename(String currentLine, boolean removeRoot) {
		// want to make sure we only have / in end result.
		String line = removeRoot ? currentLine : currentLine.replace("\\", "/");

		// HACK: See bug 18. if removeRoot==true, no - will be found because we
		// are calling for one specific file.
		String octetStream = " - application/octet-stream";
		// if is common binary file or identified as something other than text
		if (line.endsWith(octetStream) || line.lastIndexOf(" - text/") < 0 && line.lastIndexOf(" - text/") == line.lastIndexOf(" - ")) {
			line = line.substring(0, line.lastIndexOf(" - "));
			if (removeRoot)
				line = SvnInfoUtils.urlToRelativePath(line);

			return line;
		}

		return null;
	}

}
