package net.sf.statcvs.util;

import java.util.Comparator;

public class FilenameComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		if (arg0 == null || arg1 == null)
			return 0;

		String s0 = arg0.toString().replace('/', '\t');
		String s1 = arg1.toString().replace('/', '\t');

		return s0.compareTo(s1);

	}

}
