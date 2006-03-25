package net.sf.statcvs.renderer;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statcvs.output.HTMLOutput;
import net.sf.statcvs.output.HTMLTagger;

/*
 * WARNING KNOWN PROBLEM
 * If, in a single input string 's', two bug references exist, where one is a substring of the other (ex: "Bug 2" and "Bug 24"), the
 * output is corrupted because the replaceAll operation mangles the superstring.
 */

/**
 * 
 * @author jpdaigle
 *
 */
public class BugzillaTextFilter implements TextFilter {
	public static final String BUGZILLA_RELPATH = "show_bug.cgi?id=";
	
	public String applyFilter(String s) {
		if (ConfigurationOptions.getBugzillaUrl() == null || ConfigurationOptions.getBugzillaUrl() == "")
			return s;
		
		String bugzillaUrl = ConfigurationOptions.getBugzillaUrl();
		if (!bugzillaUrl.endsWith("/")) bugzillaUrl += "/";
		bugzillaUrl += BUGZILLA_RELPATH;
		
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
