package org.xmlcml.image.processing;

import org.xmlcml.euclid.Int2;

public abstract class PixelNode implements Comparable<PixelNode> {

	Pixel centrePixel; // pixel 1

	protected PixelNode() {
		
	}
	
	protected PixelNode(Pixel pixel) {
		this.centrePixel = pixel;
	}
	
	public Pixel getCentrePixel() {
		return centrePixel;
	}

	/** compare Y values of centrePixels then X.
	 * if Y values are equal compare X
	 * 
	 */
	public int compareTo(PixelNode node1) {
		int compare = -1;
		if (node1 != null) {
			Pixel centrePixel1 = node1.getCentrePixel();
			compare = this.centrePixel.compareTo(centrePixel1);
		}
		return compare;
	}
	
	public String toString() {
		return centrePixel.toString();
	}

}
