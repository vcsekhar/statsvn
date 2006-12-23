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
import net.sf.statsvn.Main;
import net.sf.statsvn.output.SvnConfigurationOptions;

import org.apache.tools.ant.BuildException;

/**
 * Ant task for running StatSVN.
 * 
 * @author Andy Glover
 * @author Richard Cyganiak
 * @author Benoit Xhenseval
 * @author Jason Kealey
 */
public class StatSvnTask extends StatCvsTask {
	private String cacheDirectory;

	private String svnPassword;

	private String svnUsername;

	private int numberSvnDiffThreads;

	private long thresholdInMsToUseConcurrency;

	/**
	 * Constructor for StatSvnTask.
	 */
	public StatSvnTask() {
		super();
	}

	/**
	 * Runs the task
	 * 
	 * @throws BuildException
	 *             if an IO Error occurs
	 */
	public void execute() throws BuildException {
		try {
			this.initProperties();

			Main.init();

			// main usually builds checks the command line here but we will skip
			// that step as it is done in initProperties

			Main.generate();
		} catch (Exception e) {
			SvnConfigurationOptions.getTaskLogger().log(Main.printStackTrace(e));
		}
	}

	/**
	 * method initializes the ConfigurationOptions object with received values.
	 */
	protected void initProperties() throws ConfigurationException {
		super.initProperties();
		if (this.cacheDirectory != null) {
			SvnConfigurationOptions.setCacheDir(this.cacheDirectory);
		}
		if (this.svnPassword != null) {
			SvnConfigurationOptions.setSvnPassword(this.svnPassword);
		}
		if (this.svnUsername != null) {
			SvnConfigurationOptions.setSvnUsername(this.svnUsername);
		}
		if (this.numberSvnDiffThreads != 0) {
			SvnConfigurationOptions.setNumberSvnDiffThreads(this.numberSvnDiffThreads);
		}
		if (this.thresholdInMsToUseConcurrency != 0) {
			SvnConfigurationOptions.setThresholdInMsToUseConcurrency(this.thresholdInMsToUseConcurrency);
		}

		SvnConfigurationOptions.setTaskLogger(new AntTaskLogger(this));
	}

	/**
	 * @param cacheDirectory
	 *            String representing the cache directory of the program
	 */
	public void setCacheDirectory(final String cacheDir) {
		this.cacheDirectory = cacheDir;
	}

	/**
	 * @param svnPassword
	 *            The svnPassword to set.
	 */
	public void setPassword(final String svnPassword) {
		this.svnPassword = svnPassword;
	}

	/**
	 * @param svnUsername
	 *            The svnUsername to set.
	 */
	public void setUsername(final String svnUsername) {
		this.svnUsername = svnUsername;
	}

	/**
	 * @param numberSvnDiffThreads
	 *            the numberSvnDiffThreads to set
	 */
	public void setThreads(int numberSvnDiffThreads) {
		this.numberSvnDiffThreads = numberSvnDiffThreads;
	}

	/**
	 * @param thresholdInMsToUseConcurrency
	 *            the thresholdInMsToUseConcurrency to set
	 */
	public void setConcurrencyThreshold(long thresholdToUseConcurrency) {
		this.thresholdInMsToUseConcurrency = thresholdToUseConcurrency;
	}

}
