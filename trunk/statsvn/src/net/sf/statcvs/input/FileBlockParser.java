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
    
	$RCSfile: FileBlockParser.java,v $ 
	Created on $Date: 2004/12/14 13:38:13 $ 
*/

package net.sf.statcvs.input;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.statcvs.util.CvsLogUtils;
import net.sf.statcvs.util.LookaheadReader;

/**
 * Parses the information of one file from a CVS logfile
 * {@link net.sf.statcvs.util.LookaheadReader}. A {@link Builder} must be
 * specified which constructs some representation of that file. The lookahead
 * reader must be positioned on the first line of the file's section in the
 * log ("RCS file: ...").
 * 
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @author Tammo van Lessen
 * @version $Id: FileBlockParser.java,v 1.46 2004/12/14 13:38:13 squig Exp $
 */
public class FileBlockParser {
	private static Logger logger = Logger.getLogger(FileBlockParser.class.getName());
	private LookaheadReader logReader;
	private SvnLogBuilder builder;
	private boolean isLogWithoutSymbolicNames = false;
	private boolean isFirstFile;
    private Map revBySymNames = new HashMap();

	/**
	 * Default Constructor FileBlockParser.
	 * @param logReader reader
	 * @param builder a <tt>Builder</tt> for the creation process
	 * @param isFirstFile Is this the first file of the log?
	 */
	public FileBlockParser(LookaheadReader logReader, SvnLogBuilder builder,
							  boolean isFirstFile) {
		this.logReader = logReader;
		this.builder = builder;
		this.isFirstFile = isFirstFile;
	}

	/**
	 * Parses one file from the input reader.
	 * 
	 * @throws LogSyntaxException on syntax error
	 * @throws IOException on read/write error
	 */
	public void parse() throws LogSyntaxException, IOException {
		String rcsFile = parseSingleLine(this.logReader.getCurrentLine(), "RCS file: ");
		String workingFile = parseSingleLine(this.logReader.nextLine(), "Working file: ");
		boolean isInAttic = CvsLogUtils.isInAttic(rcsFile, workingFile);
		requireLine(this.logReader.nextLine(), "head:");
		requireLine(this.logReader.nextLine(), "branch:");
		requireLine(this.logReader.nextLine(), "locks:");
		parseLocksAndAccessList();
		parseSymbolicNames();
		String keywordSubst = parseSingleLine(this.logReader.getCurrentLine(),
				"keyword substitution: ");
		boolean isBinary = false;
		try {
			isBinary = CvsLogUtils.isBinaryKeywordSubst(keywordSubst);
		} catch (IllegalArgumentException unknownKeywordSubst) {
			logger.warning("unknown keyword substitution '" + keywordSubst
					+ "' in line " + this.logReader.getLineNumber());
		}
		requireLine(this.logReader.nextLine(), "total revisions:");
		parseDescription();
		if (this.isFirstFile) {
			this.builder.buildModule(CvsLogUtils.getModuleName(rcsFile, workingFile));
		}
		this.builder.buildFile(workingFile, isBinary, isInAttic, this.revBySymNames);
		if (!RevisionParser.FILE_DELIMITER.equals(this.logReader.getCurrentLine())) {
			new RevisionParser(this.logReader, this.builder).parse();
		}
	}

	/**
	 * Returns <tt>true</tt> if the log was generated
	 * with the "-N" switch of "cvs log"
	 * 
	 * @return Returns <tt>true</tt> if the log was generated
	 * with the "-N" switch of "cvs log"
	 */
	public boolean isLogWithoutSymbolicNames() {
		return this.isLogWithoutSymbolicNames;
	}

	private String parseSingleLine(String line, String lineStart)
			throws LogSyntaxException {

		if (!line.startsWith(lineStart)) {
			throw new LogSyntaxException(
				"line " + this.logReader.getLineNumber() + ": expected '"
						+ lineStart + "' but found '" + line + "'");
		}

		return line.substring(lineStart.length());
	}

	private void requireLine(String line, String lineStart)
			throws LogSyntaxException {

		parseSingleLine(line, lineStart); // ignore this line
	}

	private void parseSymbolicNames() throws IOException {
		if (this.logReader.getCurrentLine().startsWith("keyword substitution: ")) {
			return;
		}
		String line;
		if (this.logReader.getCurrentLine().equals("symbolic names:")) {
			line = this.logReader.nextLine();
		} else {
			this.isLogWithoutSymbolicNames = true;
			line = this.logReader.getCurrentLine();
			}
		while (line != null && !line.startsWith("keyword substitution: ")) {
			int firstColon = line.indexOf(':');
			String tagName = line.substring(1, firstColon);
			String tagRevision = line.substring(firstColon + 2);
            this.revBySymNames.put(tagName, tagRevision);
			line = this.logReader.nextLine();
		}
	}

	private void parseLocksAndAccessList() throws IOException {
		while (!"access list:".equals(this.logReader.nextLine())) {
			// ignore locks lines until "access list:" is reached
		}
		String line;
		do {
			line = this.logReader.nextLine();
			// ignore access list lines until next section is reached
		} while (!line.equals("symbolic names:") &&
				!line.startsWith("keyword substitution: "));
	}

	private void parseDescription() throws LogSyntaxException, IOException {
		String line = this.logReader.nextLine();
		if (line.equals(RevisionParser.FILE_DELIMITER)) {
			throw new LogSyntaxException("line " +
					this.logReader.getLineNumber() +
					": missing description; please don't use the -h switch of 'cvs log'!");
		}
		requireLine(this.logReader.getCurrentLine(), "description:");
		while (!isDescriptionDelimiter(this.logReader.nextLine())) {
			// ignore description lines
		}
	}
	
	private boolean isDescriptionDelimiter(String line) {
		return RevisionParser.REVISION_DELIMITER.equals(line)
				|| RevisionParser.FILE_DELIMITER.equals(line);
	}
}