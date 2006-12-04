package net.sf.statsvn.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.statcvs.util.LookaheadReader;

/**
 * Utilities class that manages calls to svn propget. Used to find binary files.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * 
 * @version $Id$
 */
public final class SvnPropgetUtils {

	private static List binaryFiles;
	private static final Logger LOGGER = Logger.getLogger(SvnPropgetUtils.class.getName());

	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private SvnPropgetUtils() {
	}

	/**
	 * Get the svn:mime-types for all files, latest revision.
	 * 
	 * @return the inputstream from which to read the information.
	 */
	private static synchronized ProcessUtils getFileMimeTypes() {
		return getFileMimeTypes(null, null);
	}

	/**
	 * Get the svn:mime-type for a certain file (leave null for all files).
	 * 
	 * @param revision
	 *            revision for which to query; 
	 * @param filename
	 *            the filename (or null for all files)
	 * @return the inputstream from which to read the information.
	 */
	protected static synchronized ProcessUtils getFileMimeTypes(final String revision, final String filename) {
		String svnPropgetCommand = "svn propget svn:mime-type";
		if (revision != null && revision.length() > 0) {
			svnPropgetCommand += " -r " + revision;
		} 

		if (filename != null && filename.length() > 0) {
			svnPropgetCommand += " " + SvnInfoUtils.relativePathToUrl(filename);
            
            if (revision != null && revision.length() > 0) {
				svnPropgetCommand += "@" + revision;
			} 
        } else {
			svnPropgetCommand += " -R ";
		}

		svnPropgetCommand += SvnCommandHelper.getAuthString();

		try {
			return ProcessUtils.call(svnPropgetCommand);
		} catch (final Exception e) {
			LOGGER.warning(e.toString());
			return null;
		}
	}

	/**
	 * Returns the list of binary files in the working directory.
	 * 
	 * @return the list of binary files
	 */
	public static List getBinaryFiles() {
		if (binaryFiles == null) {
			ProcessUtils pUtils = null;
			try {
				pUtils = getFileMimeTypes();
				loadBinaryFiles(pUtils);
			} finally {
				if (pUtils != null) {
					try {
						pUtils.close();
					} catch (final IOException e) {
						LOGGER.warning(e.toString());
					}
				}
			}
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
	public static void loadBinaryFiles(final ProcessUtils pUtils) {
		// public for tests
		binaryFiles = new ArrayList();
		final LookaheadReader mimeReader = new LookaheadReader(new InputStreamReader(pUtils.getInputStream()));
		try {
			while (mimeReader.hasNextLine()) {
				mimeReader.nextLine();
				final String file = getBinaryFilename(mimeReader.getCurrentLine(), false);
				if (file != null) {
					binaryFiles.add(file);
				}
			}
			if (pUtils.hasErrorOccured()) {
				throw new IOException(pUtils.getErrorMessage());
			}
		} catch (final IOException e) {
			LOGGER.warning(e.getMessage());
		}
	}

	/**
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
	public static boolean isBinaryFile(final String revision, final String filename) {
		ProcessUtils pUtils = null;
		try {
			pUtils = getFileMimeTypes(revision, filename);
			final LookaheadReader mimeReader = new LookaheadReader(new InputStreamReader(pUtils.getInputStream()));
			while (mimeReader.hasNextLine()) {
				mimeReader.nextLine();
				final String file = getBinaryFilename(mimeReader.getCurrentLine(), true);
				if (file != null && file.equals(filename)) {
					return true;
				}
			}
		} catch (final IOException e) {
			LOGGER.warning(e.toString());
		} finally {
			if (pUtils != null) {
				try {
					pUtils.close();
				} catch (final IOException e) {
					LOGGER.warning(e.toString());
				}
			}
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
	private static String getBinaryFilename(final String currentLine, final boolean removeRoot) {
		// want to make sure we only have / in end result.
		String line = removeRoot ? currentLine : currentLine.replace('\\', '/');

		// HACK: See bug 18. if removeRoot==true, no - will be found because we
		// are calling for one specific file.
		final String octetStream = " - application/octet-stream";
		// if is common binary file or identified as something other than text
		if (line.endsWith(octetStream) || line.lastIndexOf(" - text/") < 0 && line.lastIndexOf(" - text/") == line.lastIndexOf(" - ")) {
			line = line.substring(0, line.lastIndexOf(" - "));
			if (removeRoot) {
				line = SvnInfoUtils.urlToRelativePath(line);
			}

			return line;
		}

		return null;
	}

}
