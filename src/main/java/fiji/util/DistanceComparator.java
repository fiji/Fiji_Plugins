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
/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * @author Johannes Schindelin and Stephan Preibisch
 */
package fiji.util;

import java.util.Comparator;

import fiji.util.node.Leaf;

/**
 * Compares which {@link Leaf} is closer to another {@link Leaf}
 * 
 * @author Johannes Schindelin and Stephan Preibisch
 *
 * @param <T>
 *            the type of object stored in the leaf.
 */
public class DistanceComparator< T extends Leaf<T> > implements Comparator<T>
{
	final T point;
	
	public DistanceComparator( final T point )
	{
		this.point = point;
	}
	
	@Override
	public int compare( final T a, final T b ) 
	{
		final double distA = point.distanceTo( a );
		final double distB = point.distanceTo( b );
		return distA < distB ? -1 : distA > distB ? +1 : 0;
	}
}
