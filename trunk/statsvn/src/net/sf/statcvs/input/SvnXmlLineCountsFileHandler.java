package net.sf.statcvs.input;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the SAX parser for the our line count persistence mechanism. It feeds information to (@link net.sf.statcvs.input.LineCountsBuilder).
 * 
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public class SvnXmlLineCountsFileHandler extends DefaultHandler {

    
    private static final String ADDED = "added";
    private static final String FATAL_ERROR_MESSAGE = "Invalid StatSvn line count file.";
    private static final String LINECOUNTS = "lineCounts";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String PATH = "path";
    private static final String REMOVED = "removed";
    private static final String REVISION = "revision";
    private String lastElement = "";
    private LineCountsBuilder lineCountsBuilder;

    /**
     * Default constructor
     * 
     * @param lineCountsBuilder
     *            the lineCountsBuilder to which to send back line count information.
     */
    public SvnXmlLineCountsFileHandler(LineCountsBuilder lineCountsBuilder) {
        this.lineCountsBuilder = lineCountsBuilder;
    }

    /**
     * Makes sure the last element received is appropriate.
     * 
     * @param last
     *            the expected last element.
     * @throws SAXException
     *             unexpected event.
     */
    private void checkLastElement(String last) throws SAXException {
        if (!lastElement.equals(last)) {
            fatalError(FATAL_ERROR_MESSAGE);
        }
    }

    /**
     * Handles the end of an xml element and redirects to the appropriate end* method.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String eName = localName; // element name
        if ("".equals(eName))
            eName = qName; // namespaceAware = false

        if (eName.equals(LINECOUNTS)) {
            endLineCounts();
        } else if (eName.equals(PATH)) {
            endPath();
        } else if (eName.equals(REVISION)) {
            endRevision();
        } else {
            fatalError(FATAL_ERROR_MESSAGE);
        }
    }

    /**
     * End of line counts element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endLineCounts() throws SAXException {
        checkLastElement(LINECOUNTS);
        lastElement = "";
    }

    /**
     * End of path element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endPath() throws SAXException {
        checkLastElement(PATH);
        lastElement = LINECOUNTS;
    }

    /**
     * End of revision element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endRevision() throws SAXException {
        checkLastElement(PATH);
    }

    /**
     * Throws a fatal error with the specified message.
     * 
     * @param message
     *            the reason for the error
     * @throws SAXException
     *             the error
     */
    private void fatalError(String message) throws SAXException {
        fatalError(new SAXParseException(message, null));
    }

    /**
     * Handles the start of an xml element and redirects to the appropriate start* method.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        String eName = localName; // element name
        if ("".equals(eName))
            eName = qName; // namespaceAware = false

        if (eName.equals(LINECOUNTS)) {
            startLineCounts();
        } else if (eName.equals(PATH)) {
            startPath(attributes);
        } else if (eName.equals(REVISION)) {
            startRevision(attributes);
        } else {
            fatalError(FATAL_ERROR_MESSAGE);
        }
    }

    /**
     * Handles the start of the document. Initializes the line count builder.
     * 
     * @throws SAXException
     *             unable to build the root.
     */
    private void startLineCounts() throws SAXException {
        checkLastElement("");
        lastElement = LINECOUNTS;
        try {
            lineCountsBuilder.buildRoot();
        } catch (ParserConfigurationException e) {
            fatalError(FATAL_ERROR_MESSAGE);
        }
    }

    /**
     * Handles start of a path. Initializes line count builder for use with this filename.
     * 
     * @param attributes
     *            element's xml attributes.
     * @throws SAXException
     *             missing some data.
     */
    private void startPath(Attributes attributes) throws SAXException {
        checkLastElement(LINECOUNTS);
        lastElement = PATH;
        if (attributes != null && attributes.getValue(NAME) != null) {
            String name = attributes.getValue(NAME);
            lineCountsBuilder.buildPath(name);
        } else
            fatalError(FATAL_ERROR_MESSAGE);
    }

    /**
     * Handles start of a revision. Gives information back to the line count builder.
     * 
     * @param attributes
     *            element's xml attributes.
     * @throws SAXException
     *             missing some data.
     */
    private void startRevision(Attributes attributes) throws SAXException {
        checkLastElement(PATH);
        if (attributes != null && attributes.getValue(NUMBER) != null && attributes.getValue(ADDED) != null && attributes.getValue(REMOVED) != null) {
            String number = attributes.getValue(NUMBER);
            String added = attributes.getValue(ADDED);
            String removed = attributes.getValue(REMOVED);
            lineCountsBuilder.buildRevision(number, added, removed);
        } else
            fatalError(FATAL_ERROR_MESSAGE);
    }

}
