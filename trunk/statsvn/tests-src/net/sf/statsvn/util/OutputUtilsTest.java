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
    
	$Name:  $ 
	Created on $Date: 2003/03/25 01:20:13 $ 
*/
package net.sf.statsvn.util;

import junit.framework.TestCase;

/**
 * Test cases for {link net.sf.statsvn.util.OutputUtils}
 * 
 * @author Richard Cyganiak
 * @version $Id: OutputUtilsTest.java,v 1.2 2003/03/25 01:20:13 lukasz Exp $
 */
public class OutputUtilsTest extends TestCase {

	/**
	 * Constructor for OutputUtilsTest.
	 * @param arg0 input 
	 */
	public OutputUtilsTest(final String arg0) {
		super(arg0);
	}

	/**
	 * Method testNormalString.
	 */
	public void testNormalString() {
		assertEquals("abc", OutputUtils.escapeHtml("abc"));
	}
	
	/**
	 * Method testAmp.
	 */
	public void testAmp() {
		assertEquals("x &amp;&amp; y", OutputUtils.escapeHtml("x && y"));
	}
	
	/**
	 * Method testLessThan.
	 */
	public void testLessThan() {
		assertEquals("x &lt; y", OutputUtils.escapeHtml("x < y"));
	}
	
	/**
	 * Method testGreaterThan.
	 */
	public void testGreaterThan() {
		assertEquals("x &gt; y", OutputUtils.escapeHtml("x > y"));
	}
	
	/**
	 * Method testLineBreak.
	 */
	public void testLineBreak() {
		assertEquals("line1<br/>\nline2<br/>\n",
				OutputUtils.escapeHtml("line1\nline2\n"));
	}
	
	/**
	 * Method testCombination.
	 */
	public void testCombination() {
		assertEquals("(x &lt; y) &amp;&amp;<br/>\n(y &gt; x)",
				OutputUtils.escapeHtml("(x < y) &&\n(y > x)"));
	}
}
