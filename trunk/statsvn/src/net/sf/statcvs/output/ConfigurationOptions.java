/*
 StatCvs - CVS statistics generation 
 Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
 http://statcvs.sf.net/
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 $RCSfile: ConfigurationOptions.java,v $
 $Date: 2005/03/20 19:12:25 $ 
 */
package net.sf.statcvs.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.statcvs.util.FilePatternMatcher;
import net.sf.statcvs.util.FileUtils;

/**
 * Class for storing all command line parameters. The parameters are set by the {@link net.sf.statcvs.Main#main} method. Interested classes can read all
 * parameter values from here.
 * 
 * TODO: Should be moved to more appropriate package and made non-public
 * 
 * @author jentzsch
 * @version $Id: ConfigurationOptions.java,v 1.17 2005/03/20 19:12:25 squig Exp $
 */
public class ConfigurationOptions {

    private static final String LOGGING_CONFIG_DEFAULT = "logging.properties";
    private static final String LOGGING_CONFIG_VERBOSE = "logging-verbose.properties";
    private static final String LOGGING_CONFIG_DEBUG = "logging-debug.properties";

    private static String logFileName = null;
    private static String checkedOutDirectory = null;
    private static String projectName = null;
    private static String outputDir = "";
    private static String cacheDir = "";
    private static String defaultCacheDir = System.getProperty("user.home") + FileUtils.getDirSeparator() + ".statsvn" + FileUtils.getDirSeparator();
    private static String loggingProperties = LOGGING_CONFIG_DEFAULT;
    private static String notesFile = null;
    private static String notes = null;
    private static String svnUsername = null;
    private static String svnPassword = null;
    
    private static FilePatternMatcher includePattern = null;
    private static FilePatternMatcher excludePattern = null;

    private static CssHandler cssHandler = new DefaultCssHandler("statcvs.css");
    private static WebRepositoryIntegration webRepository = null;
    private static WebBugtrackerIntegration webBugTracker = null;
    private static Pattern symbolicNamesPattern;
    private static String outputFormat = "html";

    /**
     * returns the {@link CssHandler}
     * 
     * @return the CssHandler
     */
    public static CssHandler getCssHandler() {
        return cssHandler;
    }

    /**
     * Method getProjectName.
     * 
     * @return String name of the project
     */
    public static String getProjectName() {
        return projectName;
    }

    /**
     * Method getCheckedOutDirectory.
     * 
     * @return String name of the checked out directory
     */
    public static String getCheckedOutDirectory() {
        return checkedOutDirectory;
    }

    /**
     * Method getLogfilename.
     * 
     * @return String name of the logfile to be parsed
     */
    public static String getLogFileName() {
        return logFileName;
    }

    /**
     * Returns the outputDir.
     * 
     * @return String output Directory
     */
    public static String getOutputDir() {
        return outputDir;
    }

    /**
     * Returns the cacheDir.
     * 
     * @return String output Directory
     */
    public static String getCacheDir() {
        return cacheDir;
    }
    
    /**
     * Returns the report notes (from "-notes filename" switch) or <tt>null</tt> if not specified
     * 
     * @return the report notes
     */
    public static String getNotes() {
        return notes;
    }

    /**
     * Returns a {@link WebRepositoryIntegration} object if the user has specified a URL to one. <tt>null</tt> otherwise.
     * 
     * @return the web repository
     */
    public static WebRepositoryIntegration getWebRepository() {
        return webRepository;
    }

    /**
     * Sets the checkedOutDirectory.
     * 
     * @param checkedOutDirectory
     *            The checkedOutDirectory to set
     * @throws ConfigurationException
     *             if directory does not exist
     */
    public static void setCheckedOutDirectory(String checkedOutDirectory) throws ConfigurationException {
        File directory = new File(checkedOutDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ConfigurationException("directory does not exist: " + checkedOutDirectory);
        }
        ConfigurationOptions.checkedOutDirectory = checkedOutDirectory;
    }

    /**
     * Sets the cssFile. Currently, the css file can be any local file or a HTTP URL. If it is a local file, a copy will be included in the output directory. If
     * this method is never called, a default CSS file will be generated in the output directory.
     * 
     * @param cssFile
     *            The cssFile to set
     * @throws ConfigurationException
     *             if the specified CSS file can not be accessed from local file system or from URL source, or if the specified CSS file is local and does not
     *             exist
     */
    public static void setCssFile(String cssFile) throws ConfigurationException {
        try {
            URL url = new URL(cssFile);
            if (!url.getProtocol().equals("http")) {
                throw new ConfigurationException("Only HTTP URLs or local files allowed for -css");
            }
            cssHandler = new UrlCssHandler(url);
        } catch (MalformedURLException isLocalFile) {
            cssHandler = new LocalFileCssHandler(cssFile);
        }
        cssHandler.checkForMissingResources();
    }
    
    /**
     * Allow change between css that are shipped with StatSvn.
     * @param cssName statcvs.css or objectlab-statcvs-xdoc.css
     */
    public static void setDefaultCssFile(String cssName) {
        cssHandler = new DefaultCssHandler(cssName);
    }

    /**
     * Sets the logFileName.
     * 
     * @param logFileName
     *            The logFileName to set
     * @throws ConfigurationException
     *             if the file does not exist
     */
    public static void setLogFileName(String logFileName) throws ConfigurationException {
        File inputFile = new File(logFileName);
        if (!inputFile.exists()) {
            throw new ConfigurationException("Specified logfile not found: " + logFileName);
        }
        ConfigurationOptions.logFileName = logFileName;
    }

    /**
     * Sets the outputDir.
     * 
     * @param outputDir
     *            The outputDir to set
     * @throws ConfigurationException
     *             if the output directory cannot be created
     */
    public static void setOutputDir(String outputDir) throws ConfigurationException {
        if (!outputDir.endsWith(FileUtils.getDirSeparator())) {
            outputDir += FileUtils.getDefaultDirSeparator();
        }
        File outDir = new File(outputDir);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new ConfigurationException("Can't create output directory: " + outputDir);
        }
        ConfigurationOptions.outputDir = outputDir;
    }

    /**
     * Sets the cacheDir.
     * 
     * @param cacheDir
     *            The cacheDir to set
     * @throws ConfigurationException
     *             if the cache directory cannot be created
     */
    public static void setCacheDir(String cacheDir) throws ConfigurationException {
        if (!cacheDir.endsWith(FileUtils.getDirSeparator())) {
            cacheDir += FileUtils.getDefaultDirSeparator();
        }
        File cDir = new File(cacheDir);
        if (!cDir.exists() && !cDir.mkdirs()) {
            throw new ConfigurationException("Can't create cache directory: " + cacheDir);
        }
        ConfigurationOptions.cacheDir = cacheDir;
    }
    
    /**
     * Sets the cacheDir to the defaultCacheDir
     * 
     * @throws ConfigurationException
     *             if the cache directory cannot be created
     */
    public static void setCacheDirToDefault() throws ConfigurationException {
    	setCacheDir(defaultCacheDir);
    }
    
    /**
     * Sets the name of the notes file. The notes file will be included on the {@link IndexPage} of the output. It must contain a valid block-level HTML
     * fragment (for example <tt>"&lt;p&gt;Some notes&lt;/p&gt;"</tt>)
     * 
     * @param notesFile
     *            a local filename
     * @throws ConfigurationException
     *             if the file is not found or can't be read
     */
    public static void setNotesFile(String notesFile) throws ConfigurationException {
        File f = new File(notesFile);
        if (!f.exists()) {
            throw new ConfigurationException("Notes file not found: " + notesFile);
        }
        if (!f.canRead()) {
            throw new ConfigurationException("Can't read notes file: " + notesFile);
        }
        ConfigurationOptions.notesFile = notesFile;
        try {
            notes = readNotesFile();
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    /**
     * Sets the URL to a <a href="http://www.viewvc.org/">ViewVC</a> web-based CVS/SVN browser. This must be the URL at which the checked-out module's
     * root can be viewed in ViewVC.
     * 
     * @param url
     *            URL to a ViewVC repository
     */
    public static void setViewVcURL(String url) {
        ConfigurationOptions.webRepository = new ViewVcIntegration(url);
    }


    /**
     * Sets the URL to a <a href="http://www.horde.org/chora/">Chora</a> web-based CVS/SVN browser. This must be the URL at which the checked-out module's root can
     * be viewed in Chora.
     * 
     * @param url
     *            URL to a cvsweb repository
     */
    public static void setChoraURL(String url) {
        ConfigurationOptions.webRepository = new ChoraIntegration(url);
    }

    /**
     * Sets a project title to be used in the reports
     * 
     * @param projectName
     *            The project title to be used in the reports
     */
    public static void setProjectName(String projectName) {
        ConfigurationOptions.projectName = projectName;
    }

    /**
     * Gets the name of the logging properties file
     * 
     * @return the name of the logging properties file
     */
    public static String getLoggingProperties() {
        return loggingProperties;
    }

    /**
     * Sets the logging level to verbose
     */
    public static void setVerboseLogging() {
        ConfigurationOptions.loggingProperties = LOGGING_CONFIG_VERBOSE;
    }

    /**
     * Sets the logging level to debug
     */
    public static void setDebugLogging() {
        ConfigurationOptions.loggingProperties = LOGGING_CONFIG_DEBUG;
    }

    private static String readNotesFile() throws IOException {
    	FileReader fileReader = new FileReader(notesFile);
        BufferedReader r = new BufferedReader(fileReader);
        String line = r.readLine();
        StringBuffer result = new StringBuffer();
        while (line != null) {
            result.append(line);
            line = r.readLine();
        }
        fileReader.close();
        return result.toString();
    }

    /**
     * Sets a file include pattern list. Only files matching one of the patterns will be included in the analysis.
     * 
     * @param patternList
     *            a list of Ant-style wildcard patterns, seperated by : or ;
     * @see net.sf.statcvs.util.FilePatternMatcher
     */
    public static void setIncludePattern(String patternList) {
        includePattern = new FilePatternMatcher(patternList);
    }

    /**
     * Sets a file exclude pattern list. Files matching any of the patterns will be excluded from the analysis.
     * 
     * @param patternList
     *            a list of Ant-style wildcard patterns, seperated by : or ;
     * @see net.sf.statcvs.util.FilePatternMatcher
     */
    public static void setExcludePattern(String patternList) {
        excludePattern = new FilePatternMatcher(patternList);
    }

    /**
     * @return Returns the excludePattern.
     */
    public static FilePatternMatcher getExcludePattern() {
        return excludePattern;
    }

    /**
     * @return Returns the includePattern.
     */
    public static FilePatternMatcher getIncludePattern() {
        return includePattern;
    }

    public static void setSymbolicNamesPattern(String symbolicNamesPattern) throws ConfigurationException {
        try {
            ConfigurationOptions.symbolicNamesPattern = Pattern.compile(symbolicNamesPattern);
        } catch (PatternSyntaxException e) {
            throw new ConfigurationException("Invalid regular expression for tags: " + e.getLocalizedMessage());
        }
    }

    public static Pattern getSymbolicNamesPattern() {
        return symbolicNamesPattern;
    }

    public static File getCheckedOutDirectoryAsFile() {
        return new File(FileUtils.getPathWithoutEndingSlash(getCheckedOutDirectory()) + FileUtils.getDirSeparator());
    }

	/**
	 * @return Returns the svnPassword.
	 */
	public static String getSvnPassword() {
		return svnPassword;
	}

	/**
	 * @param svnPassword The svnPassword to set.
	 */
	public static void setSvnPassword(String svnPassword) {
		ConfigurationOptions.svnPassword = svnPassword;
	}

	/**
	 * @return Returns the svnUsername.
	 */
	public static String getSvnUsername() {
		return svnUsername;
	}

	/**
	 * @param svnUsername The svnUsername to set.
	 */
	public static void setSvnUsername(String svnUsername) {
		ConfigurationOptions.svnUsername = svnUsername;
	}


	public static void setBugzillaUrl(String bugzillaUrl) {
		ConfigurationOptions.webBugTracker = new BugzillaIntegration(bugzillaUrl);
	}
	
	public static WebBugtrackerIntegration getWebBugtracker() {
		return ConfigurationOptions.webBugTracker;
	}

    public static void setOutputFormat(final String outputFormat) {
        ConfigurationOptions.outputFormat = outputFormat;
    }

    public static String getOutputFormat() {
        return ConfigurationOptions.outputFormat; 
    }
}
