package net.sf.statsvn.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import net.sf.statcvs.util.LookaheadReader;
import net.sf.statsvn.output.SvnConfigurationOptions;

/**
 * Utilities class that manages calls to svn diff.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public final class SvnDiffUtils {
	private static final Logger LOGGER = Logger.getLogger(SvnDiffUtils.class.getName());

	private static final int PROPERTY_NAME_LINE = 4;

	private static final String PROPERTY_CHANGE = "Property changes on:";

	private static final String PROPERTY_NAME = "Name:";

	private static final String BINARY_TYPE = "Cannot display: file marked as a binary type.";

	/**
	 * A utility class (only static methods) should be final and have a private
	 * constructor.
	 */
	private SvnDiffUtils() {
	}

	/**
	 * Calls svn diff for the filename and revisions given. Will use URL
	 * invocation, to ensure that we get diffs even for deleted files.
	 * 
	 * @param oldRevNr
	 *            old revision number
	 * @param newRevNr
	 *            new revision number
	 * @param filename
	 *            filename.
	 * @return the InputStream related to the call. If the error steam is
	 *         non-empty, will return the error stream instead of the default
	 *         input stream.
	 */
	private static synchronized ProcessUtils callSvnDiff(final String oldRevNr, final String newRevNr, String filename) throws IOException {
		String svnDiffCommand = null;
		filename = SvnInfoUtils.relativePathToUrl(filename);
		svnDiffCommand = "svn diff  --old \"" + filename + "@" + oldRevNr + "\"  --new \"" + filename + "@" + newRevNr + "\""
		        + SvnCommandHelper.getAuthString();
		SvnConfigurationOptions.getTaskLogger().log(Thread.currentThread().getName() + " FIRING command line:\n[" + svnDiffCommand + "]");
		return ProcessUtils.call(svnDiffCommand);
	}

	/**
	 * Returns line count differences between two revisions of a file.
	 * 
	 * @param oldRevNr
	 *            old revision number
	 * @param newRevNr
	 *            new revision number
	 * @param filename
	 *            the filename
	 * @return A int[2] array of [lines added, lines removed] is returned.
	 * @throws IOException
	 *             problem parsing the stream
	 * @throws BinaryDiffException
	 *             if the error message is due to trying to diff binary files.
	 */
	public static int[] getLineDiff(final String oldRevNr, final String newRevNr, final String filename) throws IOException, BinaryDiffException {
		int[] lineDiff;
		ProcessUtils pUtils = null;
		try {
			pUtils = callSvnDiff(oldRevNr, newRevNr, filename);
			final InputStream diffStream = pUtils.getInputStream();

			final LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
			lineDiff = parseDiff(diffReader);

			if (pUtils.hasErrorOccured()) {
				// The binary checking code here might be useless... as it may
				// be output on the standard out.
				final String msg = pUtils.getErrorMessage();
				if (isBinaryErrorMessage(msg)) {
					throw new BinaryDiffException();
				} else {
					throw new IOException(msg);
				}
			}
		} finally {
			if (pUtils != null) {
				pUtils.close();
			}
		}

		return lineDiff;
	}

	/**
	 * Returns true if msg is an error message display that the file is binary.
	 * 
	 * @param msg
	 *            the error message given by ProcessUtils.getErrorMessage();
	 * @return true if the file is binary
	 */
	private static boolean isBinaryErrorMessage(final String msg) {
		/*
		 * Index: junit.jar
		 * ===================================================================
		 * Cannot display: file marked as a binary type.
		 * 
		 * svn:mime-type = application/octet-stream
		 */
		return (msg.indexOf(BINARY_TYPE) >= 0);
	}

	/**
	 * Returns line count differences between two revisions of a file.
	 * 
	 * @param diffReader
	 *            the stream reader for svn diff
	 * 
	 * @return A int[2] array of [lines added, lines removed] is returned.
	 * @throws IOException
	 *             problem parsing the stream
	 */
	private static int[] parseDiff(final LookaheadReader diffReader) throws IOException, BinaryDiffException {
		final int[] lineDiff = { -1, -1 };
		boolean propertyChange = false;
		if (!diffReader.hasNextLine()) {
			// diff has no output because we modified properties or the changes
			// are auto-generated ($id$ $author$ kind of thing)
			// http://svnbook.red-bean.com/nightly/en/svn.advanced.props.html#svn.advanced.props.special.keywords
			lineDiff[0] = 0;
			lineDiff[1] = 0;
		}
		while (diffReader.hasNextLine()) {
			diffReader.nextLine();
			String currentLine = diffReader.getCurrentLine();
			
			LOGGER.fine(Thread.currentThread().getName() + " Diff Line: [" + currentLine + "]");
			
			if (currentLine.length() == 0) {
				continue;
			}
			final char firstChar = currentLine.charAt(0);
			// very simple algorithm
			if (firstChar == '+') {
				lineDiff[0]++;
			} else if (firstChar == '-') {
				lineDiff[1]++;
			} else if (currentLine.indexOf(PROPERTY_CHANGE) == 0
			        || (currentLine.indexOf(PROPERTY_NAME) == 0 && diffReader.getLineNumber() == PROPERTY_NAME_LINE)) {
				propertyChange = true;
			} else if (currentLine.indexOf(BINARY_TYPE) == 0) {
				throw new BinaryDiffException();
			}
		}
		if (propertyChange && (lineDiff[0] == -1 || lineDiff[1] == -1)) {
			lineDiff[0] = 0;
			lineDiff[1] = 0;
		}

		return lineDiff;
	}

}
