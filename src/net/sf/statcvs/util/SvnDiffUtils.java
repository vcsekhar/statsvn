package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.statcvs.input.RevisionData;
import net.sf.statcvs.output.ConfigurationOptions;

public class SvnDiffUtils {

	private synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr) {
		InputStream istream = null;
		String svnDiffCommand = "svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted";
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
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
		if (deleted) {
			svnDiffCommand = "svn diff " + filename + "@" + oldRevNr + " " + filename + "@" + newRevNr;			
		}
		else {
			svnDiffCommand = "svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted " + filename;			
		}
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
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
		if (lineDiff[0] == -1 || lineDiff[1] == -1) {
			// condition is true only for deleted files
			String urlFileName = SvnInfoUtils.relativePathToUrl(filename);
			diffStream = callSvnDiff(oldRevNr, newRevNr, urlFileName, true);
			diffReader = new LookaheadReader(new InputStreamReader(diffStream));
			lineDiff = parseDiff(diffReader);
		}
		return lineDiff;
	}

	private static int[] parseDiff(LookaheadReader diffReader) throws IOException {
		int lineDiff[] = { -1, -1 };
		while (diffReader.hasNextLine()) {
			diffReader.nextLine();
			// very simple algorithm
			if (diffReader.getCurrentLine().charAt(0) == '+')
				lineDiff[0]++;
			else if (diffReader.getCurrentLine().charAt(0) == '-')
				lineDiff[1]++;
			//System.out.println(diffReader.getCurrentLine());
		}
		System.out.println(lineDiff[0] + " " + lineDiff[1]);
		return lineDiff;
	}

}
