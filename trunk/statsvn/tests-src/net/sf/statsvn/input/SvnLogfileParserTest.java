package net.sf.statsvn.input;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.statsvn.Main;
import net.sf.statsvn.output.SvnCommandLineParser;
import net.sf.statcvs.input.EmptyRepositoryException;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;

/**
 * High-level scenarios to verify parsing without actually needing a server.
 *
 * @author jkealey
 *
 */
public class SvnLogfileParserTest extends TestCase {

	protected final static String sRoot = "./tests-src/net/sf/statsvn/input/samplefiles/";

	protected RepositoryFileManager repFileMan;

	public void testJUCMNav1() throws EmptyRepositoryException, ConfigurationException, IOException, LogSyntaxException {
		final String args[] = { "-title", "jUCMNav", "-output-dir", sRoot + "stats", sRoot + "seg.jUCMNav.log", sRoot };
		new SvnCommandLineParser(args).parse();
		repFileMan = new DummyRepositoryFileManager(ConfigurationOptions.getCheckedOutDirectory(), sRoot + "seg.jUCMNav.info", sRoot + "seg.jUCMNav.propget",
				sRoot + "seg.jUCMNav.linecounts");
		Main.generateDefaultHTMLSuite(repFileMan);
	}
}
