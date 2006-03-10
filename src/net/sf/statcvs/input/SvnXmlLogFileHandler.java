package net.sf.statcvs.input;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.statcvs.util.SvnInfoUtils;
import net.sf.statcvs.util.SvnPropgetUtils;
import net.sf.statcvs.util.XMLUtil;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the SAX parser for the svn log in xml format. It feeds information to the (@link net.sf.statcvs.input.SvnLogBuilder).
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public class SvnXmlLogFileHandler extends DefaultHandler {

    private static Logger logger = Logger.getLogger(SvnXmlLogFileHandler.class.getName());
    private static final String INVALID_SVN_LOG_FILE = "Invalid SVN log file.";
    private static final String AUTHOR = "author";
    private static final String DATE = "date";
    private static final String FATAL_ERROR_MESSAGE = INVALID_SVN_LOG_FILE;
    private static final String LOG = "log";
    private static final String LOGENTRY = "logentry";
    private static final String MSG = "msg";
    private static final String PATH = "path";
    private static final String PATHS = "paths";
    private SvnLogBuilder builder;
    private ArrayList currentFilenames;
    private RevisionData currentRevisionData;
    private ArrayList currentRevisions;
    private boolean isFirstPath;
    private String lastElement = "";
    private String pathAction = "";
    private String stringData = "";

    /**
     * Default constructor.
     * 
     * @param builder
     *            where to send the information
     */
    public SvnXmlLogFileHandler(SvnLogBuilder builder) {
        this.builder = builder;
        isFirstPath = true;
    }

    /**
     * Builds the string that was read; default implementation can invoke this function multiple times while reading the data.
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        stringData += new String(ch, start, length);
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
     * End of author element. Saves author to the current revision.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endAuthor() throws SAXException {
        checkLastElement(LOGENTRY);
        currentRevisionData.setLoginName(stringData);
    }

    /**
     * End of date element. See (@link XMLUtil#parseXsdDateTime(String)) for parsing of the particular datetime format.
     * 
     * Saves date to the current revision.
     * 
     * @throws SAXException
     *             unexpected event.
     */
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
            fatalError(INVALID_SVN_LOG_FILE);
        }
    }

    /**
     * End of log element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endLog() throws SAXException {
        checkLastElement(LOG);
        lastElement = "";
    }

    /**
     * End of log entry element. For each file that was found, builds the file and revision in (@link SvnLogBuilder).
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endLogEntry() throws SAXException {
        checkLastElement(LOGENTRY);
        lastElement = LOG;

        for (int i = 0; i < currentFilenames.size(); i++) {
            RevisionData revisionData = (RevisionData) currentRevisions.get(i);
            revisionData.setComment(currentRevisionData.getComment());
            revisionData.setDate(currentRevisionData.getDate());
            revisionData.setLoginName(currentRevisionData.getLoginName());
            String currentFilename = currentFilenames.get(i).toString();

            boolean isBinary = false;
            // if this file is not in the current working folder, discard it.
            if (!SvnInfoUtils.existsInWorkingCopy(currentFilename)) {
                // continue;
                // isBinary=true;
            } else {
                // check to see if binary in local copy (cached)
                isBinary = SvnPropgetUtils.getBinaryFiles().contains(currentFilename);
            }

            builder.buildFile(currentFilename, isBinary, revisionData.isDeletion(), new HashMap());
            // FileBuilder fileBuilder = (FileBuilder) builder.getFileBuilders().get(currentFilename);
            // if (!fileBuilder.existRevision() || (fileBuilder.existRevision() && !fileBuilder.getFirstRevision().isCreation())) {
            builder.buildRevision(revisionData);
            // }

        }

    }

    /**
     * End of msg element. Saves comment to the current revision.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endMsg() throws SAXException {
        checkLastElement(LOGENTRY);
        currentRevisionData.setComment(stringData);
    }

    /**
     * End of path element. Builds a revision data for this element using the information that is known to date; rest is done in (@link #endLogEntry())
     * 
     * @throws SAXException
     *             unexpected event.
     */
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
            data.setStateExp(true);
            if (pathAction.equals("A") || pathAction.equals("R"))
                data.setStateAdded(true);
        } else {
            data.setStateDead(true);
        }

        // must add directories because of implicit additions
        // if (!SvnInfoUtils.isDirectory(filename)) {
        currentRevisions.add(data);
        currentFilenames.add(filename);
        // }
    }

    /**
     * End of paths element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void endPaths() throws SAXException {
        checkLastElement(PATHS);
        lastElement = LOGENTRY;
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
     * Start of author, date or message.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void startAuthorDateMsg() throws SAXException {
        checkLastElement(LOGENTRY);
    }

    /**
     * Start of the document. Pre-loads the binary files for future use.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        SvnPropgetUtils.getBinaryFiles();
    }

    /**
     * Handles the start of an xml element and redirects to the appropriate start* method.
     * 
     * @throws SAXException
     *             unexpected event.
     */
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
            fatalError(INVALID_SVN_LOG_FILE);
        }
    }

    /**
     * Start of the log element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void startLog() throws SAXException {
        checkLastElement("");
        lastElement = LOG;
        // must delay until first load of path.
        // builder.buildModule(SvnInfoUtils.getModuleName());

    }

    /**
     * Start of the log entry element. Initializes information, to be filled during this log entry and used in (@link #endLogEntry())
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void startLogEntry(Attributes attributes) throws SAXException {
        checkLastElement(LOG);
        lastElement = LOGENTRY;
        currentRevisionData = new RevisionData();
        currentRevisions = new ArrayList();
        currentFilenames = new ArrayList();
        if (attributes != null && attributes.getValue("revision") != null)
            currentRevisionData.setRevisionNumber(attributes.getValue("revision"));
        else
            fatalError(INVALID_SVN_LOG_FILE);
    }

    /**
     * Start of the path element. Saves the path action.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void startPath(Attributes attributes) throws SAXException {
        checkLastElement(PATHS);
        if (attributes != null && attributes.getValue("action") != null)
            pathAction = attributes.getValue("action");
        else
            fatalError(INVALID_SVN_LOG_FILE);
    }

    /**
     * Start of the paths element.
     * 
     * @throws SAXException
     *             unexpected event.
     */
    private void startPaths() throws SAXException {
        checkLastElement(LOGENTRY);
        lastElement = PATHS;
    }

    /**
     * Logs a warning.
     * 
     * @param message
     *            the reason for the error
     * @throws SAXException
     *             the error
     */
    private void warning(String message) throws SAXException {
        logger.finer(message);
    }

}
