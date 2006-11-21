package net.sf.statsvn.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.statcvs.util.LookaheadReader;
import net.sf.statsvn.output.SvnConfigurationOptions;

/**
 * This class provides a way of launching new processes. It is not the best way
 * and it surely does not work well in multi-threaded environments. It is
 * sufficient for StatSVN's single thread.
 * 
 * We should be launching two threads with readers for both, but we are lazy.
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps_p.html
 * 
 * @author jkealey <jkealey@shade.ca>
 * 
 */
public final class ProcessUtils {
	private static Process lastProcess;
	private static BufferedInputStream lastInputStream;
	private static BufferedInputStream lastErrorStream;

	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private ProcessUtils() {
	}

	public static synchronized InputStream call(final String sCommand) throws IOException {
		lastProcess = Runtime.getRuntime().exec(sCommand, null, getWorkingFolder());
		lastErrorStream = new BufferedInputStream(lastProcess.getErrorStream());
		lastInputStream = new BufferedInputStream(lastProcess.getInputStream());

		return lastInputStream;
	}

	protected static File getWorkingFolder() {
		return SvnConfigurationOptions.getCheckedOutDirectoryAsFile();
	}

	protected static boolean hasErrorOccured() throws IOException {
		return lastErrorStream != null && lastErrorStream.available() > 0;
	}

	protected static String getErrorMessage() {
		if (lastErrorStream == null) {
			return null;
		} else {
			final LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(lastErrorStream));
			final StringBuilder builder = new StringBuilder();
			try {
				while (diffReader.hasNextLine()) {
					builder.append(diffReader.nextLine());
				}
			} catch (final IOException e) {
			}

			return builder.toString();
		}
	}
}
