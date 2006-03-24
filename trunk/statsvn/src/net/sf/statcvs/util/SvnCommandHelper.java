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
public class SvnCommandHelper {
	
	/**
	 * Gets the authentication / non-interactive command part to use when invoking
	 * the subversion binary.
	 * 
	 * @return A String with the username, password and non-interactive settings
	 */
	public static String getAuthString() {
		String strAuth = " --non-interactive";
		if (ConfigurationOptions.getSvnUsername() != null)
			strAuth	+= " --username " + ConfigurationOptions.getSvnUsername()
					+  " --password " + ConfigurationOptions.getSvnPassword();

		return strAuth;
	}
	
}
