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

 $RCSfile: Main.java,v $
 Created on $Date: 2005/03/20 19:12:25 $
 */
package net.sf.statsvn;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.statcvs.Messages;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.ReportConfig;
import net.sf.statcvs.pages.ReportSuiteMaker;
import net.sf.statsvn.input.Builder;
import net.sf.statsvn.input.RepositoryFileManager;
import net.sf.statsvn.input.SvnLogfileParser;
import net.sf.statsvn.output.ChurnPageMaker;
import net.sf.statsvn.output.RepoMapPageMaker;
import net.sf.statsvn.output.SvnCommandLineParser;
import net.sf.statsvn.output.SvnConfigurationOptions;
import net.sf.statsvn.util.SvnStartupUtils;
import net.sf.statsvn.util.SvnVersionMismatchException;

/**
 * StatSvn Main Class; it starts the application and controls command-line
 * related stuff
 *
 * @author Lukasz Pekacki
 * @author Richard Cyganiak
 * @version $Id: Main.java,v 1.47 2005/03/20 19:12:25 squig Exp $
 */
public final class Main {
	private static final int KB_IN_ONE_MB = 1024;
	private static final int NUMBER_OF_MS_IN_ONE_SEC = 1000;
	private static final Logger LOGGER = Logger.getLogger("net.sf.statcvs");
	private static final LogManager LM = LogManager.getLogManager();

	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private Main() {
	}

	/**
	 * Main method of StatCvs
	 *
	 * @param args
	 *            command line options
	 */
	public static void main(final String[] args) {
		Messages.setPrimaryResource("net.sf.statsvn.statcvs"); // primary is statcvs.properties in net.sf.statsvn
		
		SvnConfigurationOptions.getTaskLogger().log(Messages.getString("PROJECT_NAME") + Messages.NL);

		if (args.length == 0) {
			printProperUsageAndExit();
		}
		if (args.length == 1) {
			final String arg = args[0].toLowerCase(Locale.getDefault());
			if (arg.equals("-h") || arg.equals("-help")) {
				printProperUsageAndExit();
			} else if (arg.equals("-version")) {
				printVersionAndExit();
			}
		}

		try {
			new SvnCommandLineParser(args).parse();
			SvnStartupUtils.checkSvnVersionSufficient();
			SvnStartupUtils.checkRepoRootAvailable();
			generateDefaultHTMLSuite();
		} catch (final ConfigurationException cex) {
			SvnConfigurationOptions.getTaskLogger().log(cex.getMessage());
			System.exit(1);
		} catch (final LogSyntaxException lex) {
			printLogErrorMessageAndExit(lex.getMessage());
		} catch (final IOException ioex) {
			printIoErrorMessageAndExit(ioex.getMessage());
		} catch (final OutOfMemoryError oome) {
			printOutOfMemMessageAndExit();
		} catch (final SvnVersionMismatchException ever) {
			printErrorMessageAndExit(ever.getMessage());
		}
		System.exit(0);
	}

	private static void initLogManager(final String loggingProperties) {
		try {
			final InputStream stream = Main.class.getResourceAsStream(loggingProperties);
			LM.readConfiguration(stream);
			stream.close();
		} catch (final IOException e) {
			SvnConfigurationOptions.getTaskLogger().log("ERROR: Logging could not be initialized!");
		}
	}

	private static void printProperUsageAndExit() {
		SvnConfigurationOptions.getTaskLogger().log(
		// max. 80 chars
				// 12345678901234567890123456789012345678901234567890123456789012345678901234567890
				"Usage: java -jar statsvn.jar [options] <logfile> <directory>\n" + "\n" + "Required parameters:\n"
						+ "  <logfile>          path to the cvs logfile of the module\n"
						+ "  <directory>        path to the directory of the checked out module\n" + "\n" 
						+ "Some options:\n"
						+ "  -version           print the version information and exit\n" 
						+ "  -output-dir <dir>  directory where HTML suite will be saved\n"
						+ "  -include <pattern> include only files matching pattern, e.g. **/*.c;**/*.h\n"
						+ "  -exclude <pattern> exclude matching files, e.g. tests/**;docs/**\n"
						+ "  -tags <regexp>     show matching tags in lines of code chart, e.g. version-.*\n"
						+ "  -title <title>     Project title to be used in reports\n" 
						+ "  -viewvc <url>      integrate with ViewVC installation at <url>\n"
						+ "  -bugzilla <url>    integrate with Bugzilla installation at <url>\n" 
						+ "  -username <svnusername> username to pass to svn\n"
						+ "  -password <svnpassword> password to pass to svn\n" 
						+ "  -verbose           print extra progress information\n"
                        + "  -xdoc              optional switch output to xdoc\n"
                        + "  -threads <int>     how many threads for svn diff (default: 25)"
                        + "  -concurrencyThreshold <millisec> switch to concurrent svn diff if 1st call>threshold (default: 4000)"
						+ "\nFull options list: http://www.statsvn.org");
		System.exit(1);
	}

	private static void printVersionAndExit() {
		SvnConfigurationOptions.getTaskLogger().log("Version " + Messages.getString("PROJECT_VERSION"));
		System.exit(1);
	}

	private static void printOutOfMemMessageAndExit() {
		SvnConfigurationOptions.getTaskLogger().log("OutOfMemoryError.");
		SvnConfigurationOptions.getTaskLogger().log("Try running java with the -mx option (e.g. -mx128m for 128Mb).");
		System.exit(1);
	}

	private static void printLogErrorMessageAndExit(final String message) {
		SvnConfigurationOptions.getTaskLogger().log("Logfile parsing failed.");
		SvnConfigurationOptions.getTaskLogger().log(message);
		System.exit(1);
	}

	private static void printIoErrorMessageAndExit(final String message) {
		SvnConfigurationOptions.getTaskLogger().log(message);
		System.exit(1);
	}
	
	public static String printStackTrace(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e2) {
			if (e != null)
				return e.getMessage();
			else
				return "";
		}
	}

	private static void printErrorMessageAndExit(final String message) {
		SvnConfigurationOptions.getTaskLogger().log(message);
		System.exit(1);
	}

	/**
	 * Generates HTML report. {@link net.sf.statsvn.output.ConfigurationOptions}
	 * must be initialized before calling this method.
	 *
	 * @throws LogSyntaxException
	 *             if the logfile contains unexpected syntax
	 * @throws IOException
	 *             if some file can't be read or written
	 * @throws ConfigurationException
	 *             if a required ConfigurationOption was not set
	 */
	public static void generateDefaultHTMLSuite() throws LogSyntaxException, IOException, ConfigurationException {
		generateDefaultHTMLSuite(new RepositoryFileManager(ConfigurationOptions.getCheckedOutDirectory()));
	}

	/**
	 * Generates HTML report. {@link net.sf.statsvn.output.ConfigurationOptions}
	 * must be initialized before calling this method.
	 *
	 * @param externalRepositoryFileManager
	 *            RepositoryFileManager which is used to access the files in the
	 *            repository.
	 *
	 * @throws LogSyntaxException
	 *             if the logfile contains unexpected syntax
	 * @throws IOException
	 *             if some file can't be read or written
	 * @throws ConfigurationException
	 *             if a required ConfigurationOption was not set
	 */
	public static void generateDefaultHTMLSuite(final RepositoryFileManager repFileMan) throws LogSyntaxException, IOException, ConfigurationException {

		if (ConfigurationOptions.getLogFileName() == null) {
			throw new ConfigurationException("Missing logfile name");
		}
		if (ConfigurationOptions.getCheckedOutDirectory() == null) {
			throw new ConfigurationException("Missing checked out directory");
		}

		final long memoryUsedOnStart = Runtime.getRuntime().totalMemory();
		final long startTime = System.currentTimeMillis();

		initLogManager(ConfigurationOptions.getLoggingProperties());

		LOGGER.info("Parsing SVN log '" + ConfigurationOptions.getLogFileName() + "'");

		final FileInputStream logFile = new FileInputStream(ConfigurationOptions.getLogFileName());
		final Builder builder = new Builder(repFileMan, ConfigurationOptions.getIncludePattern(), ConfigurationOptions.getExcludePattern());
		new SvnLogfileParser(repFileMan, logFile, builder).parse();
		logFile.close();

		if (ConfigurationOptions.getProjectName() == null) {
			ConfigurationOptions.setProjectName(builder.getProjectName());
		}
		if (ConfigurationOptions.getWebRepository() != null) {
			ConfigurationOptions.getWebRepository().setAtticFileNames(builder.getAtticFileNames());
		}

		LOGGER.info("Generating report for " + ConfigurationOptions.getProjectName() + " into " + ConfigurationOptions.getOutputDir());
		LOGGER.info("Using " + ConfigurationOptions.getCssHandler());
		final Repository content = builder.createRepository();

		// make JFreeChart work on systems without GUI
		System.setProperty("java.awt.headless", "true");
 
		ReportConfig config = new ReportConfig(
			content, 
			ConfigurationOptions.getProjectName(),
			ConfigurationOptions.getOutputDir(), 
			ConfigurationOptions.getMarkupSyntax(),
			ConfigurationOptions.getCssHandler());
		config.setWebRepository(ConfigurationOptions.getWebRepository());
		config.setWebBugtracker(ConfigurationOptions.getWebBugtracker());
		config.setNonDeveloperLogins(ConfigurationOptions.getNonDeveloperLogins());
		
		// add new reports
		List extraReports = new ArrayList();
		extraReports.add(new RepoMapPageMaker(config).toFile());
		extraReports.add(new ChurnPageMaker(config).toFile());
		
		new ReportSuiteMaker(config, ConfigurationOptions.getNotes(), extraReports).toFile().write();
		
		final long endTime = System.currentTimeMillis();
		final long memoryUsedOnEnd = Runtime.getRuntime().totalMemory();

		LOGGER.info("runtime: " + (((double) endTime - startTime) / NUMBER_OF_MS_IN_ONE_SEC) + " seconds");
		LOGGER.info("memory usage: " + (((double) memoryUsedOnEnd - memoryUsedOnStart) / KB_IN_ONE_MB) + " kb");
	}
}
