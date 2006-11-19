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
    
	$RCSfile: TopAuthorsTableReport.java,v $
	$Date: 2003/12/17 23:56:31 $
*/
package net.sf.statsvn.reports;

import java.util.Iterator;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Author;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.reportmodel.AuthorColumn;
import net.sf.statsvn.reportmodel.IntegerColumn;
import net.sf.statsvn.reportmodel.Table;

/**
 * Table report which creates a table containing the names of the
 * top 10 authors and their LOC contributions.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: TopAuthorsTableReport.java,v 1.2 2003/12/17 23:56:31 cyganiak Exp $
 */
public class TopAuthorsTableReport extends AbstractLocTableReport 
		implements TableReport {

	private Table table = null;

	/**
	 * Creates a table report containing the top 10 authors and their
	 * LOC contributions
	 * @param content the version control source data
	 */
	public TopAuthorsTableReport(final Repository content) {
		super(content);
	}
	
	/**
	 * @see net.sf.statsvn.reports.TableReport#calculate()
	 */
	public void calculate() {
		String summary;
		if (getContent().getAuthors().size() > 10) {
			summary = Messages.getString("TOP_AUTHORS_TABLE_SUMMARY1");
		} else {
			summary = Messages.getString("TOP_AUTHORS_TABLE_SUMMARY2");
		}
		table = new Table(summary);
		final AuthorColumn authors = new AuthorColumn();
		final IntegerColumn linesOfCode =
				new IntegerColumn(Messages.getString("COLUMN_LOC"));
		linesOfCode.setShowPercentages(true);
		table.addColumn(authors);
		table.addColumn(linesOfCode);
		table.setKeysInFirstColumn(true);

		calculateChangesAndLinesPerAuthor(getContent().getRevisions());
		final Iterator it = getLinesMap().iteratorSortedByValueReverse();
		for (int i = 0; i < 10; i++) {
			if (!it.hasNext()) {
				break;
			}
			final Author author = (Author) it.next();
			authors.addValue(author);
			linesOfCode.addValue(getLinesMap().get(author));
		}
		linesOfCode.setSum(getLinesMap().sum());
	}

	/**
	 * @see net.sf.statsvn.reports.TableReport#getTable()
	 */
	public Table getTable() {
		return table;
	}
}
