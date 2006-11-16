package net.sf.statcvs.output;

import net.sf.statcvs.model.Repository;

public class XDocOutput extends HTMLOutput {

    public XDocOutput(Repository content) {
        super(content);
    }

    /* (non-Javadoc)
     * @see net.sf.statcvs.output.HTMLOutput#getEndOfPage()
     */
    public String getEndOfPage() {
        return "</section></body>\n</document>";
    }

    /* (non-Javadoc)
     * @see net.sf.statcvs.output.HTMLOutput#getFileExtension()
     */
    public String getFileExtension() {
        return ".xml";
    }

    /* (non-Javadoc)
     * @see net.sf.statcvs.output.HTMLOutput#getHeader(java.lang.String)
     */
    public String getHeader(String pageName) {
        return "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n"
            + "<document xmlns:lxslt=\"http://xml.apache.org/xslt\">\n"
            + "<properties><title>" + pageName + "</title></properties>\n"
            + "<head>\n"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; "
            + "charset=ISO-8859-1\"/>\n"
            + "    <meta name=\"Generator\" content=\"StatSVN v0.1.3\"/>\n"
            + "    <link rel=\"stylesheet\" href=\""
            + ConfigurationOptions.getCssHandler().getLink()
            + "\" type=\"text/css\"/>\n"
            + "  </head>\n\n"
            + "<body>";
    }

    public String startSection1(final String title) {
        return "<section name=\"" + title + "\">\n";
    }

    public String endSection1() {
        return "</section>";
    }

    public String startSection2(final String title) {
        return "\n<subsection name =\"" + title + "\">\n";
    }

    public String endSection2() {
        return "</subsection>";
    }

    public String getTableFormat() {
        return " class=\"statCvsTable\"";
    }

//    /**
//     * We let Maven decide
//     */
//    public String getOddRowFormat() {
//        return "";
//    }
//
//    /**
//     * We let Maven decide
//     */
//    public String getEvenRowFormat() {
//        return "";
//    }
}
