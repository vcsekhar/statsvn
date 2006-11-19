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

 $RCSfile: HTMLOutput.java,v $
 $Date: 2005/05/19 13:49:45 $
 */
package net.sf.statsvn.output;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;

import net.sf.statsvn.Main;
import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Author;
import net.sf.statsvn.model.Directory;
import net.sf.statsvn.model.Repository;
import net.sf.statsvn.model.Revision;
import net.sf.statsvn.model.SymbolicName;
import net.sf.statsvn.renderer.BarChart;
import net.sf.statsvn.renderer.CombinedCommitScatterChart;
import net.sf.statsvn.renderer.CommitLogRenderer;
import net.sf.statsvn.renderer.LOCChart;
import net.sf.statsvn.renderer.PieChart;
import net.sf.statsvn.renderer.StackedBarChart;
import net.sf.statsvn.renderer.SymbolicNameAnnotation;
import net.sf.statsvn.renderer.TimeLineChart;
import net.sf.statsvn.reportmodel.TimeLine;
import net.sf.statsvn.reports.AbstractLocTableReport;
import net.sf.statsvn.reports.AvgFileSizeTimeLineReport;
import net.sf.statsvn.reports.FileCountTimeLineReport;
import net.sf.statsvn.util.FileUtils;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

/**
 * This class creates HTML File output
 * 
 * @author Anja Jentzsch
 * @author Benoit Xhenseval (OutputRenderer interface)
 * @version $Id: HTMLOutput.java,v 1.125 2005/05/19 13:49:45 squig Exp $
 */
public class HTMLOutput implements OutputRenderer {

	// private static Logger logger = Logger.getLogger("net.sf.statcvs");

	/**
	 * Path to web distribution files inside the distribution JAR, relative to
	 * the {@link net.sf.statsvn.Main} class
	 */
	public static final String WEB_FILE_PATH = "web-files/";

	/**
	 * Filename for folder icon
	 */
	public static final String DIRECTORY_ICON = "folder.png";

	/**
	 * Filename for bug icon
	 */
	public static final String BUG_ICON = "bug.png";

	/**
	 * Filename for deleted folder icon
	 */
	public static final String DELETED_DIRECTORY_ICON = "folder-deleted.png";

	/**
	 * Filename for file icon
	 */
	public static final String FILE_ICON = "file.png";

	/**
	 * Filename for deleted file icon
	 */
	public static final String DELETED_FILE_ICON = "file-deleted.png";

	/**
	 * Width of file icons
	 */
	public static final int ICON_WIDTH = 15;

	/**
	 * Height of file icons
	 */
	public static final int ICON_HEIGHT = 13;

	/**
	 * Length for Most Recent Commits list in Author pages and Directory pages
	 */
	public static final int MOST_RECENT_COMMITS_LENGTH = 20;

	/**
	 * standard image width
	 */
	public static final int IMAGE_WIDTH = 640;

	/**
	 * standard image height
	 */
	public static final int IMAGE_HEIGHT = 480;

	/**
	 * small image width
	 */
	public static final int SMALL_IMAGE_WIDTH = 500;

	/**
	 * small image height
	 */
	public static final int SMALL_IMAGE_HEIGHT = 300;

	/**
	 * loc image width
	 */
	public static final int LOC_IMAGE_WIDTH = 400;

	/**
	 * loc image height
	 */
	public static final int LOC_IMAGE_HEIGHT = 300;

	private final String[] categoryNamesHours = new String[] { "0", "1", "2", "3",
			"4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
			"16", "17", "18", "19", "20", "21", "22", "23" };

	private final String[] categoryNamesDays = new String[] { "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private Repository content;

	/**
	 * Creates a new <tt>HTMLOutput</tt> object for the given repository
	 * 
	 * @param content
	 *            the repository
	 */
	public HTMLOutput(final Repository content) {
		this.content = content;
	}

	/**
	 * Creates the Suite of HTML Files
	 * 
	 * @throws IOException
	 *             thrown if problems occur with the creation of files
	 */
	public void createHTMLSuite() throws IOException {

		// make JFreeChart work on systems without GUI
		System.setProperty("java.awt.headless", "true");

		ConfigurationOptions.getCssHandler().createOutputFiles();
		if (content.isEmpty()) {
			new NoFilesPage(content, this);
			return;
		}
		createIcon(BUG_ICON);
		createIcon(DIRECTORY_ICON);
		createIcon(DELETED_DIRECTORY_ICON);
		createIcon(FILE_ICON);
		createIcon(DELETED_FILE_ICON);
		final boolean authorsPageCreated = (content.getAuthors().size() > 1);
		final boolean locImageCreated = createLOCChart();
		final boolean commitScatterImageCreated = createCommitScatterChartPerAuthor();
		createFileCountChart();
		createModuleSizesChart();
		if (authorsPageCreated) {
			createActivityChart(content.getRevisions(), Messages
					.getString("ACTIVITY_TIME_TITLE"), "activity_time.png",
					categoryNamesHours);
			createActivityChart(content.getRevisions(), Messages
					.getString("ACTIVITY_DAY_TITLE"), "activity_day.png",
					categoryNamesDays);
			createAuthorActivityChart();
			final boolean locPerAuthorImageCreated = createLOCPerAuthorChart();
			new CPAPage(content, AbstractLocTableReport.SORT_BY_LINES,
					locPerAuthorImageCreated, this);
			new CPAPage(content, AbstractLocTableReport.SORT_BY_NAME,
					locPerAuthorImageCreated, this);
		}
		new IndexPage(content, locImageCreated, commitScatterImageCreated,
				authorsPageCreated, this);
		new LOCPage(content, locImageCreated, this);
		new FileSizesPage(content, this);
		new DirectorySizesPage(content, this);
		createModulePagesAndCharts();
		createCommitLogPages();
		createAuthorPages();
	}

	private void createAuthorActivityChart() {
		new StackedBarChart(content, Messages
				.getString("AUTHOR_ACTIVITY_TITLE"), "activity.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getDirectoryPageFilename(net.sf.statsvn.model.Directory,
	 *      boolean)
	 */
	public String getDirectoryPageFilename(final Directory directory,
			final boolean asLink) {
		return "module" + escapeDirectoryName(directory.getPath())
				+ (asLink ? getLinkExtension() : getFileExtension());
	}

	/**
	 * Returns the filename for a directory's LOC chart
	 * 
	 * @param directory
	 *            a directory
	 * @return filename for directory's LOC chart
	 */
	public static String getDirectoryLocChartFilename(final Directory directory) {
		return "loc_module" + escapeDirectoryName(directory.getPath()) + ".png";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getAuthorPageFilename(net.sf.statsvn.model.Author,
	 *      boolean)
	 */
	public String getAuthorPageFilename(final Author author, final boolean asLink) {
		return "user_" + escapeAuthorName(author.getName())
				+ (asLink ? getLinkExtension() : getFileExtension());
	}

	/**
	 * @param author
	 *            an author
	 * @return filename for author's activity by hour of day chart
	 */
	public static String getActivityTimeChartFilename(final Author author) {
		return "activity_time_" + escapeAuthorName(author.getName()) + ".png";
	}

	/**
	 * @param author
	 *            an author
	 * @return filename for author's activity by day of week chart
	 */
	public static String getActivityDayChartFilename(final Author author) {
		return "activity_day_" + escapeAuthorName(author.getName()) + ".png";
	}

	/**
	 * @param author
	 *            an author
	 * @return filename for author's code distribution chart
	 */
	public static String getCodeDistributionChartFilename(final Author author) {
		return "module_sizes_" + escapeAuthorName(author.getName()) + ".png";
	}

	private static String escapeDirectoryName(String directoryName) {
		if (!directoryName.startsWith("/")) {
			directoryName = "/" + directoryName;
		}
		return directoryName.substring(0, directoryName.length() - 1)
				.replaceAll("/", "_");
	}

	/**
	 * <p>
	 * Escapes evil characters in author's names. E.g. "#" must be escaped
	 * because for an author "my#name" a page "author_my#name.html" will be
	 * created, and you can't link to that in HTML
	 * </p>
	 * 
	 * TODO: Replace everything *but* known good characters, instead of just
	 * evil ones
	 * 
	 * @param authorName
	 *            an author's name
	 * @return a version safe for creation of files and URLs
	 */
	private static String escapeAuthorName(final String authorName) {
		return authorName.replaceAll("#", "_").replaceAll("\\\\", "_");
	}

	private void createIcon(final String iconFilename) throws IOException {
		final InputStream stream = Main.class.getResourceAsStream(WEB_FILE_PATH
				+ iconFilename);
		FileUtils.copyFile(stream, new File(ConfigurationOptions.getOutputDir()
				+ iconFilename));
		stream.close();
	}

	private boolean createLOCChart() {
		final String subtitle = Messages.getString("TIME_LOC_SUBTITLE");
		final TimeSeries series = getLOCTimeSeries(content.getRevisions(), subtitle);
		if (series == null) {
			return false;
		}
		final List annotations = createSymbolicNames(content.getSymbolicNames());
		new LOCChart(series, subtitle, "loc.png", 640, 480, annotations);
		new LOCChart(series, subtitle, "loc_small.png", 400, 300, annotations);
		return true;
	}

	private List createSymbolicNames(final Set symbolicNames) {
		final Pattern pattern = ConfigurationOptions.getSymbolicNamesPattern();
		if (pattern == null) {
			return null;
		}

		final List annotations = new ArrayList();
		for (final Iterator it = symbolicNames.iterator(); it.hasNext();) {
			final SymbolicName sn = (SymbolicName) it.next();
			if (sn.getDate() != null && pattern.matcher(sn.getName()).matches()) {
				annotations.add(new SymbolicNameAnnotation(sn));
			}
		}
		return (annotations.isEmpty()) ? null : annotations;
	}

	private boolean createCommitScatterChartPerAuthor() {
		final String subtitle = Messages.getString("TIME_CSC_SUBTITLE");

		final Iterator itAll = content.getRevisions().iterator();
		final TimeSeries seriesAll = new TimeSeries("Test", Second.class);
		Date lastDateAll = new Date();
		while (itAll.hasNext()) {
			final Revision rev = (Revision) itAll.next();
			if (lastDateAll != null) {
				final Calendar cal = Calendar.getInstance();
				cal.setTime(lastDateAll);
				final double lastDateSeconds = cal.get(Calendar.SECOND);
				cal.setTime(rev.getDate());
				final double dateSeconds = cal.get(Calendar.SECOND);
				if (lastDateSeconds == dateSeconds) {
					continue;
				}
			}
			lastDateAll = rev.getDate();
			final Calendar cal = Calendar.getInstance();
			cal.setTime(lastDateAll);
			final double hour = cal.get(Calendar.HOUR_OF_DAY);
			final double minutes = cal.get(Calendar.MINUTE);
			seriesAll.add(new Second(lastDateAll), hour + minutes / 60.0);
		}
		if (seriesAll == null) {
			return false;
		}

		final Iterator authorsIt = content.getAuthors().iterator();
		final Map authorSeriesMap = new HashMap();
		while (authorsIt.hasNext()) {
			final Author author = (Author) authorsIt.next();

			final Iterator it = author.getRevisions().iterator();
			final TimeSeries series = new TimeSeries("Test", Second.class);
			Date lastDate = new Date();
			while (it.hasNext()) {
				final Revision rev = (Revision) it.next();
				if (lastDate != null) {
					final Calendar cal = Calendar.getInstance();
					cal.setTime(lastDate);
					final double lastDateSeconds = cal.get(Calendar.SECOND);
					cal.setTime(rev.getDate());
					final double dateSeconds = cal.get(Calendar.SECOND);
					if (lastDateSeconds == dateSeconds) {
						continue;
					}
				}
				lastDate = rev.getDate();
				final Calendar cal = Calendar.getInstance();
				cal.setTime(lastDate);
				final double hour = cal.get(Calendar.HOUR_OF_DAY);
				final double minutes = cal.get(Calendar.MINUTE);
				series.add(new Second(lastDate), hour + minutes / 60.0);
			}
			if (series == null) {
				return false;
			}
			authorSeriesMap.put(author, series);
		}

		new CombinedCommitScatterChart(seriesAll, authorSeriesMap, subtitle,
				"commitscatterauthors.png", 640,
				70 * (authorSeriesMap.size() + 1) + 110);
		return true;
	}

	private void createModulePagesAndCharts() throws IOException {
		final Iterator it = content.getDirectories().iterator();
		while (it.hasNext()) {
			final Directory dir = (Directory) it.next();
			final boolean moduleImageCreated = createLOCChart(dir);
			new ModulePage(content, dir, moduleImageCreated, this);
		}
	}

	private void createAuthorPages() throws IOException {
		final Collection authors = content.getAuthors();
		final Iterator it = authors.iterator();
		while (it.hasNext()) {
			final Author author = (Author) it.next();
			createActivityChart(author.getRevisions(), Messages
					.getString("ACTIVITY_TIME_FOR_AUTHOR_TITLE")
					+ " " + author.getName(),
					getActivityTimeChartFilename(author), categoryNamesHours);
			createActivityChart(author.getRevisions(), Messages
					.getString("ACTIVITY_DAY_FOR_AUTHOR_TITLE")
					+ " " + author.getName(),
					getActivityDayChartFilename(author), categoryNamesDays);
			final boolean chartCreated = createCodeDistributionChart(author);
			new AuthorPage(content, author, chartCreated, this);
		}
	}

	private void createCommitLogPages() throws IOException {
		final List commits = content.getCommits();
		final CommitLogRenderer logRenderer = new CommitLogRenderer(commits);
		final int pages = logRenderer.getPages();
		for (int i = 1; i <= pages; i++) {
			new CommitLogPage(content, logRenderer, i, pages, this);
		}
	}

	private boolean createLOCChart(final Directory dir) {
		final String subtitle = Messages.getString("TIME_LOC_SUBTITLE");
		final TimeSeries series = getLOCTimeSeries(dir.getRevisions(), subtitle);
		if (series == null) {
			return false;
		}
		final String fileName = getDirectoryLocChartFilename(dir);
		final List annotations = this.createSymbolicNames(content.getSymbolicNames());
		new LOCChart(series, dir.getPath() + " " + subtitle, fileName, 640,
				480, annotations);
		return true;
	}

	private TimeSeries getLOCTimeSeries(final SortedSet revisions, final String title) {
		final Iterator it = revisions.iterator();
		final LOCSeriesBuilder locCounter = new LOCSeriesBuilder(title, true);
		while (it.hasNext()) {
			locCounter.addRevision((Revision) it.next());
		}
		return locCounter.getTimeSeries();
	}

	private void createFileCountChart() {
		final SortedSet files = content.getFiles();
		final List annotations = this.createSymbolicNames(this.content
				.getSymbolicNames());
		final TimeLine fileCount = new FileCountTimeLineReport(files).getTimeLine();
		new TimeLineChart(fileCount, "file_count.png", IMAGE_WIDTH,
				IMAGE_HEIGHT, annotations);
		final TimeLine avgFileSize = new AvgFileSizeTimeLineReport(files)
				.getTimeLine();
		new TimeLineChart(avgFileSize, "file_size.png", IMAGE_WIDTH,
				IMAGE_HEIGHT, annotations);
	}

	private void createModuleSizesChart() {
		new PieChart(content, Messages.getString("PIE_MODSIZE_SUBTITLE"),
				"module_sizes.png", null, PieChart.FILTERED_BY_REPOSITORY);
	}

	private void createActivityChart(final SortedSet revisions, final String title,
			final String fileName, final String[] categoryNames) {
		new BarChart(revisions, title, fileName, categoryNames.length,
				categoryNames);
	}

	private boolean createLOCPerAuthorChart() {
		Iterator authorsIt = content.getAuthors().iterator();
		final Map authorSeriesMap = new HashMap();
		while (authorsIt.hasNext()) {
			final Author author = (Author) authorsIt.next();
			authorSeriesMap.put(author, new LOCSeriesBuilder(author.getName(),
					false));
		}
		final Iterator allRevs = content.getRevisions().iterator();
		while (allRevs.hasNext()) {
			final Revision rev = (Revision) allRevs.next();
			if (rev.isBeginOfLog()) {
				continue;
			}
			final LOCSeriesBuilder builder = (LOCSeriesBuilder) authorSeriesMap
					.get(rev.getAuthor());
			builder.addRevision(rev);
		}
		final List authors = new ArrayList(authorSeriesMap.keySet());
		Collections.sort(authors);
		final List seriesList = new ArrayList();
		authorsIt = authors.iterator();
		while (authorsIt.hasNext()) {
			final Author author = (Author) authorsIt.next();
			final LOCSeriesBuilder builder = (LOCSeriesBuilder) authorSeriesMap
					.get(author);
			final TimeSeries series = builder.getTimeSeries();
			if (series != null) {
				seriesList.add(series);
			}
		}
		if (seriesList.isEmpty()) {
			return false;
		}
		final String subtitle = Messages.getString("TIME_LOCPERAUTHOR_SUBTITLE");
		new LOCChart(seriesList, subtitle, "loc_per_author.png", 640, 480,
				createSymbolicNames(content.getSymbolicNames()));
		return true;
	}

	private boolean createCodeDistributionChart(final Author author) {
		final Iterator it = author.getRevisions().iterator();
		int totalLinesOfCode = 0;
		while (it.hasNext()) {
			final Revision rev = (Revision) it.next();
			totalLinesOfCode += rev.getNewLines();
		}
		if (totalLinesOfCode == 0) {
			return false;
		}
		new PieChart(content, Messages
				.getString("PIE_CODEDISTRIBUTION_SUBTITLE")
				+ " " + author.getName(),
				getCodeDistributionChartFilename(author), author,
				PieChart.FILTERED_BY_USER);
		return true;
	}

	// + New BX Section to allow HTML and XDOC output (these will be
	// overwritten).
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getFileExtension()
	 */
	public String getFileExtension() {
		return ".html";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getLinkExtension()
	 */
	public String getLinkExtension() {
		return ".html";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getHeader(java.lang.String)
	 */
	public String getHeader(final String pageName) {
		return "<?xml version=\"1.0\"?>\n"
				+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
				+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
				+ "<head>\n    <title>"
				+ Messages.getString("PROJECT_SHORTNAME") + " - " + pageName
				+ "</title>\n"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; "
				+ "charset=ISO-8859-1\"/>\n"
				+ "    <meta name=\"Generator\" content=\"StatSVN v0.1.3\"/>\n"
				+ "    <link rel=\"stylesheet\" href=\""
				+ ConfigurationOptions.getCssHandler().getLink()
				+ "\" type=\"text/css\"/>\n" + "  </head>\n\n" + "<body>\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getEndOfPage()
	 */
	public String getEndOfPage() {
		return "</body>\n</html>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#startSection1(java.lang.String)
	 */
	public String startSection1(final String title) {
		return "\n\n<h1>" + title + "</h1>\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#endSection1()
	 */
	public String endSection1() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#startSection2(java.lang.String)
	 */
	public String startSection2(final String title) {
		return "\n<h2>" + title + "</h2>\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#endSection2()
	 */
	public String endSection2() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getOddRowFormat()
	 */
	public String getOddRowFormat() {
		return " class=\"even\"";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getEvenRowFormat()
	 */
	public String getEvenRowFormat() {
		return " class=\"odd\"";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.statsvn.output.OutputRenderer#getTableFormat()
	 */
	public String getTableFormat() {
		return "";
	}
}