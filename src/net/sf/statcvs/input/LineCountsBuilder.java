package net.sf.statcvs.input;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * CVS log files include lines modified for each commit while SVN log files do not offer this additional information.
 * </p>
 * 
 * <p>
 * StatSVN must query the Subversion repository for this information using svn diff. However, this is very costly, performance-wise. Therefore, the decision was
 * taken to persist this information in an XML file. This class receives information from (@link net.sf.statcvs.input.SvnXmlLineCountsFileHandler) to build a
 * DOM-based xml structure. It also forwards line counts to the appropriate (@link net.sf.statcvs.input.FileBuilder).
 * </p>
 * 
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * @version $Id$
 */
public class LineCountsBuilder {
    private static final String ADDED = "added";
    private static final String LINECOUNTS = "lineCounts";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String PATH = "path";
    private static final String REMOVED = "removed";
    private static final String REVISION = "revision";
    private FileBuilder currentFileBuilder;
    private Element currentPath = null;
    private Document document = null;
    private Map fileBuilders;
    private Element lineCounts = null;

    /**
     * Constructs the LineCountsBuilder by giving it a reference to the builder currently in use.
     * 
     * @param builder
     *            the SvnLogBuilder which contains all the FileBuilders.
     */
    public LineCountsBuilder(SvnLogBuilder builder) {
        fileBuilders = builder.getFileBuilders();
    }

    /**
     * Adds a path in the DOM. To be followed by invocations to (@link #addRevision(String, String, String))
     * 
     * @param name
     *            the filename
     */
    private void addPath(String name) {
        currentPath = (Element) document.createElement(PATH);
        Attr attr = document.createAttribute(NAME);
        attr.setTextContent(name);
        currentPath.setAttributeNode(attr);
        lineCounts.appendChild(currentPath);
    }

    /**
     * Adds a revision to the current path in the DOM. To be preceeded by (@link #addPath(String))
     * 
     * @param number
     *            the revision number
     * @param added
     *            the number of lines that were added
     * @param removed
     *            the number of lines that were removed
     */
    private void addRevision(String number, String added, String removed) {
        Element revision = (Element) document.createElement(REVISION);
        Attr attrRev1 = document.createAttribute(NUMBER);
        attrRev1.setTextContent(number);
        revision.setAttributeNode(attrRev1);
        Attr attrRev2 = document.createAttribute(ADDED);
        attrRev2.setTextContent(added);
        revision.setAttributeNode(attrRev2);
        Attr attrRev3 = document.createAttribute(REMOVED);
        attrRev3.setTextContent(removed);
        revision.setAttributeNode(attrRev3);
        currentPath.appendChild(revision);
    }

    /**
     * Initializes the builder for subsequent invocations of (@link #buildRevision(String, String, String)).
     * 
     * @param name
     *            the filename
     */
    public void buildPath(String name) {
        addPath(name);

        // get returns null if we have counts for files that don't (yet) exist
        // in the svn log.
        // or, we are operating on another project alltogether.
        currentFileBuilder = (FileBuilder) fileBuilders.get(name);
    }

    /**
     * Given the file specified by the preceeding invocation to (@link #buildPath(String)), set the line counts for the given revision.
     * 
     * If the path given in the preceeding invocation to (@link #buildPath(String)) is not used by the (@link SvnLogBuilder), this call does nothing.
     * 
     * @param number
     *            the revision number
     * @param added
     *            the number of lines added
     * @param removed
     *            the number of lines removed.
     */
    public void buildRevision(String number, String added, String removed) {
        // we read linecounts from file but they aren't in the current project.
        if (currentFileBuilder == null)
            return;
        addRevision(number, added, removed);
        RevisionData data = currentFileBuilder.findRevision(number);
        if (data != null && !added.equals("-1") && !removed.equals("-1"))
            data.setLines(Integer.parseInt(added), Integer.parseInt(removed));
    }

    /**
     * Builds the DOM root.
     * 
     * @throws ParserConfigurationException
     */
    public void buildRoot() throws ParserConfigurationException {
        DocumentBuilderFactory factoryDOM = DocumentBuilderFactory.newInstance();
        DocumentBuilder builderDOM;
        builderDOM = factoryDOM.newDocumentBuilder();
        document = builderDOM.newDocument();
        lineCounts = (Element) document.createElement(LINECOUNTS);
        document.appendChild(lineCounts);
    }

    /**
     * Returns the DOM object when building is complete.
     * 
     * @return the DOM document.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Adds a revision to the DOM.
     * 
     * Encapsulates calls to (@link #buildRoot()), (@link #buildPath(String)), and (@link #buildRevision(String, String, String)) into one easy to use
     * interface.
     * 
     * 
     * @param name
     *            the filename
     * @param number
     *            the revision number
     * @param added
     *            the number of lines added
     * @param removed
     *            the number of lines removed
     */
    public void newRevision(String name, String number, String added, String removed) {
        if (document == null) {
            try {
                buildRoot();
            } catch (ParserConfigurationException e) {
                document = null;
            }
        }
        if (document != null) {
            addPath(name);
            addRevision(number, added, removed);
        }
    }

}
