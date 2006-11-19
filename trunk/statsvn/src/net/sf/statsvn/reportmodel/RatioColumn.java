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
    
	$RCSfile: RatioColumn.java,v $
	$Date: 2003/04/19 13:16:22 $
*/
package net.sf.statsvn.reportmodel;

import net.sf.statsvn.renderer.TableCellRenderer;

/**
 * A column showing the ratio between two {@link IntegerColumn}s.
 * The two columns do not have to be shown in the table.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: RatioColumn.java,v 1.1 2003/04/19 13:16:22 cyganiak Exp $
 */
public class RatioColumn extends Column {

	private String title;
	private IntegerColumn col1;
	private IntegerColumn col2;

	/**
	 * Creates a new <tt>RatioColumn</tt>, which contains the ratio
	 * between col1 and col2
	 * @param title the title for the column
	 * @param col1 the first column
	 * @param col2 the second column
	 */
	public RatioColumn(final String title, final IntegerColumn col1, final IntegerColumn col2) {
		this.title = title;
		this.col1 = col1;
		this.col2 = col2;
	}

	/**
	 * @see net.sf.statsvn.reportmodel.Column#getRows()
	 */
	public int getRows() {
		return col1.getRows();
	}

	/**
	 * @see net.sf.statsvn.reportmodel.Column#renderHead(net.sf.statsvn.renderer.TableCellRenderer)
	 */
	public void renderHead(final TableCellRenderer renderer) {
		renderer.renderCell(title);
	}

	/**
	 * @see net.sf.statsvn.reportmodel.Column#renderCell
	 */
	public void renderCell(final int rowIndex, final TableCellRenderer renderer) {
		renderer.renderCell(getRatio(col1.getValue(rowIndex), col2.getValue(rowIndex)));		
	}

	/**
	 * @see net.sf.statsvn.reportmodel.Column#renderTotal(net.sf.statsvn.renderer.TableCellRenderer)
	 */
	public void renderTotal(final TableCellRenderer renderer) {
		renderer.renderCell(getRatio(col1.getSum(), col2.getSum()));		
	}

	private String getRatio(final int val1, final int val2) {
		if (val2 == 0) {
			return "-";
		}
		final int ratioTimes10 = (val1 * 10) / val2;
		final double ratio = (double) ratioTimes10 / 10;
		return Double.toString(ratio);
	}
}
