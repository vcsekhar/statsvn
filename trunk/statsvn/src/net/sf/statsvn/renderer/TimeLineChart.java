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
    
	$RCSfile: TimeLineChart.java,v $
	$Date: 2005/03/29 23:22:28 $ 
*/
package net.sf.statsvn.renderer;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.statsvn.Messages;
import net.sf.statsvn.output.ConfigurationOptions;
import net.sf.statsvn.reportmodel.TimeLine;
import net.sf.statsvn.reportmodel.TimePoint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 * Creates charts from {@link net.sf.statsvn.reportmodel.TimeLine}s and
 * saves them to PNG.
 *
 * TODO: Should call TimeLine#isEmpty and not generate the chart if true
 *  
 * @author Richard Cyganiak
 * @version $Id: TimeLineChart.java,v 1.7 2005/03/29 23:22:28 cyganiak Exp $
 */
public class TimeLineChart extends Chart {
	private static final Logger LOGGER =
			Logger.getLogger("net.sf.statsvn.renderer.LOCChart");

	/**
	 * Creates a chart from a time line.
	 * @param timeLine the time line data for the chart 
	 * @param fileName the file name for the PNG image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	public TimeLineChart(final TimeLine timeLine, final String fileName,
			final int width, final int height, final List annotations) {

		super(timeLine.getTitle(), fileName);
		
		//Paint[] colors = new Paint[1];
		//colors[0] = Color.blue;

		final TimeSeriesCollection collection = new TimeSeriesCollection();
		collection.addSeries(createTimeSeries(timeLine));

		LOGGER.finer("creating time line chart for "
				+ timeLine.getTitle() + " / " + timeLine.getTitle());

		final String range = timeLine.getRangeLabel();
		final String domain = Messages.getString("DOMAIN_TIME");

		final XYDataset data = collection;
		setChart(ChartFactory.createTimeSeriesChart(
			ConfigurationOptions.getProjectName(), domain, range, data, false, false, false
		));
		
		final XYPlot plot = (XYPlot) getChart().getPlot();
		plot.getRenderer().setSeriesPaint(0, Color.blue);
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setVerticalTickLabels(true);
		plot.setRenderer(new XYStepRenderer());
		if (annotations != null) {
		    for (final Iterator it = annotations.iterator(); it.hasNext();) {
                plot.addAnnotation((XYAnnotation)it.next());
            }
		}
		createChart();
		saveChart(width, height);
	}

	private TimeSeries createTimeSeries(final TimeLine timeLine) {
		final TimeSeries result = new TimeSeries("!??!SERIES_LABEL!??!", Millisecond.class);
		final Iterator it = timeLine.getDataPoints().iterator();
		while (it.hasNext()) {
			final TimePoint timePoint = (TimePoint) it.next();
			result.add(new Millisecond(timePoint.getDate()), timePoint.getValue());
		}
		return result;
	}
}
