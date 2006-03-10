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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.util.FilenameComparator;
import net.sf.statcvs.util.SvnDiffUtils;
import net.sf.statcvs.util.SvnInfoUtils;
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * Parses a CVS logfile. A {@link Builder} must be specified which does the construction work.
 * 
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @version $Id$
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

    protected void handleLineCounts(SAXParserFactory factory) throws IOException {
        long startTime = System.currentTimeMillis();

        LineCountsBuilder lineCountsBuilder = new LineCountsBuilder(builder);
        try {
            FileInputStream lineCountsFile = new FileInputStream("lineCounts.xml");
            SAXParser parser = factory.newSAXParser();
            parser.parse(lineCountsFile, new SvnXmlLineCountsFileHandler(lineCountsBuilder));
            // XMLUtil.writeXmlFile(lineCountsBuilder.getDocument(),
            // "lineCounts2.xml");
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }
        logger.fine("parsing line counts finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();

        int limit = 20000;
        int c = 0;
        Collection fileBuilders = builder.getFileBuilders().values();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            if (fileBuilder.isBinary())
                continue;

            String fileName = fileBuilder.getName();
            List revisions = fileBuilder.getRevisions();
            for (int i = 0; i < revisions.size(); i++) {
                if (i + 1 < revisions.size() && ((RevisionData) revisions.get(i)).hasNoLines() && !((RevisionData) revisions.get(i)).isDeletion()) {

                    if (((RevisionData) revisions.get(i + 1)).isDeletion())
                        continue;
                    String revNrNew = ((RevisionData) revisions.get(i)).getRevisionNumber();
                    String revNrOld = ((RevisionData) revisions.get(i + 1)).getRevisionNumber();
                    System.out.println(fileName + " " + revNrOld + " " + revNrNew);
                    int lineDiff[] = SvnDiffUtils.getLineDiff(revNrOld, revNrNew, fileName);
                    if (lineDiff[0] != -1 && lineDiff[1] != -1) {
                        ((RevisionData) revisions.get(i)).setLines(lineDiff[0], lineDiff[1]);
                        lineCountsBuilder.newRevision(fileName, revNrNew, lineDiff[0] + "", lineDiff[1] + "");
                    } else {
                        // file is binary and has been deleted
                        fileBuilder.setBinary(true);
                        break;
                    }

                }
            }
            if (c++ > limit)
                break;
        }
        XMLUtil.writeXmlFile(lineCountsBuilder.getDocument(), "lineCounts.xml");

        logger.fine("parsing svn diff finished in " + (System.currentTimeMillis() - startTime) + " ms.");
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

        SAXParserFactory factory = parseSvnLog();

        verifyImplicitActions();

        // must be after verifyImplicitActions();
        removeDirectories();

        handleLineCounts(factory);

    }

    protected void verifyImplicitActions() {

        // this method most certainly has issues with implicit actions on root folder.

        long startTime = System.currentTimeMillis();
        logger.fine("verifying implicit actions ...");

        HashSet implicitActions = new HashSet();

        ArrayList files = new ArrayList();
        Collection fileBuilders = builder.getFileBuilders().values();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            files.add(fileBuilder.getName());
        }

        Collections.sort(files, new FilenameComparator());

        FileBuilder parentBuilder, childBuilder;
        RevisionData parentData, childData;
        String parent, child;
        int parentRevision, childRevision;

        for (int i = 0; i < files.size(); i++) {
            parent = files.get(i).toString();
            parentBuilder = (FileBuilder) builder.getFileBuilders().get(parent);
            for (int j = i + 1; j < files.size() && files.get(j).toString().indexOf(parent + "/") == 0; j++) {
                SvnInfoUtils.addDirectory(parent);
                child = files.get(j).toString();
                childBuilder = (FileBuilder) builder.getFileBuilders().get(child);
                for (Iterator iter = parentBuilder.getRevisions().iterator(); iter.hasNext();) {
                    parentData = (RevisionData) iter.next();
                    try {
                        parentRevision = Integer.parseInt(parentData.getRevisionNumber());
                    } catch (Exception e) {
                        continue;
                    }

                    int k;
                    // ignore modifications to folders
                    if (parentData.isCreation() || parentData.isDeletion()) {
                        for (k = 0; k < childBuilder.getRevisions().size(); k++) {
                            childData = (RevisionData) childBuilder.getRevisions().get(k);
                            childRevision = Integer.parseInt(childData.getRevisionNumber());
                            if (parentRevision == childRevision) {
                                k = childBuilder.getRevisions().size();
                                break;
                            }

                            if (parentRevision > childRevision)
                                break; // we must insert it here!
                        }

                        if (k < childBuilder.getRevisions().size()) {
                            // avoid concurrent modification errors.
                            List toMove = new ArrayList();
                            for (Iterator it = childBuilder.getRevisions().subList(k, childBuilder.getRevisions().size()).iterator(); it.hasNext();) {
                                toMove.add(it.next());
                            }
                            childBuilder.getRevisions().removeAll(toMove);
                            // don't call addRevision directly. buildRevision
                            // does more
                            builder.buildFile(child, false, false, new HashMap());
                            RevisionData implicit = (RevisionData) parentData.clone();
                            implicitActions.add(implicit);
                            builder.buildRevision(implicit);
                            for (Iterator it = toMove.iterator(); it.hasNext();) {
                                builder.buildRevision((RevisionData) it.next());
                            }
                        }
                    }
                }
            }
        }

        // in the preceeding block, we add implicit additions to too may files.
        // possibly a folder was deleted and restored later on, without the
        // specific file being re-added. we get rid of those here. however,
        // without knowledge of what was copied during the implicit additions /
        // replacements, we will remove as many implicit actions as possible
        // 
        // this solution is imperfect.
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder filebuilder = (FileBuilder) iter.next();

            // make sure our attic is well set, with our new deletions that we might have added.
            if (!((Builder) builder).getAtticFileNames().contains(filebuilder.getName()) && !SvnInfoUtils.existsInWorkingCopy(filebuilder.getName())) {
                ((Builder) builder).getAtticFileNames().add(filebuilder.getName());
            }

            if (!SvnInfoUtils.existsInWorkingCopy(filebuilder.getName()) && !filebuilder.finalRevisionIsDead()) {
                int earliestDelete = -1;
                for (int i = 0; i < filebuilder.getRevisions().size(); i++) {
                    RevisionData data = (RevisionData) filebuilder.getRevisions().get(i);

                    if (data.isDeletion()) {
                        earliestDelete = i;
                    }

                    if ((!data.isCreation() && data.isChangeOrRestore()) || !implicitActions.contains(data)) {
                        break;
                    }
                }

                if (earliestDelete > 0) {
                    // avoid concurrent modification errors.
                    List toRemove = new ArrayList();
                    for (Iterator it = filebuilder.getRevisions().subList(0, earliestDelete).iterator(); it.hasNext();) {
                        toRemove.add(it.next());
                    }
                    filebuilder.getRevisions().removeAll(toRemove);
                }

            }

        }

        // builder.getFileBuilders().get("src/seg/jUCMNav/tests/CommandTest.java");
        logger.fine("verifying implicit actions finished in " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    protected void removeDirectories() {
        Collection fileBuilders = builder.getFileBuilders().values();
        ArrayList toRemove = new ArrayList();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            if (SvnInfoUtils.isDirectory(fileBuilder.getName())) {
                toRemove.add(fileBuilder.getName());
            }
        }

        for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
            builder.getFileBuilders().remove(iter.next());
        }

    }

    protected SAXParserFactory parseSvnLog() throws IOException, LogSyntaxException {
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

        logger.fine("parsing svn log finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        return factory;
    }

}
