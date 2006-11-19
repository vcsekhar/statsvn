package net.sf.statcvs.output;

import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.Directory;

public interface OutputRenderer {

    /**
     * Returns the filename for a directory
     * @param directory a directory
     * @return filename for the directory page
     */
    String getDirectoryPageFilename(Directory directory, final boolean asLink);

    /**
     * @param author an author
     * @return filename for author's page
     */
    String getAuthorPageFilename(Author author, final boolean asLink);

    //+ New BX Section to allow HTML and XDOC output (these will be overwritten).
    String getFileExtension();

    String getLinkExtension();

    String getHeader(final String pageName);

    String getEndOfPage();

    String startSection1(final String title);

    String endSection1();

    String startSection2(final String title);

    String endSection2();

    String getOddRowFormat();

    String getEvenRowFormat();

    String getTableFormat();
}