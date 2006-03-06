package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvnStartupUtils {
	private static String SVN_VERSION_COMMAND = "svn --version";
	private static String SVN_MINIMUM_VERSION = "1.3.0";
	private static String SVN_VERSION_LINE_PATTERN = ".*version [0-9]+\\.[0-9]+\\.[0-9]+.*";
	private static String SVN_VERSION_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+";
	
	public synchronized static void checkSvnVersionSufficient() throws SvnVersionMismatchException {
        try {
            Process p = Runtime.getRuntime().exec(SVN_VERSION_COMMAND, null, null);
            InputStream istream = new BufferedInputStream(p.getInputStream());
            LookaheadReader reader = new LookaheadReader(new InputStreamReader(istream));

            while(reader.hasNextLine()) {
            		String line = reader.nextLine();
            		if (line.matches(SVN_VERSION_LINE_PATTERN)) {
            			// We have our version line
            			//System.out.println(">>>" + line);
            			Pattern pRegex = Pattern.compile(SVN_VERSION_PATTERN);
            			Matcher m = pRegex.matcher(line);
            			if (m.find()) {
            				String versionString = line.substring(m.start(), m.end());
            				
            				// we perform a simple string comparison against the version numbers
            				if (versionString.compareTo(SVN_MINIMUM_VERSION) >= 0)
            					return; // success
            				else
            					throw new SvnVersionMismatchException(versionString, SVN_MINIMUM_VERSION);
            			}
            		}
            }
            	istream.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new SvnVersionMismatchException();
	}
}
