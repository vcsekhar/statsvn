/**
 * 
 */
package net.sf.statsvn.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.TestCase;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.output.ConfigurationException;
import net.sf.statcvs.output.ConfigurationOptions;
import net.sf.statsvn.output.SvnConfigurationOptions;

/**
 * @author Jason Kealey
 *
 */
public class SvnDiffUtilsTest extends TestCase {
	public void testSimple() {
		try {
			SvnConfigurationOptions.setSvnUsername("jkealey");
			SvnConfigurationOptions.setSvnPassword("PASSWORD");
			ConfigurationOptions.setCheckedOutDirectory("k:\\work\\lavablast");
			SvnInfoUtils.loadInfo();
			
	        Vector output = SvnDiffUtils.getLineDiff("2435");
	        
	        for (Iterator iter = output.iterator(); iter.hasNext();) {
	            Object[] element = (Object[]) iter.next();
	            if (element.length == 3)
	            {
	            	String file = element[0].toString();
	            	int[] diff = (int[]) element[1];
	            	Boolean isBinary = (Boolean) element[2];
	            	System.out.println("File: " + file + ", Added: " + diff[0] + ", Removed: " + diff[1] + ", Binary:" + isBinary);
	            }
            }
	        
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (BinaryDiffException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (ConfigurationException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (LogSyntaxException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
}
