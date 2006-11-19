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
    
	$RCSfile: BarChart.java,v $
	$Date: 2004/10/12 07:22:42 $ 
*/
package net.sf.statcvs.renderer;

import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedSet;

import net.sf.statcvs.model.Revision;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.HTMLOutput;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Class for producing bar charts
 * @author jentzsch
 * @version $Id: BarChart.java,v 1.12 2004/10/12 07:22:42 cyganiak Exp $
 */
public class BarChart extends Chart {

	private double[] categories;
	
	/**
	 * creates a Bar Chart
	 * @param revisions a set of {@link Revision}s
	 * @param title chart title
	 * @param fileName fileName for chart
	 * @param categoryCount number of catgories
	 * @param categoryNames names for categories
	 */
	public BarChart(
		final SortedSet revisions,
		final String title,
		final String fileName,
		final int categoryCount,
		final String[] categoryNames) {

		super(title, fileName);
		
		categories = new double[categoryCount];
		for (int i = 0; i < categories.length; i++) {
			categories[i] = 0;
		}

		final Iterator it = revisions.iterator();
 		while (it.hasNext()) {
			final Revision rev = (Revision) it.next();
			final Date date = rev.getDate();
			final Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			if (categoryCount == 7) {
				final int day = cal.get(Calendar.DAY_OF_WEEK);
				categories[day - 1]++;
			} else if (categoryCount == 24) {
				final int hour = cal.get(Calendar.HOUR_OF_DAY);
				categories[hour]++;
			} 
		}

 		final DefaultCategoryDataset data = new DefaultCategoryDataset();
 		for (int i = 0; i < categoryCount; i++) {
 			data.addValue(categories[i], "Commits", categoryNames[i]);  
 		}
		
 		setChart(ChartFactory.createBarChart(
 			ConfigurationOptions.getProjectName(), "", "commits", data, PlotOrientation.VERTICAL, 
				false, false, false));

		final CategoryPlot plot = getChart().getCategoryPlot();
		plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		plot.getRenderer().setSeriesPaint(0, Color.blue);
		
		createChart();
		saveChart(HTMLOutput.SMALL_IMAGE_WIDTH, HTMLOutput.SMALL_IMAGE_HEIGHT);
	}
}
