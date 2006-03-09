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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.util.FileUtils;
import net.sf.statcvs.util.SvnDiffUtils;
import net.sf.statcvs.util.SvnInfoUtils;
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.SAXException;

/**
 * Parses a CVS logfile. A {@link Builder} must be specified which does the
 * construction work.
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

	private void handleLineCounts(SAXParserFactory factory) throws IOException {
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
	 * Parses the logfile. After <tt>parse()</tt> has finished, the result of
	 * the parsing process can be obtained from the builder.
	 * 
	 * @throws LogSyntaxException
	 *             if syntax errors in log
	 * @throws IOException
	 *             if errors while reading from the log Reader
	 */
	public void parse() throws LogSyntaxException, IOException {

		SAXParserFactory factory = parseSvnLog();

		verifyAddition();

		removeDirectories();

		handleLineCounts(factory);

	}

	private void removeDirectories() {
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

	private SAXParserFactory parseSvnLog() throws IOException, LogSyntaxException {
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

	private void verifyAddition() {
		long startTime = System.currentTimeMillis();
		logger.fine("finding implicit additions ...");

		Collection fileBuilders = builder.getFileBuilders().values();
		for (Iterator iter = fileBuilders.iterator(); iter.hasNext();) {
			FileBuilder fileBuilder = (FileBuilder) iter.next();

			// file's first revision is not a creation... either not in log or
			// was implicit
			if (!fileBuilder.existRevision() || !fileBuilder.getFirstRevision().isCreation()) {

				String path = fileBuilder.getName() + "/";
				String parentPath = FileUtils.getParentDirectoryPath(path);
				if (parentPath.length() > 0)
					parentPath = parentPath.substring(0, parentPath.length() - 1); // remove
																					// last
																					// /
				while (parentPath.length() > 0) {
					// do we have a builder for the parent?
					if (builder.getFileBuilders().get(parentPath) != null) {
						FileBuilder parentBuilder = (FileBuilder) builder.getFileBuilders().get(parentPath);
						// do we have a creation for that parent?
						if (parentBuilder.getFirstRevision().isCreation()) {
							// don't call addRevision directly. buildRevision
							// does more
							builder.buildFile(fileBuilder.getName(), false, false, new HashMap());
							builder.buildRevision((RevisionData) parentBuilder.getFirstRevision().clone());
							break;
						}

					}

					// continue to recurse up
					parentPath = FileUtils.getParentDirectoryPath(parentPath + "/");
					if (parentPath.length() == 0) {
						// System.out.println("Did not find creation for " +
						// fileBuilder.getName());
						break;
					}
					parentPath = parentPath.substring(0, parentPath.length() - 1); // remove
																					// last
																					// /
				}
			}
		}

		// list is sorted
		List directories = SvnInfoUtils.getDirectories();
		for (Iterator iter = directories.iterator(); iter.hasNext();) {
			String directory = iter.next().toString();
			FileBuilder parent = (FileBuilder) builder.getFileBuilders().get(directory);
			if (!parent.existRevision() || !parent.getFirstRevision().isCreation())
				continue;

			int parentRevision;
			try {
				parentRevision = Integer.parseInt(parent.getFirstRevision().getRevisionNumber());
			} catch (Exception e) {
				continue;
			}
			for (Iterator iterator = fileBuilders.iterator(); iterator.hasNext();) {
				FileBuilder child = (FileBuilder) iterator.next();
				if (child.getName().indexOf(directory + "/") == 0) {
					// check to see that the addition wasn't somewhere in
					// fileBuilder's existence (meaning we copied over the file
					// from another branch, using
					// the parent folder)
					int childRevision;
					try {
						int i;
						for (i = 0; i < child.getRevisions().size(); i++) {
							RevisionData data = (RevisionData) child.getRevisions().get(i);
							childRevision = Integer.parseInt(data.getRevisionNumber());
							if (parentRevision > childRevision)
								break;
						}
						if (i < child.getRevisions().size()) {
							// get rid of older revisions

							// avoid concurrent modification errors.
							List toKeep = new ArrayList();
							for (Iterator it = child.getRevisions().subList(0, i).iterator(); it.hasNext();) {
								toKeep.add(it.next());
							}
							child.getRevisions().retainAll(toKeep);
							// don't call addRevision directly. buildRevision
							// does more
							builder.buildFile(child.getName(), false, false, new HashMap());
							builder.buildRevision((RevisionData) parent.getFirstRevision().clone());
						}

					} catch (Exception e) {
						continue;
					}

				}
			}
		}

		logger.fine("cleaning finished in " + (System.currentTimeMillis() - startTime) + " ms.");

	}
}
