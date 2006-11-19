package net.sf.statsvn.util;

import java.text.SimpleDateFormat;

import net.sf.statsvn.Messages;

public final class StatSvnConstants {
	public static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat(Messages.getString("DATE_FORMAT"));
	public static final SimpleDateFormat OUTPUT_DATE_TIME_FORMAT = new SimpleDateFormat(Messages.getString("DATE_TIME_FORMAT"));

	private StatSvnConstants() {
		
	}
}
