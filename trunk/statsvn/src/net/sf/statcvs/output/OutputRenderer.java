package net.sf.statcvs.output;

import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Directory;

public interface OutputRenderer {

    /**
     * Returns the filename for a direcotry
     * @param directory a directory
     * @return filename for the directory page
     */
    public abstract String getDirectoryPageFilename(Directory directory, final boolean asLink);

    /**
     * @param author an author
     * @return filename for author's page
     */
    public abstract String getAuthorPageFilename(Author author, final boolean asLink);

    //+ New BX Section to allow HTML and XDOC output (these will be overwritten).
    public abstract String getFileExtension();

    public abstract String getLinkExtension();

    public abstract String getHeader(final String pageName);

    public abstract String getEndOfPage();

    public abstract String startSection1(final String title);

    public abstract String endSection1();

    public abstract String startSection2(final String title);

    public abstract String endSection2();

    public abstract String getOddRowFormat();

    public abstract String getEvenRowFormat();

    public abstract String getTableFormat();

}