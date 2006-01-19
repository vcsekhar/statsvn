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
	Created on $Date: 2004/12/14 13:38:13 $ 
*/
package net.sf.statcvs.input;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Commit;
import net.sf.statcvs.model.CvsFile;
import net.sf.statcvs.model.CvsRevision;
import net.sf.statcvs.model.Directory;

/**
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @version $Id: CommitListBuilderTest.java,v 1.8 2004/12/14 13:38:13 squig Exp $
 */
public class CommitListBuilderTest extends TestCase {

	private static final int DATE = 10000000;

	private CvsRevision rev1;
	private CvsRevision rev2;
	private CvsRevision rev3;
	private CvsRevision rev4;
	private CvsRevision rev4b;
	private CvsRevision rev5;
	private CvsRevision rev6;
	private CvsRevision rev6b;
	private CvsRevision rev7;
	private CvsRevision rev8;
	private List commits;
	private Author author1;
	private Author author2;
	private Author author3;
	
	/**
	 * Constructor for CommitListBuilderTest.
	 * @param arg0 input argument
	 */
	public CommitListBuilderTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		author1 = new Author("author1");
		author2 = new Author("author2");
		author3 = new Author("author3");
		Directory root = Directory.createRoot();
		CvsFile file1 = new CvsFile("file1", root);
		CvsFile file2 = new CvsFile("file2", root);
		CvsFile file3 = new CvsFile("file3", root);
		CvsFile file4 = new CvsFile("file4", root);
		CvsFile file4b = new CvsFile("file4b", root);
		CvsFile file5 = new CvsFile("file5", root);
		CvsFile file6 = new CvsFile("file6", root);
		rev1 = createRevision(file1, "rev1", DATE, author1, "message1");
		rev2 = createRevision(file2, "rev2", DATE + 100, author2, "message1");
		rev3 = createRevision(file3, "rev3", DATE + 200, author1, "message2");
		rev4 = createRevision(file4, "rev4", DATE + 100000, author3, "message1");
		rev4b = createRevision(file4b, "rev4b", DATE + 100000, author1, "message1");
		rev5 = createRevision(file5, "rev5", DATE + 200000, author1, "message1");
		rev6 = createRevision(file6, "rev6", DATE + 250000, author2, "message1");
		rev6b = createRevision(file6, "rev6b", DATE + 400000, author2, "message1");
		rev7 = createRevision(file2, "rev7", DATE + 650000, author1, "message1");
		rev8 = createRevision(file4, "rev8", DATE + 900000, author1, "message1");
	}

	/**
	 * Method testCreation.
	 */
	public void testCreation() {
		runBuilder(null);
		assertEquals(0, commits.size());
	}
	
	/**
	 * Method testOneRevision.
	 */
	public void testOneRevision() {
		CvsRevision[] revs = {rev1};
		runBuilder(revs);
		assertEquals(1, commits.size());
		assertEquals(1, getCommit(0).getRevisions().size());
		assertTrue(getCommit(0).getRevisions().contains(rev1));
	}

	/**
	 * Method testOneCommitMultipleRevisions.
	 */
	public void testOneCommitMultipleRevisions() {
		CvsRevision[] revs = {rev1, rev4b, rev5};
		runBuilder(revs);
		assertEquals(1, commits.size());
		assertEquals(3, getCommit(0).getRevisions().size());
		assertTrue(getCommit(0).getRevisions().contains(rev1));
		assertTrue(getCommit(0).getRevisions().contains(rev4b));
		assertTrue(getCommit(0).getRevisions().contains(rev5));
	}

	/**
	 * Method testMultipleCommits.
	 */
	public void testMultipleCommits() {
		CvsRevision[] revs = {rev1, rev2, rev3};
		runBuilder(revs);
		assertEquals(3, commits.size());
		assertEquals(1, getCommit(0).getRevisions().size());
		assertEquals(1, getCommit(1).getRevisions().size());
		assertEquals(1, getCommit(2).getRevisions().size());
		assertTrue(getCommit(0).getRevisions().contains(rev1));
		assertTrue(getCommit(1).getRevisions().contains(rev2));
		assertTrue(getCommit(2).getRevisions().contains(rev3));
	}
		
	/**
	 * Method testSimultaneousCommits.
	 */
	public void testSimultaneousCommits() {
		CvsRevision[] revs = {rev1, rev2, rev4, rev5, rev6};
		runBuilder(revs);
		assertEquals(3, commits.size());
		assertEquals(2, getCommit(0).getRevisions().size());
		assertEquals(2, getCommit(1).getRevisions().size());
		assertEquals(1, getCommit(2).getRevisions().size());
		assertTrue(getCommit(0).getRevisions().contains(rev1));
		assertTrue(getCommit(0).getRevisions().contains(rev5));
		assertTrue(getCommit(1).getRevisions().contains(rev2));
		assertTrue(getCommit(1).getRevisions().contains(rev6));
		assertTrue(getCommit(2).getRevisions().contains(rev4));
	}

	/**
	 * Method testIsSameCommit.
	 */
	public void testIsSameCommit() {
		Commit commit = new Commit(rev1);
		assertTrue("has different author", !CommitListBuilder.isSameCommit(commit, rev2));
		assertTrue("has different comment", !CommitListBuilder.isSameCommit(commit, rev3));
		assertTrue("is same commit", CommitListBuilder.isSameCommit(commit, rev4b));
	}

	/**
	 * Method testIsInTimeFrame1.
	 */
	public void testIsInTimeFrame1() {
		Commit commit = new Commit(rev6b);
		assertTrue("rev5 should be in time frame", CommitListBuilder.isInTimeFrame(commit, rev5.getDate()));
		assertTrue("rev7 should be in time frame", CommitListBuilder.isInTimeFrame(commit, rev7.getDate()));
		assertTrue("rev1 should not be in time frame", !CommitListBuilder.isInTimeFrame(commit, rev1.getDate()));
		assertTrue("rev8 should not be in time frame", !CommitListBuilder.isInTimeFrame(commit, rev8.getDate()));
	}

	private Commit getCommit(int index) {
		return (Commit) commits.get(index);
	}

	private CvsRevision createRevision(CvsFile file, String revision, long time, Author author, String message) {
		return file.addChangeRevision(revision, author, new Date(time), message, 0, 0, 0, null);
	}

	private void runBuilder(CvsRevision[] revisions) {
		TreeSet revList = new TreeSet();
		if (revisions != null) {
			for (int i = 0; i < revisions.length; i++) {
				revList.add(revisions[i]);
			}
		}
		commits = new CommitListBuilder(revList).createCommitList();
	}
}
