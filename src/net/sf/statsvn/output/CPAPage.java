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
    
	$RCSfile: CPAPage.java,v $ 
	Created on $Date: 2003/12/18 00:26:17 $ 
*/

package net.sf.statsvn.output;

import java.io.IOException;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.renderer.TableRenderer;
import net.sf.statsvn.reportmodel.Table;
import net.sf.statsvn.reports.AuthorsTableReport;
import net.sf.statsvn.reports.AbstractLocTableReport;
import net.sf.statsvn.reports.TableReport;

/**
 * @author anja
 */
public class CPAPage extends HTMLPage {
	private static final int ACTIVITY_CHART_HEIGHT = 300;
	private static final int ACTIVITY_CHART_WIDTH = 500;
	private int sortType;
	private boolean withImage;

	/**
	 * Method CPUPage.
	 * @param content to dispay
	 * @param sortType to use
	 * @param withImage <tt>true</tt> if the LOC per Author image was generated
	 * @throws IOException on error
	 */
	public CPAPage(final Repository content, final int sortType, final boolean withImage, final OutputRenderer renderer)
			throws IOException {

		super(content, renderer);
		this.sortType = sortType;
		if (sortType == AbstractLocTableReport.SORT_BY_LINES) {
			setFileName("authors" + renderer.getFileExtension());
		} else {
			setFileName("authors2" + renderer.getFileExtension());
		}
		this.withImage = withImage;
		setPageName(Messages.getString("CPU_TITLE"));
		createPage();
	}

	protected void printBody() throws IOException {
		printBackLink();
		final TableReport report = new AuthorsTableReport(getContent(), sortType);
		report.calculate();
		final Table table = report.getTable();
		print(new TableRenderer(table, getRenderer()).getRenderedTable());
		if (sortType == AbstractLocTableReport.SORT_BY_LINES) {
			printParagraph(Messages.getString("NAVIGATION_ORDER_BY") + ": "
				+ strong(Messages.getString("ORDER_BY_LOC"))
				+ " / "
				+ a("authors2.html", Messages.getString("ORDER_BY_NAME")));
		} else {
			printParagraph(Messages.getString("NAVIGATION_ORDER_BY") + ": "
				+ a("authors.html", Messages.getString("ORDER_BY_LOC"))
				+ " / "
				+ strong(Messages.getString("ORDER_BY_NAME")));
		}
		if (withImage) {
			printStartSection2(Messages.getString("LOC_TITLE"));
			printParagraph(img("loc_per_author.png", 640, 480));
            printEndSection2();
		}
		printStartSection2(Messages.getString("ACTIVITY_TITLE"));
		printParagraph(img("activity_time.png", ACTIVITY_CHART_WIDTH, ACTIVITY_CHART_HEIGHT));
		printParagraph(img("activity_day.png", ACTIVITY_CHART_WIDTH, ACTIVITY_CHART_HEIGHT));
		print(getAuthorActivityChartSection());
        printEndSection2();
	}
	
	private String getAuthorActivityChartSection() {
		final StringBuffer result = new StringBuffer();
		result.append(startSection2(Messages.getString("AUTHOR_ACTIVITY_TITLE")));
		result.append(p(img("commitscatterauthors.png")));
		result.append(p(img("activity.png")));
        result.append(endSection2());
		return result.toString();
	}
}
