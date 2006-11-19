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
    
	$RCSfile: Chart.java,v $
	$Date: 2006/06/14 10:25:33 $ 
*/
package net.sf.statsvn.renderer;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.sf.statsvn.output.ConfigurationOptions;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;

/**
 * superclass of all charts
 * provides several methods for charts
 * @author jentzsch
 *
 */
public class Chart {
	private static final Logger LOGGER =
		Logger.getLogger("sf.net.statsvn.renderer.ChartRenderer");
	private String title;
	private String fileName;
	private final Font font = new Font("SansSerif", Font.PLAIN, 12);
	private JFreeChart chart;
	
	/**
	 * creates charts
	 * @param title chart title
	 * @param fileName fileName for chart
	 */
	public Chart(
		final String title,
		final String fileName) {
		this.title = title;
		this.fileName = fileName;
	}
	
	/**
	 * create chart with titles and credit information
	 */
	public void createChart() {
		addTitles();
		chart.setBackgroundPaint(new Color(204, 204, 187));
	}

	/**
	 * save chart as PNG, restore old file name
	 * @param imageWidth image width
	 * @param imageHeight image height
	 * @param filename file name
	 */
	public void saveChart(final int imageWidth, final int imageHeight, final String filename) {
		final String oldFileName = this.fileName;
		this.fileName = filename;
		saveChart(imageWidth, imageHeight);
		this.fileName = oldFileName;
	}
	
	/**
	 * save chart as PNG
	 * @param imageWidth image width
	 * @param imageHeight image height
	 */
	public void saveChart(final int imageWidth, final int imageHeight) {
		try {
			ChartUtilities.saveChartAsPNG(
				new File(ConfigurationOptions.getOutputDir() + fileName),
				chart,
				imageWidth,
				imageHeight);
			LOGGER.fine("saved chart '" + title + "' as '" + fileName + "'");
		} catch (final IOException e) {
			LOGGER.warning(
				"Could not save Chart as png Image: "
					+ fileName
					+ e.toString());
		}
	}

	private void addTitles() {
		final TextTitle title2 = new TextTitle(title, font);
		title2.setMargin(new RectangleInsets(UnitType.RELATIVE, 0.05, 0.05, 
                0.05, 0.0));
		chart.addSubtitle(title2);
	}
	
	/** 
	 * get chart
	 * @return chart the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/** 
	 * set chart
	 * @param chart the chart
	 */
	public void setChart(final JFreeChart chart) {
		this.chart = chart;
	}
	
	/** 
	 * get fileName
	 * @return fileName file name
	 */
	public String getFileName() {
		return fileName;
	}
}
