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
    
	$RCSfile: AuthorsForDirectoryTableReport.java,v $
	$Date: 2003/12/17 23:56:31 $
*/
package net.sf.statsvn.reports;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.model.Directory;
import net.sf.statsvn.reportmodel.AuthorColumn;
import net.sf.statsvn.reportmodel.Table;

/**
 * Table report which creates a table containing the names of the
 * authors who have committed changes to a certain directory,
 * and their LOC contributions and number of changes in that directory.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: AuthorsForDirectoryTableReport.java,v 1.3 2003/12/17 23:56:31 cyganiak Exp $
 */
public class AuthorsForDirectoryTableReport extends AbstractLocTableReport 
		implements TableReport {

	private Directory directory;
	private Table table = null;

	/**
	 * Creates a table report containing authors who have committed
	 * changes to a specified directory, their number of changes
	 * and LOC contributions.
	 * @param content the version control source data
	 * @param directory a directory
	 */
	public AuthorsForDirectoryTableReport(final Repository content,
			final Directory directory) {
		super(content);
		this.directory = directory;
	}
	
	/**
	 * @see net.sf.statsvn.reports.TableReport#calculate()
	 */
	public void calculate() {
		calculateChangesAndLinesPerAuthor(directory.getRevisions());
		table = createChangesAndLinesTable(
				new AuthorColumn(),
				SORT_BY_LINES,
				Messages.getString("AUTHORS_TABLE_FOR_DIRECTORY_SUMMARY"));
	}

	/**
	 * @see net.sf.statsvn.reports.TableReport#getTable()
	 */
	public Table getTable() {
		return table;
	}
}