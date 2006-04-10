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
    
	$RCSfile: WebRepositoryIntegrationTest.java,v $
	$Date: 2005/01/03 03:03:54 $
*/
package net.sf.statcvs.output;

import java.util.Date;

import junit.framework.TestCase;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.model.VersionedFile;

/**
 * Test cases for {WebRepositoryIntegrationTest}
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: WebRepositoryIntegrationTest.java,v 1.19 2005/01/03 03:03:54 cyganiak Exp $
 */
public class WebRepositoryIntegrationTest extends TestCase {

	private static final String BASE = "http://example.com/";

	private WebRepositoryIntegration viewvc;
	private WebRepositoryIntegration chora;
	private Date date = new Date(100000000);
	private Directory root;
	private Directory path;

	/**
	 * Checkstyle drives me nuts
	 * @param arg0 stuff
	 */
	public WebRepositoryIntegrationTest(String arg0) {
		super(arg0);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		viewvc = new ViewVcIntegration(BASE);
		chora = new ChoraIntegration(BASE);
		root = Directory.createRoot();
		path = root.createSubdirectory("path");
	}

	/**
	 * test
	 */
	public void testViewvcCreation() {
		assertEquals("ViewVC", viewvc.getName());
	}
	
	/**
	 * Tests if stuff still works when the trailing slash is omitted from
	 * the base URL
	 */
	public void testViewvcForgivingBaseURL() {
		ViewVcIntegration viewvc2 = new ViewVcIntegration("http://example.com");
		VersionedFile file = new VersionedFile("file", root);
		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		assertEquals("http://example.com/file", viewvc2.getFileHistoryUrl(file));
	}

	/**
	 * test URLs for a normal file
	 */
	public void testViewvcNormalFile() {
		VersionedFile file = new VersionedFile("path/file", path);
		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		assertEquals(BASE + "path/file", viewvc.getFileHistoryUrl(file));
		assertEquals(BASE + "path/file?rev=HEAD&content-type=text/vnd.viewcvs-markup",
				viewvc.getFileViewUrl(file));
	}

	/**
	 * Test for a ViewVC URL that includes a cvsroot parameter
	 */
	public void testViewvcWithCvsroot() {
		this.viewvc = new ViewVcIntegration("http://example.com/cgi-bin/viewvc.cgi/module?cvsroot=CvsRoot");
		VersionedFile file = new VersionedFile("path/file", this.path);
		file.addChangeRevision("1.1", null, this.date, null, 0, 0, 0, null);
		assertEquals(
				"http://example.com/cgi-bin/viewvc.cgi/module/path/file?cvsroot=CvsRoot",
				this.viewvc.getFileHistoryUrl(file));
		assertEquals(
				"http://example.com/cgi-bin/viewvc.cgi/module/path/file?rev=HEAD&content-type=text/vnd.viewcvs-markup&cvsroot=CvsRoot",
				this.viewvc.getFileViewUrl(file));
	}

//	/**
//	 * test URLs for an attic file
//	 */
//	public void testViewvcAtticFile() {
//		VersionedFile file = new VersionedFile("path/file", path);
//		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
//		HashSet atticFileNames = new HashSet();
//		atticFileNames.add("path/file");
//		viewvc.setAtticFileNames(atticFileNames);
//		assertEquals(BASE + "path/Attic/file", viewvc.getFileHistoryUrl(file));
//		assertEquals(BASE + "path/Attic/file?rev=HEAD&content-type=text/vnd.viewcvs-markup",
//				viewvc.getFileViewUrl(file));
//	}
//	
	/**
	 * Test URLs for directories
	 */
	public void testViewvcDirectory() {
		assertEquals("http://example.com/",
				viewvc.getDirectoryUrl(root));
		assertEquals("http://example.com/path/",
				viewvc.getDirectoryUrl(path));
	}
	
	/**
	 * Test URLs for diff
	 */
	public void testViewvcDiff() {
		VersionedFile file = new VersionedFile("file", root);
		Revision rev1 = file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		Revision rev2 = file.addChangeRevision("1.2", null, date, null, 0, 0, 0, null);
		assertEquals(
				"http://example.com/file.diff?r1=1.1&r2=1.2",
				viewvc.getDiffUrl(rev1, rev2));
	}

	/**
	 * Test URLs for diff with cvsroot parameter
	 */
	public void testViewvcDiffWithCvsroot() {
		this.viewvc = new ViewVcIntegration("http://example.com/cgi-bin/viewvc.cgi/module?cvsroot=CvsRoot");
		VersionedFile file = new VersionedFile("file", this.root);
		Revision rev1 = file.addChangeRevision("1.1", null, this.date, null, 0, 0, 0, null);
		Revision rev2 = file.addChangeRevision("1.2", null, this.date, null, 0, 0, 0, null);
		assertEquals(
				"http://example.com/cgi-bin/viewvc.cgi/module/file.diff?r1=1.1&r2=1.2&cvsroot=CvsRoot",
				this.viewvc.getDiffUrl(rev1, rev2));
	}



	/**
	 * test
	 */
	public void testChoraCreation() {
		assertEquals("Chora", chora.getName());
	}
	
	/**
	 * Tests if stuff still works when the trailing slash is omitted from
	 * the base URL
	 */
	public void testChoraForgivingBaseURL() {
		ChoraIntegration chora2 = new ChoraIntegration("http://example.com");
		VersionedFile file = new VersionedFile("file", root);
		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		assertEquals("http://example.com/?f=file", chora2.getFileHistoryUrl(file));
	}

	/**
	 * test URLs for a normal file
	 */
	public void testChoraNormalFile() {
		VersionedFile file = new VersionedFile("path/file", path);
		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		assertEquals(BASE + "?f=path/file", chora.getFileHistoryUrl(file));
		assertEquals(BASE + "co.php?f=path/file&r=HEAD", chora.getFileViewUrl(file));
	}

//	/**
//	 * test URLs for an attic file
//	 */
//	public void testChoraAtticFile() {
//		VersionedFile file = new VersionedFile("path/file", path);
//		file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
//		HashSet atticFileNames = new HashSet();
//		atticFileNames.add("path/file");
//		chora.setAtticFileNames(atticFileNames);
//		assertEquals(BASE + "path/Attic/file", chora.getFileHistoryUrl(file));
//		assertEquals(BASE + "path/Attic/file?r=HEAD", chora.getFileViewUrl(file));
//	}
	
	/**
	 * Test URLs for directories
	 */
	public void testChoraDirectory() {
		assertEquals("http://example.com/",
				viewvc.getDirectoryUrl(root));
		assertEquals("http://example.com/path/",
				viewvc.getDirectoryUrl(path));
	}
	
	/**
	 * Test URLs for diff
	 */
	public void testChoraDiff() {
		VersionedFile file = new VersionedFile("file", root);
		Revision rev1 = file.addChangeRevision("1.1", null, date, null, 0, 0, 0, null);
		Revision rev2 = file.addChangeRevision("1.2", null, date, null, 0, 0, 0, null);
		assertEquals(
				"http://example.com/diff.php?f=file&r1=1.1&r2=1.2&ty=h",
				chora.getDiffUrl(rev1, rev2));
	}
}