package net.sf.statsvn.util;

import junit.framework.TestCase;

public class SvnInfoUtilsTest extends TestCase {

	private static final String COMP5900_ABS_1 = "/index.html";
	private static final String COMP5900_ABS_2 = "/src/seg3203/AllTests.java";
	private static final String COMP5900_ABS_3 = "/src/seg3203";
	private static final String COMP5900_ABS_4 = "/src/seg3203/";
	private static final String COMP5900_ABS_5 = "/";
	private static final String COMP5900_ABS_6 = "/";
	private static final String COMP5900_REL_1 = "index.html";
	private static final String COMP5900_REL_2 = "src/seg3203/AllTests.java";
	private static final String COMP5900_REL_3 = "src/seg3203";
	private static final String COMP5900_REL_4 = "src/seg3203/";
	private static final String COMP5900_REL_5 = ".";
	private static final String COMP5900_REL_6 = ".";
	private static final String COMP5900_REPO = "svn://rda01/comp5900";
	private static final String COMP5900_ROOT = "svn://rda01/comp5900";
	private static final String COMP5900_URL_1 = "svn://rda01/comp5900/index.html";
	private static final String COMP5900_URL_2 = "svn://rda01/comp5900/src/seg3203/AllTests.java";
	private static final String COMP5900_URL_3 = "svn://rda01/comp5900/src/seg3203";
	private static final String COMP5900_URL_4 = "svn://rda01/comp5900/src/seg3203/";
	private static final String COMP5900_URL_5 = "svn://rda01/comp5900";
	private static final String COMP5900_URL_6 = "svn://rda01/comp5900/";

	private static final String JUCMNAV_ABS_1 = "/trunk/seg.jUCMNav/src/seg/jUCMNav/Messages.java";
	private static final String JUCMNAV_ABS_2 = "/trunk/seg.jUCMNav/build.xml";
	private static final String JUCMNAV_ABS_3 = "/branches/seg.jUCMNav/1.0/build.xml";
	private static final String JUCMNAV_ABS_4 = "/trunk/seg.jUCMNav/src/seg/jUCMNav";
	private static final String JUCMNAV_ABS_5 = "/trunk/seg.jUCMNav/src/seg/jUCMNav/";
	private static final String JUCMNAV_ABS_6 = "/trunk/seg.jUCMNav";
	private static final String JUCMNAV_ABS_7 = "/trunk/seg.jUCMNav/";
	private static final String JUCMNAV_REL_1 = "src/seg/jUCMNav/Messages.java";
	private static final String JUCMNAV_REL_2 = "build.xml";
	private static final String JUCMNAV_REL_3 = "src/seg/jUCMNav";
	private static final String JUCMNAV_REL_4 = "src/seg/jUCMNav/";
	private static final String JUCMNAV_REL_5 = ".";
	private static final String JUCMNAV_REL_6 = ".";
	private static final String JUCMNAV_REPO = "svn://jucmnav.softwareengineering.ca/projetseg";
	private static final String JUCMNAV_ROOT = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav";
	private static final String JUCMNAV_URL_1 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav/src/seg/jUCMNav/Messages.java";
	private static final String JUCMNAV_URL_2 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav/build.xml";
	private static final String JUCMNAV_URL_3 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav/src/seg/jUCMNav";
	private static final String JUCMNAV_URL_4 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav/src/seg/jUCMNav/";
	private static final String JUCMNAV_URL_5 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav";
	private static final String JUCMNAV_URL_6 = "svn://jucmnav.softwareengineering.ca/projetseg/trunk/seg.jUCMNav/";
	private static final String JUCMNAV_URL_7 = "svn://jucmnav.softwareengineering.ca/projetseg/branches/seg.jUCMNav/1.0/build.xml";

	protected void setUp() throws Exception {
	}

	private void setupComp5900() {
		SvnInfoUtils.setRepositoryUrl(COMP5900_REPO);
		SvnInfoUtils.setRootUrl(COMP5900_ROOT);
	}

	private void setupjUCMNav() {
		SvnInfoUtils.setRepositoryUrl(JUCMNAV_REPO);
		SvnInfoUtils.setRootUrl(JUCMNAV_ROOT);
	}

	private void setupjUCMNav_BAD() {
		SvnInfoUtils.setRepositoryUrl(JUCMNAV_REPO);
		SvnInfoUtils.setRootUrl(JUCMNAV_ROOT + "/");
	}

	public void testAbsoluteToRelativePath_Directories1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_REL_3, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_4));
		assertEquals(JUCMNAV_REL_3, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_5));
		assertEquals(JUCMNAV_REL_5, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_6));
		assertEquals(JUCMNAV_REL_5, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_7));
	}

	public void testAbsoluteToRelativePath_Directories2() {
		setupComp5900();
		assertEquals(COMP5900_REL_3, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_3));
		assertEquals(COMP5900_REL_3, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_4));
		assertEquals(COMP5900_REL_5, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_5));
		assertEquals(COMP5900_REL_5, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_6));
	}

	public void testAbsoluteToRelativePath1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_REL_1, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_1));
		assertEquals(JUCMNAV_REL_2, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_2));

		// because absolute path doesn't match module name
		assertNull(SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_3));
	}

	public void testAbsoluteToRelativePath2() {
		setupjUCMNav_BAD();
		assertEquals(JUCMNAV_REL_1, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_1));
		assertEquals(JUCMNAV_REL_2, SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_2));

		// because absolute path doesn't match module name
		assertNull(SvnInfoUtils.absoluteToRelativePath(JUCMNAV_ABS_3));
	}

	public void testAbsoluteToRelativePath3() {
		setupComp5900();
		assertEquals(COMP5900_REL_1, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_1));
		assertEquals(COMP5900_REL_2, SvnInfoUtils.absoluteToRelativePath(COMP5900_ABS_2));
	}

	public void testAbsolutePathToUrl1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_URL_1, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_1));
		assertEquals(JUCMNAV_URL_2, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_2));
		assertEquals(JUCMNAV_URL_7, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_3));
		assertEquals(JUCMNAV_URL_3, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_4));
		assertEquals(JUCMNAV_URL_3, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_5));
		assertEquals(JUCMNAV_URL_5, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_6));
		assertEquals(JUCMNAV_URL_5, SvnInfoUtils.absolutePathToUrl(JUCMNAV_ABS_7));
	}

	public void testAbsolutePathToUrl2() {
		setupComp5900();
		assertEquals(COMP5900_URL_1, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_1));
		assertEquals(COMP5900_URL_2, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_2));
		assertEquals(COMP5900_URL_3, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_3));
		assertEquals(COMP5900_URL_3, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_4));
		assertEquals(COMP5900_URL_5, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_5));
		assertEquals(COMP5900_URL_5, SvnInfoUtils.absolutePathToUrl(COMP5900_ABS_6));
	}
	
	public void testRelativePathToUrl1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_URL_1, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_1));
		assertEquals(JUCMNAV_URL_2, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_2));
		assertEquals(JUCMNAV_URL_3, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_3));
		assertEquals(JUCMNAV_URL_3, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_4));
		assertEquals(JUCMNAV_URL_5, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_5));
		assertEquals(JUCMNAV_URL_5, SvnInfoUtils.relativePathToUrl(JUCMNAV_REL_6));
	}

	public void testRelativePathToUrl2() {
		setupComp5900();
		assertEquals(COMP5900_URL_1, SvnInfoUtils.relativePathToUrl(COMP5900_REL_1));
		assertEquals(COMP5900_URL_2, SvnInfoUtils.relativePathToUrl(COMP5900_REL_2));
		assertEquals(COMP5900_URL_3, SvnInfoUtils.relativePathToUrl(COMP5900_REL_3));
		assertEquals(COMP5900_URL_3, SvnInfoUtils.relativePathToUrl(COMP5900_REL_4));
		assertEquals(COMP5900_URL_5, SvnInfoUtils.relativePathToUrl(COMP5900_REL_5));
		assertEquals(COMP5900_URL_5, SvnInfoUtils.relativePathToUrl(COMP5900_REL_6));
	}
		
	
	public void testRelativeToAbsolutePath1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_ABS_1, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_1));
		assertEquals(JUCMNAV_ABS_2, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_2));
		assertEquals(JUCMNAV_ABS_4, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_3));
		assertEquals(JUCMNAV_ABS_4, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_4));
		assertEquals(JUCMNAV_ABS_6, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_5));
		assertEquals(JUCMNAV_ABS_6, SvnInfoUtils.relativeToAbsolutePath(JUCMNAV_REL_6));
	}

	public void testRelativeToAbsolutePath2() {
		setupComp5900();
		assertEquals(COMP5900_ABS_1, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_1));
		assertEquals(COMP5900_ABS_2, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_2));
		assertEquals(COMP5900_ABS_3, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_3));
		assertEquals(COMP5900_ABS_3, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_4));
		assertEquals(COMP5900_ABS_5, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_5));
		assertEquals(COMP5900_ABS_5, SvnInfoUtils.relativeToAbsolutePath(COMP5900_REL_6));
	}
			
	public void testGetRepositoryURL1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_REPO, SvnInfoUtils.getRepositoryUrl());
	}

	public void testGetRepositoryURL2() {
		setupjUCMNav_BAD();
		assertEquals(JUCMNAV_REPO, SvnInfoUtils.getRepositoryUrl());
	}

	public void testGetRepositoryURL3() {
		setupComp5900();
		assertEquals(COMP5900_REPO, SvnInfoUtils.getRepositoryUrl());
	}

	public void testUrlToAbsolutePath_Directories1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_ABS_4, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_3));
		assertEquals(JUCMNAV_ABS_4, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_4));
		assertEquals(JUCMNAV_ABS_6, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_5));
		assertEquals(JUCMNAV_ABS_6, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_6));
	}

	public void testUrlToAbsolutePath_Directories2() {
		setupComp5900();
		assertEquals(COMP5900_ABS_3, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_3));
		assertEquals(COMP5900_ABS_3, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_4));
		assertEquals(COMP5900_ABS_5, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_5));
		assertEquals(COMP5900_ABS_5, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_6));
	}

	public void testUrlToAbsolutePath1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_ABS_1, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_1));
		assertEquals(JUCMNAV_ABS_2, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_2));
	}

	public void testUrlToAbsolutePath2() {
		setupjUCMNav_BAD();
		assertEquals(JUCMNAV_ABS_1, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_1));
		assertEquals(JUCMNAV_ABS_2, SvnInfoUtils.urlToAbsolutePath(JUCMNAV_URL_2));
	}

	public void testUrlToAbsolutePath3() {
		setupComp5900();
		assertEquals(COMP5900_ABS_1, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_1));
		assertEquals(COMP5900_ABS_2, SvnInfoUtils.urlToAbsolutePath(COMP5900_URL_2));
	}

	public void testUrlToRelativePath_Directories1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_REL_3, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_3));
		assertEquals(JUCMNAV_REL_3, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_4));
		assertEquals(JUCMNAV_REL_5, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_5));
		assertEquals(JUCMNAV_REL_5, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_6));
	}

	public void testUrlToRelativePath_Directories2() {
		setupComp5900();
		assertEquals(COMP5900_REL_3, SvnInfoUtils.urlToRelativePath(COMP5900_URL_3));
		assertEquals(COMP5900_REL_3, SvnInfoUtils.urlToRelativePath(COMP5900_URL_4));
		assertEquals(COMP5900_REL_5, SvnInfoUtils.urlToRelativePath(COMP5900_URL_5));
		assertEquals(COMP5900_REL_5, SvnInfoUtils.urlToRelativePath(COMP5900_URL_6));
	}

	public void testUrlToRelativePath1() {
		setupjUCMNav();
		assertEquals(JUCMNAV_REL_1, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_1));
		assertEquals(JUCMNAV_REL_2, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_2));
	}

	public void testUrlToRelativePath2() {
		setupjUCMNav_BAD();
		assertEquals(JUCMNAV_REL_1, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_1));
		assertEquals(JUCMNAV_REL_2, SvnInfoUtils.urlToRelativePath(JUCMNAV_URL_2));
	}

	public void testUrlToRelativePath3() {
		setupComp5900();
		assertEquals(COMP5900_REL_1, SvnInfoUtils.urlToRelativePath(COMP5900_URL_1));
		assertEquals(COMP5900_REL_2, SvnInfoUtils.urlToRelativePath(COMP5900_URL_2));
	}
}
