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
    
	$RCSfile: ModulePage.java,v $ 
	Created on $Date: 2004/10/11 15:04:16 $ 
*/

package net.sf.statcvs.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Commit;
import net.sf.statcvs.model.CvsContent;
import net.sf.statcvs.model.CvsFile;
import net.sf.statcvs.model.CvsRevision;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.renderer.CommitLogRenderer;
import net.sf.statcvs.renderer.TableRenderer;
import net.sf.statcvs.reportmodel.Table;
import net.sf.statcvs.reports.AuthorsForDirectoryTableReport;
import net.sf.statcvs.reports.TableReport;

/**
 * @author anja
 */
public class ModulePage extends HTMLPage {
	private Directory directory;
	private int locInModule = 0;
	private boolean locImageCreated;

	/**
	 * Method ModulePage.
	 * @param content of the Page
	 * @param directory the directory for this page
	 * @param locImageCreated <tt>true</tt> if a LOC image is available for this module
	 * @throws IOException on error
	 */
	public ModulePage(CvsContent content, Directory directory,
			boolean locImageCreated) throws IOException {
		super(content);
		setFileName(HTMLOutput.getDirectoryPageFilename(directory));
		setPageName("Module " + directory.getPath());
		this.directory = directory;
		this.locImageCreated = locImageCreated;
		Iterator it = directory.getFiles().iterator();
		while (it.hasNext()) {
			CvsFile file = (CvsFile) it.next();
			locInModule += file.getCurrentLinesOfCode();
		}
		createPage();
	}

	protected void printBody() throws IOException {
		printBackLink();
		print(getModuleInfo());
		print(getWebRepositoryLink());
		printH2(Messages.getString("SUBTREE_TITLE"));
		printParagraph(getModuleLinks());
		print(getLOCImage());
		print(getCPUTable());
		print(getLastCommits());
	}

	private String getModuleInfo() {
		if (directory.getRevisions().isEmpty()) {
			return "";
		}
		CvsRevision firstRev = (CvsRevision) directory.getRevisions().first();
		CvsRevision lastRev = (CvsRevision) directory.getRevisions().last();
		return HTMLTagger.getSummaryPeriod(
				firstRev.getDate(),
				lastRev.getDate());
	}

	private String getWebRepositoryLink() {
		if (ConfigurationOptions.getWebRepository() == null) {
			return "";
		}
		WebRepositoryIntegration rep = ConfigurationOptions.getWebRepository();
		String text = Messages.getString("BROWSE_WEB_REPOSITORY") + " " + rep.getName();
		return p(a(rep.getDirectoryUrl(directory), text));
	}

	private String getModuleLinks() {
		String result;
		Iterator it = directory.getSubdirectoriesRecursive().iterator();
		Directory current = (Directory) it.next();
		result = getRootLinks(current) + "<br>";
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			result += getFolderHtml(dir, directory.getDepth());
		}
		return result;
	}

	private String getLOCImage() {
		if (!locImageCreated) {
			return "";
		}
		String result = h2(Messages.getString("LOC_TITLE"));
		result += p(img(HTMLOutput.getDirectoryLocChartFilename(directory), 640, 480)
				+ br() + strong("Total Lines Of Code: ") + locInModule
				+ " (" + HTMLTagger.getDateAndTime(getContent().getLastDate()) + ")");
		return result;
	}

	private String getCPUTable() {
		if (directory.getRevisions().isEmpty()) {
			return "";
		}
		String result = h2(Messages.getString("CPU_TITLE"));
		TableReport report = 
				new AuthorsForDirectoryTableReport(getContent(), directory);
		report.calculate();
		Table table = report.getTable();
		result += new TableRenderer(table).getRenderedTable();
		return result;
	}

	private String getLastCommits() {
		List dirCommits = getCommitsInDirectory();
		int commitCount = dirCommits.size();
		if (commitCount == 0) {
			return "";
		}
		String result = h2(Messages.getString("MOST_RECENT_COMMITS"));
		CommitLogRenderer renderer = new CommitLogRenderer(dirCommits);
		result += renderer.renderMostRecentCommits(HTMLOutput.MOST_RECENT_COMMITS_LENGTH);
		return result;
	}

	private String getRootLinks(Directory dir) {
		String result = dir.isRoot()
				? strong(Messages.getString("NAVIGATION_ROOT"))
				: strong(dir.getName());
		while (!dir.isRoot()) {
			Directory parent = dir.getParent();
			String caption = parent.isRoot()
					? Messages.getString("NAVIGATION_ROOT")
					: parent.getName();
			String parentPageFilename = HTMLOutput.getDirectoryPageFilename(parent);
			result = a(parentPageFilename, caption) + "/" + result;
			dir = parent;
		}
		return result;
	}
	
	private Commit getCommit(CvsRevision rev) {
		Iterator it = getContent().getCommits().iterator();
		while (it.hasNext()) {
			Commit commit = (Commit) it.next();
			if (commit.getRevisions().contains(rev)) {
				return commit;
			}
		}
		return null;
	}
	
	private List getCommitsInDirectory() {
		Map commitsToFilteredCommits = new HashMap();
		Iterator it = this.directory.getRevisions().iterator();
		while (it.hasNext()) {
			CvsRevision rev = (CvsRevision) it.next();
			Commit commit = getCommit(rev);
			if (commit == null) {
				continue;
			}
			if (commitsToFilteredCommits.containsKey(commit)) {
				Commit filteredCommit = (Commit) commitsToFilteredCommits.get(commit);
				filteredCommit.addRevision(rev);
			} else {
				Commit filteredCommit = new Commit(rev);
				commitsToFilteredCommits.put(commit, filteredCommit);
			}
		}
		List commits = new ArrayList(commitsToFilteredCommits.values());
		Collections.sort(commits);
		return commits;
	}
}