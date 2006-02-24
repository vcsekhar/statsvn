package net.sf.statcvs.input;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SvnXmlLineCountsFileHandler extends DefaultHandler {

	private static final String FATAL_ERROR_MESSAGE = "Invalid StatSvn line count file.";
	private static final String LINECOUNTS = "lineCounts";
	private static Logger logger = Logger
			.getLogger(SvnXmlLineCountsFileHandler.class.getName());
	private static final String PATH = "path";
	private static final String REVISION = "revision";
	private Document document;
	private Element lineCounts;
	private Element currentPath;
	private String lastElement = "";

	public SvnXmlLineCountsFileHandler(Document document) {
		this.document = document;
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
			if (!lastElement.equals(LINECOUNTS)) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
			lastElement = "";
		} else if (eName.equals(PATH)) {
			if (!lastElement.equals(PATH)) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
			lastElement = LINECOUNTS;
		} else if (eName.equals(REVISION)) {
			if (!lastElement.equals(PATH)) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
		} else {
			fatalError(FATAL_ERROR_MESSAGE);
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

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);

		String eName = localName; // element name
		if ("".equals(eName))
			eName = qName; // namespaceAware = false

		if (eName.equals(LINECOUNTS)) {
			if (!lastElement.equals("")) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
			lastElement = LINECOUNTS;
			lineCounts = (Element) document.createElement("lineCounts");
			document.appendChild(lineCounts);
		} else if (eName.equals(PATH)) {
			if (!lastElement.equals(LINECOUNTS)) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
			lastElement = PATH;
			String name = "";
			if (attributes != null && attributes.getValue("name") != null)
				name = attributes.getValue("name");
			else
				fatalError(FATAL_ERROR_MESSAGE);
			currentPath = (Element) document.createElement("path");
			Attr attr = document.createAttribute("name");
			attr.setTextContent(name);
			currentPath.setAttributeNode(attr);
			lineCounts.appendChild(currentPath);
		} else if (eName.equals(REVISION)) {
			if (!lastElement.equals(PATH)) {
				fatalError(FATAL_ERROR_MESSAGE);
			}
			boolean error = false;
			String number = "";
			String added = "";
			String removed = "";
			if (attributes != null) {
				if (attributes.getValue("number") != null)
					number = attributes.getValue("number");
				else
					error = true;
				if (attributes.getValue("added") != null)
					added = attributes.getValue("added");
				else
					error = true;
				if (attributes.getValue("removed") != null)
					removed = attributes.getValue("removed");
				else
					error = true;
			} else
				error = true;
			if (error)
				fatalError(FATAL_ERROR_MESSAGE);
			Element revision = (Element) document.createElement("revision");
			Attr attrRev1 = document.createAttribute("number");
			attrRev1.setTextContent(number);
			revision.setAttributeNode(attrRev1);
			Attr attrRev2 = document.createAttribute("added");
			attrRev2.setTextContent(added);
			revision.setAttributeNode(attrRev2);
			Attr attrRev3 = document.createAttribute("removed");
			attrRev3.setTextContent(removed);
			revision.setAttributeNode(attrRev3);
			currentPath.appendChild(revision);
		} else {
			fatalError(FATAL_ERROR_MESSAGE);
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
