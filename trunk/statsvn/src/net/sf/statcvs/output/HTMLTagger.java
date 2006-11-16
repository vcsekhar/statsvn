/*
    StatCvs - CVS statistics generation 
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
    
	$RCSfile: HTMLTagger.java,v $
	$Date: 2006/10/10 09:23:45 $ 
*/
package net.sf.statcvs.output;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.statcvs.Messages;
import net.sf.statcvs.model.Author;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statcvs.model.Directory;
import net.sf.statcvs.util.OutputUtils;

/**
 * //TODO: lots of duplicate code here and in HTMLPage 
 * @author Anja Jentzsch
 * @version $Id: HTMLTagger.java,v 1.40 2006/10/10 09:23:45 cyganiak Exp $
 */
public class HTMLTagger {
	private static SimpleDateFormat outputDateFormat =
		new SimpleDateFormat(Messages.getString("DATE_FORMAT"));
	private static SimpleDateFormat outputDateTimeFormat =
		new SimpleDateFormat(Messages.getString("DATE_TIME_FORMAT"));

	/**
	 * Creates a HTML representation of a hyperlink
	 * @param link URL
	 * @param linkName Name of the Link
	 * @return String HTML code of the hyperlink
	 */
	public static String getLink(String link, String linkName) {
		return getLink(link, linkName, "", "");
	}

	/**
	 * Creates a HTML representation of a hyperlink
	 * @param link URL
	 * @param linkName Name of the Link
	 * @param prefix A prefix to be inserted before the link label; no HTML escaping is performed
	 * @param prefix A suffix to be inserted after the link label; no HTML escaping is performed
	 * @return String HTML code of the hyperlink
	 */
	public static String getLink(String link, String linkName, String prefix, String suffix) {
	    return "<a href=\"" + OutputUtils.escapeHtml(link) + "\">"
	            + prefix + OutputUtils.escapeHtml(linkName) + suffix + "</a>";
	}

	/**
	 * Returns HTML code for a link to an author page
	 * @param author the author
	 * @return HTML code for the link
	 */
	public static String getAuthorLink(Author author, final OutputRenderer renderer) {
		return "<a href=\""
		+ OutputUtils.escapeHtml(renderer.getAuthorPageFilename(author, true))
		+ "\" class=\"author\">" + OutputUtils.escapeHtml(author.getName()) + "</a>";
	}

	/**
	 * Returns HTML code for a date
	 * @param date the date
	 * @return HTML code for the date
	 */
	public static String getDate(Date date) {
		return "<span class=\"date\">" + outputDateFormat.format(date) + "</span>";
	}

	/**
	 * Returns HTML code for a date, including time
	 * @param date the date
	 * @return HTML code for the date
	 */
	public static String getDateAndTime(Date date) {
		return "<span class=\"date\">" + outputDateTimeFormat.format(date) + "</span>";
	}

	/**
	 * Returns HTML code for a directory page link
	 * @param directory a directory
	 * @return HTML code for the link
	 */
	public static String getDirectoryLink(Directory directory, final OutputRenderer renderer) {
		String caption = directory.isRoot() ? "/" : directory.getPath();
		return "<a href=\""
				+ OutputUtils.escapeHtml(renderer.getDirectoryPageFilename(directory, true))
				+ "\" class=\"directory\">"
				+ OutputUtils.escapeHtml(caption) + "</a>";
	}

	/**
	 * Returns HTML code for a file. If connected to a web repository,
	 * it will be a link. Otherwise, just the filename.
	 * @param file a file
	 * @return HTML code for the file
	 */
	public static String getFileLink(VersionedFile file) {
		WebRepositoryIntegration wri = ConfigurationOptions.getWebRepository();
		if (wri == null) {
			return file.getFilenameWithPath();
		}
		return "<a href=\"" + OutputUtils.escapeHtml(wri.getFileViewUrl(file))
				+ "\">" + OutputUtils.escapeHtml(file.getFilenameWithPath())
				+ "</a>";
	}

	/**
	 * generates HTML string describing the summary period of a chart or table
	 * @param startDate start date of the period
	 * @param endDate end date of the period
	 * @return HTML string
	 */
	public static String getSummaryPeriod(Date startDate, Date endDate) {
		return getSummaryPeriod(startDate, endDate, null, false);
	}

	/**
	 * generates HTML string describing the summary period of a chart or table
	 * @param startDate start date of the period
	 * @param endDate end date of the period
	 * @param additionalText additional text, added to the output string
	 * @param newLine should additionalText be placed on a new line?
	 * @return HTML string
	 */
	public static String getSummaryPeriod(Date startDate, Date endDate, String additionalText, boolean newLine) {
		String result = "<p class=\"summaryperiod\">\n  "
			+ Messages.getString("SUMMARY_PERIOD") + ":\n  "
			+ HTMLTagger.getDate(startDate) + " to\n  "
			+ HTMLTagger.getDate(endDate);
		if (additionalText != null && !"".equals(additionalText)) {
			result += newLine ? "<br />\n" : " ";
			result += OutputUtils.escapeHtml(additionalText) + "\n";
		}
		return result + "</p>\n";
	}
	
	/**
	 * Generates HTML for an icon
	 * @param iconFilename an icon filename (HTMLOutput.XXXX_ICON constants)
	 * @return HTML string
	 */
	public static String getIcon(String iconFilename) {
		return "<img src=\"" + OutputUtils.escapeHtml(iconFilename) + "\" width=\""				+ HTMLOutput.ICON_WIDTH + "\" height=\""
				+ HTMLOutput.ICON_HEIGHT + "\" alt=\"\"/>";
	}
}
