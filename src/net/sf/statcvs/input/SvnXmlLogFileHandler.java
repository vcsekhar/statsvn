package net.sf.statcvs.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.sf.statcvs.util.SvnInfoUtils;
import net.sf.statcvs.util.SvnPropgetUtils;
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SvnXmlLogFileHandler extends DefaultHandler {

    private static final String FATAL_ERROR_MESSAGE = "Invalid SVN log file.";
    private static final String AUTHOR = "author";
    private static final String DATE = "date";
    private static final String LOG = "log";
    private static final String LOGENTRY = "logentry";
    private static final String MSG = "msg";
    private static final String PATH = "path";
    private static final String PATHS = "paths";
    private SvnLogBuilder builder;
    private RevisionData currentRevisionData;
    private ArrayList currentRevisions;
    private ArrayList currentFilenames;
    private String lastElement = "";
    private String pathAction = "";
    private String stringData = "";
    private boolean isFirstPath;

    public SvnXmlLogFileHandler(SvnLogBuilder builder) {
        this.builder = builder;
        isFirstPath = true;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        stringData += new String(ch, start, length);
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
            endLog();
        } else if (eName.equals(LOGENTRY)) {
            endLogEntry();
        } else if (eName.equals(AUTHOR)) {
            endAuthor();
        } else if (eName.equals(DATE)) {
            endDate();
        } else if (eName.equals(MSG)) {
            endMsg();
        } else if (eName.equals(PATHS)) {
            endPaths();
        } else if (eName.equals(PATH)) {
            endPath();
        } else {
            fatalError("Invalid SVN log file.");
        }
    }

    private void endPath() throws SAXException {
        checkLastElement(PATHS);
        if (isFirstPath) {
            isFirstPath = false;
            try {
                SvnInfoUtils.loadInfo(stringData);
                // we can't get the module name until we have one file to see the loader
                builder.buildModule(SvnInfoUtils.getModuleName());
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }
        String filename = SvnInfoUtils.absoluteToRelativePath(stringData);
        RevisionData data = (RevisionData) currentRevisionData.clone();
        if (!pathAction.equals("D")) {
            data.setStateExp();
        } else {
            data.setStateDead();
        }
        if (!SvnInfoUtils.isDirectory(filename)) {
            currentRevisions.add(data);
            currentFilenames.add(filename);
        }
    }

    private void checkLastElement(String last) throws SAXException {
        if (!lastElement.equals(last)) {
            fatalError(FATAL_ERROR_MESSAGE);
        }
    }

    private void endPaths() throws SAXException {
        checkLastElement(PATHS);
        lastElement = LOGENTRY;
    }

    private void endAuthor() throws SAXException {
        checkLastElement(LOGENTRY);
        currentRevisionData.setLoginName(stringData);
    }

    private void endDate() throws SAXException {
        checkLastElement(LOGENTRY);
        Date dt;
        try {
            dt = XMLUtil.parseXsdDateTime(stringData);
            currentRevisionData.setDate(dt);
        } catch (ParseException e) {
            warning("Invalid date specified.");
        }
    }

    private void endMsg() throws SAXException {
        checkLastElement(LOGENTRY);
        currentRevisionData.setComment(stringData);
    }

    private void endLogEntry() throws SAXException {
        checkLastElement(LOGENTRY);
        lastElement = LOG;

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
                if (existingBuilder==null || (existingBuilder != null && !existingBuilder.existRevision())) {
                    // query the history to know if it is binary
                    isBinary = SvnPropgetUtils.isBinaryFile(revisionData.getRevisionNumber(), currentFilename);
                }
            }
            builder.buildFile(currentFilename, isBinary, revisionData.isDeletion(), new HashMap());
            builder.buildRevision(revisionData);
        }
        
    }

    private void endLog() throws SAXException {
        checkLastElement(LOG);
        lastElement = "";
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
        SvnPropgetUtils.getBinaryFiles();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
        stringData = "";
        String eName = localName; // element name
        if ("".equals(eName))
            eName = qName; // namespaceAware = false
        if (eName.equals(LOG)) {
            startLog();
        } else if (eName.equals(LOGENTRY)) {
            startLogEntry(attributes);
        } else if (eName.equals(AUTHOR) || eName.equals(DATE) || eName.equals(MSG)) {
            startAuthorDateMsg();
        } else if (eName.equals(PATHS)) {
            startPaths();
        } else if (eName.equals(PATH)) {
            startPath(attributes);
        } else {
            fatalError("Invalid SVN log file.");
        }
    }

    private void startPath(Attributes attributes) throws SAXException {
        checkLastElement(PATHS);
        if (attributes != null && attributes.getValue("action") != null)
            pathAction = attributes.getValue("action");
        else
            fatalError("Invalid SVN log file.");
    }

    private void startAuthorDateMsg() throws SAXException {
        checkLastElement(LOGENTRY);
    }

    private void startPaths() throws SAXException {
        checkLastElement(LOGENTRY);
        lastElement = PATHS;
    }

    private void startLogEntry(Attributes attributes) throws SAXException {
        checkLastElement(LOG);
        lastElement = LOGENTRY;
        currentRevisionData = new RevisionData();
        currentRevisions = new ArrayList();
        currentFilenames = new ArrayList();
        if (attributes != null && attributes.getValue("revision") != null)
            currentRevisionData.setRevisionNumber(attributes.getValue("revision"));
        else
            fatalError("Invalid SVN log file.");
    }

    private void startLog() throws SAXException {
        checkLastElement("");
        lastElement = LOG;
        // must delay until first load of path.
        // builder.buildModule(SvnInfoUtils.getModuleName());

    }

    public void warning(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub
        super.warning(e);
    }

    private void warning(String message) throws SAXException {
        warning(new SAXParseException(message, null));
    }

}
