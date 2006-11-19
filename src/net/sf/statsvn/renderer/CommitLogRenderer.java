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
    
	$Name:  $ 
	Created on $Date: 2006/10/10 09:23:45 $ 
*/
package net.sf.statsvn.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.statsvn.Messages;
import net.sf.statsvn.model.Commit;
import net.sf.statsvn.model.Revision;
import net.sf.statsvn.output.ConfigurationOptions;
import net.sf.statsvn.output.HTMLTagger;
import net.sf.statsvn.output.OutputRenderer;
import net.sf.statsvn.output.WebBugtrackerIntegration;
import net.sf.statsvn.output.WebRepositoryIntegration;
import net.sf.statsvn.util.OutputUtils;

/**
 * Class for rendering a list of commits as HTML.
 * 
 * @author Anja Jentzsch
 * @author Richard Cyganiak
 * @version $Id: CommitLogRenderer.java,v 1.32 2006/10/10 09:23:45 cyganiak Exp $
 */
public class CommitLogRenderer {

	private static final int PAGE_SIZE = 50;

	private List commits;
	private int currentPage;
	private List pageCommits;
	private final HashMap commitHashMap = new HashMap();
	private WebRepositoryIntegration webRepository;
	private WebBugtrackerIntegration webBugtracker;

	/**
	 * Creates a new instance for the list of commits.
	 * 
	 * @param commits A list of {@link Commit} objects
	 */
	public CommitLogRenderer(final List commit) {
		this.commits = new ArrayList(commit);
		Collections.reverse(this.commits);
		webRepository = ConfigurationOptions.getWebRepository();
	}


	/**
	 * Returns HTML code for the commit log without splitting the list
	 * into pages.
	 * 
	 * @param maxCommits maximum number of commits for the log; if there
	 * are more, only the most recent will be used
	 * @return HTML code for the commit log
	 */
	public String renderMostRecentCommits(final int maxCommits, final OutputRenderer renderer) {
		if (commits.size() > maxCommits) {
			final List recentCommits = commits.subList(0, maxCommits);
			return renderCommitList(recentCommits, renderer)
					+ "<p>(" + (commits.size() - maxCommits) + " "
					+ Messages.getString("MORE_COMMITS") + ")</p>\n";
		}
		return renderCommitList(commits, renderer);
	}
	
	/**
	 * Returns HTML code for a page.
	 * @param page the page number
	 * @return HTML code
	 */
	public String renderPage(final int page, final OutputRenderer renderer) {
		this.currentPage = page;
		this.pageCommits =
			commits.subList(getFirstCommitOfPage(page), getLastCommitOfPage(page) + 1);
		final StringBuffer result = new StringBuffer();
		if (getPages() > 1) {
			result.append(renderNavigation(renderer));
		}
		result.append(renderTimespan());
		result.append(renderCommitList(pageCommits, renderer));
		if (getPages() > 1) {
			result.append(renderNavigation(renderer));
		}
		return result.toString();
	}

	private String renderTimespan() {
		final Date time1 = ((Commit) pageCommits.get(0)).getDate();
		final Date time2 = ((Commit) pageCommits.get(pageCommits.size() - 1)).getDate();
		final StringBuffer commitsText = new StringBuffer();
		if (getPages() > 1) {
			commitsText.append(Messages.getString("COMMITS"))
					.append(" ")
					.append((commits.size() - getLastCommitOfPage(currentPage)))
					.append("-")
					.append((commits.size() - getFirstCommitOfPage(currentPage)))
					.append(" of ")
					.append(commits.size());
		} else {
			commitsText.append(commits.size()).append(" ").append(Messages.getString("COMMITS"));
		}
		return HTMLTagger.getSummaryPeriod(time1, time2, " (" + commitsText.toString() + ")", false);
	}

	private String renderNavigation(final OutputRenderer renderer) {
		final StringBuffer result = new StringBuffer("<p>").append(Messages.getString("PAGES")).append(": ");
		if (currentPage > 1) {
			result.append(HTMLTagger.getLink(
					getFilename(currentPage - 1, renderer, true),
					Messages.getString("NAVIGATION_PREVIOUS"), "&#171; ", ""))
					.append(" ");
		}
		for (int i = 1; i <= getPages(); i++) {
			if (i == currentPage) {
				result.append((i)).append(" ");
			} else {
				result.append(HTMLTagger.getLink(getFilename(i, renderer, true), Integer.toString(i)))
						.append(" ");
			}
		}
		if (currentPage < getPages()) {
			result.append(HTMLTagger.getLink(getFilename(currentPage + 1, renderer, true),
					Messages.getString("NAVIGATION_NEXT"), "", " &#187;")).append(" ");
		}
		result.append("</p>\n");
		return result.toString();
	}

	private int getFirstCommitOfPage(final int page) {
		return (page - 1) * PAGE_SIZE;
	}

	private int getLastCommitOfPage(final int page) {
		return Math.min(commits.size(), (page * PAGE_SIZE)) - 1;
	}

	/**
	 * Returns the number of pages for this renderer.
	 * @return the number of pages for this renderer
	 */
	public int getPages() {
		return (commits.size() + PAGE_SIZE - 1) / PAGE_SIZE;
	}

	/**
	 * Returns the filename for a commit log page.
	 * @param page specified page
	 * @return the filename for a commit log page
	 */
	public static String getFilename(final int page, final OutputRenderer renderer, final boolean asLink) {
		if (page == 1) {
			return "commit_log" + (asLink ? renderer.getLinkExtension() : renderer.getFileExtension());
		}
		return "commit_log_page_" + page + (asLink ? renderer.getLinkExtension() : renderer.getFileExtension());
	}

	private String renderCommitList(final List commitList, final OutputRenderer renderer) {
		if (commitList.isEmpty()) {
			return "<p>No commits</p>\n";
		}
		final Iterator it = commitList.iterator();
		final StringBuffer result = new StringBuffer("<dl class=\"commitlist\">\n");

		while (it.hasNext()) {
			final Commit commit = (Commit) it.next();
			result.append(renderCommit(commit, renderer));
		}
		result.append("</dl>\n\n");
		return result.toString();
	}

	private String renderCommit(final Commit commit, final OutputRenderer renderer) {
		final StringBuffer result = new StringBuffer("  <dt>\n    ").append(getAuthor(commit, renderer)).append("\n");
		result.append("    ").append(getDate(commit)).append("\n  </dt>\n");
		result.append("  <dd>\n    <p class=\"comment\">\n").append(getComment(commit)).append("\n    </p>\n");
		result.append("    <p class=\"commitdetails\"><strong>");
		result.append(getLinesOfCode(commit)).append("</strong> ");
		result.append("lines of code changed in:</p>\n");
		result.append(getAffectedFiles(commit)).append("  </dd>\n\n");
		if (webBugtracker != null) {
			return webBugtracker.applyFilter(result.toString());
		}
		return result.toString();
	}

	private String getDate(final Commit commit) {
		return HTMLTagger.getDateAndTime(commit.getDate());
	}

	private String getAuthor(final Commit commit, final OutputRenderer renderer) {
		return HTMLTagger.getAuthorLink(commit.getAuthor(), renderer);
	}

	private String getComment(final Commit commit) {
		return OutputUtils.escapeHtml(commit.getComment());
	}

	private String getLinesOfCode(final Commit commit) {
		final Iterator it = commit.getRevisions().iterator();
		int locSum = 0;
		while (it.hasNext()) {
			final Revision each = (Revision) it.next();
			locSum += each.getNewLines();
			saveRevision(each);
		}
		return Integer.toString(locSum);
	}

	private void saveRevision(final Revision revision) {
		commitHashMap.put(revision.getFile().getFilenameWithPath(), revision);
	}
	
	private String getAffectedFiles(final Commit commit) {
		final StringBuffer result = new StringBuffer("    <ul class=\"commitdetails\">\n");
		final FileCollectionFormatter formatter =
				new FileCollectionFormatter(commit.getAffectedFiles());
		final Iterator it = formatter.getDirectories().iterator();
		while (it.hasNext()) {
			result.append("      <li>\n");
			final String directory = (String) it.next();
			if (!directory.equals("")) {
				result.append("        <strong>")
					.append(directory.substring(0, directory.length() - 1))
					.append("</strong>:\n");
			}
			final Iterator files = formatter.getFiles(directory).iterator();
			final StringBuffer fileList = new StringBuffer();
			while (files.hasNext()) {
				if (fileList.length()>0) {
					fileList.append(",\n");
				}
				fileList.append("        ");
				final String file = (String) files.next();
				final Revision revision =
						(Revision) commitHashMap.get(directory + file);
				if (webRepository != null) {
					final Revision previous = revision.getPreviousRevision();
					String url; 
					if (previous == null || revision.isInitialRevision()) {
						url = webRepository.getFileViewUrl(revision);
					} else if (revision.isDead()) {
						url = webRepository.getFileViewUrl(previous);
					} else {
						url = webRepository.getDiffUrl(previous, revision);
					}
					fileList.append("<a href=\"").append(OutputUtils.escapeHtml(url))
						.append("\" class=\"webrepository\">").append(file).append("</a>"); 
				} else {
					fileList.append(file);
				}
				if (revision.isInitialRevision()) {
					final int linesAdded = revision.getLines();
					fileList.append("&#160;<span class=\"new\">(new");
					if (linesAdded > 0) {
						fileList.append("&#160;").append(linesAdded);
					}
					fileList.append(")</span>");
				} else if (revision.isDead()) {
					fileList.append("&#160;<span class=\"del\">(del)</span>");
				} else {
					final int delta = revision.getLinesDelta();
					final int linesAdded = revision.getReplacedLines() + ((delta > 0) ? delta : 0);
					final int linesRemoved = revision.getReplacedLines() - ((delta < 0) ? delta : 0);
					fileList.append("&#160;<span class=\"change\">(");
					if (linesAdded > 0) {
						fileList.append("+").append(linesAdded);
						if (linesRemoved > 0) {
							fileList.append("&#160;-").append(linesRemoved);
						}
					} else if (linesRemoved > 0) {
						fileList.append("-").append(linesRemoved);
					} else {	// linesAdded == linesRemoved == 0
						// should be binary file or keyword subst change
						fileList.append("changed");
					}
					fileList.append(")</span>");
				}
			}
			result.append(fileList.toString()).append("\n      </li>\n");
		}
		result.append("    </ul>\n");
		return result.toString();
	}
}
