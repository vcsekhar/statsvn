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
    
	$RCSfile: CommandLineParser.java,v $
	Created on $Date: 2005/03/20 19:12:25 $ 
*/
package net.sf.statcvs.output;

import java.util.ArrayList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Takes a command line, like given to the {@link net.sf.statcvs.Main#main} method,
 * and turns it into a {@link ConfigurationOptions} object.
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: CommandLineParser.java,v 1.16 2005/03/20 19:12:25 squig Exp $
 */
public class CommandLineParser {

	private String[] argsArray;
	private List args = new ArrayList();
	private int argCount = 0;
	private boolean setCacheDir = false;

	/**
	 * Constructor for CommandLineParser
	 * 
	 * @param args the command line parameters
	 */
	public CommandLineParser(String[] args) {
		argsArray = args;
	}
	
	/**
	 * Parses the command line and sets the options (as static
	 * fields in {@link ConfigurationOptions}).
	 * 
	 * @throws ConfigurationException if errors are present on the command line
	 */
	public void parse() throws ConfigurationException {
		for (int i = 0; i < argsArray.length; i++) {
			args.add(argsArray[i]);
		}
		while (!args.isEmpty()) {
			String currentArg = popNextArg();
			if (currentArg.startsWith("-")) {
				parseSwitch(currentArg.substring(1));
			} else {
				parseArgument(currentArg);
			}
		}
		// this creates the default cache dir if it has not been set up yet
		if (!setCacheDir) {
			ConfigurationOptions.setCacheDirToDefault();
		}
		checkForRequiredArgs();
	}

	private String popNextArg() {
		return (String) args.remove(0);
	}

	private void parseSwitch(String switchName) throws ConfigurationException {
		String s = switchName.toLowerCase();
		if (s.equals("css")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -css");
			}
			ConfigurationOptions.setCssFile(popNextArg());
		} else if (s.equals("output-dir")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -output-dir");
			}
			ConfigurationOptions.setOutputDir(popNextArg());
		} else if (s.equals("verbose")) {
			ConfigurationOptions.setVerboseLogging();
		} else if (s.equals("debug")) {
			ConfigurationOptions.setDebugLogging();
		} else if (s.equals("notes")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -notes");
			}
			ConfigurationOptions.setNotesFile(popNextArg());
		} else if (s.equals("viewcvs") || s.equals("viewvc")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -viewvc");
			}
			ConfigurationOptions.setViewVcURL(popNextArg());
		} else if (s.equals("cvsweb")) {
//			if (args.isEmpty()) {
//				throw new ConfigurationException("Missing argument for -cvsweb");
//			}
//			ConfigurationOptions.setCvswebURL(popNextArg());
            popNextArg();
            throw new ConfigurationException("CVSweb is not supported by StatSVN");
		} else if (s.equals("chora")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -chora");
			}
			ConfigurationOptions.setChoraURL(popNextArg());
		} else if (s.equals("include")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -include");
			}
			ConfigurationOptions.setIncludePattern(popNextArg());
		} else if (s.equals("exclude")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -exclude");
			}
			ConfigurationOptions.setExcludePattern(popNextArg());
		} else if (s.equals("title")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -title");
			}
			ConfigurationOptions.setProjectName(popNextArg());
		} else if (s.equals("tags")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -tags");
			}
			ConfigurationOptions.setSymbolicNamesPattern(popNextArg());
		} else if (s.equals("cache-dir")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -cache-dir");
			}
			ConfigurationOptions.setCacheDir(popNextArg());
			setCacheDir = true;
		} else if (s.equals("username")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -username");
			}
			ConfigurationOptions.setSvnUsername(popNextArg());
		} else if (s.equals("password")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -password");
			}
			ConfigurationOptions.setSvnPassword(popNextArg());
		} else if (s.equals("bugzilla")) {
			if (args.isEmpty()) {
				throw new ConfigurationException("Missing argument for -bugzilla");
			}
			ConfigurationOptions.setBugzillaUrl(popNextArg());
		} else {
			// exception path - option was not recognized above
			throw new ConfigurationException("Unrecognized option -" + s);
		}
	}
	
	private void parseArgument(String arg) throws ConfigurationException {
		argCount++;
		switch (argCount) {
			case 1:
				ConfigurationOptions.setLogFileName(arg);
				break;
			case 2:
				ConfigurationOptions.setCheckedOutDirectory(arg);
				break;
			default:
				throw new ConfigurationException("Too many arguments");
		}
	}

	private void checkForRequiredArgs() throws ConfigurationException {
		switch (argCount) {
			case 0:
				throw new ConfigurationException("Not enough arguments - <logfile> is missing");
			case 1:
				throw new ConfigurationException("Not enough arguments - <directory> is missing");
		}
	}
}