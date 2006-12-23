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

import java.io.IOException;

import net.sf.statcvs.ant.StatCvsTask;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statsvn.Main;
import net.sf.statsvn.output.SvnConfigurationOptions;

import org.apache.tools.ant.BuildException;
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
	 * Runs the task
	 * @throws BuildException if an IO Error occurs
	 */
	public void execute() throws BuildException {
		try {
			this.initProperties();
			Main.generateDefaultHTMLSuite();
		} catch (ConfigurationException e) {
			throw new BuildException(e.getMessage());
		} catch (IOException e) {
			throw new BuildException(e.getMessage());
		} catch (LogSyntaxException e) {
			throw new BuildException(e.getMessage());
		}
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
		SvnConfigurationOptions.setTaskLogger(new AntTaskLogger(this));
	}

	/**
	 * @param cacheDirectory String representing the cache directory of the program
	 */
	public void setCacheDirectory(final String cacheDir) {
		this.cacheDirectory = cacheDir;
	}
}
