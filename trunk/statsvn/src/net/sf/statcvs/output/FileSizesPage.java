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
    
	$RCSfile: FileSizesPage.java,v $ 
	Created on $Date: 2003/06/05 10:51:54 $ 
*/

package net.sf.statcvs.output;

import java.io.IOException;
import java.util.Iterator;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.renderer.TableRenderer;
import net.sf.statcvs.reports.FilesWithMostRevisionsTableReport;
import net.sf.statcvs.reports.LargestFilesTableReport;
import net.sf.statcvs.reports.TableReport;

/**
 * This page displays the timeline of file count, a table with the largest
 * files, and a table with the files having most revisions
 * 
 * @author anja
 */
public class FileSizesPage extends HTMLPage {
	private static final int MAX_LARGEST_FILES = 20;
	private static final int MAX_FILES_WITH_MOST_REVISIONS = 20;

	/**
	 * @see net.sf.statcvs.output.HTMLPage#HTMLPage(Repository)
	 */
	public FileSizesPage(final Repository content, final OutputRenderer renderer) throws IOException {
		super(content, renderer);
		setFileName("file_sizes" + renderer.getFileExtension());
		setPageName(Messages.getString("FILE_SIZES_TITLE"));
		createPage();
	}

	protected void printBody() throws IOException {
		printBackLink();
		print(startSection2(Messages.getString("FILE_COUNT_TITLE")));
		print(getFileCountImage());
        print(endSection2());
		print(startSection2(Messages.getString("AVERAGE_FILE_SIZE_TITLE")));
		print(getFileSizeImage());
        print(endSection2());
		print(getLargestFilesSection());
		print(getFilesWithMostRevisionsSection());
	}

	private String getLargestFilesSection() {
		final StringBuffer result = new StringBuffer();
		result.append(startSection2(Messages.getString("LARGEST_FILES_TITLE")));
		final TableReport report = new LargestFilesTableReport(
				getContent().getFiles(), 
				MAX_LARGEST_FILES);
		report.calculate();
		result.append(new TableRenderer(report.getTable(), getRenderer()).getRenderedTable());
        result.append(endSection2());
		return result.toString();
	}

	private String getFilesWithMostRevisionsSection() {
		final StringBuffer result = new StringBuffer();
		result.append(startSection2(Messages.getString("FILES_WITH_MOST_REVISIONS_TITLE")));
		final TableReport report = new FilesWithMostRevisionsTableReport(
				getContent().getFiles(), 
				MAX_FILES_WITH_MOST_REVISIONS);
		report.calculate();
		result.append(new TableRenderer(report.getTable(), getRenderer()).getRenderedTable());
        result.append(endSection2());
		return result.toString();
	}

	private String getFileCountImage() {
		final int fileCount = getCurrentFileCount();
		final StringBuffer result = new StringBuffer(img("file_count.png", 640, 480)).append(br());
		result.append(strong(Messages.getString("TOTAL_FILE_COUNT") + ": ")).append(fileCount);
		result.append(" (").append(HTMLTagger.getDateAndTime(getContent().getLastDate())).append(")");
		return p(result.toString());
	}
	
	private String getFileSizeImage() {
		return p(img("file_size.png", 640, 480));
	}
	
	private int getCurrentFileCount() {
		int result = 0;
		final Iterator fileIt = getContent().getFiles().iterator();
		while (fileIt.hasNext()) {
			final VersionedFile file = (VersionedFile) fileIt.next();
			if (!file.isDead()) {
				result++;
			}
		}
		return result;
	}
}