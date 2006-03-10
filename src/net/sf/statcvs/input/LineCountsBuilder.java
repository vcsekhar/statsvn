package net.sf.statcvs.input;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LineCountsBuilder {
    private static final String REMOVED = "removed";
    private static final String ADDED = "added";
    private static final String NUMBER = "number";
    private static final String NAME = "name";
    private Document document = null;
    private Element lineCounts = null;
    private Element currentPath = null;
    private static final String LINECOUNTS = "lineCounts";
    private static final String PATH = "path";
    private static final String REVISION = "revision";
    private Map fileBuilders;
    private FileBuilder currentFileBuilder;

    public LineCountsBuilder(SvnLogBuilder builder) {
        fileBuilders = builder.getFileBuilders();
    }

    public Document getDocument() {
        return document;
    }

    public void buildRoot() throws ParserConfigurationException {
        DocumentBuilderFactory factoryDOM = DocumentBuilderFactory.newInstance();
        DocumentBuilder builderDOM;
        builderDOM = factoryDOM.newDocumentBuilder();
        document = builderDOM.newDocument();
        lineCounts = (Element) document.createElement(LINECOUNTS);
        document.appendChild(lineCounts);
    }

    public void buildPath(String name) {
        addPath(name);

        // get returns null if we have counts for files that don't (yet) exist
        // in the svn log.
        // or, we are operating on another project alltogether.
        currentFileBuilder = (FileBuilder) fileBuilders.get(name);
    }

    private void addPath(String name) {
        currentPath = (Element) document.createElement(PATH);
        Attr attr = document.createAttribute(NAME);
        attr.setTextContent(name);
        currentPath.setAttributeNode(attr);
        lineCounts.appendChild(currentPath);
    }

    public void buildRevision(String number, String added, String removed) {
        // we read linecounts from file but they aren't in the current project.
        if (currentFileBuilder == null)
            return;
        addRevision(number, added, removed);
        RevisionData data = currentFileBuilder.findRevision(number);
        if (data != null && !added.equals("-1") && !removed.equals("-1"))
            data.setLines(Integer.parseInt(added), Integer.parseInt(removed));
    }

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
