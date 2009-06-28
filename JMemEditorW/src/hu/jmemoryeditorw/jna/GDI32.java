/*
 * Classname            : jna.GDI32
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Library interface for GDI32.DLL for Windows.
 * 
 * @author karnokd, 2008.12.16.
 * @version $Revision 1.0$
 */
public interface GDI32 extends StdCallLibrary {
	/** The library instance. */
	GDI32 INSTANCE = (GDI32) Native.loadLibrary("gdi32", GDI32.class);

	/**
	 * The DeleteObject function deletes a logical pen, brush, font, bitmap,
	 * region, or palette, freeing all system resources associated with the
	 * object. After the object is deleted, the specified handle is no longer
	 * valid.
	 * 
	 * @param hObject
	 *            Handle to a logical pen, brush, font, bitmap, region, or
	 *            palette.
	 * @return If the function succeeds, the return value is nonzero. If the
	 *         specified handle is not valid or is currently selected into a DC,
	 *         the return value is zero.
	 */
	boolean DeleteObject(int hObject);

	/** An uncompressed format. */
	int BI_RGB = 0;
	/**
	 * A run-length encoded (RLE) format for bitmaps with 8 bpp. The compression
	 * format is a 2-byte format consisting of a count byte followed by a byte
	 * containing a color index. For more information, see Bitmap Compression.
	 */
	int BI_RLE8 = 0;
	/**
	 * An RLE format for bitmaps with 4 bpp. The compression format is a 2-byte
	 * format consisting of a count byte followed by two word-length color
	 * indexes. For more information, see Bitmap Compression.
	 */
	int BI_RLE4 = 0;
	/**
	 * Specifies that the bitmap is not compressed and that the color table
	 * consists of three DWORD color masks that specify the red, green, and blue
	 * components, respectively, of each pixel. This is valid when used with 16-
	 * and 32-bpp bitmaps.
	 */
	int BI_BITFIELDS = 0;
	/**
	 * Windows 98/Me, Windows 2000/XP: Indicates that the image is a JPEG image.
	 */
	int BI_JPEG = 0;
	/** Windows 98/Me, Windows 2000/XP: Indicates that the image is a PNG image. */
	int BI_PNG = 0;

	/**
	 * The GetDIBits function retrieves the bits of the specified compatible
	 * bitmap and copies them into a buffer as a DIB using the specified format.
	 * 
	 * @param hdc
	 *            Handle to the device context.
	 * @param hbmp
	 *            Handle to the bitmap. This must be a compatible bitmap (DDB).
	 * @param uStartScan
	 *            Specifies the first scan line to retrieve.
	 * @param uScanLines
	 *            Specifies the number of scan lines to retrieve.
	 * @param lpvBits
	 *            Pointer to a buffer to receive the bitmap data. If this
	 *            parameter is NULL, the function passes the dimensions and
	 *            format of the bitmap to the BITMAPINFO structure pointed to by
	 *            the lpbi parameter.
	 * @param lpbi
	 *            Pointer to a BITMAPINFO structure that specifies the desired
	 *            format for the DIB data.
	 * @param uUsage
	 *            Specifies the format of the bmiColors member of the BITMAPINFO
	 *            structure. It must be one of the DIB_ constant values.
	 * @return If the lpvBits parameter is non-NULL and the function succeeds,
	 *         the return value is the number of scan lines copied from the
	 *         bitmap.
	 * 
	 *         Windows 95/98/Me: If the lpvBits parameter is NULL and GetDIBits
	 *         successfully fills the BITMAPINFO structure, the return value is
	 *         the total number of scan lines in the bitmap.
	 * 
	 *         Windows NT/2000/XP: If the lpvBits parameter is NULL and
	 *         GetDIBits successfully fills the BITMAPINFO structure, the return
	 *         value is non-zero.
	 * 
	 *         If the function fails, the return value is zero.
	 */
	int GetDIBits(int hdc, int hbmp, int uStartScan, int uScanLines,
			Pointer lpvBits, BitmapInfo lpbi, int uUsage);

	/**
	 * The color table should consist of an array of 16-bit indexes into the
	 * current logical palette.
	 */
	int DIB_PAL_COLORS = 0;
	/** The color table should consist of literal red, green, blue (RGB) values. */
	int DIB_RGB_COLORS = 0;
}
