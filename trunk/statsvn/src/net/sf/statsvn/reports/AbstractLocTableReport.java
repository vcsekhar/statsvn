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
    
	$RCSfile: AbstractLocTableReport.java,v $ 
	Created on $Date: 2004/02/18 19:00:57 $ 
*/
package net.sf.statsvn.reports;

import java.util.Collection;
import java.util.Iterator;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.model.Revision;
import net.sf.statsvn.model.Directory;
import net.sf.statsvn.reportmodel.GenericColumn;
import net.sf.statsvn.reportmodel.IntegerColumn;
import net.sf.statsvn.reportmodel.RatioColumn;
import net.sf.statsvn.reportmodel.Table;
import net.sf.statsvn.util.IntegerMap;

/**
 * Convenience superclass for table reports related to authors and directories.
 * Contains methods to calculate some common stuff for these tables.
 * @author Lukasz Pekacki
 * @version $Id: AbstractLocTableReport.java,v 1.6 2004/02/18 19:00:57 cyganiak Exp $
 */
public abstract class AbstractLocTableReport {

	/**
	 * Sort the authors table by name
	 * */
	public static final int SORT_BY_NAME = 0;

	/**
	 * Sort the authors table by lines of code
	 * */
	public static final int SORT_BY_LINES = 1;

	private Repository content;

	private final IntegerMap changesMap = new IntegerMap();
	private final IntegerMap linesMap = new IntegerMap();
	
	/**
     * Constructor
	 * @param content render table on specified content
	 */
	public AbstractLocTableReport(final Repository content) {
		this.content = content;
	}

	protected void calculateChangesAndLinesPerAuthor(final Collection revs) {
		final Iterator it = revs.iterator();
		while (it.hasNext()) {
			final Revision rev = (Revision) it.next();
			if (rev.getAuthor() == null) {
				continue;
			}
			changesMap.addInt(rev.getAuthor(), 1);
			linesMap.addInt(rev.getAuthor(), rev.getNewLines()); 
		}
	}

	protected void calculateChangesAndLinesPerDirectory(final Collection revisions) {
		final Iterator it = revisions.iterator();
		while (it.hasNext()) {
			final Revision rev = (Revision) it.next();
			final Directory dir = rev.getFile().getDirectory();
			changesMap.addInt(dir, 1);
			linesMap.addInt(dir, rev.getNewLines()); 
		}
	}

	protected Table createChangesAndLinesTable(
			final GenericColumn keys, final int sortedBy, final String summary) {

		final Table result = new Table(summary);
		final IntegerColumn changes =
				new IntegerColumn(Messages.getString("COLUMN_CHANGES"));
		final IntegerColumn linesOfCode =
				new IntegerColumn(Messages.getString("COLUMN_LOC"));
		final RatioColumn linesPerChange = new RatioColumn(
				Messages.getString("COLUMN_LOC_PER_CHANGE"),
				linesOfCode,
				changes);
		keys.setTotal(Messages.getString("TOTALS"));
		changes.setShowPercentages(true);
		linesOfCode.setShowPercentages(true);
		result.addColumn(keys);
		result.addColumn(changes);
		result.addColumn(linesOfCode);
		result.addColumn(linesPerChange);
		result.setKeysInFirstColumn(true);
		
		Iterator it;
		if (sortedBy == SORT_BY_NAME) {
			it = linesMap.iteratorSortedByKey();
		} else {
			it = linesMap.iteratorSortedByValueReverse();
		}
		while (it.hasNext()) {
			final Object key = it.next();
			keys.addValue(key);
			changes.addValue(changesMap.get(key));
			linesOfCode.addValue(linesMap.get(key));
		}
		if (result.getRowCount() > 1) {
			result.setShowTotals(true);
		}
		return result;
	}

	protected Repository getContent() {
		return content;
	}

	protected IntegerMap getChangesMap() {
		return changesMap;
	}

	protected IntegerMap getLinesMap() {
		return linesMap;
	}

}