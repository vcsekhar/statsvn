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
	Created on $Date: 2002/12/07 04:50:20 $ 
*/
package net.sf.statcvs.model;

import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Test cases for {@link CvsFile}
 * 
 * @author Richard Cyganiak
 * @version $Id: CvsFileTest.java,v 1.8 2002/12/07 04:50:20 lukasz Exp $
 */
public class CvsFileTest extends TestCase {
	private Directory dirRoot;
	private Directory dirTest;
	private Date date1 = new Date(1100000000);
	private Date date2 = new Date(1200000000);
	private Date date3 = new Date(1300000000);
	private Author author;
	
	/**
	 * Constructor for CvsFileTest.
	 * @param arg0 input
	 */
	public CvsFileTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		dirRoot = Directory.createRoot();
		dirTest = dirRoot.createSubdirectory("test");
		author = new Author("author");
	}

	/**
	 * Method testCreation.
	 */
	public void testCreation() {
		CvsFile file = new CvsFile("file", dirRoot);
		CvsRevision rev1 = file.addInitialRevision("1.1", author, date1, "message", 0, null);
		assertEquals("file", file.getFilenameWithPath());
		assertEquals(1, file.getRevisions().size());
		assertSame(rev1, file.getLatestRevision());
		assertEquals(0, file.getCurrentLinesOfCode());
		assertEquals(dirRoot, file.getDirectory());
		assertTrue("file was not deleted", !file.isDead());
	}
	
	/**
	 * Method testMultipleRevisions.
	 */
	public void testMultipleRevisions() {
		CvsFile file = new CvsFile("file", dirRoot);
		CvsRevision rev1 = file.addInitialRevision("1.1", author, date1, "message1", 0, null);
		CvsRevision rev2 = file.addChangeRevision("1.2", author, date2, "message2", 0, 0, 0, null);
		CvsRevision rev3 = file.addChangeRevision("1.3", author, date3, "message3", 0, 0, 0, null);
		Iterator revIt = file.getRevisions().iterator();
		assertEquals(rev1, revIt.next());
		assertEquals(rev2, revIt.next());
		assertEquals(rev3, revIt.next());
		assertTrue(!revIt.hasNext());
		assertEquals(rev3, file.getLatestRevision());
		assertEquals(rev1, file.getInitialRevision());
		assertEquals(0, file.getCurrentLinesOfCode());
		assertEquals(0, rev1.getLines());
		assertEquals(0, rev2.getLines());
		assertEquals(0, rev3.getLines());
	}
	
	/**
	 * Test the assertion that revisions can be added to a file in any order.
	 */
	public void testMultipleRevisionsAnyOrder() {
		CvsFile file = new CvsFile("file", dirRoot);
		CvsRevision rev2 = file.addChangeRevision("1.2", author, date2, null, 0, 0, 0, null);
		CvsRevision rev3 = file.addDeletionRevision("1.3", author, date3, null, 0, null);
		CvsRevision rev1 = file.addInitialRevision("1.1", author, date1, null, 0, null);
		Iterator revIt = file.getRevisions().iterator();
		assertEquals(rev1, revIt.next());
		assertEquals(rev2, revIt.next());
		assertEquals(rev3, revIt.next());
		assertTrue(!revIt.hasNext());
	}
	
	/**
	 * Method testModuleName.
	 */
	public void testDirectories() {
		CvsFile file1 = new CvsFile("rootfile.file", dirRoot);
		file1.addInitialRevision("1.1", author, date1, null, 0, null);
		CvsFile file2 = new CvsFile("test/file.file", dirTest);
		assertEquals(dirRoot, file1.getDirectory());
		assertEquals(dirTest, file2.getDirectory());
	}

	/**
	 * Method testGetFilename
	 * 
	 */
	public void testGetFilename() {
		CvsFile file = new CvsFile("TestFile.java", dirRoot);
		assertEquals("TestFile.java", file.getFilename());
		file = new CvsFile("", dirRoot);
		assertEquals("", file.getFilename());
		file = new CvsFile("/", dirRoot);
		assertEquals("", file.getFilename());
	}

	/**
	 * test getPreviousRevision()
	 */
	public void testGetPreviousRevision() {
		CvsFile file = new CvsFile("file", dirRoot);
		CvsRevision rev1 = file.addInitialRevision("1.1", author, date1, "message1", 0, null);
		CvsRevision rev2 = file.addChangeRevision("1.2", author, date2, "message2", 0, 0, 0, null);
		CvsRevision rev3 = file.addChangeRevision("1.3", author, date3, "message3", 0, 0, 0, null);
		assertNull(rev1.getPreviousRevision());
		assertNull(file.getPreviousRevision(rev1));
		assertEquals(rev1, rev2.getPreviousRevision());
		assertEquals(rev1, file.getPreviousRevision(rev2));
		assertEquals(rev2, rev3.getPreviousRevision());
		assertEquals(rev2, file.getPreviousRevision(rev3));
		try {
			file.getPreviousRevision(new CvsRevision(new CvsFile("foo", dirRoot), "1.1", CvsRevision.TYPE_CHANGE, null, date3, null, 0, 0, 0, null));
			fail("should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	/**
	 * Test if files are added to their directory's file list
	 */
	public void testLinkToDirectory() {
		CvsFile file = new CvsFile("test/file", dirTest);
		file.addInitialRevision("1.1", author, date1, "message1", 0, null);
		assertEquals(dirTest, file.getDirectory());		
		assertTrue(dirTest.getFiles().contains(file));		
	}

	/**
	 * The null author used for "begin of log" revisions must not be included
	 * in the authors list
	 */
	public void testIgnoreNullAuthor() {
		CvsFile file = new CvsFile("file", dirRoot);
		new CvsRevision(file, "1.5", CvsRevision.TYPE_CHANGE, new Author("author"),
				new Date(200000000), null, 0, 0, 0, null);
		new CvsRevision(file, "0.0", CvsRevision.TYPE_BEGIN_OF_LOG, null,
				new Date(100000000), null, 0, 0, 0, null);

		assertTrue(!file.hasAuthor(null));
	}
	
	public void testCompareTo() {
		assertTrue(new CvsFile("file1", dirRoot).compareTo(new CvsFile("file2", dirRoot)) < 0);
	}
}
