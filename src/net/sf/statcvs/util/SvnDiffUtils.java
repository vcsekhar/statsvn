package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.statcvs.output.ConfigurationOptions;

/**
 * Utilities class that manages calls to svn diff.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public class SvnDiffUtils {

    /**
     * Calls svn diff for the filename and revisions given. Will use URL invocation, to ensure that we get diffs even for deleted files.
     * 
     * @param oldRevNr
     *            old revision number
     * @param newRevNr
     *            new revision number
     * @param filename
     *            filename.
     * @return the InputStream related to the call. If the error steam is non-empty, will return the error stream instead of the default input stream.
     */
    private synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr, String filename) {
        InputStream istream = null;
        String svnDiffCommand = null;

        filename = SvnInfoUtils.relativePathToUrl(filename);
        svnDiffCommand = "svn diff  --old " + filename + "@" + oldRevNr + "  --new " + filename + "@" + newRevNr + SvnCommandHelper.getAuthString();

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
     */
    public static int[] getLineDiff(String oldRevNr, String newRevNr, String filename) throws IOException {
        InputStream diffStream = callSvnDiff(oldRevNr, newRevNr, filename);
        LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
        int lineDiff[] = parseDiff(diffReader);

        // not using logger because these diffs take lots of time and we want to show on the standard output.
        System.out.println("svn diff of " + filename + ", r" + oldRevNr + " to r" + newRevNr + ", +" + lineDiff[0] + " -" + lineDiff[1]);

        return lineDiff;
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
    private static int[] parseDiff(LookaheadReader diffReader) throws IOException {
    	int lineDiff[] = { -1, -1 };
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
            if (diffReader.getCurrentLine().length() == 0)
                continue;
            // very simple algorithm
            if (diffReader.getCurrentLine().charAt(0) == '+')
                lineDiff[0]++;
            else if (diffReader.getCurrentLine().charAt(0) == '-')
                lineDiff[1]++;
            else if (diffReader.getCurrentLine().indexOf("Property changes on:") == 0)
            	propertyChange = true;
            // System.out.println(diffReader.getCurrentLine());
        }
        if (propertyChange && (lineDiff[0] == -1 || lineDiff[1] == -1)) {
        	lineDiff[0] = 0;
        	lineDiff[1] = 0;
        }
        // System.out.println(lineDiff[0] + " " + lineDiff[1]);
        return lineDiff;
    }

}
