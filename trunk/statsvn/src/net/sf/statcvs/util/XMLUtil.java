package net.sf.statcvs.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class XMLUtil {
    /**
     * For some reason, can't find this utility method in the java framework.
     * 
     * @param sDateTime
     *            an xsd:dateTime string
     * @return an equivalent java.util.Date
     * @throws ParseException
     */
    public static Date parseXsdDateTime(String sDateTime) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        int iDotPosition = 19;
        if (sDateTime.charAt(0) == '-') {
            iDotPosition = 20;
        }
        Date result;
        if (sDateTime.length() <= iDotPosition)
            return format.parse(sDateTime + "Z");

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
        return result;
    }

}
