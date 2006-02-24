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
 
 $RCSfile: SvnLogfileParser.java,v $ 
 Created on $Date: 2004/10/10 11:29:07 $ 
 */

package net.sf.statcvs.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.util.LookaheadReader;
import net.sf.statcvs.util.SvnDiffUtils;

import org.xml.sax.SAXException;

/**
 * Parses a CVS logfile. A {@link Builder} must be specified which does the construction work.
 * 
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @version $Id: SvnLogfileParser.java,v 1.15 2004/10/10 11:29:07 cyganiak Exp $
 */
public class SvnLogfileParser {

    private static Logger logger = Logger.getLogger(SvnLogfileParser.class.getName());

    private SvnLogBuilder builder;
    private InputStream logFile;

    /**
     * Default Constructor
     * 
     * @param logFile
     *            a <tt>Reader</tt> containing the CVS logfile
     * @param builder
     *            the builder that will process the log information
     */
    public SvnLogfileParser(InputStream logFile, SvnLogBuilder builder) {
        this.logFile = logFile;
        this.builder = builder;
    }

    /**
     * Parses the logfile. After <tt>parse()</tt> has finished, the result of the parsing process can be obtained from the builder.
     * 
     * @throws LogSyntaxException
     *             if syntax errors in log
     * @throws IOException
     *             if errors while reading from the log Reader
     */
    public void parse() throws LogSyntaxException, IOException {
        long startTime = System.currentTimeMillis();
        logger.fine("starting to parse...");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(logFile, new SvnXmlLogFileHandler(builder));
        } catch (ParserConfigurationException e) {
            throw new LogSyntaxException(e.getMessage());
        } catch (SAXException e) {
            throw new LogSyntaxException(e.getMessage());
        }

        // eatNonCheckedInFileLines();
        // if (!this.logReader.hasNextLine()) {
        // throw new LogSyntaxException("Empty logfile!");
        // }
        // if (!"".equals(this.logReader.getCurrentLine())) {
        // throw new LogSyntaxException("Expected '?' or empty line at line "
        // + this.logReader.getLineNumber() + ", but found '"
        // + this.logReader.getCurrentLine() + "'");
        // }
        // eatEmptyLines();
        // // TODO: uncomment when tag/branch reports are added
        // // boolean isLogWithoutSymbolicNames = false;
        // boolean isFirstFile = true;
        // do {
        // FileBlockParser parser = new FileBlockParser(
        // this.logReader, this.builder, isFirstFile);
        // parser.parse();
        // isFirstFile = false;
        // // if (parser.isLogWithoutSymbolicNames()) {
        // // isLogWithoutSymbolicNames = true;
        // // }
        // eatEmptyLines();
        // } while (this.logReader.hasNextLine());
        // if (isLogWithoutSymbolicNames) {
        // logger.warning("Log was created with '-N' switch of 'cvs log', some
        // reports will be missing!");
        // }
        logger.fine("parsing svn log finished in " + (System.currentTimeMillis() - startTime) + " ms.");

        Collection fileBuilders = builder.getFileBuilders().values();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            String fileName = fileBuilder.getName();
            List revisions = fileBuilder.getRevisions();
            for (int i = 0; i < revisions.size(); i++) {
                if (i + 1 < revisions.size()) {
                    String revNrNew = ((RevisionData) revisions.get(i)).getRevisionNumber();
                    String revNrOld = ((RevisionData) revisions.get(i + 1)).getRevisionNumber();
                    System.out.println(fileName + " " + revNrOld + " " + revNrNew);
                    InputStream diffStream = SvnDiffUtils.callSvnDiff(revNrOld, revNrNew, fileName);
                    LookaheadReader diffReader = new LookaheadReader(new InputStreamReader(diffStream));
                    int added = -1;
                    int removed = -1;
                    while (diffReader.hasNextLine()) {
                        diffReader.nextLine();
                        // very simple algorithm
                        if (diffReader.getCurrentLine().charAt(0) == '+')
                            added++;
                        else if (diffReader.getCurrentLine().charAt(0) == '-')
                            removed++;
                        // System.out.println(diffReader.getCurrentLine());
                    }
                    // System.out.println(added + " " + removed);
                    if (added!=-1 && removed!=-1) {
                    	// condition is true only for deleted files
                        ((RevisionData) revisions.get(i)).setLines(added, removed);                    	
                    }
                }
            }
        }

        logger.fine("parsing svn diff finished in " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    // private void eatNonCheckedInFileLines() throws IOException {
    // while (this.logReader.hasNextLine() &&
    // this.logReader.nextLine().startsWith("? ")) {
    // // ignore lines starting with "? "
    // }
    // }
    //
    // /**
    // * Calls nextLine() on the reader until EOF or a non-empty line
    // * is found
    // */
    // private void eatEmptyLines() throws IOException {
    // while (this.logReader.hasNextLine() &&
    // "".equals(this.logReader.nextLine())) {
    // // ignore empty lines
    // }
    // }
}