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
    
	$RCSfile: StackedBarChart.java,v $
	$Date: 2006/06/14 10:25:33 $ 
*/
package net.sf.statcvs.renderer;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Commit;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.output.ConfigurationOptions;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

/**
 * Class for producing stacked bar charts
 * @author jentzsch
 * @version $Id: StackedBarChart.java,v 1.24 2006/06/14 10:25:33 mungady Exp $
 */
public class StackedBarChart extends Chart {
	private static final int MODIFYING = 0;
	private static final int ADDING = 1;
	private Repository content;
	private double[][] categories;
	private ArrayList categoryNames = new ArrayList();
	
	/**
	 * creates an Stacked Bar Chart
	 * @param content Repository
	 * @param title chart title
	 * @param fileName fileName for chart
	 */
	public StackedBarChart(Repository content, String title, String fileName) {
		super(title, fileName);
		this.content = content;
		Collection authors = content.getAuthors();
		Iterator it = authors.iterator();
		while (it.hasNext()) {
			Author author = (Author) it.next();
			categoryNames.add(author.getName());
		}
		Collections.sort(categoryNames);
		
		categories = new double[2][categoryNames.size()];
		for (int j = 0; j < categoryNames.size(); j++) {
			categories[MODIFYING][j] = 0;
			categories[ADDING][j] = 0;
		}
										
		Iterator commitIt = content.getCommits().iterator();
		while (commitIt.hasNext()) {
			Commit commit = (Commit) commitIt.next();
			Set commitRevList = commit.getRevisions();
			Iterator commitRevIt = commitRevList.iterator();
			String authorName = commit.getAuthor().getName();
			if (authorName == null) {
				continue;
			}
			int author = categoryNames.indexOf(authorName);
			int linesAdded = 0;
			int linesRemoved = 0;
			while (commitRevIt.hasNext()) {
				Revision revision = (Revision) commitRevIt.next();
				if (revision.getLinesDelta() > 0) {
					linesAdded += revision.getLinesDelta() + revision.getReplacedLines();
					linesRemoved += revision.getReplacedLines();
				} else {
					linesAdded += revision.getReplacedLines();
					linesRemoved += -revision.getLinesDelta() + revision.getReplacedLines();
				}
			}
			if (linesAdded == linesRemoved) {
				categories[MODIFYING][author] += linesAdded;
			} 
			if (linesAdded < linesRemoved) {
				categories[MODIFYING][author] += linesRemoved;
			} 
			if (linesAdded > linesRemoved) {
				categories[ADDING][author] += linesAdded - linesRemoved;
				categories[MODIFYING][author] += linesRemoved;
			}
		}
		
		for (int i = 0; i < authors.size(); i++) {
			double maxLines = categories[MODIFYING][i] + categories[ADDING][i];
			for (int k = 0; k < 2; k++) {
				categories[k][i] *= (100 / maxLines);
			}
		}
		createStackedBarChart();
	}

	private void createStackedBarChart() {
		DefaultCategoryDataset data = new DefaultCategoryDataset();
		for (int i = 0; i < categories[MODIFYING].length; i++) {
			data.addValue(categories[MODIFYING][i], "modifying", (Comparable) categoryNames.get(i));   
		}
		for (int j = 0; j < categories[ADDING].length; j++) {
			data.addValue(categories[ADDING][j], "adding", (Comparable) categoryNames.get(j));   
		}
		//data.setSeriesName(MODIFYING, "modifying");
		//data.setSeriesName(ADDING, "adding");
		//data.setCategories(categoryNames.toArray());
		 		
		setChart(ChartFactory.createStackedBarChart(
			ConfigurationOptions.getProjectName(), "", "%", data, 
			PlotOrientation.HORIZONTAL, true, false, false
		));
		
		CategoryPlot plot = getChart().getCategoryPlot();
		//plot.setSeriesPaint(new Paint[] { Color.yellow, Color.green });
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesPaint(0, Color.yellow);
		renderer.setSeriesPaint(1, Color.green);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setTickUnit(new NumberTickUnit(20.0, new DecimalFormat("0")));
		rangeAxis.setUpperBound(100.0);
		
		LegendTitle legend = getChart().getLegend();
		legend.setPosition(RectangleEdge.TOP);
		
		createChart();
		saveChart(450, 19 * content.getAuthors().size() + 110, "activity.png");
	}
}
