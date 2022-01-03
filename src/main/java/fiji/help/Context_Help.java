/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2009 - 2022 Fiji developers.
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
package fiji.help;

import fiji.util.MenuItemDiverter;
import ij.IJ;
import ij.plugin.BrowserLauncher;

public class Context_Help extends MenuItemDiverter {
        public final static String url =
                "http://fiji.sc/wiki/index.php/";

	protected String getTitle() {
		return "Context Help";
	}

	protected void action(String arg) {
		IJ.showStatus("Opening help for " + arg + "...");
		new BrowserLauncher().run(url + arg.replace(' ', '_')
			+ "?menuentry=yes");
	}
}
