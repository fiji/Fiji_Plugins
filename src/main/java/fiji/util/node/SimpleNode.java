/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2009 - 2023 Fiji developers.
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
package fiji.util.node;


public class SimpleNode implements Leaf<SimpleNode>
{
	final float p[];
	final int numDimensions;

	public SimpleNode(final SimpleNode node) {
		this.p = node.p.clone();
		this.numDimensions = p.length;
	}

	public SimpleNode(final float[] p) {
		this.p = p.clone();
		this.numDimensions = p.length;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	public boolean equals(final SimpleNode o) {
		if (o.getNumDimensions() != numDimensions)
			return false;

		for (int d = 0; d < numDimensions; ++d)
			if (p[d] != o.p[d])
				return false;

		return true;
	}

	@Override
	public float distanceTo(final SimpleNode o) {
		double dist = 0;

		for (int d = 0; d < numDimensions; ++d) {
			final double v = o.get(d) - get(d);
			dist += v*v;
		}

		return (float)Math.sqrt(dist);
	}

	@Override
	public float get(final int k) {
		return p[k];
	}

	@Override
	public String toString() {
		String s = "(" + p[0];

		for (int d = 1; d < numDimensions; ++d)
			s += ", " + p[d];

		s += ")";
		return s;
	}

	@Override
	public SimpleNode[] createArray(final int n) {
		return new SimpleNode[n];
	}

	@Override
	public int getNumDimensions() {
		return numDimensions;
	}
}
