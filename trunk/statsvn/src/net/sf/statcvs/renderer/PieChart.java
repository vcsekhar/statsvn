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
    
	$RCSfile: PieChart.java,v $
	$Date: 2004/10/12 07:22:42 $ 
*/
package net.sf.statcvs.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.HTMLOutput;
import net.sf.statcvs.util.IntegerMap;
import net.sf.statcvs.util.OutputUtils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Class for producing module size charts
 * @author jentzsch
 * @version $Id: PieChart.java,v 1.43 2004/10/12 07:22:42 cyganiak Exp $
 */
public class PieChart extends Chart {

	private static final int SLICE_MIN_PERCENT = 5;
	/**
	 * Filter method by repository
	 */
	public static final int FILTERED_BY_REPOSITORY = 0;
	/**
	 * Filter method by username
	 */
	public static final int FILTERED_BY_USER = 1;

	/**
	 * creates an 3D Pie Chart
	 * @param content Repository
	 * @param title chart title
	 * @param fileName fileName for chart
	 * @param author author for this pie chart 
	 * @param filter filter options (users / whole repository)
	 */
	public PieChart(
		Repository content,
		String title,
		String fileName,
		Author author,
		int filter) {
		super(title, fileName);

		DefaultPieDataset data = new DefaultPieDataset();

		List directories;
		if (filter == FILTERED_BY_USER) {
			directories = new ArrayList(author.getDirectories());
		} else {
			directories = new ArrayList(content.getDirectories());
		}
		Collections.sort(directories);

		IntegerMap dirSizes = new IntegerMap();
		Iterator it = directories.iterator();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			if (filter == FILTERED_BY_USER) {
				dirSizes.addInt(dir, getLineValueOfAuthorInDir(author, dir));
			} else {	// file sizes in entire repository
				dirSizes.addInt(dir, getLOCInDir(dir));
			}
		}

		int otherSum = 0;
		List colors = new ArrayList();
		List outlines = new ArrayList();
		it = dirSizes.iteratorSortedByValue();
		while (it.hasNext()) {
			Directory dir = (Directory) it.next();
			if (dirSizes.getPercent(dir) >= SLICE_MIN_PERCENT) {
				String dirName = dir.isRoot() ? "/" : dir.getPath();
				data.setValue(dirName, dirSizes.getInteger(dir));
				colors.add(OutputUtils.getStringColor(dirName));
				outlines.add(Color.BLACK);
			} else {
				otherSum += dirSizes.get(dir);
			}
		}
		data.setValue(Messages.getString("PIE_MODSIZE_OTHER"), new Integer(otherSum));
		colors.add(Color.GRAY);
		outlines.add(Color.BLACK);

		setChart(ChartFactory.createPieChart(
			ConfigurationOptions.getProjectName(), data, false, false, false
		));
		
		//Plot plot = getChart().getPlot();
		//plot.setSeriesPaint((Color[]) colors.toArray(new Color[colors.size()]));
		//plot.setSeriesOutlinePaint((Color[]) outlines.toArray(new Color[colors.size()]));
		
		PiePlot plot = (PiePlot) getChart().getPlot();
		
		plot.setShadowPaint(null);
		plot.setLabelShadowPaint(null);
		plot.setLabelOutlinePaint(Color.LIGHT_GRAY);
		plot.setForegroundAlpha(0.8f);

		for (int i = 0; i < colors.size(); i++) {
			plot.setSectionPaint(i, (Paint) colors.get(i));   
		}
		for (int j = 0; j < outlines.size(); j++) {
			plot.setSectionOutlinePaint(j, (Paint) outlines.get(j));   
		}
		
		createChart();
		saveChart(HTMLOutput.IMAGE_WIDTH, HTMLOutput.IMAGE_HEIGHT); 
	}
	
	private int getLineValueOfAuthorInDir(Author author, Directory dir) {
		int result = 0;
		Iterator it = dir.getRevisions().iterator();
		while (it.hasNext()) {
			Revision rev = (Revision) it.next();
			if (!author.equals(rev.getAuthor())) {
				continue;
			}
			result += rev.getNewLines();
		}
		return result;
	}
	
	private int getLOCInDir(Directory dir) {
		int result = 0;
		Iterator fileIt = dir.getFiles().iterator();
		while (fileIt.hasNext()) {
			VersionedFile element = (VersionedFile) fileIt.next();
			result += element.getCurrentLinesOfCode();
		}
		return result;
	}
}
