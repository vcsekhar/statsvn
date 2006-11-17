package net.sf.statcvs.renderer;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

/**
 * @author Anja
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PointXYRenderer extends StandardXYItemRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -353014143852731889L;

	public PointXYRenderer(final int type, final XYToolTipGenerator toolTipGenerator) {
		super(type, toolTipGenerator);
	}
	
	protected double getShapeScale(Plot plot, int series, int item, double x, double y) {
	  return 5;
	}
}
