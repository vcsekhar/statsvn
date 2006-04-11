package net.sf.statcvs.input;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>
 * CVS log files include lines modified for each commit while SVN log files do
 * not offer this additional information.
 * </p>
 * 
 * <p>
 * StatSVN must query the Subversion repository for this information using svn
 * diff. However, this is very costly, performance-wise. Therefore, the decision
 * was taken to persist this information in an XML file. This class receives
 * information from (@link net.sf.statcvs.input.SvnXmlLineCountsFileHandler) to
 * build a DOM-based xml structure. It also forwards line counts to the
 * appropriate (@link net.sf.statcvs.input.FileBuilder).
 * </p>
 * 
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * @version $Id$
 */
public class CacheBuilder {
	private static final String ADDED = "added";
	private static final String CACHE = "cache";
	private static final String NAME = "name";
	private static final String NUMBER = "number";
	private static final String PATH = "path";
	private static final String REMOVED = "removed";
	private static final String REVISION = "revision";
    private static final String BINARY_STATUS = "binaryStatus";
	private SvnLogBuilder builder;
	private RepositoryFileManager repositoryFileManager;
	private Element currentPath = null;
	private Document document = null;
	private String currentFilename;
	private Element cache = null;


	/**
	 * Constructs the LineCountsBuilder by giving it a reference to the builder
	 * currently in use.
	 * 
	 * @param builder
	 *            the SvnLogBuilder which contains all the FileBuilders.
	 */
	public CacheBuilder(SvnLogBuilder builder, RepositoryFileManager repositoryFileManager) {
		this.builder = builder;
		this.repositoryFileManager = repositoryFileManager;
	}

	/**
	 * Adds a path in the DOM. To be followed by invocations to (@link
	 * #addRevision(String, String, String))
	 * 
	 * @param name
	 *            the filename
	 */
	private void addDOMPath(String name) {
		currentPath = (Element) document.createElement(PATH);
		Attr attr = document.createAttribute(NAME);
		attr.setTextContent(name);
		currentPath.setAttributeNode(attr);
		cache.appendChild(currentPath);
	}

	/**
	 * Finds a path in the DOM.
	 * 
	 * @param name
	 *            the filename
	 * @return the path or null if the path does not exist
	 */
	private Element findDOMPath(String name) {
		if (currentPath != null) {
			if (name.equals(currentPath.getAttribute(NAME))) {
				return currentPath;
			}
		}
		NodeList paths = cache.getChildNodes();
		for (int i = 0; i < paths.getLength(); i++) {
			Element path = (Element) paths.item(i);
			if (name.equals(path.getAttribute(NAME))) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Adds a revision to the current path in the DOM. To be preceeded by (@link
	 * #addPath(String))
	 * 
	 * @param number
	 *            the revision number
	 * @param added
	 *            the number of lines that were added
	 * @param removed
	 *            the number of lines that were removed
	 */
	private void addDOMRevision(String number, String added, String removed, String binaryStatus) {
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
		Attr attrRev4 = document.createAttribute(BINARY_STATUS);
		attrRev4.setTextContent(binaryStatus);
		revision.setAttributeNode(attrRev4);
		currentPath.appendChild(revision);
	}

	/**
	 * Initializes the builder for subsequent invocations of (@link
	 * #buildRevision(String, String, String)).
	 * 
	 * @param name
	 *            the filename
	 */
	public void buildPath(String name) {
		currentFilename = repositoryFileManager.absoluteToRelativePath(name);
		addDOMPath(name);

	}

	/**
	 * Given the file specified by the preceeding invocation to (@link
	 * #buildPath(String)), set the line counts for the given revision.
	 * 
	 * If the path given in the preceeding invocation to (@link
	 * #buildPath(String)) is not used by the (@link SvnLogBuilder), this call
	 * does nothing.
	 * 
	 * @param number
	 *            the revision number
	 * @param added
	 *            the number of lines added
	 * @param removed
	 *            the number of lines removed.
	 */
	public void buildRevision(String number, String added, String removed, String binaryStatus) {
		if (!added.equals("-1") && !removed.equals("-1")) {
			addDOMRevision(number, added, removed, binaryStatus);
			builder.updateRevision(currentFilename, number, Integer
					.parseInt(added), Integer.parseInt(removed));
		}
	}

	/**
	 * Builds the DOM root.
	 * 
	 * @throws ParserConfigurationException
	 */
	public void buildRoot() throws ParserConfigurationException {
		DocumentBuilderFactory factoryDOM = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builderDOM;
		builderDOM = factoryDOM.newDocumentBuilder();
		document = builderDOM.newDocument();
		cache = (Element) document.createElement(CACHE);
		document.appendChild(cache);
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
	 * Encapsulates calls to (@link #buildRoot()), (@link #buildPath(String)),
	 * and (@link #buildRevision(String, String, String)) into one easy to use
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
	public void newRevision(String name, String number, String added,
			String removed) {
		name = repositoryFileManager.relativeToAbsolutePath(name);
		if (document == null) {
			try {
				buildRoot();
			} catch (ParserConfigurationException e) {
				document = null;
			}
		}
		if (document != null) {
			currentPath = findDOMPath(name);
			if (currentPath == null) {
				// changes currentPath to new one
				addDOMPath(name);
			}
			addDOMRevision(number, added, removed, "FALSE");
		}
	}

}
