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

package net.sf.statsvn.input;

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

import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statsvn.output.SvnConfigurationOptions;
import net.sf.statsvn.util.BinaryDiffException;
import net.sf.statsvn.util.FilenameComparator;
import net.sf.statsvn.util.XMLUtil;

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

    private static final String REPOSITORIES_XML = "repositories.xml";
	private static final Logger LOGGER = Logger.getLogger(SvnLogfileParser.class.getName());
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
    public SvnLogfileParser(final RepositoryFileManager repositoryFileManager, final InputStream logFile, final SvnLogBuilder builder) {
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
    protected void handleLineCounts(final SAXParserFactory factory) throws IOException {
        long startTime = System.currentTimeMillis();
    	final String xmlFile = SvnConfigurationOptions.getCacheDir() + REPOSITORIES_XML;
    	
        final RepositoriesBuilder repositoriesBuilder = new RepositoriesBuilder();
        FileInputStream repositoriesFile = null; 
        try {
            repositoriesFile = new FileInputStream(xmlFile);
            final SAXParser parser = factory.newSAXParser();
            parser.parse(repositoriesFile, new SvnXmlRepositoriesFileHandler(repositoriesBuilder));
            repositoriesFile.close();
        } catch (final ParserConfigurationException e) {
        } catch (final SAXException e) {
        } catch (final IOException e) {
        } finally {
        	if (repositoriesFile != null) {
        		repositoriesFile.close();
        	}
        }
        final String cacheFileName = SvnConfigurationOptions.getCacheDir() + repositoriesBuilder.getFileName(repositoryFileManager.getRepositoryUuid());
        XMLUtil.writeXmlFile(repositoriesBuilder.getDocument(), xmlFile);
        LOGGER.fine("parsing repositories finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();
        
        final CacheBuilder cacheBuilder = new CacheBuilder(builder, repositoryFileManager);
        FileInputStream cacheFile = null;
        try {
            cacheFile = new FileInputStream(cacheFileName);
            final SAXParser parser = factory.newSAXParser();
            parser.parse(cacheFile, new SvnXmlCacheFileHandler(cacheBuilder));
            cacheFile.close();
        } catch (final ParserConfigurationException e) {
        } catch (final SAXException e) {
        } catch (final IOException e) {
        } finally {
        	if (cacheFile != null) {
        		cacheFile.close();
        	}
        }
        LOGGER.fine("parsing line counts finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        startTime = System.currentTimeMillis();

        // update the cache xml file with the latest binary status information
        // from the working copy
        cacheBuilder.updateBinaryStatus(builder.getFileBuilders().values(), repositoryFileManager.getRootRevisionNumber());
        
        final Collection fileBuilders = builder.getFileBuilders().values();
        boolean isFirstDiff=true;
        for (final Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            final FileBuilder fileBuilder = (FileBuilder) iter.next();
            if (fileBuilder.isBinary()) {
				continue;
			}
            final String fileName = fileBuilder.getName();
            final List revisions = fileBuilder.getRevisions();
            for (int i = 0; i < revisions.size(); i++) {
				// line diffs are expensive operations. therefore, the result is stored in the
				// cacheBuilder and eventually persisted in the cache xml file. the next time
				// the file is read the line diffs (or 0/0 in case of binary files) are intialized
				// in the RevisionData. this cause hasNoLines to be false which in turn causes the 
            	// if clause below to be skipped.
                if (i + 1 < revisions.size() && ((RevisionData) revisions.get(i)).hasNoLines() && !((RevisionData) revisions.get(i)).isDeletion()) {
                    if (((RevisionData) revisions.get(i + 1)).isDeletion()) {
						continue;
					}
                    final String revNrNew = ((RevisionData) revisions.get(i)).getRevisionNumber();
                    if (cacheBuilder.isBinary(fileName, revNrNew)) {
						continue;
					}
                    final String revNrOld = ((RevisionData) revisions.get(i + 1)).getRevisionNumber();
					int[] lineDiff;
					try {
						if (isFirstDiff) {
							SvnConfigurationOptions.getTaskLogger().log("Contacting server to obtain line count information.");
							SvnConfigurationOptions.getTaskLogger().log("This information will be cached so that the next time you run StatSVN, "
									+ "results will be returned more quickly.");
							isFirstDiff=false;
						}
						lineDiff = repositoryFileManager.getLineDiff(revNrOld, revNrNew, fileName);
					} catch (final BinaryDiffException e) {
						// file is binary and has been deleted
						cacheBuilder.newRevision(fileName, revNrNew, "0", "0", true);
						fileBuilder.setBinary(true);
						break;
					} catch (final IOException e) {
						SvnConfigurationOptions.getTaskLogger().log("Unable to obtain diff: " + e.getMessage());
                       continue;
                    }
					if (lineDiff[0] != -1 && lineDiff[1] != -1) {
						builder.updateRevision(fileName, revNrNew, lineDiff[0], lineDiff[1]);
						cacheBuilder.newRevision(fileName, revNrNew, lineDiff[0] + "", lineDiff[1] + "", false);
					} else {
						SvnConfigurationOptions.getTaskLogger().log("unknown behaviour; to be investigated");
					}
                }
            }
        }
        XMLUtil.writeXmlFile(cacheBuilder.getDocument(), cacheFileName);
        LOGGER.fine("parsing svn diff finished in " + (System.currentTimeMillis() - startTime) + " ms.");
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

        final SAXParserFactory factory = parseSvnLog();

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

        final long startTime = System.currentTimeMillis();
        LOGGER.fine("verifying implicit actions ...");

        final HashSet implicitActions = new HashSet();

        // get all filenames
        final ArrayList files = new ArrayList();
        final Collection fileBuilders = builder.getFileBuilders().values();
        for (final Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            final FileBuilder fileBuilder = (FileBuilder) iter.next();
            files.add(fileBuilder.getName());
        }

        // sort them so that folders are immediately followed by the folder entries and then by other files which are prefixed by the folder name.
        Collections.sort(files, new FilenameComparator());

        // for each file
        for (int i = 0; i < files.size(); i++) {
            final String parent = files.get(i).toString();
            final FileBuilder parentBuilder = (FileBuilder) builder.getFileBuilders().get(parent);
            // check to see if there are files that indicate that parent is a folder.
            for (int j = i + 1; j < files.size() && files.get(j).toString().indexOf(parent + "/") == 0; j++) {
                // we might not know that it was a folder.
                repositoryFileManager.addDirectory(parent);

                final String child = files.get(j).toString();
                final FileBuilder childBuilder = (FileBuilder) builder.getFileBuilders().get(child);
                // for all revisions in the the parent folder
                for (final Iterator iter = parentBuilder.getRevisions().iterator(); iter.hasNext();) {
                	final RevisionData parentData = (RevisionData) iter.next();
                    int parentRevision;
                    try {
                        parentRevision = Integer.parseInt(parentData.getRevisionNumber());
                    } catch (final Exception e) {
                        continue;
                    }

                    int k;
                    // ignore modifications to folders
                    if (parentData.isCreationOrRestore() || parentData.isDeletion()) {

                        // check to see if the parent revision is an implicit action acting on the child.
                        for (k = 0; k < childBuilder.getRevisions().size(); k++) {
                        	final RevisionData childData = (RevisionData) childBuilder.getRevisions().get(k);
                            final int childRevision = Integer.parseInt(childData.getRevisionNumber());

                            // we don't want to add duplicate entries for the same revision
                            if (parentRevision == childRevision) {
                                k = childBuilder.getRevisions().size();
                                break;
                            }

                            if (parentRevision > childRevision) {
								break; // we must insert it here!
							}
                        }

                        // we found something to insert
                        if (k < childBuilder.getRevisions().size()) {
                            // we want to memorize this implicit action.
                            final RevisionData implicit = (RevisionData) parentData.createCopy();
                            implicitActions.add(implicit);

                            // avoid concurrent modification errors.
                            final List toMove = new ArrayList();
                            for (final Iterator it = childBuilder.getRevisions().subList(k, childBuilder.getRevisions().size()).iterator(); it.hasNext();) {
                                toMove.add(it.next());
                            }

                            // remove the revisions to be moved.
                            childBuilder.getRevisions().removeAll(toMove);

                            // don't call addRevision directly. buildRevision does more.
                            builder.buildFile(child, false, false, new HashMap());
                            builder.buildRevision(implicit);

                            // copy back the revisions we removed.
                            for (final Iterator it = toMove.iterator(); it.hasNext();) {
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
        for (final Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            final FileBuilder filebuilder = (FileBuilder) iter.next();

            // make sure our attic is well set, with our new deletions that we might have added.
            if (!repositoryFileManager.existsInWorkingCopy(filebuilder.getName())) {
				builder.addToAttic(filebuilder.getName());
			}

            // do we detect an inconsistency?
            if (!repositoryFileManager.existsInWorkingCopy(filebuilder.getName()) && !filebuilder.finalRevisionIsDead()) {
                int earliestDelete = -1;
                for (int i = 0; i < filebuilder.getRevisions().size(); i++) {
                    final RevisionData data = (RevisionData) filebuilder.getRevisions().get(i);

                    if (data.isDeletion()) {
                        earliestDelete = i;
                    }

                    if ((!data.isCreationOrRestore() && data.isChange()) || !implicitActions.contains(data)) {
						break;
					}
                }

                if (earliestDelete > 0) {
                    // avoid concurrent modification errors.
                    final List toRemove = new ArrayList();
                    for (final Iterator it = filebuilder.getRevisions().subList(0, earliestDelete).iterator(); it.hasNext();) {
                        toRemove.add(it.next());
                    }
                    filebuilder.getRevisions().removeAll(toRemove);
                }
            }
        }
        LOGGER.fine("verifying implicit actions finished in " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    /**
     * We have created FileBuilders for directories because we needed the information to be able to find implicit actions. However, we don't want to query
     * directories for their line counts later on. Therefore, we must remove them here.
     * 
     * (@link SvnInfoUtils#isDirectory(String)) is used to know what files are directories. Deleted directories are assumed to have been added in (@link
     * #verifyImplicitActions())
     */
    protected void removeDirectories() {
        final Collection fileBuilders = builder.getFileBuilders().values();
        final ArrayList toRemove = new ArrayList();
        for (final Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
            final FileBuilder fileBuilder = (FileBuilder) iter.next();
            if (repositoryFileManager.isDirectory(fileBuilder.getName())) {
                toRemove.add(fileBuilder.getName());
            }
        }

        for (final Iterator iter = toRemove.iterator(); iter.hasNext();) {
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
        final long startTime = System.currentTimeMillis();
        LOGGER.fine("starting to parse...");

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            parser.parse(logFile, new SvnXmlLogFileHandler(builder, repositoryFileManager));
        } catch (final ParserConfigurationException e) {
            throw new LogSyntaxException(e.getMessage());
        } catch (final SAXException e) {
            throw new LogSyntaxException(e.getMessage());
        }

        LOGGER.fine("parsing svn log finished in " + (System.currentTimeMillis() - startTime) + " ms.");
        return factory;
    }

}
