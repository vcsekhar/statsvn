package net.sf.statcvs.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.statcvs.util.SvnLogUtils;
import net.sf.statcvs.util.SvnPropgetUtils;
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SvnXmlLogFileHandler extends DefaultHandler {

    private static final String AUTHOR = "author";
    private static final String DATE = "date";
    private static final String LOG = "log";
    private static final String LOGENTRY = "logentry";
    private static Logger logger = Logger.getLogger(SvnXmlLogFileHandler.class.getName());
    private static final String MSG = "msg";
    private static final String PATH = "path";
    private static final String PATHS = "paths";
    private SvnLogBuilder builder;
    private RevisionData currentRevisionData;
    private ArrayList currentRevisions;
    private ArrayList currentFilenames;
    private String lastElement = "";
    private String currentElement = "";
    private String pathAction = "";
    private String stringData = "";

    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        stringData += new String(ch, start, length);
    }

    public SvnXmlLogFileHandler(SvnLogBuilder builder) {
        this.builder = builder;
    }

    public void endDocument() throws SAXException {
        super.endDocument();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        String eName = localName; // element name
        if ("".equals(eName))
            eName = qName; // namespaceAware = false

        if (eName.equals(LOG)) {
            if (!lastElement.equals(LOG)) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = "";
            currentElement = "";
        } else if (eName.equals(LOGENTRY)) {
            if (!lastElement.equals(LOGENTRY)) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = LOG;
            currentElement = LOG;
            for (int i = 0; i < currentFilenames.size(); i++) {
                RevisionData revisionData = (RevisionData) currentRevisions.get(i);
                revisionData.setComment(currentRevisionData.getComment());
                revisionData.setDate(currentRevisionData.getDate());
                revisionData.setLoginName(currentRevisionData.getLoginName());

                String currentFilename = currentFilenames.get(i).toString();
                // check to see if binary in local copy (cached)
                boolean isBinary = SvnPropgetUtils.getBinaryFiles().contains(currentFilename);
                // is this a deletion?
                if (revisionData.isDeletion()) {
                    FileBuilder existingBuilder = (FileBuilder) builder.getFileBuilders().get(currentFilename);
                    // the deletion is the last revision of this file?
                    if (existingBuilder != null && !existingBuilder.existRevision())
                        // query the history to know if it is binary
                        isBinary = SvnPropgetUtils.isBinaryFile(revisionData.getRevisionNumber(), currentFilename);
                }

                builder.buildFile(currentFilename, isBinary, revisionData.isDeletion(), new HashMap());
                builder.buildRevision(revisionData);
            }

        } else if (eName.equals(AUTHOR) || eName.equals(DATE) || eName.equals(MSG)) {
            if (!lastElement.equals(LOGENTRY)) {
                fatalError("Invalid SVN log file.");
            }
            if (currentElement.equals(AUTHOR)) {
                currentRevisionData.setLoginName(stringData);
            } else if (currentElement.equals(DATE)) {
                Date dt;
                try {
                    dt = XMLUtil.parseXsdDateTime(stringData);
                    currentRevisionData.setDate(dt);
                } catch (ParseException e) {
                    warning("Invalid date specified.");
                }
            } else if (currentElement.equals(MSG)) {
                currentRevisionData.setComment(stringData);
            }
            currentElement = LOGENTRY;
        } else if (eName.equals(PATHS)) {
            if (!lastElement.equals(PATHS)) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = LOGENTRY;
            currentElement = LOGENTRY;
        } else if (eName.equals(PATH)) {
            if (!lastElement.equals(PATHS)) {
                fatalError("Invalid SVN log file.");
            }
            String filename = SvnLogUtils.getRelativeFileName(stringData);

            RevisionData data = (RevisionData) currentRevisionData.clone();
            if (!pathAction.equals("D")) {
                data.setStateExp();
            } else {
                data.setStateDead();
            }
            if (pathAction.equals("M")) {
               // data.setLines(10, 50);
            }
            // if (pathAction.equals("A")) {
            // data(0, 0);
            // }
            if (!filename.endsWith("/") && filename.length() > 0) {
                currentRevisions.add(data);
                currentFilenames.add(filename.substring(1));
            }
            currentElement = PATHS;
        } else {
            fatalError("Invalid SVN log file.");
        }
    }

    public void error(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.error(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.fatalError(e);
    }

    private void fatalError(String message) throws SAXException {
        fatalError(new SAXParseException(message, null));
    }

    public void startDocument() throws SAXException {
        super.startDocument();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
        stringData = "";

        String eName = localName; // element name
        if ("".equals(eName))
            eName = qName; // namespaceAware = false
        // if (attrs != null) {
        // for (int i = 0; i < attrs.getLength(); i++) {
        // String aName = attrs.getLocalName(i); // Attr name
        // if ("".equals(aName)) aName = attrs.getQName(i);
        // }
        // }

        currentElement = eName;
        if (eName.equals(LOG)) {
            if (!lastElement.equals("")) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = LOG;

            SvnPropgetUtils.getBinaryFiles();
            builder.buildModule(SvnLogUtils.getModuleName());

        } else if (eName.equals(LOGENTRY)) {
            if (!lastElement.equals(LOG)) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = LOGENTRY;
            currentRevisionData = new RevisionData();
            currentRevisions = new ArrayList();
            currentFilenames = new ArrayList();

            if (attributes != null && attributes.getValue("revision") != null)
                currentRevisionData.setRevisionNumber(attributes.getValue("revision"));
            else
                fatalError("Invalid SVN log file.");

        } else if (eName.equals(AUTHOR) || eName.equals(DATE) || eName.equals(MSG)) {
            if (!lastElement.equals(LOGENTRY)) {
                fatalError("Invalid SVN log file.");
            }

        } else if (eName.equals(PATHS)) {
            if (!lastElement.equals(LOGENTRY)) {
                fatalError("Invalid SVN log file.");
            }
            lastElement = PATHS;
        } else if (eName.equals(PATH)) {
            if (!lastElement.equals(PATHS)) {
                fatalError("Invalid SVN log file.");
            }

            if (attributes != null && attributes.getValue("action") != null)
                pathAction = attributes.getValue("action");
            else
                fatalError("Invalid SVN log file.");

        } else {
            fatalError("Invalid SVN log file.");
        }
    }

    public void warning(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.warning(e);
    }

    private void warning(String message) throws SAXException {
        warning(new SAXParseException(message, null));
    }

}
