package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.statcvs.output.ConfigurationOptions;

public class SvnPropgetUtils {

    protected static List binaryFiles;

    protected synchronized static InputStream getFileMimeTypes() {
        return getFileMimeTypes(null, null);
    }

    protected synchronized static InputStream getFileMimeTypes(String revision, String filename) {
        InputStream istream = null;

        String svnPropgetCommand = "svn propget svn:mime-type";
        if (revision != null && revision.length() > 0)
            svnPropgetCommand += " -r " + revision;

        if (filename != null && filename.length() > 0)
            svnPropgetCommand += " " + filename;
        else
            svnPropgetCommand += " -R ";

        try {
            Process p = Runtime.getRuntime().exec(svnPropgetCommand, null, ConfigurationOptions.getCheckedOutDirectoryAsFile());
            istream = new BufferedInputStream(p.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return istream;
    }

    public static List getBinaryFiles() {
        if (binaryFiles == null) {
            binaryFiles = new ArrayList();
            LookaheadReader mimeReader = new LookaheadReader(new InputStreamReader(getFileMimeTypes()));
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
        return binaryFiles;
    }

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
     * Given a string such as: "lib\junit.jar - application/octet-stream" or "svn:\\host\repo\lib\junit.jar - application/octet-stream" will return the filename
     * if the mime type is binary (doesn't end with text/*)
     * 
     * Will return the filename with / was a directory seperator.
     * 
     * @param currentLine
     *            the line obtained from svn propget svn:mime-type
     * @param removeRoot
     *            if true, will remove any repository prefix
     * @return should return lib\junit.jar in both cases, given that removeRoot==true in the second case.
     */
    private static String getBinaryFilename(String currentLine, boolean removeRoot) {
        // want to make sure we only have / in end result.
        String line = currentLine.replace(FileUtils.getDefaultDirSeparator(), "/");
        String octetStream = " - application/octet-stream";
        // if is common binary file or identified as something other than text
        if (line.endsWith(octetStream) || line.lastIndexOf(" - text/") < 0 && line.lastIndexOf(" - text/") == line.lastIndexOf(" - ")) {
            line = line.substring(0, line.lastIndexOf(" - "));
            if (removeRoot)
                line = SvnInfoUtils.absoluteToRelativePath(line);
            return line;
        }

        return null;
    }

}
