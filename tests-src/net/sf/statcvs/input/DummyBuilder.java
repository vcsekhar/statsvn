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
*/
package net.sf.statcvs.input;

import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Directory;

/**
 * Test dummy of {@link Builder} for use in
 * {@link FileBuilderTest}. Returns authors, directories
 * and LOC counts for a fixed set of arguments.
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @version $Id: DummyBuilder.java,v 1.3 2004/02/19 23:15:44 cyganiak Exp $
 */
public class DummyBuilder extends Builder {
	private Author author1 = new Author("author1");
	private Author author2 = new Author("author2");
	private Author author3 = new Author("author3");
	private Directory root = Directory.createRoot();
	private Directory dir1 = root.createSubdirectory("dir1");
	private Directory dir2 = root.createSubdirectory("dir2");
	private Directory subdir = dir1.createSubdirectory("subdir");

	public DummyBuilder() {
		super(null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.Builder#getAuthor(java.lang.String)
	 */
	public Author getAuthor(String name) {
		if ("author1".equals(name)) {
			return author1;
		} else if ("author2".equals(name)) {
			return author2;
		} else if ("author3".equals(name)) {
			return author3;
		}
		throw new IllegalArgumentException(name);
	}

	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.Builder#getDirectory(java.lang.String)
	 */
	public Directory getDirectory(String filename) {
		if ("file".equals(filename) || "nolinecount".equals(filename)) {
			return root;
		} else if ("dir1/file".equals(filename)) {
			return dir1;
		} else if ("dir2/file".equals(filename)) {
			return dir2;
		} else if ("dir1/subdir/file".equals(filename)) {
			return subdir;
		}
		throw new IllegalArgumentException(filename);
	}

	/* (non-Javadoc)
	 * @see net.sf.statcvs.input.Builder#getLOC(java.lang.String)
	 */
	public int getLOC(String filename) throws NoLineCountException {
		if ("file".equals(filename)) {
			return 100;
		} else if ("nolinecount".equals(filename)) {
			throw new NoLineCountException();
		} else if ("dir1/file".equals(filename)) {
			return 10;
		} else if ("dir2/file".equals(filename)) {
			return 20;
		} else if ("dir1/subdir/file".equals(filename)) {
			return 500;
		}
		throw new IllegalArgumentException(filename);
	}

}
