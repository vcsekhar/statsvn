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
*/
package net.sf.statcvs.model;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Tests for {@link net.sf.statcvs.model.CvsRevision}
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @version $Id: RevisionTest.java,v 1.4 2004/12/14 13:38:13 squig Exp $
 */
public class RevisionTest extends TestCase {
	private Author author;
	private Date date1;
	private Date date2;
	private Date date3;
	private Date date4;
	
	public RevisionTest(String arg) {
		super(arg);
	}

	public void setUp() {
		author = new Author("author");
		date1 = new Date(110000000);		
		date2 = new Date(120000000);		
		date3 = new Date(130000000);		
		date4 = new Date(140000000);		
	}

	public void testGetFileCountChange1() {
		CvsFile file = new CvsFile("file", Directory.createRoot());
		CvsRevision rev4 = new CvsRevision(file, "1.4", CvsRevision.TYPE_CREATION, author, date4, null, 0, 0, 0, null);
		CvsRevision rev3 = new CvsRevision(file, "1.3", CvsRevision.TYPE_CHANGE, author, date3, null, 0, 0, 0, null);
		CvsRevision rev2 = new CvsRevision(file, "1.2", CvsRevision.TYPE_DELETION, author, date2, null, 0, 0, 0, null);
		CvsRevision rev1 = new CvsRevision(file, "1.1", CvsRevision.TYPE_CREATION, author, date1, null, 0, 0, 0, null);
		assertEquals(1, rev4.getFileCountDelta());
		assertEquals(0, rev3.getFileCountDelta());
		assertEquals(-1, rev2.getFileCountDelta());
		assertEquals(1, rev1.getFileCountDelta());
	}

	public void testGetFileCountChange2() {
		CvsFile file = new CvsFile("file", Directory.createRoot());
		CvsRevision rev = new CvsRevision(file, null, CvsRevision.TYPE_BEGIN_OF_LOG, author, date1, null, 0, 0, 0, null);
		assertEquals(0, rev.getFileCountDelta());
	}
}
