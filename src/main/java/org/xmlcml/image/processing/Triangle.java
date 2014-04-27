package org.xmlcml.image.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmlcml.euclid.Int2;
import org.xmlcml.image.compound.PixelList;

public class Triangle {

	private Pixel[] pixel = new Pixel[3];
	private Set<Pixel> set = new HashSet<Pixel>();
	private PixelIsland island;
	private List<Pixel> diagonal;

	Triangle (Pixel pixel0, Pixel pixel1, Pixel pixel2, PixelIsland island) {
		this.island = island;
		this.pixel[0] = pixel0;
		this.pixel[1] = pixel1;
		this.pixel[2] = pixel2;
		set.addAll(Arrays.asList(pixel));
	}

	/** returns a Triangle if points for right-angled isosceles.
	 * 
	 * @param pixel0
	 * @param pixel1
	 * @param pixel2
	 * @param island
	 * @return null if not RH isosceles
	 */
	public static Triangle createTriangle(Pixel pixel0, Pixel pixel1, Pixel pixel2, PixelIsland island) {
		Triangle triangle = new Triangle(pixel0, pixel1, pixel2, island);
		return triangle.createSet() ? triangle : null;
	}

	private boolean createSet() {
		boolean created = false;
		// we must set up neightbours
		PixelList neighbours0 = pixel[0].getNeighbours(island);
		PixelList neighbours1 = pixel[1].getNeighbours(island);
		PixelList neighbours2 = pixel[2].getNeighbours(island);
		boolean n01 = neighbours0.contains(pixel[1]);
		boolean n02 = neighbours0.contains(pixel[2]);
		boolean n12 = neighbours1.contains(pixel[2]);
		if (n01 && n02 && n12) {
			set.addAll(Arrays.asList(pixel));
			created = true;
		}
		return created;
	}
	
	@Override 
	public boolean equals(Object o) {
		boolean equals = false;
		if (o != null && o instanceof Triangle) {
			Triangle triangle = (Triangle) o;
			equals = set.equals(triangle.set);
		}
		return equals;
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}
	
	public Set<Pixel> addAll(Triangle triangle) {
		Set<Pixel> union = new HashSet<Pixel>(set);
		union.addAll(triangle.set);
		return union;
	}
	
	public Set<Pixel> retainAll(Triangle triangle) {
		Set<Pixel> retained = new HashSet<Pixel>(set);
		retained.retainAll(triangle.set);
		return retained;

	}
	
	/** looks for pixel whose neighbours are in x and y axial direction.
	 * 
	 * @return
	 */
	public Pixel findRightAnglePixel() {
		for (int i = 0; i < 3; i++) {
			Pixel pixeli = pixel[i];
			Pixel pixelj = pixel[(i+1)%3];
			Pixel pixelk = pixel[(i+2)%3];
			if (isTriangle(pixel[i], pixelj, pixelk)) {
				return pixel[i];
			}
			if (isTriangle(pixel[i], pixelk, pixelj)) {
				return pixel[i];
			}
		}
		return null;
	}
	
	private boolean isTriangle(Pixel pixel0, Pixel pixel1, Pixel pixel2) {
		Int2 i0 = pixel0.getInt2();
		Int2 i1 = pixel1.getInt2();
		Int2 i2 = pixel2.getInt2();
		Int2 d01 = i0.subtract(i1);
		Int2 d02 = i0.subtract(i2);
		int dot = d01.dotProduct(d02);
		return dot == 0;
	}

	public List<Pixel> getDiagonal() {
		diagonal = null;
		Pixel rightAngle = findRightAnglePixel();
		if (rightAngle != null) {
			diagonal = new ArrayList<Pixel>(Arrays.asList(set.toArray(new Pixel[0])));
			diagonal.remove(rightAngle);
		}
		return diagonal;
	}

	public void removeDiagonalNeighbours() {
		getDiagonal();
		Pixel pixel0 = diagonal.get(0);
		Pixel pixel1 = diagonal.get(1);
		pixel0.getNeighbours(island).remove(pixel1);
		pixel1.getNeighbours(island).remove(pixel0);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(pixel[0]);
		sb.append(" "+pixel[1]);
		sb.append(" "+pixel[2]);
		sb.append("; diag "+diagonal);
		return sb.toString();
	}
}
