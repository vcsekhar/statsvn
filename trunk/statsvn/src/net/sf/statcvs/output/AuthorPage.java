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
    
	$RCSfile: AuthorPage.java,v $ 
	Created on $Date: 2004/02/18 19:00:47 $ 
*/

package net.sf.statcvs.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Commit;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.renderer.CommitLogRenderer;
import net.sf.statcvs.renderer.TableRenderer;
import net.sf.statcvs.reportmodel.Table;
import net.sf.statcvs.reports.DirectoriesForAuthorTableReport;
import net.sf.statcvs.reports.TableReport;

/**
 * @author anja
 */
public class AuthorPage extends HTMLPage {
	private static Logger logger = Logger.getLogger("net.sf.statcvs.output.UserPage");
	private Author author;
	private boolean codeDistributionChartCreated;
	private int userChangeCount = 0;
	private int userLineCount = 0;
	private int totalChangeCount = 0;
	private int totalLineCount = 0;

	/**
	 * Method UserPage.
	 * @param content of the page
	 * @param author selected author
	 * @param codeDistributionChartCreated <tt>true</tt> if the code distribution
	 *                                     pie chart was created
	 * @throws IOException on error
	 */
	public AuthorPage(Repository content, Author author,
			boolean codeDistributionChartCreated, final OutputRenderer output) throws IOException {
		super(content, output);
		this.author = author;
		this.codeDistributionChartCreated = codeDistributionChartCreated;
		setFileName(output.getAuthorPageFilename(author, false));
		setPageName("User statistics for " + author.getName());
		logger.fine("creating author page for '" + author.getName() + "'");

		totalChangeCount = getContent().getRevisions().size();
		totalLineCount = getLineValue(getContent().getRevisions());

		userChangeCount = author.getRevisions().size();
		userLineCount = getLineValue(author.getRevisions());

		createPage();
	}

	private int getLineValue(Collection revisions) {
		int result = 0;
		Iterator it = revisions.iterator();
		while (it.hasNext()) {
			Revision element = (Revision) it.next();
			result += element.getNewLines();
		}
		return result;
	}

	protected void printBody() throws IOException {
		printBackLink();
		print(getAuthorInfo());
		if (totalChangeCount > 0) {
			print(getChangesSection());
		}
		if (totalLineCount > 0) {
			print(getLinesOfCodeSection());
		}
		print(getModulesSection());
		print(getActivitySection());
		print(getLastCommits());
	}

	private String getActivitySection() {
		StringBuffer result = new StringBuffer();
		result.append(startSection2(Messages.getString("ACTIVITY_TITLE")));
		result.append(p(img(HTMLOutput.getActivityTimeChartFilename(author), 500, 300)));
		result.append(p(img(HTMLOutput.getActivityDayChartFilename(author), 500, 300)));
        result.append(endSection2());
		return result.toString();
	}

	private String getAuthorInfo() {
		Revision firstRev = (Revision) author.getRevisions().first();
		Revision lastRev = (Revision) author.getRevisions().last();
		return HTMLTagger.getSummaryPeriod(
				firstRev.getDate(),
				lastRev.getDate());
	}

	private String getChangesSection() {
		StringBuffer result = new StringBuffer(startSection2("Total Changes"));
		String percentage = getPercentage(totalChangeCount, userChangeCount); 
		result.append(p(userChangeCount + " (" + percentage + ")"));
        result.append(endSection2());
		return result.toString();
	}

	private String getLinesOfCodeSection() {
		if (totalLineCount == 0) {
			return "";
		}
		StringBuffer result = new StringBuffer(startSection2(Messages.getString("LOC_TITLE")));
		result.append(p(userLineCount + " (" + getPercentage(totalLineCount, userLineCount) + ")"));
        result.append(endSection2());
		return result.toString();
	}

	private String getModulesSection() {
		StringBuffer result = new StringBuffer(startSection2("Modules"));
		if (codeDistributionChartCreated) {
			result.append(p(img(HTMLOutput.getCodeDistributionChartFilename(author), 640, 480)));
		}
		TableReport report = 
				new DirectoriesForAuthorTableReport(getContent(), author);
		report.calculate();
		Table table = report.getTable();
		result.append(new TableRenderer(table, getRenderer()).getRenderedTable());
        result.append(endSection2());
		return result.toString();
	}

	private String getLastCommits() {
		StringBuffer result = new StringBuffer(startSection2(Messages.getString("MOST_RECENT_COMMITS")));
		List authorCommits = new ArrayList();
		Iterator it = getContent().getCommits().iterator();
		while (it.hasNext()) {
			Commit commit = (Commit) it.next();
			if (!author.equals(commit.getAuthor())) {
				continue;
			}
			authorCommits.add(commit);
		}
		CommitLogRenderer renderer = new CommitLogRenderer(authorCommits);
		result.append(renderer.renderMostRecentCommits(HTMLOutput.MOST_RECENT_COMMITS_LENGTH, getRenderer()));
        result.append(endSection2());
		return result.toString();
	}

	/**
	 * returns the percentage of a given total count and the count
	 * @param totalCount
	 * @param count 
	 * @return String percentage string
	 */
	private String getPercentage(int totalCount, int count) {
		int percentTimes10 = (count * 1000) / totalCount;
		double percent = percentTimes10 / 10.0;
		return Double.toString(percent) + "%";
	}
}
