/**
 * 
 */
package net.sf.statsvn.util;

import junit.framework.TestCase;

/**
 * @author Benoit Xhenseval
 *
 */
public class SvnInfoUtilTest extends TestCase {
	public void testReplace() {
		assertEquals("String with no space", "thisisatest", SvnInfoUtils.replace(" ", "%20", "thisisatest"));
		assertEquals("null String", null, SvnInfoUtils.replace(" ", "%20", null));
		assertEquals("empty String", "", SvnInfoUtils.replace(" ", "%20", ""));
		assertEquals("null pattern", "thisisatest", SvnInfoUtils.replace(null, "%20", "thisisatest"));
		assertEquals("empty pattern", "thisisatest", SvnInfoUtils.replace("", "%20", "thisisatest"));
		assertEquals("1 pattern", "this%20isatest", SvnInfoUtils.replace(" ", "%20", "this isatest"));
		assertEquals("2 patterns", "this%20is%20atest", SvnInfoUtils.replace(" ", "%20", "this is atest"));
		assertEquals("3 patterns", "this%20is%20a%20test", SvnInfoUtils.replace(" ", "%20", "this is a test"));
		assertEquals("4 patterns", "%20this%20is%20a%20test", SvnInfoUtils.replace(" ", "%20", " this is a test"));
		assertEquals("5 patterns", "%20this%20is%20a%20%20test", SvnInfoUtils.replace(" ", "%20", " this is a  test"));
		assertEquals("6 patterns", "%20this%20is%20a%20%20test%20", SvnInfoUtils.replace(" ", "%20", " this is a  test "));
	}
}
