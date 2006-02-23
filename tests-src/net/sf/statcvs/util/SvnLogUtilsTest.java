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
	Created on $Date: 2003/06/04 21:50:36 $ 
*/
package net.sf.statcvs.util;

import junit.framework.TestCase;

/**
 * Test cases for {link net.sf.statcvs.util.SvnLogUtils}
 * 
 * @author Richard Cyganiak
 * @version $Id: SvnLogUtilsTest.java,v 1.3 2003/06/04 21:50:36 cyganiak Exp $
 */
public class SvnLogUtilsTest extends TestCase {

	/**
	 * Constructor
	 * @param arg0 input 
	 */
	public SvnLogUtilsTest(String arg0) {
		super(arg0);
	}

	/**
	 * Tests {@link CvsLogUtil#isInAttic}
	 */
	public void testIsInAttic() {
		assertTrue(!SvnLogUtils.isInAttic(
				"/cvsroot/module/file,v", "file"));
		assertTrue(SvnLogUtils.isInAttic(
				"/cvsroot/module/Attic/file,v", "file"));
		assertTrue(!SvnLogUtils.isInAttic(
				"/cvsroot/module/path/file,v", "path/file"));
		assertTrue(SvnLogUtils.isInAttic(
				"/cvsroot/module/path/Attic/file,v", "path/file"));
		assertTrue(!SvnLogUtils.isInAttic(
				"/cvsroot/module/attic/file,v", "attic/file"));
	}


}
