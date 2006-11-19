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
    
	$RCSfile: LOCChart.java,v $
	$Date: 2006/06/14 10:25:33 $ 
*/
package net.sf.statsvn.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.statsvn.Messages;
import net.sf.statsvn.output.ConfigurationOptions;
import net.sf.statsvn.util.OutputUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 * Class for producing Lines Of Code charts
 * 
 * TODO: Replace by TimeSeriesChart
 *
 * @author jentzsch
 * @version $Id: LOCChart.java,v 1.40 2006/06/14 10:25:33 mungady Exp $
 */
public class LOCChart extends Chart {
	private static final Logger LOGGER =
			Logger.getLogger("net.sf.statsvn.renderer.LOCChart");

	/**
	 * Creates a Lines Of Code chart from a <tt>BasicTimeSeries</tt> and
	 * saves it as PNG
	 * @param locSeries the LOC history
	 * @param title the chart title
	 * @param fileName the filename where the chart will be saved
	 * @param width width of PNG in pixels
	 * @param height height of PNG in pixels
	 * @param annotations
	 */
	public LOCChart(final TimeSeries locSeries, final String title,
			final String fileName, final int width, final int height, final List annotations) {
		super(title, fileName);
		
		final Paint[] colors = new Paint[1];
		colors[0] = Color.blue;

		final TimeSeriesCollection collection = new TimeSeriesCollection();
		collection.addSeries(locSeries);
		createLOCChart(collection, colors, title, annotations);
		createChart();
		saveChart(width, height);
	}

	/**
	 * Creates a Lines Of Code chart from a list of <tt>BasicTimesSeries</tt> and
	 * saves it as PNG
	 * @param locSeriesList a list of <tt>BasicTimesSeries</tt>
	 * @param title the chart title
	 * @param fileName the filename where the chart will be saved
	 * @param width width of PNG in pixels
	 * @param height height of PNG in pixels
	 */
	public LOCChart(final List locSeriesList, final String title,
			final String fileName, final int width, final int height, final List annotations) {
		super(title, fileName);
		
		final Paint[] colors = new Paint[locSeriesList.size()];
		int i = 0;
		final TimeSeriesCollection collection = new TimeSeriesCollection();
		final Iterator it = locSeriesList.iterator();
		while (it.hasNext()) {
			final TimeSeries series = (TimeSeries) it.next();
			collection.addSeries(series);
			colors[i] = OutputUtils.getStringColor(series.getKey().toString()); 
			i++;
		}
		createLOCChart(collection, colors, title, annotations);
		createChart();
		saveChart(width, height);
	}

	private void createLOCChart(final TimeSeriesCollection collection, final Paint[] colors, 
			final String title, final List annotations) {
		LOGGER.finer("creating LOC chart for " + title);

		final String domain = Messages.getString("TIME_LOC_DOMAIN");
		final String range = Messages.getString("TIME_LOC_RANGE");

		final XYDataset data = collection;
		final boolean legend = (collection.getSeriesCount() > 1);
		setChart(ChartFactory.createTimeSeriesChart(
				ConfigurationOptions.getProjectName(), domain, range, data, legend, false, false
		));

		// getChart().getPlot().setSeriesPaint(colors);
		final XYPlot plot = getChart().getXYPlot();
		for (int i = 0; i < colors.length; i++) {
			plot.getRenderer().setSeriesPaint(0, colors[i]);   
		}
		final DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
		domainAxis.setVerticalTickLabels(true);
		final ValueAxis valueAxis = plot.getRangeAxis();
		valueAxis.setLowerBound(0);
		plot.setRenderer(new XYStepRenderer());
		
		if (annotations != null) {
		    for (final Iterator it = annotations.iterator(); it.hasNext();) {
                plot.addAnnotation((XYAnnotation)it.next());
            }
		}
	}
}
