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
    
	$RCSfile: Column.java,v $
	$Date: 2003/06/22 19:17:44 $
*/
package net.sf.statsvn.reportmodel;

import net.sf.statsvn.renderer.TableCellRenderer;

/**
 * An abstract base class for table columns. Concrete Sublasses implement
 * different behaviour for different data types.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: Column.java,v 1.3 2003/06/22 19:17:44 cyganiak Exp $
 */
public abstract class Column {

	/**
	 * Return number of rows that have been added to this column
	 * @return number of rows that have been added to this column
	 */
	public abstract int getRows();
	
	/**
	 * Renders the head of the column into a <tt>TableCellRenderer</tt> by
	 * calling one of its
	 * {@link net.sf.statsvn.renderer.TableCellRenderer#renderCell} methods
	 * @param renderer the TableCellRenderer to use
	 * TODO: this is probably unnecessary; better add a getTitle method
	 */
	public abstract void renderHead(TableCellRenderer renderer);
	
	/**
	 * Renders a row of the column into a <tt>TableCellRenderer</tt> by
	 * calling one of its
	 * {@link net.sf.statsvn.renderer.TableCellRenderer#renderCell} methods
	 * @param rowIndex the row number, starting at 0
	 * @param renderer the TableCellRenderer to use
	 */
	public abstract void renderCell(int rowIndex, TableCellRenderer renderer);
	
	/**
	 * Renders the footer of the column into a <tt>TableCellRenderer</tt> by
	 * calling one of its
	 * {@link net.sf.statsvn.renderer.TableCellRenderer#renderCell}
	 * methods. The footer usually contains some kind of total for the column.
	 * @param renderer the TableCellRenderer to use
	 */
	public abstract void renderTotal(TableCellRenderer renderer);
}