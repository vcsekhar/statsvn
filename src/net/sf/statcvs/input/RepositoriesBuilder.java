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
 * This class receives information from the (@link net.sf.statcvs.input.SvnXmlRepositoriesFileHandler)
 * to build a DOM-based XML structure containing the names of all repositories and associated line counts xml files.
 * It then allows to retrieve the line counts XML file name for a given repository.
 * </p>
 * 
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 * 
 */
public class RepositoriesBuilder {
	private static final String REPOSITORIES = "repositories";
	private static final String UUID = "uuid";
	private static final String FILE = "file";
	private static final String REPOSITORY = "repository";
	private Document document = null;
	private Element repositories = null;

	/**
	 * Constructs the RepositoriesBuilder
	 * 
	 */
	public RepositoriesBuilder() {
	}

	/**
	 * Finds a repository in the DOM.
	 * 
	 * @param uuid
	 *            the uuid of the repository
	 * @return the repository or null if the repository does not exist
	 */
	private Element findRepository(String uuid) {
		NodeList paths = repositories.getChildNodes();
		for (int i = 0; i < paths.getLength(); i++) {
			Element path = (Element) paths.item(i);
			if (uuid.equals(path.getAttribute(UUID))) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Adds a repository to the DOM structure.
	 * 
	 * @param uuid
	 *            the uuid of the repository
	 * @param file
	 *            the filename for the XML line counts file
	 */
	public Element buildRepository(String uuid, String file) {
		Element repository = (Element) document.createElement(REPOSITORY);
		Attr attr = document.createAttribute(UUID);
		attr.setTextContent(uuid);
		repository.setAttributeNode(attr);
		Attr attr2 = document.createAttribute(FILE);
		attr2.setTextContent(file);
		repository.setAttributeNode(attr2);
		repositories.appendChild(repository);
		return repository;
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
		repositories = (Element) document.createElement(REPOSITORIES);
		document.appendChild(repositories);
	}

	/**
	 * Retrieves the file name of the line counts xml file for a given repository.
	 * Creates a new file name if the line counts xml file does not exist.
	 * 
	 * If the repositories xml file does not exist (i.e. the document is null), 
	 * a new document is created.
	 * 
	 * @param uuid
	 *            the uuid of the repository
	 *            
	 * @return the file name or "" if an unexpected error occurs          
	 */
	public String getFileName(String uuid) {
		if (document == null) {
			try {
				buildRoot();
			} catch (ParserConfigurationException e) {
				document = null;
			}
		}
		if (document != null) {
			Element repository = findRepository(uuid);
			if (repository == null) {
				repository = buildRepository(uuid, "lineCounts_" + uuid + ".xml");
			}
			return repository.getAttribute(FILE);
		}
		return "";
	}
	

	/**
	 * Returns the DOM object when building is complete.
	 * 
	 * @return the DOM document.
	 */
	public Document getDocument() {
		return document;
	}

}
