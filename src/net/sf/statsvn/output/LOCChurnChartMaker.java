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

import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import net.sf.statcvs.Messages;
import net.sf.statcvs.charts.ChartImage;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.ReportConfig;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;

/**
 * Class for producing Lines Of Code with Churn charts
 * 
 * @author Benoit Xhenseval (www.ObjectLab.co.uk)
 */
public class LOCChurnChartMaker {
	private ChartImage chartFile = null;

	/**
	 * Creates a Lines Of Code chart from a <tt>BasicTimeSeries</tt> and saves
	 * it as PNG
	 * 
	 * @param churnSeries
	 *            the Churn history
	 * @param locSeries
	 *            the LOC history
	 * @param title
	 *            the chart title
	 * @param fileName
	 *            the filename where the chart will be saved
	 * @param width
	 *            width of PNG in pixels
	 * @param height
	 *            height of PNG in pixels
	 * @param annotations
	 */
	public LOCChurnChartMaker(final ReportConfig config, final TimeSeries churnSeries, final TimeSeries locSeries, final String title, final String fileName,
	        final Dimension size, final List annotations) {
		final TimeSeriesCollection collection = new TimeSeriesCollection(locSeries);
		// collection.setDomainIsPointsInTime(false);
		final TimeSeriesCollection churnCollection = new TimeSeriesCollection(churnSeries);
		// churnCollection.setDomainIsPointsInTime(false);
		final JFreeChart chart = createChart(collection, churnCollection, title, annotations);

		chartFile = config.createChartImage(fileName, title, chart, size);
	}

	public ChartImage toFile() {
		return this.chartFile;
	}

	private JFreeChart createChart(final TimeSeriesCollection locCollection, final TimeSeriesCollection churnSet, final String title, final List annotations) {
		final String domain = Messages.getString("TIME_LOC_DOMAIN");
		final String range = Messages.getString("TIME_LOC_RANGE");

		final IntervalXYDataset data = locCollection;
		final boolean legend = true;// (locCollection.getSeriesCount() > 1);

		final JFreeChart chart = ChartFactory.createXYBarChart(ConfigurationOptions.getProjectName() + ":" + title, domain, true, range, data,
		        PlotOrientation.VERTICAL, legend, false, false);

		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(new XYStepRenderer());

		// new...
		final NumberAxis rangeAxis1 = (NumberAxis) plot.getRangeAxis();
		rangeAxis1.setLowerMargin(0.40); // to leave room for volume bars

		final DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
		domainAxis.setVerticalTickLabels(true);

		// now add the churnSet
		final NumberAxis rangeAxis2 = new NumberAxis(Messages.getString("CHURN_RANGE"));
		rangeAxis2.setUpperMargin(1.00); // to leave room for price line
		plot.setRangeAxis(1, rangeAxis2);
		plot.setDataset(1, churnSet);
		plot.setRangeAxis(1, rangeAxis2);
		plot.mapDatasetToRangeAxis(1, 1);
		final XYBarRenderer renderer2 = new XYBarRenderer(0.20);
		plot.setRenderer(1, renderer2);

		if (annotations != null) {
			for (final Iterator it = annotations.iterator(); it.hasNext();) {
				plot.addAnnotation((XYAnnotation) it.next());
			}
		}

		return chart;
	}
}
