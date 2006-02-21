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
    
	$RCSfile: CvswebIntegration.java,v $
	$Date: 2005/01/03 03:03:54 $ 
*/
package net.sf.statcvs.output;

import java.util.HashSet;
import java.util.Set;

import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.model.Directory;

/**
 * Integration of cvsweb
 *
 * @author Richard Cyganiak
 * @version $Id: CvswebIntegration.java,v 1.10 2005/01/03 03:03:54 cyganiak Exp $
 */
public class CvswebIntegration implements WebRepositoryIntegration {
	private String baseURL;
	private Set atticFileNames = new HashSet();
	private String postfix;
	
	/**
	 * @param baseURL base URL of the cvsweb installation
	 */
	public CvswebIntegration(String baseURL) {
		int i = baseURL.indexOf("?");
		if (i != -1) {
			this.postfix = baseURL.substring(i + 1);
			baseURL = baseURL.substring(0, i);		  
		}

		if (baseURL.endsWith("/")) {
			this.baseURL = baseURL.substring(0, baseURL.length() - 1);
		} else {
			this.baseURL = baseURL;
		}
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getName
	 */
	public String getName() {
		return "cvsweb";
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getDirectoryUrl
	 */
	public String getDirectoryUrl(Directory directory) {
		return baseURL + "/" + directory.getPath();
	}

	private String getFileUrl(VersionedFile file, String parameter) {
		String filename;
		if (isInAttic(file)) {
			String path = file.getDirectory().getPath();
			filename = "/" + path + "Attic/" + file.getFilename();

		} else {
			filename = "/" + file.getFilenameWithPath();
		}
		
		String append = parameter;
		if (this.postfix != null) {
			append += (append.length() > 0) ? "&" + this.postfix : "?" + this.postfix;
		}
		return this.baseURL + filename + append;
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getFileHistoryUrl
	 */
	public String getFileHistoryUrl(VersionedFile file) {
		return getFileUrl(file, "");
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getFileViewUrl(VersionedFile)
	 */
	public String getFileViewUrl(VersionedFile file) {
		return getFileUrl(file, "?rev=HEAD&content-type=text/vnd.viewcvs-markup");
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getFileViewUrl(VersionedFile)
	 */
	public String getFileViewUrl(Revision revision) {
		return getFileUrl(revision.getFile(), "?rev="
				+ revision.getRevisionNumber() + "&content-type=text/vnd.viewcvs-markup");
	}

	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#getDiffUrl
	 */
	public String getDiffUrl(Revision oldRevision, Revision newRevision) {
		if (!oldRevision.getFile().equals(newRevision.getFile())) {
			throw new IllegalArgumentException("revisions must be of the same file");
		}
		return getFileUrl(oldRevision.getFile(),
				".diff?r1=" + oldRevision.getRevisionNumber()
				+ "&r2=" + newRevision.getRevisionNumber()
				+ "&f=h");
	}
	
	/**
	 * @see net.sf.statcvs.output.WebRepositoryIntegration#setAtticFileNames(java.util.Set)
	 */
	public void setAtticFileNames(Set atticFileNames) {
		this.atticFileNames = atticFileNames;
	}

	private boolean isInAttic(VersionedFile file) {
		return atticFileNames.contains(file.getFilenameWithPath());
	}
}
