package org.xmlcml.image.colour;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.log4j.Logger;

/** not yet working
 * 
 * @author pm286
 *
 */
public class ColorUtilities {

	private final static Logger LOG = Logger.getLogger(ColorUtilities.class);
	
    private static final int RGB_WHITE = 255 + 255*256 + 255*256*256;
	private static final int RGB_BLACK = 0;
	private BufferedImage colorFrame;
    private int width;
    private int height;
    
    private BufferedImage grayFrame = 
        new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    
	public void filter1() {
	   BufferedImageOp grayscaleConv = 
	      new ColorConvertOp(colorFrame.getColorModel().getColorSpace(), 
	                         grayFrame.getColorModel().getColorSpace(), null);
	   grayscaleConv.filter(colorFrame, grayFrame);
	}
	
	// OR
	protected void filter() {       
        WritableRaster raster = grayFrame.getRaster();

        for(int x = 0; x < raster.getWidth(); x++) {
            for(int y = 0; y < raster.getHeight(); y++){
                int argb = colorFrame.getRGB(x,y);
                int r = (argb >> 16) & 0xff;
                int g = (argb >>  8) & 0xff;
                int b = (argb      ) & 0xff;

                int l = (int) (.299 * r + .587 * g + .114 * b);
                raster.setSample(x, y, 0, l);
            }
        }
    }
	
	static final public double Y0 = 100;
	static final public double gamma = 3;
	static final public double Al = 1.4456;
	static final public double Ach_inc = 0.16;

	// =======================================
		
		private void binarizeImage(BufferedImage image, int minBlack, int maxBlack) {
			Integer height = image.getHeight();
			Integer width = image.getWidth();
			Raster raster = image.getRaster();
			int numdata = raster.getNumDataElements();
			int[] values = new int[numdata];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					LOG.trace(i+" "+j);
					values = raster.getPixel(i, j, values);
					int value = ColorUtilities.getValue(values);
					if (value >= minBlack && value <= maxBlack) {
						values = new int[]{0, 0, 0,255};
					} else {
						values = new int[]{255, 255, 255, 255};
					}
					image.setRGB(i, j, ColorUtilities.getValue(values));
				}
			}
		}

	public static int getValue(int[] pix) {
		int sum = 0;
		for (Integer i : pix) {
			sum += i;
		}
		sum /= pix.length;
		return sum;
	}
	
	//TODO this and the method below need checking regarding raster types
	public static void convertTransparentToWhite(BufferedImage image) {
		if (image != null) {
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					int rgb = image.getRGB(i, j);
					int trans = rgb & 0xff000000;
					if (trans == 0) {
						rgb = 0xffffffff;
					}
					image.setRGB(i, j, rgb);
				}
			}
		}
	}

	/** flips black pixels to white and vice versa.
	 * 
	 * @param image
	 */
	public static void flipWhiteBlack(BufferedImage image) {
		if (image != null) {
			Raster raster = image.getRaster();
			raster.getSampleModel();
			int numData = raster.getNumDataElements();
			if (numData == 1) {
				int[] pix = new int[0];
				for (int i = 0; i <image.getWidth(); i++) {
					for (int j = 0; j <image.getHeight(); j++) {
						int rgb = image.getRGB(i, j) & 0x00ffffff;
						if (rgb == 0) {
							rgb = 0xffffff;
						} else if (rgb == 0xffffff) {
							rgb = 0;
						}
						image.setRGB(i, j, rgb);
					}
				}
			} else if (numData == 3) {
				int[] pix = new int[numData];
				for (int i = 0; i <image.getWidth(); i++) {
					for (int j = 0; j <image.getHeight(); j++) {
						pix = raster.getPixel(i, j, pix);
						for (int k = 0;k <pix.length; k++) {
							int rgb = RGB_BLACK;
							if (pix[0]+pix[1]+pix[2] == 0) {
								rgb = RGB_WHITE;
							}
							image.setRGB(i, j, rgb);
						}
					}
				}
			} else {
				throw new RuntimeException("I don't understand Raster yet "+numData);
			}
		}
	}

}
