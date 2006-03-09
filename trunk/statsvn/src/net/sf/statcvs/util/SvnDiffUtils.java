package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import net.sf.statcvs.input.NoLineCountException;
import net.sf.statcvs.output.ConfigurationOptions;

public class SvnDiffUtils {

	private synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr) {
		InputStream istream = null;
		String svnDiffCommand = "svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted";
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
			if (p.getErrorStream().available() > 0) {
				istream = new BufferedInputStream(p.getErrorStream());
			} else
				istream = new BufferedInputStream(p.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return istream;
	}

	private synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr, String filename, boolean deleted) {
		InputStream istream = null;
		String svnDiffCommand = null;

		filename = SvnInfoUtils.relativePathToUrl(filename);
		// if (deleted) {
		svnDiffCommand = "svn diff  --old " + filename + "@" + oldRevNr + "  --new " + filename + "@" + newRevNr;
		// } else {
		// svnDiffCommand = "svn diff -r " + oldRevNr + ":" + newRevNr + "
		// --no-diff-deleted " + filename;
		// }
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
			if (p.getErrorStream().available() > 0) {
				istream = new BufferedInputStream(p.getErrorStream());
			} else
				istream = new BufferedInputStream(p.getInputStream());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return istream;
	}

	public static int[] getLineDiff(String oldRevNr, String newRevNr, String filename) throws IOException {
		InputStream diffStream = callSvnDiff(oldRevNr, newRevNr, filename, false);
		LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
		int lineDiff[] = parseDiff(diffReader);
		return lineDiff;
	}

	private static int[] parseDiff(LookaheadReader diffReader) throws IOException {
		int lineDiff[] = { -1, -1 };
		if (!diffReader.hasNextLine()) {
			// diff has no output because we modified properties or the changes
			// are auto-generated ($id$ $author$ kind of thing)
			// http://svnbook.red-bean.com/nightly/en/svn.advanced.props.html#svn.advanced.props.special.keywords
			lineDiff[0] = 0;
			lineDiff[1] = 0;
		}
		while (diffReader.hasNextLine()) {
			diffReader.nextLine();
			if (diffReader.getCurrentLine().length() == 0)
				continue;
			// very simple algorithm
			if (diffReader.getCurrentLine().charAt(0) == '+')
				lineDiff[0]++;
			else if (diffReader.getCurrentLine().charAt(0) == '-')
				lineDiff[1]++;
			// System.out.println(diffReader.getCurrentLine());
		}
		System.out.println(lineDiff[0] + " " + lineDiff[1]);
		return lineDiff;
	}

}
