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
    
	$RCSfile: IndexPage.java,v $ 
	Created on $Date: 2006/10/10 09:23:45 $ 
*/

package net.sf.statcvs.output;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.renderer.CommitLogRenderer;
import net.sf.statcvs.renderer.TableRenderer;
import net.sf.statcvs.reportmodel.Table;
import net.sf.statcvs.reports.TableReport;
import net.sf.statcvs.reports.TopAuthorsTableReport;

/**
 * @author anja
 */
public class IndexPage extends HTMLPage {
	private static SimpleDateFormat outputDateFormat =
		new SimpleDateFormat(Messages.getString("DATE_FORMAT"));

	private boolean locImageCreated;
	private boolean commitScatterImageCreated;
	private boolean authorsPageCreated;

	/**
	 * @see net.sf.statcvs.output.HTMLPage#HTMLPage(Repository)
	 */
	public IndexPage(Repository content, boolean locImageCreated, boolean commitScatterImageCreated,
			boolean authorsPageCreated, final OutputRenderer renderer) throws IOException {
		super(content,renderer);
		this.locImageCreated = locImageCreated;
		this.commitScatterImageCreated = commitScatterImageCreated;
		this.authorsPageCreated = authorsPageCreated;
		setFileName("index" + renderer.getFileExtension());
		setPageName(Messages.getString("INDEX_TITLE") + " " + ConfigurationOptions.getProjectName());
		createPage();
	}

	protected void printBody() throws IOException {
		print(getProjectInfo());
		if (ConfigurationOptions.getNotes() != null) {
			print(ConfigurationOptions.getNotes());
		}
		print(getReportNavigation());
		print(getLOC());
		if (authorsPageCreated) {
			print(getTopAuthorsSection());
		}
		printStartSection2(Messages.getString("REPTREE_TITLE"));
		printParagraph(getIndexTree());
        printEndSection2();
	}

	private String getProjectInfo() {
		Calendar cal = Calendar.getInstance();
		String result = HTMLTagger.getSummaryPeriod(
					getContent().getFirstDate(),
					getContent().getLastDate(),
					"Generated: " + outputDateFormat.format(cal.getTime()),
					true);
		return result;
	}

	private String getReportNavigation() {
		String authorLink;
		if (authorsPageCreated) {
			authorLink = a("authors.html", Messages.getString("CPU_TITLE"));
		} else {
			Author author = getOnlyAuthor();
			String caption = Messages.getString("NAVIGATION_AUTHOR") + " " + author.getName();
			authorLink = a(getRenderer().getAuthorPageFilename(author, true), caption); 
		}
		return ul(
			  li(authorLink)
			+ li(
				a(
					CommitLogRenderer.getFilename(1, getRenderer(), true),
					Messages.getString("COMMIT_LOG_TITLE")))
			+ li(a("loc.html", Messages.getString("LOC_TITLE")))
			+ li(a("file_sizes.html", Messages.getString("FILE_SIZES_TITLE")))
			+ li(a("dir_sizes.html", Messages.getString("DIRECTORY_SIZES_TITLE"))));
	}

	private String getLOC() {
		if (!locImageCreated || !commitScatterImageCreated) {
			return "";
		}
		StringBuffer result = new StringBuffer(startSection2(Messages.getString("LOC_TITLE")));
		int loc = getContent().getCurrentLOC();
		result.append(p(a("loc.html", img("loc_small.png", 400, 300))) + br()
				+ strong("Total Lines Of Code:") + " " + loc + " ("
				+ HTMLTagger.getDateAndTime(getContent().getLastDate()) + ")");
        result.append(endSection2());
		return result.toString();
	}

	private String getTopAuthorsSection() {
		StringBuffer result = new StringBuffer();
		TableReport report = new TopAuthorsTableReport(getContent());
		report.calculate();
		Table table = report.getTable();
		if (table.getRowCount() >= 10) {
			result.append(startSection2(Messages.getString("SECTION_TOP_AUTHORS")));
		} else {
			result.append(startSection2(Messages.getString("SECTION_AUTHORS")));
		}
		result.append(new TableRenderer(table, getRenderer()).getRenderedTable());
		result.append(p(a("authors.html", Messages.getString("NAVIGATION_MORE"))));
        result.append(endSection2());
		return result.toString();
	}

	private String getIndexTree() {
		StringBuffer result = new StringBuffer();
		Iterator it = getContent().getDirectories().iterator();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			result.append(getFolderHtml(dir, 0));
		}
		return result.toString();
	}
	
	private Author getOnlyAuthor() {
		return (Author) getContent().getAuthors().iterator().next();
	}
}
