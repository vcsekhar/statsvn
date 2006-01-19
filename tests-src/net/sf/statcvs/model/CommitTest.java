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
	Created on $Date: 2002/08/23 02:04:06 $ 
*/
package net.sf.statcvs.model;

import java.util.Date;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Richard Cyganiak
 * @version $Id: CommitTest.java,v 1.6 2002/08/23 02:04:06 cyganiak Exp $
 */
public class CommitTest extends TestCase {

	private static final int DATE = 10000000;

	private CvsFile file1;
	private CvsFile file2;
	private CvsFile file3;
	private CvsFile file4;
	private CvsFile file5;
	private CvsRevision rev1;
	private CvsRevision rev2;
	private CvsRevision rev4;
	private CvsRevision rev5;
	private CvsRevision rev6;
	private CvsRevision rev7;
	private CvsRevision rev8;
	private Commit commit;
	private Author author1;
	private Author author2;
	
	/**
	 * Constructor for CommitTest.
	 * @param arg0 input
	 */
	public CommitTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		author1 = new Author("author1");
		author2 = new Author("author2");
		Directory root = Directory.createRoot();
		file1 = new CvsFile("file1", root);
		file2 = new CvsFile("file2", root);
		file3 = new CvsFile("file3", root);
		file4 = new CvsFile("file4", root);
		file5 = new CvsFile("file5", root);
		rev1 = createRevision(file1, "rev1", DATE, author1, "message1");
		rev2 = createRevision(file5, "rev2", DATE + 100, author2, "message1");
		rev4 = createRevision(file2, "rev4", DATE + 100000, author1, "message1");
		rev5 = createRevision(file3, "rev5", DATE + 200000, author1, "message1");
		rev6 = createRevision(file1, "rev6", DATE + 400000, author1, "message1");
		rev7 = createRevision(file2, "rev7", DATE + 650000, author1, "message1");
		rev8 = createRevision(file4, "rev8", DATE + 900000, author1, "message1");
	}

	/**
	 * Method testCreation.
	 */
	public void testCreation() {
		commit = new Commit(rev1);
		assertEquals(author1, commit.getAuthor());
		assertEquals("message1", commit.getComment());
		assertEquals(rev1.getDate(), commit.getDate());
		assertEquals(1, commit.getRevisions().size());
		assertTrue(commit.getRevisions().contains(rev1));
	}

	/**
	 * Method testAddAfter.
	 */
	public void testAddAfter() {
		commit = new Commit(rev6);
		commit.addRevision(rev7);
		assertEquals(author1, commit.getAuthor());
		assertEquals("message1", commit.getComment());
		assertEquals(rev6.getDate(), commit.getDate());
		assertEquals(2, commit.getRevisions().size());
		assertTrue("should contain rev6", commit.getRevisions().contains(rev6));
		assertTrue("should contain rev7", commit.getRevisions().contains(rev7));
	}

	/**
	 * Method testAddBefore.
	 */
	public void testAddBefore() {
		commit = new Commit(rev6);
		commit.addRevision(rev5);
		assertEquals(author1, commit.getAuthor());
		assertEquals("message1", commit.getComment());
		assertEquals(rev6.getDate(), commit.getDate());
		assertEquals(2, commit.getRevisions().size());
		assertTrue("should contain rev6", commit.getRevisions().contains(rev6));
		assertTrue("should contain rev5", commit.getRevisions().contains(rev5));
	}

	/**
	 * Method testAffectedFiles.
	 */
	public void testAffectedFiles() {
		commit = new Commit(rev1);
		commit.addRevision(rev4);
		commit.addRevision(rev5);
		commit.addRevision(rev6);
		commit.addRevision(rev7);
		commit.addRevision(rev8);
		Set affectedFiles = commit.getAffectedFiles();
		assertEquals(4, affectedFiles.size());
		assertTrue("should contain file1", affectedFiles.contains("file1"));
		assertTrue("should contain file2", affectedFiles.contains("file2"));
		assertTrue("should contain file3", affectedFiles.contains("file3"));
		assertTrue("should contain file4", affectedFiles.contains("file4"));
	}

	public void testCompareTo() {
		Commit commit1 = new Commit(rev1);
		Commit commit2 = new Commit(rev1);
		Commit commit3 = new Commit(rev2);
		assertEquals(0, commit1.compareTo(commit2));
		assertEquals(0, commit2.compareTo(commit1));
		assertTrue(commit1.compareTo(commit3) < 0);
		assertTrue(commit3.compareTo(commit1) > 0);
	}

	private CvsRevision createRevision(CvsFile file, String revision, long time, Author author, String message) {
		return new CvsRevision(file, revision, CvsRevision.TYPE_CHANGE, author, new Date(time), message, 0, 0, 0, null);
	}
}
