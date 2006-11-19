package net.sf.statsvn.output;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BugzillaIntegration implements WebBugtrackerIntegration {
	private String baseUrl;
	private static final String BUGZILLA_RELPATH = "show_bug.cgi?id=";
	
	public BugzillaIntegration(final String baseUrl) {
		String bugzillaUrl = baseUrl;
		if (!bugzillaUrl.endsWith("/")) {
			bugzillaUrl += "/";
		}
		this.baseUrl = bugzillaUrl;
	}
	
	public String getName() {
		return "Bugzilla";
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String applyFilter(String s) {
		if (this.getBaseUrl() == null || this.getBaseUrl().length() == 0) {
			return s;
		}
			
		final String bugzillaUrl = this.getBaseUrl() + BUGZILLA_RELPATH;
		
		final Pattern pBugReference = Pattern.compile("Bug\\s+\\d+", Pattern.CASE_INSENSITIVE);
		final Pattern pBugNumber = Pattern.compile("\\d+");
		final Matcher m = pBugReference.matcher(s);
		
		int bugCount = 0;
		final Vector listBugRefs = new Vector();
		final Vector listBugLinks = new Vector();
		
		// build list of bugs references to replace
		while (m.find()) {
			final String bugIdText = m.group();
			final Matcher mBugId = pBugNumber.matcher(bugIdText);
			mBugId.find();
			final String bugLink = HTMLTagger.getIcon(HTMLOutput.BUG_ICON) + " <a href='" + bugzillaUrl 
			+ mBugId.group() + "'>" + bugIdText + "</a>";

			if (!listBugRefs.contains(bugIdText)) {
				listBugRefs.add(bugIdText);
				listBugLinks.add(bugLink);
				bugCount++;
			}
		}
		
		for (int i=0; i<bugCount; i++) {
			s = s.replaceAll((String)listBugRefs.get(i), (String)listBugLinks.get(i));
		}
		return s;
	}

}
