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
    
	$RCSfile: LOCSeriesBuilder.java,v $
	$Date: 2004/05/09 21:29:02 $ 
*/
package net.sf.statcvs.output;

import net.sf.statcvs.model.Revision;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
/**
 * Builds a <tt>BasicTimesSeries</tt> for the LOC history of a set of
 * revisions. All revisions that should be counted must be passed to
 * the {@link #addRevision} method. When all revisions have been passed
 * to this method, a <tt>BasicTimeSeries</tt> can
 * be obtained from {@link #getTimeSeries} and can be added to a chart.
 * 
 * TODO: Replace by a custom LocTimeSeriesReport
 * 
 * @author Richard Cyganiak
 * @version $Id: LOCSeriesBuilder.java,v 1.9 2004/05/09 21:29:02 jentzsch Exp $
 **/
public class LOCSeriesBuilder {
//	private static Logger logger = Logger.getLogger(LOCSeriesBuilder.class.getName());
	private TimeSeries series;
	private boolean hasRevisions = false;
	private Minute minute;
	private int loc = 0;
	private boolean finished = false;
	private boolean countEffective;

	/**
	 * Creates a new <tt>LOCSeriesBuilder</tt>
	 * @param seriesTitle the title for the time series
	 * @param countEffective If <tt>true</tt>, the effective LOC number will
	 *                       be counted. If <tt>false</tt>, the contributed
	 *                       value of new lines will be counted. 
	 */
	public LOCSeriesBuilder(final String seriesTitle, final boolean countEffective) {
		series = new TimeSeries(seriesTitle, Minute.class);
		this.countEffective = countEffective;
	}
	
	/**
	 * Adds a revision to the time series. The revision must
	 * be at a later date than all previously added revisions.
	 * @param revision the revision to add to the series
	 */
	public void addRevision(final Revision revision) {
		if (finished) {
			throw new IllegalStateException("can't add more revisions after getTimeSeries()");
		}
		if (!hasRevisions) {
			if (revision.isBeginOfLog()) {
				loc += revision.getLines();
				return;
			}
			minute = new Minute(revision.getDate());
			series.add(minute.previous(), loc);
			hasRevisions = true;
		} else {
			final Minute currentMinute = new Minute(revision.getDate());
			if (!currentMinute.equals(minute)) {
				series.add(minute, loc);
				minute = currentMinute;
			}
		}
		if (countEffective) {
			loc += revision.getLinesDelta();
		} else {
			loc += revision.getNewLines();
		}
	}
	
	/**
	 * gets the finished time series. Should not be called before
	 * all revisions have been added.
	 * @return the resulting <tt>BasicTimeSeries</tt> or <tt>null</tt>
	 * if no LOC data is available for the revision set
	 */
	public TimeSeries getTimeSeries() {
		if (!hasRevisions) {
			return null;
		}
		if (!finished) {
			series.add(minute, loc);
			series.add(minute.next(), loc);
			finished = true;
		}
		return series;
	}
}