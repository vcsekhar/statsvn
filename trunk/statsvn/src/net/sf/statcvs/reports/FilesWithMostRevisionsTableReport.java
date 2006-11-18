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
    
	$RCSfile: FilesWithMostRevisionsTableReport.java,v $
	$Date: 2004/02/20 01:33:29 $
*/
package net.sf.statcvs.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.reportmodel.FileColumn;
import net.sf.statcvs.reportmodel.IntegerColumn;
import net.sf.statcvs.reportmodel.Table;

/**
 * Table report for a table containing the files with most changes
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: FilesWithMostRevisionsTableReport.java,v 1.3 2004/02/20 01:33:29 cyganiak Exp $
 */
public class FilesWithMostRevisionsTableReport implements TableReport {
	private List files;
	private Table table;
	private int maxRows;

	/**
	 * Creates a table containing the files with most changes from a file list
	 * @param files a <tt>SortedSet</tt> of
	 *              {@link net.sf.statcvs.model.VersionedFile}s
	 * @param maxRows the maximum number of files displayed in the table 
	 */
	public FilesWithMostRevisionsTableReport(final SortedSet files, final int maxRows) {
		this.files = new ArrayList(files);
		this.maxRows = maxRows;
	}

	/**
	 * @see net.sf.statcvs.reports.TableReport#calculate()
	 */
	public void calculate() {
		Collections.sort(files, new FilesRevisionCountComparator());
		table = new Table(Messages.getString("FILES_WITH_MOST_REVISIONS_TABLE_SUMMARY"));
		table.setKeysInFirstColumn(true);
		final FileColumn filesCol = new FileColumn();
		filesCol.setWithIcon(true);
		final IntegerColumn locCol = new IntegerColumn(Messages.getString("COLUMN_CHANGES"));
		locCol.setShowPercentages(false);
		table.addColumn(filesCol);
		table.addColumn(locCol);
		int lines = 0;
		final Iterator it = files.iterator();
		while (it.hasNext() && lines < maxRows) {
			final VersionedFile file = (VersionedFile) it.next();
			filesCol.addValue(file);
			locCol.addValue(file.getRevisions().size());
			lines++;
		} 
	}

	/**
	 * @see net.sf.statcvs.reports.TableReport#getTable()
	 */
	public Table getTable() {
		return table;
	}
}
