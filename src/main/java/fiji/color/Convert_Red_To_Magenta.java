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
package fiji.color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
* Convert all reds to magentas (to help red-green blind viewers).
* <p>
* Updated May 30, 2025 to apply to all slices in a stack
* by Jeff Hardin, Dept. of Integrative Biology
* Univ. of Wisconsin-Madison
* </p>
*/
public class Convert_Red_To_Magenta implements PlugInFilter {
	protected ImagePlus image;
	public int numSlices, currentSlice;
	public ImageStack stack;

	/**
	 * This method gets called by ImageJ / Fiji to determine
	 * whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 */
	public int setup(String arg, ImagePlus image) {
		if (image!= null) {
			stack = image.getStack();
			numSlices = stack.getSize(); // Get the number of slices
			currentSlice = image.getCurrentSlice(); // Get current slice
		}
		this.image = image;
		return DOES_RGB;
	}

	/**
	 * This method is run when the current image was accepted.
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 */
	public void run(ImageProcessor ip) {
		if (stack.isVirtual()) {
			IJ.showMessage("Virtual Stack", "This plugin does not support virtual stacks.");
			return;
		}

		if (numSlices == 1) {
			process((ColorProcessor)ip);
			image.updateAndDraw();
		}
		else {
			//Ask user if OK to apply to all slices in a stack
			GenericDialog gd = new GenericDialog("Apply to entire stack?");
			gd.addMessage("Replace red with magenta in all slices?\n \n" +
				"NOTE: There is no undo for this operation.");
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			for (int i = 0; i < numSlices; i++) {
				//image.setSlice(i+1);
				IJ.showStatus("Processing slice " + (i+1) + " of " + numSlices);
				IJ.showProgress(i/numSlices);
				ImageProcessor ip2 = stack.getProcessor(i+1);
				process((ColorProcessor)ip2);
			}
			image.setSlice(currentSlice);
			image.updateAndDraw();
		}
	}

	public static void process(ColorProcessor ip) {
		int w = ip.getWidth(), h = ip.getHeight();
		int[] pixels = (int[])ip.getPixels();
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++) {
				int value = pixels[i + j * w];
				int red = (value >> 16) & 0xff;
				int green = (value >> 8) & 0xff;
				int blue = value & 0xff;
				if (false && blue > 16)
					continue;
				pixels[i + j * w] = (red << 16) | (green << 8) | red;
			}
	}
}
