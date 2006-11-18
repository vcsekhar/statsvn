/**
 * 
 */
package net.sf.statcvs.util;

import net.sf.statcvs.output.ConfigurationOptions;

/**
 * @author jpdaigle
 *
 * Utility class to help build svn command strings
 */
public final class SvnCommandHelper {
	private SvnCommandHelper() {}
	
	/**
	 * Gets the authentication / non-interactive command part to use when invoking
	 * the subversion binary.
	 * 
	 * @return A String with the username, password and non-interactive settings
	 */
	public static String getAuthString() {
		final StringBuffer strAuth = new StringBuffer(" --non-interactive");
		if (ConfigurationOptions.getSvnUsername() != null) {
			strAuth.append(" --username ").append(ConfigurationOptions.getSvnUsername())
					.append(" --password ").append(ConfigurationOptions.getSvnPassword());
		}

		return strAuth.toString();
	}
	
}
