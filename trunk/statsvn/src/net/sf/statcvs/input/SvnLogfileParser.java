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
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * Parses a Subversion logfile and does post-parse processing. A {@link Builder} must be specified which does the construction work.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public class SvnLogfileParser {

    private static Logger logger = Logger.getLogger(SvnLogfileParser.class.getName());
    private SvnLogBuilder builder;
    private InputStream logFile;
    private RepositoryFileManager repositoryFileManager;

    /**
     * Default Constructor
     * 
     * @param repositoryFileManager
     *            the repository file manager
     * @param logFile
     *            a <tt>Reader</tt> containing the SVN logfile
     * @param builder
     *            the builder that will process the log information
     */
    public SvnLogfileParser(RepositoryFileManager repositoryFileManager, InputStream logFile, SvnLogBuilder builder) {
        this.logFile = logFile;
        this.builder = builder;
        this.repositoryFileManager = repositoryFileManager;
    }

    /**
     * Because the log file does not contain the lines added or removed in a commit, and because the logfile contains implicit actions (@link
     * #verifyImplicitActions()), we must query the repository for line differences. This method uses the (@link LineCountsBuilder) to load the persisted
     * information and (@link SvnDiffUtils) to find new information.
     * 
     * @param factory
     *            the factory used to create SAX parsers.
     * @throws IOException
     */
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
                    int lineDiff[] = repositoryFileManager.getLineDiff(revNrOld, revNrNew, fileName);
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

    /**
     * The svn log can contain deletions of directories which imply that all of its contents have been deleted.
     * 
     * Furthermore, the svn log can contain entries which are copies from other directories (additions or replacements; I haven't seen modifications with this
     * property, but am not 100% sure) meaning that all files from the other directory are copied here. We currently do not go back through copies, so we must
     * infer what files <i>could</i> have been added during those copies.
     * 
     */
    protected void verifyImplicitActions() {

        // this method most certainly has issues with implicit actions on root folder.

        long startTime = System.currentTimeMillis();
        logger.fine("verifying implicit actions ...");

        HashSet implicitActions = new HashSet();

        // get all filenames
        ArrayList files = new ArrayList();
        Collection fileBuilders = builder.getFileBuilders().values();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            files.add(fileBuilder.getName());
        }

        // sort them so that folders are immediately followed by the folder entries and then by other files which are prefixed by the folder name.
        Collections.sort(files, new FilenameComparator());

        FileBuilder parentBuilder, childBuilder;
        RevisionData parentData, childData;
        String parent, child;
        int parentRevision, childRevision;

        // for each file
        for (int i = 0; i < files.size(); i++) {
            parent = files.get(i).toString();
            parentBuilder = (FileBuilder) builder.getFileBuilders().get(parent);
            // check to see if there are files that indicate that parent is a folder.
            for (int j = i + 1; j < files.size() && files.get(j).toString().indexOf(parent + "/") == 0; j++) {
                // we might not know that it was a folder.
                repositoryFileManager.addDirectory(parent);

                child = files.get(j).toString();
                childBuilder = (FileBuilder) builder.getFileBuilders().get(child);
                // for all revisions in the the parent folder
                for (Iterator iter = parentBuilder.getRevisions().iterator(); iter.hasNext();) {
                    parentData = (RevisionData) iter.next();
                    try {
                        parentRevision = Integer.parseInt(parentData.getRevisionNumber());
                    } catch (Exception e) {
                        continue;
                    }

                    int k;
                    // ignore modifications to folders
                    if (parentData.isCreationOrRestore() || parentData.isDeletion()) {

                        // check to see if the parent revision is an implicit action acting on the child.
                        for (k = 0; k < childBuilder.getRevisions().size(); k++) {
                            childData = (RevisionData) childBuilder.getRevisions().get(k);
                            childRevision = Integer.parseInt(childData.getRevisionNumber());

                            // we don't want to add duplicate entries for the same revision
                            if (parentRevision == childRevision) {
                                k = childBuilder.getRevisions().size();
                                break;
                            }

                            if (parentRevision > childRevision)
                                break; // we must insert it here!
                        }

                        // we found something to insert
                        if (k < childBuilder.getRevisions().size()) {
                            // we want to memorize this implicit action.
                            RevisionData implicit = (RevisionData) parentData.clone();
                            implicitActions.add(implicit);

                            // avoid concurrent modification errors.
                            List toMove = new ArrayList();
                            for (Iterator it = childBuilder.getRevisions().subList(k, childBuilder.getRevisions().size()).iterator(); it.hasNext();) {
                                toMove.add(it.next());
                            }

                            // remove the revisions to be moved.
                            childBuilder.getRevisions().removeAll(toMove);

                            // don't call addRevision directly. buildRevision does more.
                            builder.buildFile(child, false, false, new HashMap());
                            builder.buildRevision(implicit);

                            // copy back the revisions we removed.
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

        // Examples:
        // IA ID IA ID M A -> ID M A
        // IA ID A D M A -> ID A D M A
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder filebuilder = (FileBuilder) iter.next();

            // make sure our attic is well set, with our new deletions that we might have added.
            if (!((Builder) builder).getAtticFileNames().contains(filebuilder.getName()) && !repositoryFileManager.existsInWorkingCopy(filebuilder.getName())) {
                ((Builder) builder).getAtticFileNames().add(filebuilder.getName());
            }

            // do we detect an inconsistency?
            if (!repositoryFileManager.existsInWorkingCopy(filebuilder.getName()) && !filebuilder.finalRevisionIsDead()) {
                int earliestDelete = -1;
                for (int i = 0; i < filebuilder.getRevisions().size(); i++) {
                    RevisionData data = (RevisionData) filebuilder.getRevisions().get(i);

                    if (data.isDeletion()) {
                        earliestDelete = i;
                    }

                    if ((!data.isCreationOrRestore() && data.isChange()) || !implicitActions.contains(data))
                        break;

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

        logger.fine("verifying implicit actions finished in " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    /**
     * We have created FileBuilders for directories because we needed the information to be able to find implicit actions. However, we don't want to query
     * directories for their line counts later on. Therefore, we must remove them here.
     * 
     * (@link SvnInfoUtils#isDirectory(String)) is used to know what files are directories. Deleted directories are assumed to have been added in (@link
     * #verifyImplicitActions())
     */
    protected void removeDirectories() {
        Collection fileBuilders = builder.getFileBuilders().values();
        ArrayList toRemove = new ArrayList();
        for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            FileBuilder fileBuilder = (FileBuilder) iter.next();
            if (repositoryFileManager.isDirectory(fileBuilder.getName())) {
                toRemove.add(fileBuilder.getName());
            }
        }

        for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
            builder.getFileBuilders().remove(iter.next());
        }

    }

    /**
     * Parses the svn log file.
     * 
     * @return the SaxParserFactory, so that it can be reused.
     * @throws IOException
     *             errors while reading file.
     * @throws LogSyntaxException
     *             invalid log syntax.
     */
    protected SAXParserFactory parseSvnLog() throws IOException, LogSyntaxException {
        long startTime = System.currentTimeMillis();
        logger.fine("starting to parse...");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(logFile, new SvnXmlLogFileHandler(builder, repositoryFileManager));
        } catch (ParserConfigurationException e) {
            throw new LogSyntaxException(e.getMessage());
        } catch (SAXException e) {
            throw new LogSyntaxException(e.getMessage());
        }

        logger.fine("parsing svn log finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        return factory;
    }

}
