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
package net.sf.statcvs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.statcvs.input.Builder;
import net.sf.statcvs.input.EmptyRepositoryException;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.input.RepositoryFileManager;
import net.sf.statcvs.input.SvnLogfileParser;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.output.CommandLineParser;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.HTMLOutput;

/**
 * StatCvs Main Class; it starts the application and controls command-line
 * related stuff
 * 
 * @author Lukasz Pekacki
 * @author Richard Cyganiak
 * @version $Id: Main.java,v 1.47 2005/03/20 19:12:25 squig Exp $
 */
public class Main {
	private static Logger logger = Logger.getLogger("net.sf.statcvs");
	private static LogManager lm = LogManager.getLogManager();

	/**
	 * Main method of StatCvs
	 * 
	 * @param args
	 *            command line options
	 */
	public static void main(String[] args) {
		System.out.println(Messages.getString("PROJECT_NAME") + Messages.NL);

		if (args.length == 0) {
			printProperUsageAndExit();
		}
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			if (arg.equals("-h") || arg.equals("-help")) {
				printProperUsageAndExit();
			} else if (arg.equals("-version")) {
				printVersionAndExit();
			}
		}

		try {
			new CommandLineParser(args).parse();
			generateDefaultHTMLSuite();
		} catch (ConfigurationException cex) {
			System.err.println(cex.getMessage());
			System.exit(1);
		} catch (LogSyntaxException lex) {
			printLogErrorMessageAndExit(lex.getMessage());
		} catch (IOException ioex) {
			printIoErrorMessageAndExit(ioex.getMessage());
		} catch (OutOfMemoryError oome) {
			printOutOfMemMessageAndExit();
		} catch (EmptyRepositoryException erex) {
			printEmptyRepositoryMessageAndExit();
		}
		System.exit(0);
	}

	private static void initLogManager(String loggingProperties) {
		try {
			lm.readConfiguration(Main.class.getResourceAsStream(loggingProperties));
		} catch (IOException e) {
			System.err.println("ERROR: Logging could not be initialized!");
		}
	}

	private static void printProperUsageAndExit() {
		System.out.println(
		// max. 80 chars
				// 12345678901234567890123456789012345678901234567890123456789012345678901234567890
				"Usage: java -jar statcvs.jar [options] <logfile> <directory>\n" + "\n" + "Required parameters:\n"
						+ "  <logfile>          path to the cvs logfile of the module\n"
						+ "  <directory>        path to the directory of the checked out module\n" + "\n" + "Some options:\n"
						+ "  -version           print the version information and exit\n" + "  -output-dir <dir>  directory where HTML suite will be saved\n"
						+ "  -include <pattern> include only files matching pattern, e.g. **/*.c;**/*.h\n"
						+ "  -exclude <pattern> exclude matching files, e.g. tests/**;docs/**\n"
						+ "  -tags <regexp>     show matching tags in lines of code chart, e.g. version-.*\n"
						+ "  -title <title>     Project title to be used in reports\n" + "  -viewcvs <url>     integrate with ViewCVS installation at <url>\n"
						+ "  -verbose           print extra progress information\n" + "\n" + "Full options list: http://statcvs.sf.net/manual");
		System.exit(1);
	}

	private static void printVersionAndExit() {
		System.out.println("Version 1.0.0");
		System.exit(1);
	}

	private static void printOutOfMemMessageAndExit() {
		System.err.println("OutOfMemoryError.");
		System.err.println("Try running java with the -mx option (e.g. -mx128m for 128Mb).");
		System.exit(1);
	}

	private static void printLogErrorMessageAndExit(String message) {
		System.err.println("Logfile parsing failed.");
		System.err.println(message);
		System.exit(1);
	}

	private static void printIoErrorMessageAndExit(String message) {
		System.err.println(message);
		System.exit(1);
	}

	private static void printEmptyRepositoryMessageAndExit() {
		System.err.println("No revisions found in the log!");
		System.exit(1);
	}

	/**
	 * Generates HTML report. {@link net.sf.statcvs.output.ConfigurationOptions}
	 * must be initialized before calling this method.
	 * 
	 * @throws LogSyntaxException
	 *             if the logfile contains unexpected syntax
	 * @throws IOException
	 *             if some file can't be read or written
	 * @throws ConfigurationException
	 *             if a required ConfigurationOption was not set
	 */
	public static void generateDefaultHTMLSuite() throws LogSyntaxException, IOException, ConfigurationException, EmptyRepositoryException {

		if (ConfigurationOptions.getLogFileName() == null) {
			throw new ConfigurationException("Missing logfile name");
		}
		if (ConfigurationOptions.getCheckedOutDirectory() == null) {
			throw new ConfigurationException("Missing checked out directory");
		}

		long memoryUsedOnStart = Runtime.getRuntime().totalMemory();
		long startTime = System.currentTimeMillis();

		initLogManager(ConfigurationOptions.getLoggingProperties());

		logger.info("Parsing SVN log '" + ConfigurationOptions.getLogFileName() + "'");

		FileInputStream logFile = new FileInputStream(ConfigurationOptions.getLogFileName());
		RepositoryFileManager repFileMan = new RepositoryFileManager(ConfigurationOptions.getCheckedOutDirectory());
		Builder builder = new Builder(repFileMan, ConfigurationOptions.getIncludePattern(), ConfigurationOptions.getExcludePattern());
		new SvnLogfileParser(logFile, builder).parse();

//		String repositoryRevision = SvnLogUtils.getRevisionNumber(".");
//
//		if (repositoryRevision == null)
//			throw new LogSyntaxException("Unable to find working directory's revision number");
//
//		int revision = Integer.parseInt(repositoryRevision);
//		for (int i = 1; i < revision; i++) {
//			System.out.println("revs " + Integer.toString(i));
//			InputStream diffStream = SvnDiffUtils.callSvnDiff(Integer.toString(i), Integer.toString(i + 1));
//			LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
//			while (diffReader.hasNextLine()) {
//				diffReader.nextLine();
//	//			System.out.println(diffReader.getCurrentLine());
//			}
//		}

		if (ConfigurationOptions.getProjectName() == null) {
			ConfigurationOptions.setProjectName(builder.getProjectName());
		}
		if (ConfigurationOptions.getWebRepository() != null) {
			ConfigurationOptions.getWebRepository().setAtticFileNames(builder.getAtticFileNames());
		}

		logger.info("Generating report for " + ConfigurationOptions.getProjectName() + " into " + ConfigurationOptions.getOutputDir());
		logger.info("Using " + ConfigurationOptions.getCssHandler());
		Repository content = builder.createRepository();
		new HTMLOutput(content).createHTMLSuite();

		long endTime = System.currentTimeMillis();
		long memoryUsedOnEnd = Runtime.getRuntime().totalMemory();

		logger.info("runtime: " + (((double) endTime - startTime) / 1000) + " seconds");
		logger.info("memory usage: " + (((double) memoryUsedOnEnd - memoryUsedOnStart) / 1024) + " kb");
	}
}
