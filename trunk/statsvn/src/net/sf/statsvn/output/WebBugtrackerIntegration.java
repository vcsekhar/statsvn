package net.sf.statsvn.output;


public interface WebBugtrackerIntegration {
	/**
	 * Returns the name of the bug tracker
	 * @return the name of the bug tracker
	 */
	String getName();

	/**
	 * Gets the base url of the bug tracker.
	 * @return The base url of the bug tracker.
	 */
	String getBaseUrl();
	
	/**
	 * Filters a String, replacing bug references with links to the tracker.
	 * @param input String to examine for bug references
	 * @return A copy of <code>input</code>, with bug references replaced with HTML links
	 */
	String applyFilter(String input);
	
}
