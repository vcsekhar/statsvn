/**
 * Copyright 2005, ObjectLab Financial Ltd
 * All rights protected.
 */

package net.sf.statsvn;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import net.sf.statcvs.model.Repository;
import net.sf.statcvs.model.Revision;
import net.sf.statcvs.model.VersionedFile;
import net.sf.statsvn.output.SvnConfigurationOptions;

/**
 * Execute a Repository Dump on the TaskLogger.
 * 
 * @author Benoit Xhenseval
 */
public class RepoDump {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final Repository repository;

	public RepoDump(final Repository repo) {
		repository = repo;
	}

	public void dump() {
		SortedSet revisions = repository.getRevisions();
		int totalDelta = 0;
		Set filesViaRevisions = new HashSet();
		int totalLastRev = 0;
		SvnConfigurationOptions.getTaskLogger().log("\n\n#### DUMP PER REVISION ####");
		String previousRevision = "";
		for (Iterator it = revisions.iterator(); it.hasNext();) {
			Revision rev = (Revision) it.next();
			if (!rev.getRevisionNumber().equals(previousRevision)) {
				previousRevision = rev.getRevisionNumber();
				SvnConfigurationOptions.getTaskLogger().log("Revision " + padRight(rev.getRevisionNumber(), 5) + " " + SDF.format(rev.getDate()));
			}
			SvnConfigurationOptions.getTaskLogger().log(
			        "\tlines:" + padRight(rev.getLines(), 6) + " D:" + padRight(rev.getLinesDelta(), 5) + " Rep:" + padRight(rev.getReplacedLines(), 5)
			                + " New:" + padRight(rev.getNewLines(), 5) + printBoolean(" Initial", rev.isInitialRevision())
			                + printBoolean(" BegLog", rev.isBeginOfLog()) + printBoolean(" Dead", rev.isDead()) + " " + rev.getFile().getFilenameWithPath());

			totalDelta += rev.getLinesDelta();
			if (rev.isBeginOfLog()) {
				totalDelta += rev.getLines();
			}
			VersionedFile file = rev.getFile();
			Revision fileRev = file.getLatestRevision();
			if (!fileRev.isDead() /*
									 * &&
									 * fileRev.getRevisionNumber().equals(rev.getRevisionNumber())
									 */&& !filesViaRevisions.contains(file.getFilenameWithPath())) {
				totalLastRev += file.getCurrentLinesOfCode();
			}
			filesViaRevisions.add(file.getFilenameWithPath());
		}

		SvnConfigurationOptions.getTaskLogger().log("\n\n#### DUMP PER FILE ####");

		int totalCurrentLOCPerFile = 0;
		SortedSet files = repository.getFiles();
		int totalNumRevision = 0;
		int fileNumber = 0;
		int totalMisMatch = 0;
		int numberMisMatch = 0;
		for (Iterator it = files.iterator(); it.hasNext();) {
			VersionedFile rev = (VersionedFile) it.next();
			totalCurrentLOCPerFile += rev.getCurrentLinesOfCode();
			totalNumRevision += rev.getRevisions().size();
			SvnConfigurationOptions.getTaskLogger().log("File " + ++fileNumber + "/ " + rev.getFilenameWithPath() + " \tLOC:" + rev.getCurrentLinesOfCode());
			int sumDelta = 0;
			// go through all revisions for this file.
			for (Iterator it2 = rev.getRevisions().iterator(); it2.hasNext();) {
				Revision revi = (Revision) it2.next();

				sumDelta += revi.getLinesDelta();
				if (revi.isBeginOfLog()) {
					sumDelta += revi.getLines();
				}
				SvnConfigurationOptions.getTaskLogger().log(
				        "\tRevision:" + padRight(revi.getRevisionNumber(), 5) + " \tDelta:" + padRight(revi.getLinesDelta(), 5) + "\tLines:"
				                + padRight(revi.getLines(), 5) + "\t" + printBoolean("Ini:", revi.isInitialRevision()) + "\t"
				                + printBoolean("BegLog", revi.isBeginOfLog()) + "\t" + printBoolean("Dead", revi.isDead()) + "\tSumDelta:"
				                + padRight(sumDelta, 5));
			}
			if (sumDelta != rev.getCurrentLinesOfCode()) {
				SvnConfigurationOptions.getTaskLogger().log(
				        "\t~~~~~SUM DELTA DOES NOT MATCH LOC " + rev.getCurrentLinesOfCode() + " vs " + sumDelta + " Diff:"
				                + (rev.getCurrentLinesOfCode() - sumDelta) + " ~~~~~~");
				totalMisMatch += (rev.getCurrentLinesOfCode() - sumDelta);
				numberMisMatch++;
			}
		}
		SvnConfigurationOptions.getTaskLogger().log("----------------------------------");
		SvnConfigurationOptions.getTaskLogger().log("Current Repo Line Code :" + repository.getCurrentLOC());
		SvnConfigurationOptions.getTaskLogger().log("-----Via Files--------------------");
		SvnConfigurationOptions.getTaskLogger().log("Number of Files via Files  :" + files.size());
		SvnConfigurationOptions.getTaskLogger().log(
		        "Sum Current LOC via File   :" + totalCurrentLOCPerFile + "\tDiff with Repo LOC " + (repository.getCurrentLOC() - totalCurrentLOCPerFile) + " "
		                + (repository.getCurrentLOC() - totalCurrentLOCPerFile == 0 ? "OK" : "NOT Ok"));
		SvnConfigurationOptions.getTaskLogger().log("# of File Revision via File:" + totalNumRevision);
		SvnConfigurationOptions.getTaskLogger().log("-----Via Revisions----------------");
		SvnConfigurationOptions.getTaskLogger().log(
		        "# of Files via Revisions   :" + padRight(filesViaRevisions.size(), 5) + "\tDiff with via Files:" + (files.size() - filesViaRevisions.size())
		                + (files.size() - filesViaRevisions.size() == 0 ? " OK" : "NOT Ok"));
		SvnConfigurationOptions.getTaskLogger().log(
		        "# of File Revision via Revi:" + padRight(revisions.size(), 5) + "\tDiff with via Files:" + (totalNumRevision - revisions.size())
		                + (totalNumRevision - revisions.size() == 0 ? " OK" : "NOT Ok"));
		SvnConfigurationOptions.getTaskLogger().log(
		        "Sum Delta via Revisions    :" + padRight(totalDelta, 5) + "\tDiff with Repo LOC " + (repository.getCurrentLOC() - totalDelta) + " "
		                + (repository.getCurrentLOC() - totalDelta == 0 ? "OK" : "NOT Ok"));
		if (numberMisMatch > 0) {
			SvnConfigurationOptions.getTaskLogger().log("**** PROBLEM ******");
			SvnConfigurationOptions.getTaskLogger().log("Number of Mismatches       :" + padRight(numberMisMatch, 5) + "\tLOC Mismatch:" + totalMisMatch);
		}
		SvnConfigurationOptions.getTaskLogger().log(
		        "Tot LOC via Last Rev file  :" + padRight(totalLastRev, 5) + "\tDiff with Repo LOC " + (repository.getCurrentLOC() - totalLastRev)
		                + (repository.getCurrentLOC() - totalDelta == 0 ? " OK" : " NOT Ok"));
		SvnConfigurationOptions.getTaskLogger().log("----------------------------------");
	}

	private String padRight(final int str, final int size) {
		return padRight(String.valueOf(str), size);
	}

	private String printBoolean(final String title, final boolean test) {
		return title + ":" + (test ? "Y" : "N");
	}

	private String padRight(final String str, final int size) {
		StringBuffer buf = new StringBuffer(str);
		int requiredPadding = size;
		if (str != null) {
			requiredPadding = requiredPadding - str.length();
		}
		if (requiredPadding > 0) {
			for (int i = 0; i < requiredPadding; i++) {
				buf.append(" ");
			}
		}
		return buf.toString();
	}

}
