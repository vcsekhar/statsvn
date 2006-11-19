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
    
	$RCSfile: StatCvsTask.java,v $
	$Date: 2005/03/24 00:19:51 $ 
*/
package net.sf.statsvn.ant;

import java.io.IOException;

import net.sf.statsvn.Main;
import net.sf.statsvn.input.EmptyRepositoryException;
import net.sf.statsvn.input.LogSyntaxException;
import net.sf.statsvn.output.ConfigurationException;
import net.sf.statsvn.output.ConfigurationOptions;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
/**
 * Ant task for running statcvs. 
 * 
 * @author Andy Glover
 * @author Richard Cyganiak
 */
public class StatCvsTask extends Task {
	private String title;
	private String cvsLogFile;
	private String pDir;
	private String outputDirectory;
	private String cacheDirectory;
	private String cssFile;
	private String notesFile;
	private String viewcvsUrl;
	private String choraUrl;
	private String includeFiles = null;
	private String excludeFiles = null;
    private String tags;
	
	/**
	 * Constructor for StatCvsTask.
	 */
	public StatCvsTask() {
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
		} catch (final ConfigurationException e) {
			throw new BuildException(e.getMessage());
		} catch (final IOException e) {
			throw new BuildException(e.getMessage());
		} catch (final LogSyntaxException e) {
			throw new BuildException(e.getMessage());
		} catch (final EmptyRepositoryException e) {
			throw new BuildException("No revisions in the log!");
		}
	}
	
	/**
	 * method initializes the ConfigurationOptions object with
	 * received values. 
	 */
	private void initProperties() throws ConfigurationException {

		// required params
		ConfigurationOptions.setLogFileName(this.cvsLogFile);
		ConfigurationOptions.setCheckedOutDirectory(this.pDir);

		// optional params
		if (this.title != null) {
			ConfigurationOptions.setProjectName(this.title);
		}
		if (this.outputDirectory != null) {
			ConfigurationOptions.setOutputDir(this.outputDirectory);
		}
		if (this.cacheDirectory != null) {
			ConfigurationOptions.setCacheDir(this.cacheDirectory);
		}
		if (cssFile != null) {
			ConfigurationOptions.setCssFile(this.cssFile);
		}
		if (notesFile != null) {
			ConfigurationOptions.setNotesFile(this.notesFile);
		}
		if (viewcvsUrl != null) {
			ConfigurationOptions.setViewVcURL(this.viewcvsUrl);
		}
		if (choraUrl != null) {
			ConfigurationOptions.setChoraURL(this.choraUrl);
		}
		if (includeFiles != null) {
			ConfigurationOptions.setIncludePattern(this.includeFiles);
		}
		if (excludeFiles != null) {
			ConfigurationOptions.setExcludePattern(this.excludeFiles);
		}
        if (tags != null) {
            ConfigurationOptions.setSymbolicNamesPattern(this.tags);
        }
	}

	/**
	 * @param title String representing the title to be used in the reports
	 */
	public void setTitle(final String title) {
		this.title = title;
	}
	
	/**
	 * @param cvsLogFile String representing the cvs log file
	 */
	public void setCvsLogFile(final String logFile) {
		this.cvsLogFile = logFile;
	}
	
	/**
	 * @param modDir String representing the directory containing the CVS project
	 */
	public void setProjectDirectory(final String modDir) {
		this.pDir = modDir;
	}
	
	/**
	 * @param outputDirectory String representing the output directory of the report
	 */
	public void setOutputDirectory(final String outDir) {
		this.outputDirectory = outDir;
	}
	
	/**
	 * @param cacheDirectory String representing the cache directory of the program
	 */
	public void setCacheDirectory(final String cacheDir) {
		this.cacheDirectory = cacheDir;
	}
	
	/**
	 * @param cssFile String representing the CSS file to use for the report
	 */
	public void setCssFile(final String cssFile) {
		this.cssFile = cssFile;
	}
	
	/**
	 * @param notesFile String representing the notes file to includeFiles on
	 * the report's index page
	 */
	public void setNotesFile(final String notesFile) {
		this.notesFile = notesFile;
	}
	
	/**
	 * @param viewcvsUrl String representing the URL of a ViewCVS installation
	 */
	public void setViewcvsURL(final String viewcvs) {
		this.viewcvsUrl = viewcvs;
	}

	/**
	 * @param choraUrl String representing the URL of a Chora installation
	 */
	public void setChoraURL(final String chora) {
		this.choraUrl = chora;
	}
	
	/**
	 * Specifies files to includeFiles in the analysis.
	 * @param includeFiles a list of Ant-style wildcard patterns, delimited by : or ;
	 * @see net.sf.statsvn.util.FilePatternMatcher
	 */
	public void setIncludeFiles(final String include) {
		this.includeFiles = include;
	}
	
	/**
	 * Specifies files to excludeFiles from the analysis.
	 * @param excludeFiles a list of Ant-style wildcard patterns, delimited by : or ;
	 * @see net.sf.statsvn.util.FilePatternMatcher
	 */
	public void setExcludeFiles(final String exclude) {
		this.excludeFiles = exclude;
	}

    /**
     * Specifies regular expression to includeFiles tag to lines
     * of code graph.
     * @param tags regular expression to included tags names.
     */
    public void setTags(final String tags) {
        this.tags = tags;
    }
}
