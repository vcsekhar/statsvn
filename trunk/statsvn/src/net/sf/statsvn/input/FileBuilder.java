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
 
 $RCSfile: FileBuilder.java,v $
 $Date: 2005/03/29 22:45:06 $
 */
package net.sf.statsvn.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import net.sf.statcvs.input.NoLineCountException;
import net.sf.statcvs.model.VersionedFile;

/**
 * <p>
 * Builds a {@link VersionedFile} with {@link Revision}s from logging data. This class is responsible for deciding if a file or revisions will be included in
 * the report, for translating from CVS logfile data structures to the data structures in the <tt>net.sf.statsvn.model</tt> package, and for calculating the
 * LOC history for the file.
 * </p>
 * 
 * <p>
 * A main goal of this class is to delay the creation of the <tt>VersionedFile</tt> object until all revisions of the file have been collected from the log.
 * We could simply create <tt>VersionedFile</tt> and <tt>Revision</tt>s on the fly as we parse through the log, but this creates a problem if we decide not
 * to include the file after reading several revisions. The creation of a <tt>VersionedFile</tt> or <tt>Revision</tt> can cause many more objects to be
 * created (<tt>Author</tt>, <tt>Directory</tt>, <tt>Commit</tt>), and it would be very hard to get rid of them if we don't want the file. This
 * problem is solved by first collecting all information about one file in this class, and then, with all information present, deciding if we want to create the
 * model instances or not.
 * </p>
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @author Tammo van Lessen
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * @version $Id$
 */
public class FileBuilder {
    private static final int ONE_MIN_IN_MS = 60000;

	private static final Logger LOGGER = Logger.getLogger(FileBuilder.class.getName());

    private Builder builder;
    private String name;
    private boolean binary;
    private final List revisions = new ArrayList();
    private Map revBySymnames;
    private int locDelta;

    /**
     * Creates a new <tt>FileBuilder</tt>.
     * 
     * @param builder
     *            a <tt>Builder</tt> that provides factory services for author and directory instances and line counts.
     * @param name
     *            the filename
     * @param binary
     *            Is this a binary file or not?
     */
    public FileBuilder(final Builder builder, final String name, final boolean isBinary, final Map revBySymnames) {
        this.builder = builder;
        this.name = name;
        this.binary = isBinary;
        this.revBySymnames = revBySymnames;

        LOGGER.fine("logging " + name);
    }

    /**
     * Adds a revision to the file. The revisions must be added in the same order as they appear in the CVS logfile, that is, most recent first.
     * 
     * @param data
     *            the revision
     */
    public void addRevisionData(final RevisionData data) {
        if (binary && !data.isCreationOrRestore()) {
            data.setLines(0, 0);
        }
        this.revisions.add(data);

        locDelta += getLOCChange(data);
    }

    /**
     * Creates and returns a {@link VersionedFile} representation of the file. <tt>null</tt> is returned if the file does not meet certain criteria, for
     * example if its filename meets an exclude filter or if it was dead during the entire logging timespan.
     * 
     * @param beginOfLogDate
     *            the date of the begin of the log
     * @return a <tt>VersionedFile</tt> representation of the file.
     */
    public VersionedFile createFile(final Date beginOfLogDate) {
        if (isFilteredFile() || !fileExistsInLogPeriod()) {
            return null;
        }

        final VersionedFile file = new VersionedFile(name, builder.getDirectory(name));

        if (revisions.isEmpty()) {
            buildBeginOfLogRevision(file, beginOfLogDate, getFinalLOC(), null);
            return file;
        }

        final Iterator it = revisions.iterator();
        RevisionData currentData = (RevisionData) it.next();
        int currentLOC = getFinalLOC();
        RevisionData previousData;
        int previousLOC;
        SortedSet symbolicNames;

        while (it.hasNext()) {
            previousData = currentData;
            previousLOC = currentLOC;
            currentData = (RevisionData) it.next();
            currentLOC = previousLOC - getLOCChange(previousData);

            // symbolic names for previousData
            symbolicNames = createSymbolicNamesCollection(previousData);

            if (previousData.isCreationOrRestore() || previousData.isChange() || isBinary()) {
                if (currentData.isDeletion()) {
                    buildCreationRevision(file, previousData, previousLOC, symbolicNames);
                } else {
                    buildChangeRevision(file, previousData, previousLOC, symbolicNames);
                }
            } else if (previousData.isDeletion()) {
                buildDeletionRevision(file, previousData, previousLOC, symbolicNames);
            } else {
                LOGGER.warning("illegal state in " + file.getFilenameWithPath() + ":" + previousData.getRevisionNumber());
            }
        }

        // symbolic names for currentData
        symbolicNames = createSymbolicNamesCollection(currentData);

        final int nextLinesOfCode = currentLOC - getLOCChange(currentData);
        if (currentData.isCreationOrRestore()) {
            buildCreationRevision(file, currentData, currentLOC, symbolicNames);
        } else if (currentData.isDeletion()) {
            buildDeletionRevision(file, currentData, currentLOC, symbolicNames);
            buildBeginOfLogRevision(file, beginOfLogDate, nextLinesOfCode, symbolicNames);
        } else if (currentData.isChange()) {
            buildCreationRevision(file, currentData, currentLOC, symbolicNames);
//            buildChangeRevision(file, currentData, currentLOC, symbolicNames);
            buildBeginOfLogRevision(file, beginOfLogDate, nextLinesOfCode, symbolicNames);
        } else {
            LOGGER.warning("illegal state in " + file.getFilenameWithPath() + ":" + currentData.getRevisionNumber());
        }
        return file;
    }

    /**
     * Gets a LOC count for the file's most recent revision. If the file exists in the local checkout, we ask the {@link RepositoryFileManager} to count its
     * lines of code. If not (that is, it is dead), return an approximated LOC value for its last non-dead revision.
     * 
     * @return the LOC count for the file's most recent revision.
     */
    private int getFinalLOC() {
        if (binary) {
            return 0;
        }

        String revision = null;
        try {
            revision = builder.getRevision(name);
        } catch (final IOException e) {
            if (!finalRevisionIsDead()) {
                LOGGER.warning(e.getMessage());
            }
        }

        try {
            // if ("1.1".equals(revision)) {
            // return builder.getLOC(name) + locDelta;
            // } else {
            if (!revisions.isEmpty()) {
                final RevisionData firstAdded = (RevisionData) revisions.get(0);
                if (!finalRevisionIsDead() && !firstAdded.getRevisionNumber().equals(revision)) {
                    LOGGER.warning("Revision of " + name + " does not match expected revision");
                }
            }
            return builder.getLOC(name);
            // }
        } catch (final NoLineCountException e) {
            if (!finalRevisionIsDead()) {
                LOGGER.warning(e.getMessage());
            }
            return approximateFinalLOC();
        }
    }

    /**
     * Returns <tt>true</tt> if the file's most recent revision is dead.
     * 
     * @return <tt>true</tt> if the file is dead.
     */
    protected boolean finalRevisionIsDead() {
        if (revisions.isEmpty()) {
            return false;
        }
        return ((RevisionData) revisions.get(0)).isDeletion();
    }

    /**
     * Returns <tt>true</tt> if the file has revisions.
     * 
     * @return Returns <tt>true</tt> if the file has revisions.
     */
    public boolean existRevision() {
        return !revisions.isEmpty();
    }

    /**
     * Approximates the LOC count for files that are not present in the local checkout. If a file was deleted at some point in history, then we can't count its
     * final lines of code. This algorithm calculates a lower bound for the file's LOC prior to deletion by following the ups and downs of the revisions.
     * 
     * @return a lower bound for the file's LOC before it was deleted
     */
    private int approximateFinalLOC() {
        int max = 0;
        int current = 0;
        final Iterator it = revisions.iterator();
        while (it.hasNext()) {
            final RevisionData data = (RevisionData) it.next();
            current += data.getLinesAdded();
            max = Math.max(current, max);
            current -= data.getLinesRemoved();
        }
        return max;
    }

    /**
     * Returns the change in LOC count caused by a revision. If there were 10 lines added and 3 lines removed, 7 would be returned. This does not take into
     * account file deletion and creation.
     * 
     * @param data
     *            a revision
     * @return the change in LOC count
     */
    private int getLOCChange(final RevisionData data) {
        return data.getLinesAdded() - data.getLinesRemoved();
    }

    private void buildCreationRevision(final VersionedFile file, final RevisionData data, final int loc, final SortedSet symbolicNames) {
        file.addInitialRevision(data.getRevisionNumber(), builder.getAuthor(data.getLoginName()), data.getDate(), data.getComment(), loc, symbolicNames);
    }

    private void buildChangeRevision(final VersionedFile file, final RevisionData data, final int loc, final SortedSet symbolicNames) {
        file.addChangeRevision(data.getRevisionNumber(), builder.getAuthor(data.getLoginName()), data.getDate(), data.getComment(), loc, data.getLinesAdded()
                - data.getLinesRemoved(), Math.min(data.getLinesAdded(), data.getLinesRemoved()), symbolicNames);
    }

    private void buildDeletionRevision(final VersionedFile file, final RevisionData data, final int loc, final SortedSet symbolicNames) {
        file.addDeletionRevision(data.getRevisionNumber(), builder.getAuthor(data.getLoginName()), data.getDate(), data.getComment(), loc, symbolicNames);
    }

    private void buildBeginOfLogRevision(final VersionedFile file, final Date beginOfLogDate, final int loc, final SortedSet symbolicNames) {
        final Date date = new Date(beginOfLogDate.getTime() - ONE_MIN_IN_MS);
        file.addBeginOfLogRevision(date, loc, symbolicNames);
    }

    /**
     * Takes a filename and checks if it should be processed or not. Can be used to filter out unwanted files.
     * 
     * @return <tt>true</tt> if this file should not be processed
     */
    private boolean isFilteredFile() {
        return !this.builder.matchesPatterns(this.name);
    }

    /**
     * Returns <tt>false</tt> if the file did never exist in the timespan covered by the log. For our purposes, a file is non-existant if it has no revisions
     * and does not exists in the module checkout. Note: A file with no revisions must be included in the report if it does exist in the module checkout. This
     * happens if it was created before the log started, and not changed before the log ended.
     * 
     * @return <tt>true</tt> if the file did exist at some point in the log period.
     */
    private boolean fileExistsInLogPeriod() {
        if (revisions.size() > 0 || binary) {
            return true;
        }
        try {
            builder.getLOC(name);
            return true;
        } catch (final NoLineCountException fileDoesNotExistInTimespan) {
            return false;
        }
    }

    /**
     * Creates a sorted set containing all symbolic name objects affected by this revision. If this revision has no symbolic names, this method returns null.
     * 
     * @param revisionData
     *            this revision
     * @return the sorted set or null
     */
    private SortedSet createSymbolicNamesCollection(final RevisionData revisionData) {
        SortedSet symbolicNames = null;

        final Iterator symIt = revBySymnames.keySet().iterator();
        while (symIt.hasNext()) {
            final String symName = (String) symIt.next();
            final String rev = (String) revBySymnames.get(symName);
            if (revisionData.getRevisionNumber().equals(rev)) {
                if (symbolicNames == null) {
                    symbolicNames = new TreeSet();
                }
                LOGGER.fine("adding revision " + name + "," + revisionData.getRevisionNumber() + " to symname " + symName);
                symbolicNames.add(builder.getSymbolicName(symName));
            }
        }

        return symbolicNames;
    }

    /**
     * New in StatSVN: Gives the FileBuilder's filename.
     * 
     * TODO: Beef up this interface to better encapsulate the data structure.
     * 
     * @return the filename
     */
    public String getName() {
        return name;
    }

    /**
     * New in StatSVN: The list of revisions made on this file.
     * 
     * TODO: Beef up this interface to better encapsulate the data structure.
     * 
     * @return the list of revisions on this file
     */
    public List getRevisions() {
        return revisions;
    }

    /**
     * New in StatSVN: Returns a particular revision made on this file or <tt>null</tt> if it doesn't exist.
     * 
     * @return a particular revision made on this file or <tt>null</tt> if it doesn't exist.
     */
    private RevisionData findRevision(final String revisionNumber) {
        for (int i = 0; i < revisions.size(); i++) {
            final RevisionData data = (RevisionData) revisions.get(i);
            if (data.getRevisionNumber().equals(revisionNumber)) {
                return data;
            }
        }
        return null;
    }

    /**
     * New in StatSVN: Returns <tt>true</tt> if this file is known to be binary.
     * 
     * TODO: Beef up this interface to better encapsulate the data structure.
     * 
     * @return <tt>true</tt> if this file is known to be binary, <tt>false</tt> otherwise.
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * New in StatSVN: Sets the file's binary flag.
     * 
     * TODO: Beef up this interface to better encapsulate the data structure.
     * 
     * @param binary
     *            is the file binary?
     */
    public void setBinary(final boolean isBinary) {
        this.binary = isBinary;
    }

    /**
     * New in StatSVN: Updates a particular revision with new line count information. If the file or revision does not exist, action will do nothing.
     * 
     * Necessary because line counts are not given in the log file and hence can only be added in a second pass.
     * 
     * @param revisionNumber
     *            the revision number to be updated
     * @param linesAdded
     *            the lines that were added
     * @param linesRemoved
     *            the lines that were removed
     */
    public void updateRevision(final String revisionNumber, final int linesAdded, final int linesRemoved) {
        final RevisionData data = findRevision(revisionNumber);
        if (data != null) {
			data.setLines(linesAdded, linesRemoved);
		}
    }

}