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
package fiji.geom;

/*
 * This class provides methods to calculate circumference, area and center
 * of gravity of polygons.
 *
 * It is part of Fiji (http://fiji.sc/), and is licensed under
 * the GPLv2.  If you do not know what the GPLv2 is, you have no license
 * to use it at all.
 */
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

public class AreaCalculations {
	protected abstract static class Calculation {
		protected double[] start = new double[2],
			  previous = new double[2], current = new double[2];

		public double result; // for convenience

		public abstract void handleSegment();

		public void apply(final PathIterator path) {
			double[] swap;

			while (!path.isDone()) {
				switch (path.currentSegment(current)) {
				case PathIterator.SEG_MOVETO:
					System.arraycopy(current, 0,
							start, 0, 2);
					break;
				case PathIterator.SEG_CLOSE:
					System.arraycopy(start, 0,
							current, 0, 2);
					/* fallthru */
				case PathIterator.SEG_LINETO:
					handleSegment();
					break;
				default:
					throw new RuntimeException("invalid "
						+ "polygon");
				}
				swap = current;
				current = previous;
				previous = swap;
				path.next();
			}
		}

		public double calculate(final PathIterator path) {
			result = 0;
			apply(path);
			return result;
		}
	}

	protected static class Circumference extends Calculation {
		@Override
		public void handleSegment() {
			final double x = current[0] - previous[0];
			final double y = current[1] - previous[1];
			result += Math.sqrt(x * x + y * y);
		}
	}

	/**
	 * Computes the perimeter of the specified path or multiple paths.
	 * 
	 * @param path
	 *            the path.
	 * @return the perimeter.
	 */
	public static double circumference(final PathIterator path) {
		return new Circumference().calculate(path);
	}

	public static double triangleArea(final double[] a, final double[] b, final double[] c) {
		/* half the scalar product between (b - a)^T and (c - a) */
		return ((a[1] - b[1]) * (c[0] - a[0]) +
			(b[0] - a[0]) * (c[1] - a[1])) / 2;
	}

	/* This assumes even/odd winding rule, and it has a sign */
	protected static class Area extends Calculation {
		@Override
		public void handleSegment() {
			result += triangleArea(start, previous, current);
		}
	}

	/**
	 * Computes the surface area of the path or multiple specified paths.
	 * Returns a positive value for counter-clockwise paths, and negative for
	 * clockwise paths. Considers holes as holes; i.e. will do the right
	 * operation.
	 * 
	 * @param path
	 *            the path.
	 * @return the area.
	 */
	public static double area(final PathIterator path) {
		return new Area().calculate(path);
	}

	protected static class Centroid extends Calculation {
		double totalArea, x, y;

		@Override
		public void handleSegment() {
			final double area = triangleArea(start, previous, current);
			totalArea += area;
			x += (start[0] + previous[0] + current[0]) / 3 * area;
			y += (start[1] + previous[1] + current[1]) / 3 * area;
		}

		public double[] getResult() {
			return new double[] { x / totalArea, y / totalArea };
		}
	}

	public static double[] centroid(final PathIterator path) {
		final Centroid centroid = new Centroid();
		centroid.apply(path);
		return centroid.getResult();
	}

	public static void main(final String[] args) {
		final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		path.moveTo(100, 100);
		path.lineTo(200, 110);
		path.lineTo(190, 213);
		path.lineTo(105, 205);
		path.closePath();

		double result = circumference(path.getPathIterator(null));
		double expect = Math.sqrt(100 * 100 + 10 * 10) +
			Math.sqrt(10 * 10 + 103 * 103) +
			Math.sqrt(85 * 85 + 8 * 8) +
			Math.sqrt(5 * 5 + 105 * 105);
		System.err.println("result: " + result + ", expect: " + expect +
				", diff: " + (result - expect));

		result = triangleArea(new double[] { 100, 100 },
			new double[] { 200, 110 }, new double[] { 190, 213 });
		expect = 100 * 113 - 100 * 10 / 2 - 10 * 103 / 2 - 90 * 113 / 2;
		System.err.println("triangleArea: " + result
				+ ", expect: " + expect
				+ ", diff: " + (result - expect));

		result = triangleArea(new double[] { 100, 100 },
			new double[] { 190, 213 }, new double[] { 105, 205 });
		expect = 90 * 113 - 90 * 113 / 2 - 85 * 8 / 2 - 5 * 105.0 / 2
			- 5 * 8;
		System.err.println("triangleArea: " + result
				+ ", expect: " + expect
				+ ", diff: " + (result - expect));

		result = area(path.getPathIterator(null));
		expect = 100 * 113 - 100 * 10 / 2 - 10 * 103 / 2
			- 85 * 8 / 2 - 5 * 105.0 / 2.0 - 5 * 8;
		System.err.println("result: " + result + ", expect: " + expect +
				", diff: " + (result - expect));

		final double[] result2 = centroid(path.getPathIterator(null));
		final double area1 = triangleArea(new double[] { 100, 100 },
			new double[] { 200, 110 }, new double[] { 190, 213 });
		final double area2 = triangleArea(new double[] { 100, 100 },
			new double[] { 190, 213 }, new double[] { 105, 205 });
		final double[] expect2 = new double[2];
		expect2[0] = (area1 * (100 + 200 + 190) / 3
				+ area2 * (100 + 190 + 105) / 3)
			/ (area1 + area2);
		expect2[1] = (area1 * (100 + 110 + 213) / 3
				+ area2 * (100 + 213 + 205) / 3)
			/ (area1 + area2);
		System.err.println("result: " + result2[0] + ", " + result2[1]
			+ ", expect: " + expect2[0] + ", " + expect2[1] +
			", diff: " + Math.sqrt((result2[0] - expect2[0])
				* (result2[0] - expect2[0]) +
				(result2[1] - expect2[1]) *
				(result2[1] - expect2[1])));
	}
}
