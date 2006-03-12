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
 
 $RCSfile: Builder.java,v $
 $Date: 2004/12/14 13:38:13 $
 */
package net.sf.statcvs.input;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.SymbolicName;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.util.FilePatternMatcher;
import net.sf.statcvs.util.FileUtils;

/**
 * <p>
 * Helps building the {@link net.sf.statcvs.model.Repository} from a SVN log. The <tt>Builder</tt> is fed by some SVN history data source, for example a SVN
 * log parser. The <tt>Repository</tt> can be retrieved using the {@link #createRepository} method.
 * </p>
 * 
 * <p>
 * The class also takes care of the creation of <tt>Author</tt> and </tt>Directory</tt> objects and makes sure that there's only one of these for each
 * author name and path. It also provides LOC count services.
 * </p>
 * 
 * @author Richard Cyganiak <richard@cyganiak.de>
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 * 
 */
public class Builder implements SvnLogBuilder {
    private static Logger logger = Logger.getLogger(Builder.class.getName());
    private final Set atticFileNames = new HashSet();
    private final Map authors = new HashMap();
    private FileBuilder currentFileBuilder = null;
    private final Map directories = new HashMap();
    private final FilePatternMatcher excludePattern;
    private final Map fileBuilders = new HashMap();
    private final FilePatternMatcher includePattern;
    private String projectName = null;
    private final RepositoryFileManager repositoryFileManager;
    private Date startDate = null;
    private final Map symbolicNames = new HashMap();

    /**
     * Creates a new <tt>Builder</tt>
     * 
     * @param repositoryFileManager
     *            the {@link RepositoryFileManager} that can be used to retrieve LOC counts for the files that this builder will create
     * @param includePattern
     *            a list of Ant-style wildcard patterns, seperated by : or ;
     * @param excludePattern
     *            a list of Ant-style wildcard patterns, seperated by : or ;
     */
    public Builder(RepositoryFileManager repositoryFileManager, FilePatternMatcher includePattern, FilePatternMatcher excludePattern) {
        this.repositoryFileManager = repositoryFileManager;
        this.includePattern = includePattern;
        this.excludePattern = excludePattern;
        directories.put("", Directory.createRoot());
    }

    /**
     * Adds a file to the attic. This method should only be called if our first invocation to (@link #buildFile(String, boolean, boolean, Map)) was given an
     * invalid isInAttic field.
     * 
     * This is a hack to handle post-processing of implicit deletions at the same time as the implicit additions that can be found in Subversion.
     * 
     * @param filename
     *            the filename to add to the attic.
     */
    public void addToAttic(String filename) {
        if (!atticFileNames.contains(filename)) {
            atticFileNames.add(filename);
        }
    }

    /**
     * <p>
     * Starts building a new file. The files are not expected to be created in any particular order. Subsequent calls to (@link #buildRevision(RevisionData))
     * will add revisions to this file.
     * </p>
     * 
     * <p>
     * New in StatSVN: If the method has already been invoked with the same filename, the original file will be re-loaded and the other arguments are ignored.
     * </p>
     * 
     * @param filename
     *            the file's name with path, for example "path/file.txt"
     * @param isBinary
     *            <tt>true</tt> if it's a binary file
     * @param isInAttic
     *            <tt>true</tt> if the file is dead on the main branch
     * @param revBySymnames
     *            maps revision (string) by symbolic name (string) (Not used in StatSVN; kept for compatibility with StatCVS)
     */
    public void buildFile(String filename, boolean isBinary, boolean isInAttic, Map revBySymnames) {
        if (fileBuilders.containsKey(filename))
            currentFileBuilder = (FileBuilder) fileBuilders.get(filename);
        else {
            currentFileBuilder = new FileBuilder(this, filename, isBinary, revBySymnames);
            fileBuilders.put(filename, currentFileBuilder);
            if (isInAttic)
                addToAttic(filename);
        }
    }

    /**
     * Starts building the module.
     * 
     * @param moduleName
     *            name of the module
     */
    public void buildModule(String moduleName) {
        this.projectName = moduleName;
    }

    /**
     * Adds a revision to the current file. The revisions must be added in SVN logfile order, that is starting with the most recent one.
     * 
     * @param data
     *            the revision
     */
    public void buildRevision(RevisionData data) {

        currentFileBuilder.addRevisionData(data);

        if (startDate == null || startDate.compareTo(data.getDate()) > 0) {
            startDate = data.getDate();
        }
    }

    /**
     * Returns a Repository object of all files.
     * 
     * @return Repository a Repository object
     * @throws EmptyRepositoryException
     *             if no adequate files were found in the log.
     */
    public Repository createRepository() throws EmptyRepositoryException {

        if (startDate == null) {
            throw new EmptyRepositoryException();
        }

        Repository result = new Repository();
        Iterator it = fileBuilders.values().iterator();
        while (it.hasNext()) {
            FileBuilder fileBuilder = (FileBuilder) it.next();
            VersionedFile file = fileBuilder.createFile(startDate);
            if (file == null) {
                continue;
            }
            result.addFile(file);
            logger.finer("adding " + file.getFilenameWithPath() + " (" + file.getRevisions().size() + " revisions)");
        }

        if (result.isEmpty()) {
            throw new EmptyRepositoryException();
        }

        // Uh oh...
        SortedSet revisions = result.getRevisions();
        List commits = new CommitListBuilder(revisions).createCommitList();
        result.setCommits(commits);

        result.setSymbolicNames(new TreeSet(symbolicNames.values()));

        return result;
    }

    /**
     * Returns the <tt>Set</tt> of filenames that are "in the attic".
     * 
     * @return a <tt>Set</tt> of <tt>String</tt>s
     */
    public Set getAtticFileNames() {
        return atticFileNames;
    }

    /**
     * returns the <tt>Author</tt> of the given name or creates it if it does not yet exist. Author names are handled as case-insensitive.
     * 
     * @param name
     *            the author's name
     * @return a corresponding <tt>Author</tt> object
     */
    public Author getAuthor(String name) {
        if (this.authors.containsKey(name.toLowerCase())) {
            return (Author) this.authors.get(name.toLowerCase());
        }
        Author newAuthor = new Author(name);
        this.authors.put(name.toLowerCase(), newAuthor);
        return newAuthor;
    }

    /**
     * Returns the <tt>Directory</tt> of the given filename or creates it if it does not yet exist.
     * 
     * @param filename
     *            the name and path of a file, for example "src/Main.java"
     * @return a corresponding <tt>Directory</tt> object
     */
    public Directory getDirectory(String filename) {
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash == -1) {
            return getDirectoryForPath("");
        }
        return getDirectoryForPath(filename.substring(0, lastSlash + 1));
    }

    /**
     * @param path
     *            for example "src/net/sf/statcvs/"
     * @return the <tt>Directory</tt> corresponding to <tt>statcvs</tt>
     */
    private Directory getDirectoryForPath(String path) {
        if (directories.containsKey(path)) {
            return (Directory) directories.get(path);
        }
        Directory parent = getDirectoryForPath(FileUtils.getParentDirectoryPath(path));
        Directory newDirectory = parent.createSubdirectory(FileUtils.getDirectoryName(path));
        directories.put(path, newDirectory);
        return newDirectory;
    }

    /**
     * New in StatSVN: We need to have access to FileBuilders after they have been created to populate them with version numbers later on.
     * 
     * TODO: Beef up this interface to better encapsulate the data structure.
     * 
     * @return this builder's contained (@link FileBuilder)s.
     */
    public Map getFileBuilders() {
        return fileBuilders;
    }

    /**
     * @see RepositoryFileManager#getLinesOfCode(String)
     */
    public int getLOC(String filename) throws NoLineCountException {
        if (repositoryFileManager == null) {
            throw new NoLineCountException("no RepositoryFileManager");
        }

        return repositoryFileManager.getLinesOfCode(filename);
    }

    public String getProjectName() {
        return projectName;
    }

    /**
     * @see RepositoryFileManager#getRevision(String)
     */
    public String getRevision(String filename) throws IOException {
        if (repositoryFileManager == null) {
            throw new IOException("no RepositoryFileManager");
        }
        return repositoryFileManager.getRevision(filename);
    }

    /**
     * Returns the {@link SymbolicName} with the given name or creates it if it does not yet exist.
     * 
     * @param name
     *            the symbolic name's name
     * @return the corresponding symbolic name object
     */
    public SymbolicName getSymbolicName(String name) {
        SymbolicName sym = (SymbolicName) symbolicNames.get(name);

        if (sym != null) {
            return sym;
        } else {
            sym = new SymbolicName(name);
            symbolicNames.put(name, sym);

            return sym;
        }
    }

    /**
     * Matches a filename against the include and exclude patterns. If no include pattern was specified, all files will be included. If no exclude pattern was
     * specified, no files will be excluded.
     * 
     * @param filename
     *            a filename
     * @return <tt>true</tt> if the filename matches one of the include patterns and does not match any of the exclude patterns. If it matches an include and
     *         an exclude pattern, <tt>false</tt> will be returned.
     */
    public boolean matchesPatterns(String filename) {
        if (excludePattern != null && excludePattern.matches(filename)) {
            return false;
        }
        if (includePattern != null) {
            return includePattern.matches(filename);
        }
        return true;
    }
}