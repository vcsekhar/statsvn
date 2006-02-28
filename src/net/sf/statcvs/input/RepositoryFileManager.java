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
 
 $RCSfile: RepositoryFileManager.java,v $ 
 Created on $Date: 2004/12/14 13:38:13 $ 
 */
package net.sf.statcvs.input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import net.sf.statcvs.util.FileUtils;
import net.sf.statcvs.util.SvnInfoUtils;

/**
 * Manages a checked-out repository and provides access to line number counts for repository files.
 * 
 * @author Manuel Schulze
 * @author Steffen Pingel
 * @version $Id: RepositoryFileManager.java,v 1.24 2004/12/14 13:38:13 squig Exp $
 */
public class RepositoryFileManager {
    private static Logger logger;
    private String path;

    /**
     * Creates a new instance with root at <code>pathName</code>.
     * 
     * @param pathName
     *            the root of the checked out repository
     */
    public RepositoryFileManager(String pathName) {
        path = pathName;
        logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Returns the lines of code for a repository file.
     * 
     * @param filename
     *            a file in the repository
     * @return the lines of code for a repository file
     * @throws NoLineCountException
     *             when the line count could not be retrieved, for example when the file was not found.
     */
    public int getLinesOfCode(String filename) throws NoLineCountException {
        final String absoluteName = FileUtils.getAbsoluteName(this.path, filename);
        try {
            FileReader freader = new FileReader(absoluteName);
            BufferedReader reader = new BufferedReader(freader);
            int linecount = getLineCount(reader);
            logger.finer("line count for '" + absoluteName + "': " + linecount);
            freader.close();
            return linecount;
        } catch (IOException e) {
            throw new NoLineCountException("could not get line count for '" + absoluteName + "': " + e);
        }
    }

    private int getLineCount(BufferedReader reader) throws IOException {
        int linecount = 0;
        while (reader.readLine() != null) {
            linecount++;
        }
        return linecount;
    }

    /**
     * Returns the revision of filename in the local working directory by reading the ./svn/entries file.
     * 
     * @param filename
     *            the filename
     * @return the revision of filename
     */
    public String getRevision(String filename) throws IOException {
        String rev = SvnInfoUtils.getRevisionNumber(filename);
        if (rev != null) {
            return rev;
        } else if (!SvnInfoUtils.isDirectory(filename))
            throw new IOException("File " + filename + " has no revision");
        else
            return null;
    }

}
