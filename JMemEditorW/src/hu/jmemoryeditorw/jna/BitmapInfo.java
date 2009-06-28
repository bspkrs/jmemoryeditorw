/*
 * Classname            : hu.jmemoryeditorw.jna.BitmapInfo
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/**
 * Record for BITMAPINFO windows structure.
 * 
 * @author karnokd, 2008.12.16.
 * @version $Revision 1.0$
 */
public class BitmapInfo extends Structure {
	/**
	 * Specifies a BITMAPINFOHEADER structure that contains information about
	 * the dimensions of color format.
	 */
	public BitmapInfoHeader bmiHeader = new BitmapInfoHeader();
	/**
	 * The bmiColors member contains one of the following: An array of RGBQUAD.
	 * The elements of the array that make up the color table. An array of
	 * 16-bit unsigned integers that specifies indexes into the currently
	 * realized logical palette. This use of bmiColors is allowed for functions
	 * that use DIBs.
	 */
	public RGBQuad[] bmiColors;
	/**
	 * Constructor.
	 * @param nColors the number of entries in the bmiColors array.
	 */
	public BitmapInfo(int nColors) {
		bmiColors = new RGBQuad[nColors];
		for (int i = 0; i < nColors; i++) {
			bmiColors[i] = new RGBQuad();
		}
	}
}
