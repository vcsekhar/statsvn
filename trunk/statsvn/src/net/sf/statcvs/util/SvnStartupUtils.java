package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that verifies if the correct version of subversion is used.
 * 
 * @author Jean-Philippe Daigle <jpdaigle@softwareengineering.ca>
 * 
 * @version $Id$
 */
public class SvnStartupUtils {
    private static String SVN_VERSION_COMMAND = "svn --version";
    private static String SVN_MINIMUM_VERSION = "1.3.0";
    private static String SVN_VERSION_LINE_PATTERN = ".*version [0-9]+\\.[0-9]+\\.[0-9]+.*";
    private static String SVN_VERSION_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+";
    private static String SVN_INFO_WITHREPO_LINE_PATTERN= ".*<root>.+</root>.*";	//HACK: we "should" parse the output and check for a node named root, but this will work well enough
    private static String SVN_REPO_ROOT_NOTFOUND = "Repository root not available - verify that the project was checked out with svn version " + SVN_MINIMUM_VERSION + " or above.";
    private static Logger _logger = Logger.getLogger(SvnStartupUtils.class.getName());

    /**
     * Verifies that the current revision of SVN is SVN_MINIMUM_VERSION
     * 
     * @throws SvnVersionMismatchException
     *             if SVN executable not found or version less than SVN_MINIMUM_VERSION
     */
    public synchronized static void checkSvnVersionSufficient() throws SvnVersionMismatchException {
        try {
            
            
            InputStream istream = ProcessUtils.call(SVN_VERSION_COMMAND);
            LookaheadReader reader = new LookaheadReader(new InputStreamReader(istream));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.matches(SVN_VERSION_LINE_PATTERN)) {
                    // We have our version line
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
            
            if (ProcessUtils.hasErrorOccured())
                throw new IOException(ProcessUtils.getErrorMessage());
            

        } catch (Exception e) {
        		_logger.warning(e.getMessage());
        }

        throw new SvnVersionMismatchException();
    }
    
    /**
     * Verifies that the "svn info" command can return the repository root
     * (info available in svn >= 1.3.0)
     * 
     * @throws SvnVersionMismatchException
     *             if <tt>svn info</tt> failed to provide a non-empty repository root
     */
    public synchronized static void checkRepoRootAvailable() throws SvnVersionMismatchException {
        try {
        		boolean ROOT_ONLY_TRUE = true;
            InputStream istream = SvnInfoUtils.getSvnInfo(ROOT_ONLY_TRUE);
            LookaheadReader reader = new LookaheadReader(new InputStreamReader(istream));

            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                if (line.matches(SVN_INFO_WITHREPO_LINE_PATTERN)) {
                    // We have our <root> element in the svn info AND it's not empty --> checkout performed 
                		// with a compatible version of subversion client.
                		istream.close();
                		return; // success
                }
            }
            
			if (ProcessUtils.hasErrorOccured())
			{
				throw new IOException(ProcessUtils.getErrorMessage());
			}
            
            istream.close();
            
            

        } catch (Exception e) {
        		_logger.warning(e.getMessage());
        }

        throw new SvnVersionMismatchException(SVN_REPO_ROOT_NOTFOUND);

	}
}
