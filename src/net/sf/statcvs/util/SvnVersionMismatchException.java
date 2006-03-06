package net.sf.statcvs.util;

public class SvnVersionMismatchException extends Exception {
	private static final long serialVersionUID = 1L;

	public SvnVersionMismatchException() {
		super("Subversion binary is incorrect version or not found.");
	}
	
	public SvnVersionMismatchException(String m){
		super(m);
	}
	
	public SvnVersionMismatchException(String found, String required) {
		super("Subversion binary is incorrect version. Found: " + found + ", required: " + required);
	}
}

