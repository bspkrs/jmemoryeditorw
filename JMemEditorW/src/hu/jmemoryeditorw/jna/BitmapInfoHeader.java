/*
 * Classname            : jna.BitmapInfoHeader
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/**
 * The record for the BITMAPINFOHEADER windows structure.
 * 
 * @author karnokd, 2008.12.16.
 * @version $Revision 1.0$
 */
public class BitmapInfoHeader extends Structure {
	/** Specifies the number of bytes required by the structure. */
	public int biSize;
	/**
	 * Specifies the width of the bitmap, in pixels. Windows 98/Me, Windows
	 * 2000/XP: If biCompression is BI_JPEG or BI_PNG, the biWidth member
	 * specifies the width of the decompressed JPEG or PNG image file,
	 * respectively.
	 */
	public int biWidth;
	/**
	 * Specifies the height of the bitmap, in pixels. If biHeight is positive,
	 * the bitmap is a bottom-up DIB and its origin is the lower-left corner. If
	 * biHeight is negative, the bitmap is a top-down DIB and its origin is the
	 * upper-left corner. If biHeight is negative, indicating a top-down DIB,
	 * biCompression must be either BI_RGB or BI_BITFIELDS. Top-down DIBs cannot
	 * be compressed.
	 * 
	 * Windows 98/Me, Windows 2000/XP: If biCompression is BI_JPEG or BI_PNG,
	 * the biHeight member specifies the height of the decompressed JPEG or PNG
	 * image file, respectively.
	 */
	public int biHeight;
	/**
	 * Specifies the number of planes for the target device. This value must be
	 * set to 1.
	 */
	public short biPlanes;
	/**
	 * Specifies the number of bits-per-pixel. The biBitCount member of the
	 * BITMAPINFOHEADER structure determines the number of bits that define each
	 * pixel and the maximum number of colors in the bitmap. This member must be
	 * one of the following values.
	 * 
	 * <a
	 * href='http://msdn.microsoft.com/en-us/library/ms532290(VS.85).aspx'>See
	 * MSDN</a>
	 */
	public short biBitCount;
	/**
	 * Specifies the type of compression for a compressed bottom-up bitmap
	 * (top-down DIBs cannot be compressed). This member can be one of the
	 * following values. <a
	 * href='http://msdn.microsoft.com/en-us/library/ms532290(VS.85).aspx'>See
	 * MSDN</a>. See BI_* constants in GDI32.
	 */
	public int biCompression;
	/**
	 * Specifies the size, in bytes, of the image. This may be set to zero for
	 * BI_RGB bitmaps. Windows 98/Me, Windows 2000/XP: If biCompression is
	 * BI_JPEG or BI_PNG, biSizeImage indicates the size of the JPEG or PNG
	 * image buffer, respectively.
	 */
	public int biSizeImage;
	/**
	 * Specifies the horizontal resolution, in pixels-per-meter, of the target
	 * device for the bitmap. An application can use this value to select a
	 * bitmap from a resource group that best matches the characteristics of the
	 * current device.
	 */
	public int biXPelsPerMeter;
	/**
	 * Specifies the vertical resolution, in pixels-per-meter, of the target
	 * device for the bitmap.
	 */
	public int biYPelsPerMeter;
	/**
	 * Specifies the number of color indexes in the color table that are
	 * actually used by the bitmap. If this value is zero, the bitmap uses the
	 * maximum number of colors corresponding to the value of the biBitCount
	 * member for the compression mode specified by biCompression. If biClrUsed
	 * is nonzero and the biBitCount member is less than 16, the biClrUsed
	 * member specifies the actual number of colors the graphics engine or
	 * device driver accesses. If biBitCount is 16 or greater, the biClrUsed
	 * member specifies the size of the color table used to optimize performance
	 * of the system color palettes. If biBitCount equals 16 or 32, the optimal
	 * color palette starts immediately following the three DWORD masks.
	 * 
	 * When the bitmap array immediately follows the BITMAPINFO structure, it is
	 * a packed bitmap. Packed bitmaps are referenced by a single pointer.
	 * Packed bitmaps require that the biClrUsed member must be either zero or
	 * the actual size of the color table.
	 */
	public int biClrUsed;
	/**
	 * Specifies the number of color indexes that are required for displaying
	 * the bitmap. If this value is zero, all colors are required.
	 */
	public int biClrImportant;

	/**
	 * Constructor. Initializes the biSize field with the structure size.
	 */
	public BitmapInfoHeader() {
		super();
		biSize = size();
	}

}
