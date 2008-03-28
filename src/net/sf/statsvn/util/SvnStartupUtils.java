package net.sf.statsvn.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.statcvs.util.LookaheadReader;
import net.sf.statsvn.output.SvnConfigurationOptions;

/**
 * Utility class that verifies if the correct version of subversion is used.
 * 
 * @author Jean-Philippe Daigle <jpdaigle@softwareengineering.ca>
 * 
 * @version $Id$
 */
public final class SvnStartupUtils {
    private static final String SVN_VERSION_COMMAND = "svn --version";
    private static final String SVN_MINIMUM_VERSION = "1.3.0";
    private static final String SVN_MINIMUM_VERSION_DIFF_PER_REV = "1.4.0";
    private static final String SVN_VERSION_LINE_PATTERN = ".* [0-9]+\\.[0-9]+\\.[0-9]+.*";
    private static final String SVN_VERSION_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+";
    
    //  HACK: we "should" parse the output and check for a node named root, but this will work well enough
    private static final String SVN_INFO_WITHREPO_LINE_PATTERN= ".*<root>.+</root>.*";	
    
    private static final String SVN_REPO_ROOT_NOTFOUND = "Repository root not available - verify that the project was checked out with svn version " 
    	+ SVN_MINIMUM_VERSION + " or above.";

	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private SvnStartupUtils() {
	}

	/**
	 * Verifies that the current revision of SVN is SVN_MINIMUM_VERSION
	 * 
	 * @throws SvnVersionMismatchException
	 *             if SVN executable not found or version less than
	 *             SVN_MINIMUM_VERSION
	 * @return the version string
	 */
	public static synchronized String checkSvnVersionSufficient() throws SvnVersionMismatchException {
    	ProcessUtils pUtils = null;
        try {
            
            pUtils = ProcessUtils.call(SVN_VERSION_COMMAND);
            final InputStream istream = pUtils.getInputStream();
            final LookaheadReader reader = new LookaheadReader(new InputStreamReader(istream));

            while (reader.hasNextLine()) {
                final String line = reader.nextLine();
                if (line.matches(SVN_VERSION_LINE_PATTERN)) {
                    // We have our version line
                    final Pattern pRegex = Pattern.compile(SVN_VERSION_PATTERN);
                    final Matcher m = pRegex.matcher(line);
                    if (m.find()) {
                        final String versionString = line.substring(m.start(), m.end());

                        // we perform a simple string comparison against the version numbers
                        if (versionString.compareTo(SVN_MINIMUM_VERSION) >= 0) {
							return versionString; // success
						} else {
							throw new SvnVersionMismatchException(versionString, SVN_MINIMUM_VERSION);
						}
                    }
                }
            }
            
            if (pUtils.hasErrorOccured()) {
				throw new IOException(pUtils.getErrorMessage());
			}
        } catch (final IOException e) {
        	SvnConfigurationOptions.getTaskLogger().info(e.getMessage());
        } catch (final RuntimeException e) {
        	SvnConfigurationOptions.getTaskLogger().info(e.getMessage());
        } finally {
        	if (pUtils != null) {
        		try {
					pUtils.close();
				} catch (final IOException e) {
					SvnConfigurationOptions.getTaskLogger().info(e.getMessage());
				}
        	}
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
    public static synchronized void checkRepoRootAvailable() throws SvnVersionMismatchException {
    	ProcessUtils pUtils = null;
        try {
       		final boolean rootOnlyTrue = true;
       		pUtils = SvnInfoUtils.getSvnInfo(rootOnlyTrue);
            final InputStream istream = pUtils.getInputStream();
            final LookaheadReader reader = new LookaheadReader(new InputStreamReader(istream));

            while (reader.hasNextLine()) {
                final String line = reader.nextLine();
                if (line.matches(SVN_INFO_WITHREPO_LINE_PATTERN)) {
                    // We have our <root> element in the svn info AND it's not empty --> checkout performed 
                		// with a compatible version of subversion client.
                		istream.close();
                		return; // success
                }
            }
            
			if (pUtils.hasErrorOccured())	{
				throw new IOException(pUtils.getErrorMessage());
			}
        } catch (final Exception e) {
        	SvnConfigurationOptions.getTaskLogger().info(e.getMessage());
        } finally {
        	if (pUtils != null) {
        		try {
					pUtils.close();
				} catch (final IOException e) {
					SvnConfigurationOptions.getTaskLogger().info(e.getMessage());
				}
        	}
        }

        throw new SvnVersionMismatchException(SVN_REPO_ROOT_NOTFOUND);

	}
    
    public static synchronized boolean checkDiffPerRevPossible(String version) {
        // we perform a simple string comparison against the version numbers
        return version.compareTo(SVN_MINIMUM_VERSION_DIFF_PER_REV) >= 0;
    }
}
