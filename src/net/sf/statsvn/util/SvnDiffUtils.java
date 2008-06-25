package net.sf.statsvn.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Vector;

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
	public static final int RESULT_SIZE = 3;

    private static final int PROPERTY_NAME_LINE = 4;

	private static final String PROPERTY_CHANGE = "Property changes on:";

	private static final String PROPERTY_NAME = "Name:";

	private static final String BINARY_TYPE = "Cannot display: file marked as a binary type.";

	private static final String INDEX_MARKER = "Index: ";

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
		filename = SvnInfoUtils.replace(" ", "%20", filename);
		svnDiffCommand = "svn diff --old " + filename + "@" + oldRevNr + "  --new " + filename + "@" + newRevNr + "" + SvnCommandHelper.getAuthString();
		SvnConfigurationOptions.getTaskLogger().log(Thread.currentThread().getName() + " FIRING command line:\n[" + svnDiffCommand + "]");
		return ProcessUtils.call(svnDiffCommand);
	}

	/**
	 * Calls svn diff on all files for given revision and revision-1.  
	 * 
	 * @param newRevNr
	 *            revision number
	 * @return the InputStream related to the call. If the error steam is
	 *         non-empty, will return the error stream instead of the default
	 *         input stream.
	 */
	private static synchronized ProcessUtils callSvnDiff(final String newRevNr) throws IOException {
		String svnDiffCommand = null;
		svnDiffCommand = "svn diff -c " + newRevNr + " " + SvnInfoUtils.getRootUrl() + " " + SvnCommandHelper.getAuthString();
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

			verifyOutput(pUtils);
		} finally {
			if (pUtils != null) {
				pUtils.close();
			}
		}

		return lineDiff;
	}

	/**
	 * Verifies the process error stream. 
	 * @param pUtils the process call
	 * @throws IOException problem parsing the stream
	 * @throws BinaryDiffException if the error message is due to trying to diff binary files.
	 */
	private static void verifyOutput(final ProcessUtils pUtils) throws IOException, BinaryDiffException {
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
	}

	/**
	* Returns line count differences for all files in a particular revision.
	* 
	* @param newRevNr
	*            new revision number
	* @return A vector of object[3] array of [filename, int[2](lines added, lines removed), isBinary] is returned.
	* @throws IOException
	*             problem parsing the stream
	* @throws BinaryDiffException
	*             if the error message is due to trying to diff binary files.
	*/
	public static Vector getLineDiff(final String newRevNr) throws IOException, BinaryDiffException {
		final Vector answer = new Vector();

		ProcessUtils pUtils = null;
		try {
			pUtils = callSvnDiff(newRevNr);
			final InputStream diffStream = pUtils.getInputStream();
			final LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
			String currFile = null;
			StringBuffer sb = new StringBuffer();
			while (diffReader.hasNextLine()) {
				final String currLine = diffReader.nextLine();

				if (currFile == null && currLine.startsWith(INDEX_MARKER)) {
					currFile = currLine.substring(INDEX_MARKER.length());
				} else if (currFile != null && currLine.startsWith(INDEX_MARKER)) {
					appendResults(answer, currFile, sb);
					sb = new StringBuffer();
					currFile = currLine.substring(INDEX_MARKER.length());
				}

				sb.append(currLine);
				sb.append(System.getProperty("line.separator"));
			}

            // last file
            if (currFile!=null) {
                appendResults(answer, currFile, sb);
            }

			verifyOutput(pUtils);
		} finally {
			if (pUtils != null) {
				pUtils.close();
			}
		}

		return answer;
	}

	/**
	 * Append results to answer vector.  
	 * @param answer the current answers
	 * @param currFile the current file being added. 
	 * @param sb the diff for this individual file. 
	 * @throws IOException
	 *             problem parsing the stream
	 * @throws BinaryDiffException
	 *             if the error message is due to trying to diff binary files.
	 */
	private static void appendResults(final Vector answer, final String currFile, final StringBuffer sb) throws IOException {
		int[] lineDiff;
		Boolean isBinary = Boolean.FALSE;

		final LookaheadReader individualDiffReader = new LookaheadReader(new StringReader(sb.toString()));
		try {
			lineDiff = parseDiff(individualDiffReader);
		} catch (final BinaryDiffException e) {
			lineDiff = new int[2];
			lineDiff[0] = 0;
			lineDiff[1] = 0;
			isBinary = Boolean.TRUE;

		}
		final Object[] results = new Object[RESULT_SIZE];
		results[0] = currFile;
		results[1] = lineDiff;
		results[2] = isBinary;
		answer.add(results);
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
			final String currentLine = diffReader.getCurrentLine();

			SvnConfigurationOptions.getTaskLogger().log(Thread.currentThread().getName() + " Diff Line: [" + currentLine + "]");

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
