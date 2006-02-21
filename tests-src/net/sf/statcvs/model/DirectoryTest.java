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
package net.sf.statcvs.model;

import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * @author Richard Cyganiak
 * @version $Id: DirectoryTest.java,v 1.17 2004/12/14 13:38:13 squig Exp $
 */
public class DirectoryTest extends TestCase {

	private Directory root;
	private Directory rootSrc;
	private Directory rootSrcNet;
	private Directory rootSrcNetSf;
	private Directory rootSrcNetSfStatcvs;
	private Date date1;
	private Date date2;
	private Date date3;
	private Date date4;
	private Author author;

	/**
	 * Constructor
	 * @param arg0 input
	 */
	public DirectoryTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		root = Directory.createRoot();
		rootSrc = root.createSubdirectory("src");
		rootSrcNet = rootSrc.createSubdirectory("net");
		rootSrcNetSf = rootSrcNet.createSubdirectory("sf");
		rootSrcNetSfStatcvs = rootSrcNetSf.createSubdirectory("statcvs");
		date1 = new Date(100000000);
		date2 = new Date(200000000);
		date3 = new Date(300000000);
		date4 = new Date(400000000);
		author = new Author("chevette");
	}

	/**
	 * test the {@link DirectoryRoot} object
	 */
	public void testRoot() {
		assertNotNull(root);
		assertTrue(root.isRoot());
		assertNull(root.getParent());
		assertEquals("", root.getName());
		assertEquals("", root.getPath());
	}
	
	/**
	 * test the {@link DirectoryImpl} object
	 */
	public void testNonRootDirectory() {
		assertNotNull(rootSrcNetSfStatcvs);
		assertTrue(!rootSrcNetSfStatcvs.isRoot());
		assertNotNull(rootSrcNetSfStatcvs.getParent());
		assertEquals("statcvs", rootSrcNetSfStatcvs.getName());
	}
	
	/**
	 * test the correct linking of parents, and {@link Directory.getPath()}
	 */
	public void testPath() {
		assertEquals(rootSrcNetSf, rootSrcNetSfStatcvs.getParent());
		assertEquals(rootSrcNet, rootSrcNetSf.getParent());
		assertEquals(rootSrc, rootSrcNet.getParent());
		assertEquals(root, rootSrc.getParent());
		assertEquals("src/net/sf/statcvs/", rootSrcNetSfStatcvs.getPath());
	}
	
	/**
	 * tests {@link Directory.getRevisions()}
	 */
	public void testRevisions() {
		VersionedFile file1 = new VersionedFile("src/net/sf/statcvs/Main.java", rootSrcNetSfStatcvs);
		Revision rev11 = file1.addInitialRevision("1.1", author, date1, null, 0, null);
		Revision rev12 = file1.addChangeRevision("1.2", author, date3, null, 0, 0, 0, null);
		VersionedFile file2 = new VersionedFile("src/net/sf/statcvs/README", rootSrcNetSfStatcvs);
		Revision rev21 = file2.addInitialRevision("2.1", author, date2, null, 0, null);
		VersionedFile file3 = new VersionedFile("fileInRoot", root);
		file3.addInitialRevision("3.1", author, date4, null, 0, null);
		Iterator revIt = rootSrcNetSfStatcvs.getRevisions().iterator();
		assertTrue(revIt.hasNext());
		assertEquals(rev11, revIt.next());
		assertTrue(revIt.hasNext());
		assertEquals(rev21, revIt.next());
		assertTrue(revIt.hasNext());
		assertEquals(rev12, revIt.next());
		assertTrue(!revIt.hasNext());
	}

	/**
	 * tests {@link Directory.compareTo(Object)
	 */
	public void testCompareSame() {
		assertEquals(0, rootSrcNetSf.compareTo(rootSrcNetSf));
	}

	/**
	 * tests {@link Directory.compareTo(Object)
	 */
	public void testCompareDifferent() {
		Directory dir1 = root.createSubdirectory("abc");
		Directory dir2 = root.createSubdirectory("abc");
		Directory dir3 = root.createSubdirectory("xyz");
		assertEquals(0, dir1.compareTo(dir2));
		assertTrue(dir1.compareTo(dir3) < 0);
		assertTrue(dir3.compareTo(dir2) > 0);
	}

	/**
	 * tests {@link Directory.getDepth()}
	 */
	public void testGetDepth() {
		assertEquals(0, root.getDepth());
		assertEquals(1, rootSrc.getDepth());
		assertEquals(2, rootSrcNet.getDepth());
		assertEquals(3, rootSrcNetSf.getDepth());
		assertEquals(4, rootSrcNetSfStatcvs.getDepth());
	}

	/**
	 * Tests automatic creation of backlinks to subdirectories
	 */
	public void testSubdirectories() {
		assertEquals(1, root.getSubdirectories().size());
		assertTrue(root.getSubdirectories().contains(rootSrc));
		assertEquals(1, rootSrc.getSubdirectories().size());
		assertTrue(rootSrc.getSubdirectories().contains(rootSrcNet));
		assertEquals(1, rootSrcNet.getSubdirectories().size());
		assertTrue(rootSrcNet.getSubdirectories().contains(rootSrcNetSf));
		assertEquals(1, rootSrcNetSf.getSubdirectories().size());
		assertTrue(rootSrcNetSf.getSubdirectories().contains(rootSrcNetSfStatcvs));
		assertTrue(rootSrcNetSfStatcvs.getSubdirectories().isEmpty());
	}

	public void testEmptyRepository() {
		assertTrue(root.isEmpty());
	}
	
	public void testNonEmptyRepository() {
		VersionedFile file1 = new VersionedFile("src/README", rootSrc);
		file1.addInitialRevision("1.1", author, date1, null, 100, null);
		assertTrue(!root.isEmpty());
		assertTrue(!rootSrc.isEmpty());
		assertTrue(rootSrcNet.isEmpty());
		assertEquals(0, root.getCurrentFileCount());
		assertEquals(1, rootSrc.getCurrentFileCount());
		assertEquals(0, rootSrcNet.getCurrentFileCount());
		assertEquals(0, root.getCurrentLOC());
		assertEquals(100, rootSrc.getCurrentLOC());
		assertEquals(0, rootSrcNet.getCurrentLOC());
	}

	public void testEmptyRepositoryWithDeletedFile() {
		VersionedFile file1 = new VersionedFile("src/README", rootSrc);
		file1.addInitialRevision("1.1", author, date1, null, 100, null);
		file1.addDeletionRevision("1.2", author, date2, null, 100, null);
		assertTrue(root.isEmpty());
		assertTrue(rootSrc.isEmpty());
		assertTrue(rootSrcNet.isEmpty());
		assertEquals(0, rootSrc.getCurrentFileCount());
		assertEquals(0, rootSrc.getCurrentLOC());
	}

	public void testNonEmptyRepositoryWithDeletedFile() {
		VersionedFile file1 = new VersionedFile("src/README", rootSrc);
		file1.addInitialRevision("1.1", author, date1, null, 100, null);
		VersionedFile file2 = new VersionedFile("src/README2", rootSrc);
		file2.addInitialRevision("1.1", author, date1, null, 100, null);
		VersionedFile file3 = new VersionedFile("fileInRoot", root);
		file3.addInitialRevision("1.1", author, date1, null, 100, null);
		VersionedFile file4 = new VersionedFile("src/deleted", rootSrc);
		file4.addInitialRevision("1.1", author, date1, null, 100, null);
		file4.addDeletionRevision("1.2", author, date2, null, 100, null);
		assertTrue(!root.isEmpty());
		assertTrue(!rootSrc.isEmpty());
		assertTrue(rootSrcNet.isEmpty());
		assertEquals(1, root.getCurrentFileCount());
		assertEquals(2, rootSrc.getCurrentFileCount());
		assertEquals(0, rootSrcNet.getCurrentFileCount());
		assertEquals(100, root.getCurrentLOC());
		assertEquals(200, rootSrc.getCurrentLOC());
		assertEquals(0, rootSrcNet.getCurrentLOC());
	}

	public void testGetSubdirectoriesRecursive() {
		Directory rootDir = Directory.createRoot();
		Directory dir2 = rootDir.createSubdirectory("dir2");
		Directory dir2sub = dir2.createSubdirectory("sub");
		Directory dir1 = rootDir.createSubdirectory("dir1");
		Directory dir1sub2 = dir1.createSubdirectory("sub2");
		Directory dir1sub1 = dir1.createSubdirectory("sub1");
		Iterator dirIt = rootDir.getSubdirectoriesRecursive().iterator();
		assertEquals(rootDir, dirIt.next());
		assertEquals(dir1, dirIt.next());
		assertEquals(dir1sub1, dirIt.next());
		assertEquals(dir1sub2, dirIt.next());
		assertEquals(dir2, dirIt.next());
		assertEquals(dir2sub, dirIt.next());
		assertTrue(!dirIt.hasNext());
	}
}
