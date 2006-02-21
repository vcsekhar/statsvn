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
package net.sf.statcvs.input;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Assert;

/**
 * Mock Object implementing {@link SvnLogBuilder}
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @version $Id: MockLogBuilder.java,v 1.4 2004/12/14 13:38:13 squig Exp $
 */
public class MockLogBuilder implements SvnLogBuilder {
	private LinkedList expectedMethods = new LinkedList();
	private LinkedList expectedData = new LinkedList();

	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.SvnLogBuilder#buildModule(java.lang.String)
	 */
	public void buildModule(String moduleName) {
		Assert.assertEquals(expectedMethods.removeFirst(), "buildModule");
		Assert.assertEquals(expectedData.removeFirst(), moduleName);
	}

	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.SvnLogBuilder#buildFile(java.lang.String, boolean, boolean)
	 */
	public void buildFile(String filename, boolean isBinary, boolean isInAttic, Map revBySymnames) {
		Assert.assertEquals(expectedMethods.removeFirst(), "buildFile");
		Assert.assertEquals(expectedData.removeFirst(), filename);
		Assert.assertEquals(expectedData.removeFirst(), new Boolean(isBinary));
		Assert.assertEquals(expectedData.removeFirst(), new Boolean(isInAttic));
	}

	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.SvnLogBuilder#buildRevision(net.sf.statcvs.input.RevisionData)
	 */
	public void buildRevision(RevisionData data) {
		if (expectedMethods.isEmpty()) {
			Assert.fail("expected no more revisions");
		}
		if (!"buildRevision".equals(expectedMethods.getFirst())
				&& !"nextRevision".equals(expectedMethods.getFirst())
				&& !((String) expectedMethods.getFirst()).startsWith("current")) {
			Assert.assertEquals(expectedMethods.getFirst(), "buildRevision");
		}
		if ("buildRevision".equals(expectedMethods.getFirst())) {
			Assert.assertEquals(expectedMethods.removeFirst(), "buildRevision");
			RevisionData expected = (RevisionData) expectedData.removeFirst();
			Assert.assertEquals(expected.getRevisionNumber(), data.getRevisionNumber());
			Assert.assertEquals(expected.getDate().getTime() / 1000, data.getDate().getTime() / 1000);
			Assert.assertEquals(expected.getLoginName(), data.getLoginName());
			Assert.assertEquals(expected.isAddOnSubbranch(), data.isAddOnSubbranch());
			Assert.assertEquals(expected.isDeletion(), data.isDeletion());
			Assert.assertEquals(expected.isCreation(), data.isCreation());
			Assert.assertEquals(expected.isChangeOrRestore(), data.isChangeOrRestore());
			Assert.assertEquals(expected.getComment(), data.getComment());
			Assert.assertEquals(expected.getLinesAdded(), data.getLinesAdded());
			Assert.assertEquals(expected.getLinesRemoved(), data.getLinesRemoved());
			return;
		}
		while (!expectedMethods.isEmpty() && ((String) expectedMethods.getFirst()).startsWith("current")) {
			String expected = (String) expectedMethods.removeFirst();
			if ("currentRevisionNumber".equals(expected)) {
				Assert.assertEquals(expectedData.removeFirst(), data.getRevisionNumber());
			} else if ("currentDate".equals(expected)) {
				Assert.assertEquals(((Date) expectedData.removeFirst()).getTime() / 1000, data.getDate().getTime() / 1000);
			} else if ("currentAuthor".equals(expected)) {
				Assert.assertEquals(expectedData.removeFirst(), data.getLoginName());
			} else if ("currentComment".equals(expected)) {
				Assert.assertEquals(expectedData.removeFirst(), data.getComment());				
			} else if ("currentStateExp".equals(expected)) {
				Assert.assertTrue(data.isStateExp());				
			} else if ("currentStateDead".equals(expected)) {
				Assert.assertTrue(data.isStateDead());				
			} else if ("currentLines".equals(expected)) {
				Assert.assertEquals(expectedData.removeFirst(), new Integer(data.getLinesAdded()));
				Assert.assertEquals(expectedData.removeFirst(), new Integer(data.getLinesRemoved()));
			} else if ("currentNoLines".equals(expected)) {
				Assert.assertTrue(data.hasNoLines());
			} else {	// can't happen
				Assert.fail("bad state: " + expected);
			}
		}
		if (!expectedMethods.isEmpty() && "nextRevision".equals(expectedMethods.getFirst())) {
			expectedMethods.removeFirst();
		}
	}

	public void expectBuildModule(String moduleName) {
		expectedMethods.add("buildModule");
		expectedData.add(moduleName);
	}

	public void expectBuildFile(String filename, boolean isBinary, boolean isInAttic) {
		expectedMethods.add("buildFile");
		expectedData.add(filename);
		expectedData.add(new Boolean(isBinary));
		expectedData.add(new Boolean(isInAttic));
	}

	public void expectBuildRevision(RevisionData data) {
		expectedMethods.add("buildRevision");
		expectedData.add(data);
	}

	public void expectNextRevision() {
		expectedMethods.add("nextRevision");
	}
	
	public void expectCurrentRevisionNumber(String revision) {
		expectedMethods.add("currentRevisionNumber");
		expectedData.add(revision);
	}
	
	public void expectCurrentDate(Date date) {
		expectedMethods.add("currentDate");
		expectedData.add(date);
	}
	
	public void expectCurrentAuthor(String name) {
		expectedMethods.add("currentAuthor");
		expectedData.add(name);
	}
	
	public void expectCurrentComment(String comment) {
		expectedMethods.add("currentComment");
		expectedData.add(comment);
	}
	
	public void expectCurrentStateExp() {
		expectedMethods.add("currentStateExp");
	}
	
	public void expectCurrentStateDead() {
		expectedMethods.add("currentStateDead");
	}
	
	public void expectCurrentNoLines() {
		expectedMethods.add("currentNoLines");
	}
	
	public void expectCurrentLines(int added, int removed) {
		expectedMethods.add("currentLines");
		expectedData.add(new Integer(added));
		expectedData.add(new Integer(removed));
	}

	public void verify() {
		if (!expectedMethods.isEmpty()) {
			Assert.fail("expected " + expectedMethods.getFirst());
		}
	}
}