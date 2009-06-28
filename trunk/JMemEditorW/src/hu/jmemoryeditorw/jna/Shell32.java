/*
 * Classname            : jna.Shell32
 * Version information  : 1.0
 * Date                 : 2008.12.17.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Library for Shell32.dll windows library.
 * 
 * @author karnokd, 2008.12.17.
 * @version $Revision 1.0$
 */
public interface Shell32 extends StdCallLibrary {
	/** The instance. */
	Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class);

	/**
	 * The ExtractIconEx function creates an array of handles to large or small
	 * icons extracted from the specified executable file, DLL, or icon file.
	 * You must destroy all icons extracted by ExtractIconEx by calling the
	 * User32.DestroyIcon function.
	 * 
	 * @param lpszFile
	 *            Pointer to a null-terminated string specifying the name of an
	 *            executable file, DLL, or icon file from which icons will be
	 *            extracted.
	 * @param nIconIndex
	 *            Specifies the zero-based index of the first icon to extract.
	 *            For example, if this value is zero, the function extracts the
	 *            first icon in the specified file. If this value is –1 and
	 *            phiconLarge and phiconSmall are both NULL, the function
	 *            returns the total number of icons in the specified file. If
	 *            the file is an executable file or DLL, the return value is the
	 *            number of RT_GROUP_ICON resources. If the file is an .ico
	 *            file, the return value is 1.
	 * 
	 *            Windows 95/98/Me, Windows NT 4.0 and later: If this value is a
	 *            negative number and either phiconLarge or phiconSmall is not
	 *            NULL, the function begins by extracting the icon whose
	 *            resource identifier is equal to the absolute value of
	 *            nIconIndex. For example, use -3 to extract the icon whose
	 *            resource identifier is 3.
	 * 
	 * @param phIconLarge
	 *            Pointer to an array of icon handles that receives handles to
	 *            the large icons extracted from the file. If this parameter is
	 *            NULL, no large icons are extracted from the file.
	 * @param phIconSmall
	 *            Pointer to an array of icon handles that receives handles to
	 *            the small icons extracted from the file. If this parameter is
	 *            NULL, no small icons are extracted from the file.
	 * @param nIcons
	 *            Specifies the number of icons to extract from the file.
	 * @return If the nIconIndex parameter is -1, the phiconLarge parameter is
	 *         NULL, and the phiconSmall parameter is NULL, then the return
	 *         value is the number of icons contained in the specified file.
	 *         Otherwise, the return value is the number of icons successfully
	 *         extracted from the file.
	 */
	int ExtractIconExW(WString lpszFile, int nIconIndex, int[] phIconLarge,
			int[] phIconSmall, int nIcons);
}
