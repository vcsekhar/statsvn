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
    
	$RCSfile: StatSvnTask.java,v $
	$Date: 2005/03/24 00:19:51 $ 
*/
package net.sf.statsvn.ant;

import net.sf.statcvs.ant.StatCvsTask;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statsvn.output.SvnConfigurationOptions;
/**
 * Ant task for running statcvs. 
 * 
 * @author Andy Glover
 * @author Richard Cyganiak
 * @author Benoit Xhenseval
 */
public class StatSvnTask extends StatCvsTask {
	private String cacheDirectory;
	
	/**
	 * Constructor for StatSvnTask.
	 */
	public StatSvnTask() {
		super();
	}

	/**
	 * method initializes the ConfigurationOptions object with
	 * received values. 
	 */
	protected void initProperties() throws ConfigurationException {
		super.initProperties();
		if (this.cacheDirectory != null) {
			SvnConfigurationOptions.setCacheDir(this.cacheDirectory);
		}
	}

	/**
	 * @param cacheDirectory String representing the cache directory of the program
	 */
	public void setCacheDirectory(final String cacheDir) {
		this.cacheDirectory = cacheDir;
	}
}
