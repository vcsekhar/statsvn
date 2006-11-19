/*
    StatCVS - CVS statistics generation
    Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
    http://statcvs.sf.net/

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	$RCSfile: HTMLPage.java,v $
	Created on $Date: 2004/10/12 13:03:36 $
*/

package net.sf.statsvn.output;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.model.Directory;

/**
 * @author anja
 */
public abstract class HTMLPage {
	private static final Logger LOGGER =
		Logger.getLogger("net.sf.statsvn.output.HTMLPage");

	private static final int SPACE_COUNT = 4;
	private FileWriter htmlFileWriter;
	private Repository content;
	private String fileName;
	private String pageName;
    private OutputRenderer renderer;

	/**
	 * Method HTMLPage.
	 * @param content of the Page
	 */
	public HTMLPage(final Repository content, final OutputRenderer renderer) {
		this.content = content;
        this.renderer = renderer;
	}

	protected void createPage() throws IOException {
		LOGGER.info("Creating page '" + getPageName() + "'");
		initFileWriter();
		printHeader();
		printHeadline();
		printBody();
		printFooter();
	}

	protected void print(final String printStream) throws IOException {
		htmlFileWriter.write(printStream);
	}

	private void initFileWriter() throws IOException {
		htmlFileWriter =
			new FileWriter(ConfigurationOptions.getOutputDir() + getFileName());
	}

	/**
	 * Method printHeader.
	 */
	private void printHeader() throws IOException {
		print(renderer.getHeader(getPageName()));
	}

	private void printHeadline() throws IOException {
		print(h1(getPageName()));
	}

	//TODO: Remove this! It feels bad.
    protected void printStartSection2(final String h2) throws IOException {
        print(startSection2(h2));
    }

    protected void printEndSection2() throws IOException {
        print(endSection2());
    }

	private void printFooter() throws IOException {
		print("<div id=\"generatedby\">");
		printParagraph(
			Messages.getString("PAGE_GENERATED_BY")
			+ " "
			+ a(
				"http://www.statsvn.org",
				Messages.getString("PROJECT_SHORTNAME"))
			+ " v0.1.4");
		print("</div>\n");
		print(renderer.getEndOfPage());
		htmlFileWriter.close();
	}

	//TODO: Remove this! It feels bad.
	protected void printParagraph(final String paragraphContent) throws IOException {
		print(p(paragraphContent));
	}

	protected void printBackLink() throws IOException {
		print(p(a("index.html", Messages.getString("NAVIGATION_BACK"))));
	}

	protected String br() {
		return "<br/>\n";
	}

	protected String p(final String p) {
		return tag("p", p);
	}

	protected String h1(final String h1) {
		return renderer.startSection1(h1);
	}

	protected String startSection2(final String h2) {
        return renderer.startSection2(h2);
//		return "\n" + tag("h2", h2) + "\n";
	}

    protected String endSection2() {
        return renderer.endSection2();
    }

	protected String strong(final String b) {
		return tag("strong", b);
	}

	protected String a(final String target, final String html) {
		return "<a href=\"" + target + "\">" + html + "</a>";
	}

	protected String ul(final String ul) {
		return tag("ul", ul);
	}

	protected String li(final String li) {
		return tag("li", li);
	}

	/**
	 * Returns HTML code for a image tag
	 * @param image URL of the Image to be hyperlinked
	 * @param width width of the Image to be hyperlinked
	 * @param height height of the Image to be hyperlinked
	 * @return HTML code for the image tag
	 */
	protected String img(final String image, final int width, final int height) {
		return "<img src=\"" + image + "\" width=\"" + width + "\" height=\"" + height
			+ "\" alt=\"\"/>";
	}

	/**
	 * Returns HTML code for a image tag
	 * @param image URL of the Image to be hyperlinked
	 * @return HTML code for the image tag
	 */
	protected String img(final String image) {
		return "<img src=\"" + image + "\" alt=\"\"/>";
	}

	protected String tag(final String elementName, final String elementContent) {
		return ("<" + elementName + ">" + elementContent + "</" + elementName + ">\n");
	}

	protected abstract void printBody() throws IOException;

	/**
	 * Returns HTML code for a userPage link tag
	 * @param userName userName to be tagged
	 * @return HTML code for the userPage link tag
	 */
	public String getUserLink(final String userName) {
		return a("user_" + userName + ".html", userName);
	}

	private String getSpaces(final int count) {
		final StringBuffer result = new StringBuffer();
		for (int i = 0; i < count * SPACE_COUNT; i++) {
			result.append("&#160;");
		}
		return result.toString();
	}

	protected String deleteEndingSlash(final String path) {
		if (path.endsWith("/")) {
			return path.substring(0, path.length() - 1);
		}
		return path;
	}

	protected String getFolderHtml(final Directory dir, final int currentDepth) {
		final String name = dir.isRoot()
				? Messages.getString("NAVIGATION_ROOT")
				: dir.getName();
		final StringBuffer result = new StringBuffer(getSpaces(dir.getDepth() - currentDepth));
		if (dir.isEmpty()) {
			result.append(HTMLTagger.getIcon(HTMLOutput.DELETED_DIRECTORY_ICON));
		} else {
			result.append(HTMLTagger.getIcon(HTMLOutput.DIRECTORY_ICON));
		}
		final String pageFilename = getRenderer().getDirectoryPageFilename(dir, true);
		result.append(" \n").append(a(pageFilename, name));
		result.append(" \n(").append(dir.getCurrentFileCount()).append(" ");
		result.append(Messages.getString("DIRECTORY_TREE_FILES")).append(", ");
		result.append(dir.getCurrentLOC()).append(" ");
		result.append(Messages.getString("DIRECTORY_TREE_LINES")).append(")").append(br()).append("\n");
		return result.toString();
	}
	/**
	 * Returns the LOGGER.
	 * @return Logger
	 */
	public static Logger getLOGGER() {
		return LOGGER;
	}

	protected Repository getContent() {
		return content;
	}

	protected void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	protected String getFileName() {
		return fileName;
	}

	protected void setPageName(final String pageName) {
		this.pageName = pageName;
	}

	protected String getPageName() {
		return pageName;
	}

    /**
     * @return the renderer
     */
    protected OutputRenderer getRenderer() {
        return renderer;
    }
}
