/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2009 - 2025 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package fiji.util;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Fit a circle selection to an image. 
 * <p>
 *
 * The implementation largely follows
 *  <a>http://www.dtcenter.org/met/users/docs/write_ups/circle_fit.pdf</a>
 * (It is summarized below.)
 * <p>
 *
 * The major contributions of this plugin is that it does not take a list of
 * points, but uses all pixel locations, weighted by intensity.
 *
 * The circle fitting works like this: given a set of points
 * <tt>(x_i, y_i), i = 1, .., N</tt> try to fit a circle with radius r and center
 * <tt>(x_c, y_c)</tt> using the least squares method.
 *
 * The function to minimize is
 * <center>
 *  <tt>S = \sum g_i^2, where g_i = (x_i - x)^2 + (y_i - y)^2 - r^2</tt>
 * </center>
 * (It must be the square of g_i so that no summand is negative.)
 *
 * Without loss of generality, it can be assumed that
 *<tt>
 *  \sum x_i = \sum y_i = 0
 * </tt>
 * (If that is not the case, just subtract the means of {x_i} and {y_i} resp.
 * and add them to x_c and y_c resp.)
 *<p>
 * The derivative of S with regard to r^2 must be 0:
 *<p>
 *  d/dr^2 \sum g_i^2 = \sum 2 * g_i * -1
 *<p>
 * and therefore
 *<p>
 *  \sum g_i = 0
 *<p>
 * Expanded, with uu = \sum x_i * x_i, uv = \sum x_i * y_i and u, v, uu, uuu,
 * uuv, uvv, vvv defined analogously, this yields
 *<p>
 *  uu - 2 * u * x_c + N * x_c^2 + vv - 2 * v * y_c + N * y_c^2 - N * r^2 = 0
 *<p>
 * With u = v = 0, as per the earlier assumption, it follows that
 *<p>
 *  uu + vv + N * (x_c^2 + y_c^2 - r^2) = 0 (eq. 1)
 *<p>
 * The derivative of S with regard to x_c must be 0:
 *<p>
 *  d/dx_c \sum g_i^2 = \sum 2 * g_i * (2 * (x_i - x_c) * (-1))
 *  = -4 * \sum g_i * x_i + 4 * x_c * \sum g_i
 *<p>
 * and therefore
 *<p>
 *  \sum g_i * x_i = 0 (as \sum g_i must be 0 already)
 *<p>
 * Expanded, with uu and friends defined as before, this yields
 *<p>
 *  uuu - 2 * uu * x_c + u * x_c^2
 *  + uvv - 2 * uv * y_c + v * y_c^2 + u * r^2 = 0
 *<p>
 * Like before, u = v = 0, therefore
 *<p>
 *  2 * uu * x_c + 2 * uv * y_c = uuu + uvv (eq. 2)
 *<p>
 * For y_c, it is
 *<p>
 *  2 * uv * x_c + 2 * vv * y_c = uuv + vvv (eq. 3)
 *<p>
 * Equations 2 and 3 determine x_c and y_c, and equation 1 gives us the radius.
 * 
 * @author Johannes Schindelin
 * 
 */
public class Circle_Fitter implements PlugInFilter {
	ImagePlus image;
	ImageProcessor ip;
	float threshold = Float.NaN;
	int w, h;

	@Override
	public int setup(final String arg, final ImagePlus image) {
		this.image = image;
		return DOES_ALL | NO_CHANGES;
	}

	@Override
	public void run(final ImageProcessor ip) {
		this.ip = ip;
		w = ip.getWidth();
		h = ip.getHeight();
		threshold = getDefaultThreshold();

		final GenericDialog gd = new GenericDialog("Fit Circle");
		gd.addNumericField("threshold", threshold, 2);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		threshold = (float)gd.getNextNumber();
		final OvalRoi roi = calculateRoi();
		image.setRoi(roi);
	}

	/**
	 * Computes and return the circle Roi that fits. The fields for the ImageProcessor 
	 * and threshold must be set before calling this method; otherwise null is returned.
	 * 
	 * @return  the roi
	 */
	public OvalRoi calculateRoi() {

		if (ip==null || Float.isNaN(threshold)) { return null; }
		
		// calculate mean centroid
		float x, y, total;
		x = y = total = 0;
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++) {
				final float value = getValue(i, j);
				x += i * value;
				y += j * value;
				total += value;
			}

		x /= total;
		y /= total;

		// calculate the rest
		float uu, uv, vv, uuu, uuv, uvv, vvv;
		uu = uv = vv = uuu = uuv = uvv = vvv = 0;
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++) {
				final float value = getValue(i, j);
				final float u = i - x;
				final float v = j - y;
				uu += u * u * value;
				uv += u * v * value;
				vv += v * v * value;
				uuu += u * u * u * value;
				uuv += u * u * v * value;
				uvv += u * v * v * value;
				vvv += v * v * v * value;
			}

		// calculate center & radius
		final float f = 0.5f / (uu * vv - uv * uv);
		final float centerU = (vv * (uuu + uvv) - uv * (uuv + vvv)) * f;
		final float centerV = (-uv * (uuu + uvv) + uu * (uuv + vvv)) * f;
		final float radius = (float)Math.sqrt(centerU * centerU
				+ centerV * centerV + (uu + vv) / total);

		final int x0 = (int)Math.max(0, x + centerU - radius);
		final int y0 = (int)Math.max(0, y + centerV - radius);
		final int x1 = (int)Math.min(w, x + centerU + radius);
		final int y1 = (int)Math.min(h, y + centerV + radius);
		return new OvalRoi(x0, y0, x1 - x0, y1 - y0);
	}


	float getValue(final int i, final int j) {
		return Math.max(0, ip.getf(i, j) - threshold);
	}

	/**
	 * The default threshold is determined like this: as a circle is a linear
	 * structure, we want the _square root_ of the total number of pixels to
	 * contribute.
	 * <p>
	 * Therefore, we split the histogram into background and foreground where
	 * the number of pixels in the foreground part is \sqrt(w * h) and the split
	 * point is the desired threshold.
	 * 
	 * @return the default threshold.
	 */
	public float getDefaultThreshold() {
		if (ip==null) { return Float.NaN; }
		final int w = ip.getWidth(), h = ip.getHeight();
		float min, max;
		min = max = ip.getf(0, 0);
		// We cannot trust ip.getMin()!
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++) {
				final float v = ip.getf(i, j);
				if (min > v)
					min = v;
				else if (max < v)
					max = v;
			}
		final float[] histogram = new float[256];

		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++)
				histogram[(int)((ip.getf(i, j) - min)
					* 255.99f / (max - min))]++;
		int total = 0, i;
		final int want = (int)Math.sqrt(w * h);
		for (i = 255; i > 0 && total < want; i--)
			total += histogram[i];
		return min + i * (max - min) / 255.99f;
	}
	
	/*
	 * GETTER AND SETTERS
	 */

	/**
	 * Returns threshold used by this plugin.
	 * @return  the threshold
	 */
	public float getThreshold() {
		return threshold;
	}

	/**
	 * Sets the threshold for the fit. Intensities lower than the value given 
	 * will be ignored in the weight.
	 * @param threshold  the threshold.
	 */
	public void setThreshold(final float threshold) {
		this.threshold = threshold;
	}
	
	/**
	 * Sets the threshold used by this plugin to be the default one.
	 * 
	 * @see #getDefaultThreshold
	 */
	public void setAutoThreshold() {
		this.threshold = getDefaultThreshold();
	}

	/**
	 * Set the ImageProcessor that will be fitted.
	 * @param ip  the ImageProcessor
	 */
	public void setImageProcessor(final ImageProcessor ip) {
		this.ip = ip;
		w = ip.getWidth();
		h = ip.getHeight();
	}
}
