package net.sf.statcvs.input;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.statcvs.Main;
import net.sf.statcvs.output.CommandLineParser;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;

/**
 * High-level scenarios to verify parsing without actually needing a server.
 * 
 * @author jkealey
 * 
 */
public class SvnLogfileParserTest extends TestCase {

	protected final static String sRoot = "/project/statsvn/statsvn/tests-src/net/sf/statcvs/input/samplefiles/";

	protected RepositoryFileManager repFileMan;

	public void testJUCMNav1() throws EmptyRepositoryException, ConfigurationException, IOException, LogSyntaxException {
		String args[] = { "-title", "jUCMNav", "-output-dir", sRoot + "stats", sRoot + "seg.jUCMNav.log", sRoot };
		new CommandLineParser(args).parse();
		repFileMan = new DummyRepositoryFileManager(ConfigurationOptions.getCheckedOutDirectory(), sRoot + "seg.jUCMNav.info", sRoot + "seg.jUCMNav.propget",
				sRoot + "seg.jUCMNav.linecounts");
		Main.generateDefaultHTMLSuite(repFileMan);
	}
}
