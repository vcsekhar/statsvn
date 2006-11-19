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
    
	$RCSfile: DirectoryImpl.java,v $
	$Date: 2004/02/19 23:15:45 $
*/
package net.sf.statsvn.model;

/**
 * A concrete subdirectory in a directory tree. To create an instance of
 * this class, call {@link Directory#createSubdirectory}.
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @version $Id: DirectoryImpl.java,v 1.4 2004/02/19 23:15:45 cyganiak Exp $
 */
class DirectoryImpl extends Directory {
	private Directory parent;
	private String name;

	/**
	 * Use {@link Directory#createSubdirectory} to create instances of
	 * this class!
	 * Creates a new <tt>Directory</tt> with the given parent and name
	 * @param parent the parent directory
	 * @param name the directory's name without path or slashes
	 */
	DirectoryImpl(final Directory parent, final String name) {
		this.parent = parent;
		this.name = name;
	}

	/**
	 * @see net.sf.statsvn.model.Directory#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see net.sf.statsvn.model.Directory#getPath()
	 */
	public String getPath() {
		return parent.getPath() + name + "/";
	}

	/**
	 * @see net.sf.statsvn.model.Directory#getParent()
	 */
	public Directory getParent() {
		return parent;
	}

	/**
	 * @see net.sf.statsvn.model.Directory#isRoot()
	 */
	public boolean isRoot() {
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "directory " + getPath();
	}

	/**
	 * @see net.sf.statsvn.model.Directory#getDepth()
	 */
	public int getDepth() {
		return parent.getDepth() + 1;
	}
}