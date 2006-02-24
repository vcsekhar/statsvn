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
 
 $RCSfile: SvnLogUtils.java,v $
 $Date: 2003/12/16 15:12:50 $
 */
package net.sf.statcvs.util;

import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.statcvs.output.ConfigurationOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class containing various methods related to CVS logfile parsing
 * 
 * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: SvnLogUtils.java,v 1.4 2003/12/16 15:12:50 cyganiak Exp $
 */
public class SvnLogUtils {

    protected static String moduleName = null;
    protected static String revisionNumber = null;
    protected static boolean isDirectory = false;

    protected static class SvnModuleNameFinder extends DefaultHandler {
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String eName = localName; // element name
            if ("".equals(eName))
                eName = qName; // namespaceAware = false

            if (eName.equals("entry")) {
                // the root folder directory
                if (attributes != null && attributes.getValue("name") != null && attributes.getValue("kind") != null && attributes.getValue("name").equals("")
                        && attributes.getValue("kind").equals("dir")) {
                    moduleName = attributes.getValue("url");
                }
            }
        }
    }

    protected static class SvnCurrentRevisionFinder extends DefaultHandler {
        String filename;

        public SvnCurrentRevisionFinder(String filename) {
            this.filename = filename;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String eName = localName; // element name
            if ("".equals(eName))
                eName = qName; // namespaceAware = false

            if (eName.equals("entry")) {
                // the root folder directory
                if (attributes != null && attributes.getValue("name") != null && attributes.getValue("name").equals(filename))
                    if (attributes.getValue("committed-rev") != null)
                        revisionNumber = attributes.getValue("committed-rev");
                    else if (attributes.getValue("kind") != null && attributes.getValue("kind").equals("dir"))
                        isDirectory = true;
            }
        }

    }

    /**
     * <p>
     * Determines if a file is in the attic by comparing the location of the RCS file and the working file.
     * </p>
     * 
     * <p>
     * The RCS file is the file containing the version history. It is located in the CVSROOT directory of the repository. It's name ends in ",v". The filename
     * is absoulte.
     * </p>
     * 
     * <p>
     * The working filename is the actual filename relative to the root of the checked-out module.
     * </p>
     * 
     * <p>
     * A file is said to be in the attic if and only if it is dead on the main branch. If a file is in the attic, it's RCS file is moved to a subdirectory
     * called "Attic". This method checks if the RCS file is in the "Attic" subdirectory.
     * 
     * @param rcsFilename
     *            a version-controlled file's RCS filename
     * @param workingFilename
     *            a version-controlled file's working filename
     * @return <tt>true</tt> if the file is in the attic
     */
    public static boolean isInAttic(String rcsFilename, String workingFilename) {
        int lastDelim = workingFilename.lastIndexOf("/");
        String filename = workingFilename.substring(lastDelim + 1, workingFilename.length());

        int rcsPathLength = rcsFilename.length() - filename.length() - 2;
        String rcsPath = rcsFilename.substring(0, rcsPathLength);
        return rcsPath.endsWith("/Attic/");
    }

    /**
     * Determines the module name by comparing the RCS filename and the working filename.
     * 
     * @param rcsFilename
     *            a version-controlled file's RCS filename
     * @param workingFilename
     *            a version-controlled file's working filename
     * @return the module name
     */
    // public static String getModuleName(String rcsFilename, String workingFilename) {
    // int localLenght = workingFilename.length() + ",v".length();
    // if (SvnLogUtils.isInAttic(rcsFilename, workingFilename)) {
    // localLenght += "/Attic".length();
    // }
    // String cvsroot = rcsFilename.substring(0,
    // rcsFilename.length() - localLenght - 1);
    // int lastSlash = cvsroot.lastIndexOf("/");
    // if (lastSlash == -1) {
    // return "";
    // }
    // return cvsroot.substring(lastSlash + 1);
    // }
    /**
     * Returns the path of the root of the workspace in the repository.
     * 
     * @return the module name
     */
    public static String getModuleName() {
        if (moduleName == null) {
            String entries = FileUtils.getAbsoluteName(ConfigurationOptions.getCheckedOutDirectory(), ".svn" + FileUtils.getDirSeparator() + "entries");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                parser.parse(entries, new SvnModuleNameFinder());
            } catch (Exception e) {
                moduleName = "";
            }
        }

        return moduleName;
    }

    public static String getRelativeFileName(String sFileName) {

        if (sFileName.length() <= 1)
            return "/";

        if (sFileName.indexOf('/', 1) < 0)
            return sFileName;
        String prefix = sFileName.substring(0, sFileName.indexOf('/', 1));

        int size = getModuleName().length() - getModuleName().lastIndexOf(prefix);

        if (size <= 0 || sFileName.length() <= size)
            return sFileName = "/";
        else {
            return sFileName.substring(size);
        }
    }

    public static String getRevisionNumber(String filename) throws IOException {

        String entries;
        String shortFileName = "";
        String baseDir = FileUtils.getParentDirectoryPath(filename);

        if (filename.length() <= 1 || baseDir.equals(FileUtils.getDirSeparator()))
            entries = FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) + FileUtils.getDirSeparator() + ".svn"
                    + FileUtils.getDirSeparator() + "entries";
        else {
            shortFileName = filename.substring(baseDir.length());
            entries = FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) + FileUtils.getDirSeparator() + baseDir + ".svn"
                    + FileUtils.getDirSeparator() + "entries";

        }

        entries = entries.replace('/', FileUtils.getDirSeparator().charAt(0));

        revisionNumber = null;
        isDirectory = false;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(entries, new SvnCurrentRevisionFinder(shortFileName));
        } catch (Exception e) {
        }

        if (revisionNumber == "")
            return null;

        return revisionNumber;
    }

    /**
     * Returns <tt>true</tt> if files with a given keyword substitution should be treated as binary files. That is, they should be assumed to be 0 lines of
     * code. Possible values are the same as for the -kXXX option of CVS (for example, kv, kvl, b).
     * 
     * @param kws
     *            the keyword substitution, as of CVS option -kXXX
     * @return <tt>true</tt> if this is a binary keyword substitution
     * @throws IllegalArgumentException
     *             if <tt>kws</tt> is not a known keyword substitution
     */
    public static boolean isBinaryKeywordSubst(String kws) {
        if ("kv".equals(kws)) {
            return false;
        }
        if ("kvl".equals(kws)) {
            return false;
        }
        if ("k".equals(kws)) {
            return false;
        }
        if ("o".equals(kws)) {
            return false;
        }
        if ("b".equals(kws)) {
            return true;
        }
        if ("v".equals(kws)) {
            return false;
        }
        if ("u".equals(kws)) {
            return false;
        }
        throw new IllegalArgumentException("unknown keyword substitution: " + kws);
    }

    public static boolean isDirectory() {
        return isDirectory;
    }
}
