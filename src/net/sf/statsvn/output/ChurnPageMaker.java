/*
 StatSVN - SVN Subversion statistics generation 
 Copyright (C) 2006 Benoit Xhenseval
 http://www.statsvn.org
 
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
package net.sf.statsvn.output;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;

import net.sf.statcvs.Messages;
import net.sf.statcvs.charts.ChartImage;
import net.sf.statcvs.charts.SymbolicNameAnnotation;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.output.ReportConfig;
import net.sf.statcvs.pages.NavigationNode;
import net.sf.statcvs.pages.Page;
import net.sf.statcvs.reports.LOCSeriesBuilder;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

/**
 * A LOC and Churn Chart shows both the LOC and the number of lines touched per
 * day, this allows you to see the evolution of lines of code and the amount of
 * changes. A flat LOC with a lot of Churn implies a lot of refactoring, an
 * increase in LOC in line with churn implies new functionality.
 * 
 * @author Benoit Xhenseval (www.ObjectLab.co.uk)
 */
public class ChurnPageMaker {
	private final ReportConfig config;

	/**
	 * @see net.sf.statcvs.output.HTMLPage#HTMLPage(Repository)
	 */
	public ChurnPageMaker(final ReportConfig config) {
		this.config = config;
	}

	public NavigationNode toFile() {
		final Page page = this.config.createPage("churn", Messages.getString("CHURN_TITLE"), Messages.getString("CHURN_TITLE"));
		page.addRawContent("<p>" + Messages.getString("CHURN_DESCRIPTION") + "</p>");
		page.add(buildChart());
		return page;
	}

	private ChartImage buildChart() {
		Map changePerRevision = new HashMap();
		SortedSet revisions = config.getRepository().getRevisions();
		for (Iterator it = revisions.iterator(); it.hasNext();) {
			Revision rev = (Revision) it.next();
			Date dateToUse = blastTime(rev.getDate());
			Integer changes = (Integer) changePerRevision.get(dateToUse);
			if (changes == null) {
				changePerRevision.put(dateToUse, new Integer(Math.abs(getLineChanges(rev))));
			} else {
				changePerRevision.put(dateToUse, new Integer(Math.abs(changes.intValue()) + getLineChanges(rev)));
			}
		}

		List annotations = SymbolicNameAnnotation.createAnnotations(config.getRepository().getSymbolicNames());
		TimeSeries timeLine = new TimeSeries(Messages.getString("CHURN_TOUCHED_LINE"), Day.class);

		for (Iterator it = changePerRevision.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();

			SvnConfigurationOptions.getTaskLogger().log(entry.getKey() + " ==> " + entry.getValue());
			timeLine.add(new Day((Date) entry.getKey()), ((Integer) entry.getValue()).intValue());
		}

		TimeSeries locSeries = getLOCTimeSeries(revisions, Messages.getString("TIME_LOC_SUBTITLE"));

		LOCChurnChartMaker chart = new LOCChurnChartMaker(config, timeLine, locSeries, Messages.getString("LOC_CHURN_CHART_TITLE"), "locandchurn.png", config
		        .getLargeChartSize(), annotations);
		return chart.toFile();
	}

	private Date blastTime(final Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private int getLineChanges(Revision rev) {
		if (rev.isDead()) {
			return rev.getLinesDelta();
		}
		return Math.abs(rev.getLinesDelta()) + 2 * rev.getReplacedLines();
	}

	private TimeSeries getLOCTimeSeries(final SortedSet revisions, final String title) {
		final Iterator it = revisions.iterator();
		final LOCSeriesBuilder locCounter = new LOCSeriesBuilder(title, true);
		while (it.hasNext()) {
			locCounter.addRevision((Revision) it.next());
		}
		return locCounter.getTimeSeries();
	}
}
