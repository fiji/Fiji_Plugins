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
package fiji.selection;

import fiji.util.IntArray;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


/**
 * This is a template for a plugin that requires one image to
 * be opened, and takes it as parameter.
 */
public class Binary_to_Point_Selection implements PlugInFilter {
	protected ImagePlus image;

	/**
	 * This method gets called by ImageJ / Fiji to determine
	 * whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 */
	public int setup(String arg, ImagePlus image) {
		this.image = image;
		/*
		 * The current return value accepts all gray-scale
		 * images (if you access the pixels with ip.getf(x, y)
		 * anyway, that works quite well.
		 *
		 * It could also be DOES_ALL; you can add "| NO_CHANGES"
		 * to indicate that the current image will not be
		 * changed by this plugin.
		 *
		 * Beware of DOES_STACKS: this will call the run()
		 * method with all slices of the current image
		 * (channels, z-slices and frames, all). Most likely
		 * not what you want.
		 */
		return DOES_8G;
	}

	/**
	 * This method is run when the current image was accepted.
	 *
	 * @param ip is the current slice (typically, plugins use
	 * the ImagePlus set above instead).
	 */
	public void run(ImageProcessor ip) {
		int w = ip.getWidth(), h = ip.getHeight();
		IntArray x = new IntArray();
		IntArray y = new IntArray();
		for (int j = 0; j < h; j++)
			for (int i = 0; i < w; i++)
				if (ip.getf(i, j) > 127) {
					x.add(i);
					y.add(j);
				}
		image.setRoi(new PointRoi(x.buildArray(), y.buildArray(), x.size()));
	}
}
