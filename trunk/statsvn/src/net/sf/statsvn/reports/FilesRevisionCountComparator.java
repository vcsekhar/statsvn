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
    
	$RCSfile: FilesRevisionCountComparator.java,v $
	$Date: 2004/02/20 01:33:29 $
*/
package net.sf.statsvn.reports;

import java.util.Comparator;
import java.util.Iterator;

import net.sf.statsvn.model.VersionedFile;
import net.sf.statsvn.model.Revision;

/**
 * Compares two files according to their number of changes (revisions).
 * If two files have the same number of changes, the number of changed
 * lines of code is used.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: FilesRevisionCountComparator.java,v 1.3 2004/02/20 01:33:29 cyganiak Exp $
 */
public class FilesRevisionCountComparator implements Comparator {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final Object o1, final Object o2) {
		final VersionedFile file1 = (VersionedFile) o1;
		final VersionedFile file2 = (VersionedFile) o2;
		if (file1.getRevisions().size() < file2.getRevisions().size()) {
			return 2;
		} else if (file1.getRevisions().size() > file2.getRevisions().size()) {
			return -2;
		} else {
			int lines1 = 0;
			Iterator it = file1.getRevisions().iterator();
			while (it.hasNext()) {
				final Revision rev = (Revision) it.next();
				lines1 += rev.getNewLines();
			}
			int lines2 = 0;
			it = file2.getRevisions().iterator();
			while (it.hasNext()) {
				final Revision rev = (Revision) it.next();
				lines2 += rev.getNewLines();
			}
			if (lines1 < lines2) {
				return 1;
			} else if (lines1 > lines2) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
