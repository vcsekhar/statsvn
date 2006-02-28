package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.InputStream;

import net.sf.statcvs.output.ConfigurationOptions;

public class SvnDiffUtils {

    public synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr) {
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

    public synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr, String filename) {
        InputStream istream = null;
        String svnDiffCommand = "svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted " + filename;
        try {
            Process p = Runtime.getRuntime().exec(svnDiffCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
            istream = new BufferedInputStream(p.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return istream;
    }

}
