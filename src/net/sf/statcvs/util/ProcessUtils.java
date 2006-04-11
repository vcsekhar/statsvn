package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.statcvs.output.ConfigurationOptions;

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
public class ProcessUtils {
	protected static Process lastProcess;
	protected static BufferedInputStream lastInputStream;
	protected static BufferedInputStream lastErrorStream;

	public synchronized static InputStream call(String sCommand) throws IOException {
		lastProcess = Runtime.getRuntime().exec(sCommand, null, getWorkingFolder());
		lastErrorStream = new BufferedInputStream(lastProcess.getErrorStream());
		lastInputStream = new BufferedInputStream(lastProcess.getInputStream());

		return lastInputStream;
	}

	protected static File getWorkingFolder() {
		return ConfigurationOptions.getCheckedOutDirectoryAsFile();
	}

	protected static boolean hasErrorOccured() throws IOException {
		return lastErrorStream != null && lastErrorStream.available() > 0;
	}

	protected static String getErrorMessage() {
		if (lastErrorStream == null)
			return null;
		else {
			LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(lastErrorStream));
			StringBuilder builder = new StringBuilder();
			try {
				while (diffReader.hasNextLine()) {
					builder.append(diffReader.getCurrentLine());
				}
			} catch (IOException e) {
			}

			return builder.toString();
		}
	}
}
