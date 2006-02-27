package net.sf.statcvs.input;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SvnXmlLineCountsFileHandler extends DefaultHandler {

	private static final String REMOVED = "removed";
	private static final String ADDED = "added";
	private static final String NAME = "name";
	private static final String NUMBER = "number";
	private static final String FATAL_ERROR_MESSAGE = "Invalid StatSvn line count file.";
	private static final String LINECOUNTS = "lineCounts";
	private static final String PATH = "path";
	private static final String REVISION = "revision";
	private String lastElement = "";
	private LineCountsBuilder lineCountsBuilder;

	public SvnXmlLineCountsFileHandler(LineCountsBuilder lineCountsBuilder) {
		this.lineCountsBuilder = lineCountsBuilder;
	}

	public void endDocument() throws SAXException {
		super.endDocument();
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
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

	private void endRevision() throws SAXException {
		checkLastElement(PATH);
	}

	private void checkLastElement(String last) throws SAXException {
		if (!lastElement.equals(last)) {
			fatalError(FATAL_ERROR_MESSAGE);
		}
	}

	private void endPath() throws SAXException {
		checkLastElement(PATH);
		lastElement = LINECOUNTS;
	}

	private void endLineCounts() throws SAXException {
		checkLastElement(LINECOUNTS);
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
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
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

	private void startRevision(Attributes attributes) throws SAXException {
		checkLastElement(PATH);
		if (attributes != null && attributes.getValue(NUMBER) != null
				&& attributes.getValue(ADDED) != null
				&& attributes.getValue(REMOVED) != null) {
			String number = attributes.getValue(NUMBER);
			String added = attributes.getValue(ADDED);
			String removed = attributes.getValue(REMOVED);
			lineCountsBuilder.buildRevision(number, added, removed);
		} else
			fatalError(FATAL_ERROR_MESSAGE);
	}

	private void startPath(Attributes attributes) throws SAXException {
		checkLastElement(LINECOUNTS);
		lastElement = PATH;
		if (attributes != null && attributes.getValue(NAME) != null) {
			String name = attributes.getValue(NAME);
			lineCountsBuilder.buildPath(name);
		} else
			fatalError(FATAL_ERROR_MESSAGE);
	}

	private void startLineCounts() throws SAXException {
		checkLastElement("");
		lastElement = LINECOUNTS;
		try {
			lineCountsBuilder.buildRoot();
		} catch (ParserConfigurationException e) {
			fatalError(FATAL_ERROR_MESSAGE);
		}
	}



}
