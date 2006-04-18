package net.sf.statcvs.output;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BugzillaIntegration implements WebBugtrackerIntegration {
	protected String _baseUrl;
	protected static final String BUGZILLA_RELPATH = "show_bug.cgi?id=";
	
	public BugzillaIntegration(String baseUrl) {
		String bugzillaUrl = baseUrl;
		if (!bugzillaUrl.endsWith("/")) bugzillaUrl += "/";
		_baseUrl = bugzillaUrl;
	}
	
	public String getName() {
		return "Bugzilla";
	}

	public String getBaseUrl() {
		return _baseUrl;
	}

	public String applyFilter(String s) {
		if (this.getBaseUrl() == null || this.getBaseUrl() == "")
			return s;
			
		String bugzillaUrl = this.getBaseUrl() + BUGZILLA_RELPATH;
		
		Pattern pBugReference = Pattern.compile("Bug\\s+\\d+", Pattern.CASE_INSENSITIVE);
		Pattern pBugNumber = Pattern.compile("\\d+");
		Matcher m = pBugReference.matcher(s);
		
		int bugCount = 0;
		Vector listBugRefs = new Vector();
		Vector listBugLinks = new Vector();
		
		// build list of bugs references to replace
		while (m.find()) {
			String bugIdText = m.group();
			Matcher mBugId = pBugNumber.matcher(bugIdText);
			mBugId.find();
			String bugLink = HTMLTagger.getIcon(HTMLOutput.BUG_ICON) + " <a href='" + bugzillaUrl + mBugId.group() + "'>" + bugIdText + "</a>";

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
