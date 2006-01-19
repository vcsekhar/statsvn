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
package net.sf.statcvs.ant;

import java.io.IOException;

import net.sf.statcvs.Main;
import net.sf.statcvs.input.EmptyRepositoryException;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;

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
	private String logFile;
	private String pDir;
	private String outDir;
	private String cssFile;
	private String notesFile;
	private String viewcvs;
	private String cvsweb;
	private String chora;
	private String include = null;
	private String exclude = null;
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
		} catch (ConfigurationException e) {
			throw new BuildException(e.getMessage());
		} catch (IOException e) {
			throw new BuildException(e.getMessage());
		} catch (LogSyntaxException e) {
			throw new BuildException(e.getMessage());
		} catch (EmptyRepositoryException e) {
			throw new BuildException("No revisions in the log!");
		}
	}
	
	/**
	 * method initializes the ConfigurationOptions object with
	 * received values. 
	 */
	private void initProperties() throws ConfigurationException {

		// required params
		ConfigurationOptions.setLogFileName(this.logFile);
		ConfigurationOptions.setCheckedOutDirectory(this.pDir);

		// optional params
		if (this.title != null) {
			ConfigurationOptions.setProjectName(this.title);
		}
		if (this.outDir != null) {
			ConfigurationOptions.setOutputDir(this.outDir);
		}
		if (cssFile != null) {
			ConfigurationOptions.setCssFile(this.cssFile);
		}
		if (notesFile != null) {
			ConfigurationOptions.setNotesFile(this.notesFile);
		}
		if (viewcvs != null) {
			ConfigurationOptions.setViewCvsURL(this.viewcvs);
		}
		if (cvsweb != null) {
			ConfigurationOptions.setCvswebURL(this.cvsweb);
		}
		if (chora != null) {
			ConfigurationOptions.setChoraURL(this.chora);
		}
		if (include != null) {
			ConfigurationOptions.setIncludePattern(this.include);
		}
		if (exclude != null) {
			ConfigurationOptions.setExcludePattern(this.exclude);
		}
        if (tags != null) {
            ConfigurationOptions.setSymbolicNamesPattern(this.tags);
        }
	}

	/**
	 * @param title String representing the title to be used in the reports
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @param logFile String representing the cvs log file
	 */
	public void setCvsLogFile(String logFile) {
		this.logFile = logFile;
	}
	
	/**
	 * @param modDir String representing the directory containing the CVS project
	 */
	public void setProjectDirectory(String modDir) {
		this.pDir = modDir;
	}
	
	/**
	 * @param outDir String representing the output directory of the report
	 */
	public void setOutputDirectory(String outDir) {
		this.outDir = outDir;
	}
	
	/**
	 * @param cssFile String representing the CSS file to use for the report
	 */
	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}
	
	/**
	 * @param notesFile String representing the notes file to include on
	 * the report's index page
	 */
	public void setNotesFile(String notesFile) {
		this.notesFile = notesFile;
	}
	
	/**
	 * @param viewcvs String representing the URL of a ViewCVS installation
	 */
	public void setViewcvsURL(String viewcvs) {
		this.viewcvs = viewcvs;
	}

	/**
	 * @param cvsweb String representing the URL of a cvsweb installation
	 */
	public void setCvswebURL(String cvsweb) {
		this.cvsweb = cvsweb;
	}

	/**
	 * @param chora String representing the URL of a Chora installation
	 */
	public void setChoraURL(String chora) {
		this.chora = chora;
	}
	
	/**
	 * Specifies files to include in the analysis.
	 * @param include a list of Ant-style wildcard patterns, delimited by : or ;
	 * @see net.sf.statcvs.util.FilePatternMatcher
	 */
	public void setIncludeFiles(String include) {
		this.include = include;
	}
	
	/**
	 * Specifies files to exclude from the analysis.
	 * @param exclude a list of Ant-style wildcard patterns, delimited by : or ;
	 * @see net.sf.statcvs.util.FilePatternMatcher
	 */
	public void setExcludeFiles(String exclude) {
		this.exclude = exclude;
	}

    /**
     * Specifies regular expression to include tag to lines
     * of code graph.
     * @param tags regular expression to included tags names.
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
}
