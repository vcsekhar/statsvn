package net.sf.statcvs.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.statcvs.output.ConfigurationOptions;

public class SvnDiffUtils {

	public synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr) {
		InputStream istream = null;
		String svnDiffCommand = /*
								 * FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) +
								 * FileUtils.getDirSeparator() +
								 */"svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted";
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null,
					new File(FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) + FileUtils.getDirSeparator()));
			// OutputStream ostream = p.getOutputStream();
			// ostream.write(input);
			// ostream.close();
			istream = new BufferedInputStream(p.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * Status status = new Status(Status.ERROR, "seg.jUCMNav", 1,
			 * e.toString(), e); //$NON-NLS-1$ ErrorDialog.openError(getShell(),
			 * Messages.getString("AutoLayoutWizard.autoLayoutError"),
			 * //$NON-NLS-1$
			 * Messages.getString("AutoLayoutWizard.errorOccured"),
			 * //$NON-NLS-1$ status, IStatus.ERROR | IStatus.WARNING);
			 */
			return null;
		}
		return istream;
	}

	public synchronized static InputStream callSvnDiff(String oldRevNr, String newRevNr, String filename) {
		InputStream istream = null;
		String svnDiffCommand = /*
								 * FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) +
								 * FileUtils.getDirSeparator() +
								 */"svn diff -r " + oldRevNr + ":" + newRevNr + " --no-diff-deleted " + filename;
		try {
			Process p = Runtime.getRuntime().exec(svnDiffCommand, null,
					new File(FileUtils.getPathWithoutEndingSlash(ConfigurationOptions.getCheckedOutDirectory()) + FileUtils.getDirSeparator()));
			// OutputStream ostream = p.getOutputStream();
			// ostream.write(input);
			// ostream.close();
			istream = new BufferedInputStream(p.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			/*
			 * Status status = new Status(Status.ERROR, "seg.jUCMNav", 1,
			 * e.toString(), e); //$NON-NLS-1$ ErrorDialog.openError(getShell(),
			 * Messages.getString("AutoLayoutWizard.autoLayoutError"),
			 * //$NON-NLS-1$
			 * Messages.getString("AutoLayoutWizard.errorOccured"),
			 * //$NON-NLS-1$ status, IStatus.ERROR | IStatus.WARNING);
			 */
			return null;
		}
		return istream;
	}
	
}
