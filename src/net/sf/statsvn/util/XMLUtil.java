package net.sf.statsvn.util;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * 
 * Utilities class to faciliate XML management.
 * 
 * @author Jason Kealey <jkealey@shade.ca>
 * @author Gunter Mussbacher <gunterm@site.uottawa.ca>
 * 
 * @version $Id$
 */
public final class XMLUtil {
	/**
	 * A utility class (only static methods) should be final and have
	 * a private constructor.
	 */
	private XMLUtil() {}

	/**
	 * For some reason, can't find this utility method in the java framework.
	 * 
	 * @param sDateTime
	 *            an xsd:dateTime string
	 * @return an equivalent java.util.Date
	 * @throws ParseException
	 */
	public static Date parseXsdDateTime(String sDateTime) throws ParseException {
		final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		int iDotPosition = 19;
		if (sDateTime.charAt(0) == '-') {
			iDotPosition = 20;
		}
		Date result;
		if (sDateTime.length() <= iDotPosition) {
			return format.parse(sDateTime + "Z");
		}

		String millis = null;
		char c = sDateTime.charAt(iDotPosition);
		if (c == '.') {
			// if datetime has milliseconds, separate them
			int eoms = iDotPosition + 1;
			while (Character.isDigit(sDateTime.charAt(eoms))) {
				eoms += 1;
			}
			millis = sDateTime.substring(iDotPosition, eoms);
			sDateTime = sDateTime.substring(0, iDotPosition) + sDateTime.substring(eoms);
			c = sDateTime.charAt(iDotPosition);
		}
		if (c == '+' || c == '-') {
			format.setTimeZone(TimeZone.getTimeZone("GMT" + sDateTime.substring(iDotPosition)));
			sDateTime = sDateTime.substring(0, iDotPosition) + "Z";
		} else if (c != 'Z') {
			throw new ParseException("Illegal timezone specification.", iDotPosition);
		}

		result = format.parse(sDateTime);
		if (millis != null) {
			result.setTime(result.getTime() + Math.round(Float.parseFloat(millis) * 1000));
		}

		result = offsetDateFromGMT(result);
		return result;
	}

	/**
	 * This method converts from GMT to local timezone
	 * 
	 * @param date
	 * 		date in GMT timezone
	 * @return the date in local timezone
	 */
	public static Date offsetDateFromGMT(final Date date) {
		// Create a calendar - it will default to the current OS timezone.
		final GregorianCalendar gc = new GregorianCalendar();

		// Calculate the total offset from GMT
		final int totalOffset = gc.get(Calendar.ZONE_OFFSET) + gc.get(Calendar.DST_OFFSET);

		// Calculate the time in GMT
		final long localTime = date.getTime() + totalOffset;

		// Create a date using the calculated GMT time
		final Date localDate = new Date(localTime);

		return localDate;

	}

	/**
	 * This method writes a DOM document to a file
	 * 
	 * @param doc
	 *            DOM document.
	 * @param filename
	 *            the target file.
	 */
	public static void writeXmlFile(final Document doc, final String filename) {
		try {
			// Prepare the DOM document for writing
			final Source source = new DOMSource(doc);

			// Prepare the output file
			final File file = new File(filename);
			final Result result = new StreamResult(file);

			// Write the DOM document to the file
			final Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		} catch (final TransformerConfigurationException e) {
		} catch (final TransformerException e) {
		}
	}

}
