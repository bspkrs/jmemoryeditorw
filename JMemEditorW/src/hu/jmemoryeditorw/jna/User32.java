/*
 * Classname            : jna.User32Lib
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface User32 extends StdCallLibrary {
	/** The instance. */
	User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

	/**
	 * The GetTopWindow function examines the Z order of the child windows
	 * associated with the specified parent window and retrieves a handle to the
	 * child window at the top of the Z order.
	 * 
	 * @param hWnd
	 *            Handle to the parent window whose child windows are to be
	 *            examined. If this parameter is NULL, the function returns a
	 *            handle to the window at the top of the Z order.
	 * @return If the function succeeds, the return value is a handle to the
	 *         child window at the top of the Z order. If the specified window
	 *         has no child windows, the return value is NULL. To get extended
	 *         error information, use the GetLastError function.
	 */
	int GetTopWindow(int hWnd);

	/**
	 * The IsWindowVisible function retrieves the visibility state of the
	 * specified window.
	 * 
	 * @param hWnd
	 *            Handle to the window to test.
	 * @return If the specified window, its parent window, its parent's parent
	 *         window, and so forth, have the WS_VISIBLE style, the return value
	 *         is nonzero. Otherwise, the return value is zero.
	 */
	boolean IsWindowVisible(int hWnd);

	/**
	 * The GetWindowText function copies the text of the specified window's
	 * title bar (if it has one) into a buffer. If the specified window is a
	 * control, the text of the control is copied. However, GetWindowText cannot
	 * retrieve the text of a control in another application.
	 */
	int GetWindowTextW(int hWnd, char[] lpString, int nMaxCount);

	/**
	 * The GetClassName function retrieves the name of the class to which the
	 * specified window belongs.
	 */
	int GetClassNameW(int hWnd, char[] lpString, int nMaxCount);

	/**
	 * The GetWindowLong function retrieves information about the specified
	 * window. The function also retrieves the 32-bit (long) value at the
	 * specified offset into the extra window memory.
	 * 
	 * @param hWnd
	 *            Handle to the window and, indirectly, the class to which the
	 *            window belongs.
	 * @param nIndex
	 *            Specifies the zero-based offset to the value to be retrieved.
	 *            Valid values are in the range zero through the number of bytes
	 *            of extra window memory, minus four; for example, if you
	 *            specified 12 or more bytes of extra memory, a value of 8 would
	 *            be an index to the third 32-bit integer. To retrieve any other
	 *            value, specify one of the following values of GWL_* constants.
	 * @return If the function succeeds, the return value is the requested
	 *         32-bit value.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int GetWindowLongW(int hWnd, int nIndex);

	/**
	 * Constant for GetWindowLongW.nIndex parameter: Retrieves a handle to the
	 * application instance.
	 */
	int GWL_HINSTANCE = -6;

	/**
	 * The GetClassInfoEx function retrieves information about a window class,
	 * including a handle to the small icon associated with the window class.
	 * The GetClassInfo function does not retrieve a handle to the small icon.
	 */
	boolean GetClassInfoExW(int hinst, WString lpszClass, TagWndClassExW lpwcx);

	/**
	 * The GetWindowThreadProcessId function retrieves the identifier of the
	 * thread that created the specified window and, optionally, the identifier
	 * of the process that created the window.
	 * 
	 * @return The return value is the identifier of the thread that created the
	 *         window.
	 */
	int GetWindowThreadProcessId(int hWnd, IntByReference lpdwProcessId);

	/**
	 * The GetNextWindow function retrieves a handle to the next or previous
	 * window in the Z-Order. The next window is below the specified window; the
	 * previous window is above. If the specified window is a topmost window,
	 * the function retrieves a handle to the next (or previous) topmost window.
	 * If the specified window is a top-level window, the function retrieves a
	 * handle to the next (or previous) top-level window. If the specified
	 * window is a child window, the function searches for a handle to the next
	 * (or previous) child window.
	 */
	int GetWindow(int hWnd, int wCmd);

	/** Returns a handle to the window below the given window. */
	int GW_HWNDNEXT = 2;
	/** Returns a handle to the window above the given window. */
	int GW_HWNDPREV = 3;

	/**
	 * The GetIconInfo function retrieves information about the specified icon
	 * or cursor. GetIconInfo creates bitmaps for the hbmMask and hbmColor
	 * members of ICONINFO. The calling application must manage these bitmaps
	 * and delete them when they are no longer necessary using
	 * <code>GDI32.DeleteObject()</code>.
	 * 
	 * @param hIcon
	 *            Handle to the icon or cursor. To retrieve information about a
	 *            standard icon or cursor, specify one of the IDC_* values.
	 * @param piconinfo
	 *            Pointer to an ICONINFO structure. The function fills in the
	 *            structure's members.
	 * @return If the function succeeds, the return value is nonzero and the
	 *         function fills in the members of the specified ICONINFO
	 *         structure.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	boolean GetIconInfo(int hIcon, IconInfo piconinfo);

	/** Standard arrow and small hourglass cursor. */
	int IDC_APPSTARTING = 32650;
	/** Standard arrow cursor. */
	int IDC_ARROW = 32512;
	/** Crosshair cursor. */
	int IDC_CROSS = 32515;
	/** Windows 98/Me, Windows 2000/XP: Hand cursor. */
	int IDC_HAND = 32649;
	/** Arrow and question mark cursor. */
	int IDC_HELP = 32651;
	/** I-beam cursor. */
	int IDC_IBEAM = 32513;
	/** Slashed circle cursor. */
	int IDC_NO = 32648;
	/** Four-pointed arrow cursor pointing north, south, east, and west. */
	int IDC_SIZEALL = 32646;
	/** Double-pointed arrow cursor pointing northeast and southwest. */
	int IDC_SIZENESW = 32643;
	/** Double-pointed arrow cursor pointing north and south. */
	int IDC_SIZENS = 32645;
	/** Double-pointed arrow cursor pointing northwest and southeast. */
	int IDC_SIZENWSE = 32642;
	/** Double-pointed arrow cursor pointing west and east. */
	int IDC_SIZEWE = 32644;
	/** Vertical arrow cursor. */
	int IDC_UPARROW = 32516;
	/** Hourglass cursor. */
	int IDC_WAIT = 32514;
	/** Application icon. */
	int IDI_APPLICATION = 32512;
	/** Asterisk icon. */
	int IDI_ASTERISK = 32516;
	/** Exclamation point icon. */
	int IDI_EXCLAMATION = 32515;
	/** Stop sign icon. */
	int IDI_HAND = 32513;
	/** Question-mark icon. */
	int IDI_QUESTION = 32514;
	/** Windows logo icon. Windows XP: Application icon. */
	int IDI_WINLOGO = 32517;
	/** Shield icon on Vista and later. */
	int IDI_SHIELD = 32518;
	/** Alias for IDI_EXCLAMATION. */
	int IDI_WARNING = IDI_EXCLAMATION;
	/** Alias for IDI_HELP. */
	int IDI_ERROR = IDI_HAND;
	/** Alias for IDI_ASTERISK. */
	int IDI_INFORMATION = IDI_ASTERISK;

	/**
	 * The GetDC function retrieves a handle to a device context (DC) for the
	 * client area of a specified window or for the entire screen. You can use
	 * the returned handle in subsequent GDI functions to draw in the DC. The
	 * device context is an opaque data structure, whose values are used
	 * internally by GDI.
	 * 
	 * @param hWnd
	 *            Handle to the window whose DC is to be retrieved. If this
	 *            value is NULL, GetDC retrieves the DC for the entire screen.
	 *            Windows 98/Me, Windows 2000/XP: To get the DC for a specific
	 *            display monitor, use the EnumDisplayMonitors and CreateDC
	 *            functions.
	 * 
	 * @return If the function succeeds, the return value is a handle to the DC
	 *         for the specified window's client area.
	 * 
	 *         If the function fails, the return value is NULL.
	 */
	int GetDC(int hWnd);

	/**
	 * The ReleaseDC function releases a device context (DC), freeing it for use
	 * by other applications. The effect of the ReleaseDC function depends on
	 * the type of DC. It frees only common and window DCs. It has no effect on
	 * class or private DCs.
	 * 
	 * The application must call the ReleaseDC function for each call to the
	 * GetWindowDC function and for each call to the GetDC function that
	 * retrieves a common DC.
	 * 
	 * An application cannot use the ReleaseDC function to release a DC that was
	 * created by calling the CreateDC function; instead, it must use the
	 * DeleteDC function. ReleaseDC must be called from the same thread that
	 * called GetDC.
	 * 
	 * @param hWnd
	 *            Handle to the window whose DC is to be released.
	 * @param hdc
	 *            Handle to the DC to be released.
	 * @return The return value indicates whether the DC was released. If the DC
	 *         was released, the return value is 1.
	 * 
	 *         If the DC was not released, the return value is zero.
	 */
	int ReleaseDC(int hWnd, int hdc);

	/**
	 * Destroys an icon and frees any memory the icon occupied.
	 * 
	 * @param hIcon
	 *            Handle to the icon to be destroyed. The icon must not be in
	 *            use.
	 * @return If the function succeeds, the return value is nonzero.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int DestroyIcon(int hIcon);

	/**
	 * Sends the specified message to a window or windows. The SendMessage
	 * function calls the window procedure for the specified window and does not
	 * return until the window procedure has processed the message.
	 * 
	 * @param hWnd
	 *            Handle to the window whose window procedure will receive the
	 *            message. If this parameter is HWND_BROADCAST, the message is
	 *            sent to all top-level windows in the system, including
	 *            disabled or invisible unowned windows, overlapped windows, and
	 *            pop-up windows; but the message is not sent to child windows.
	 * 
	 *            Microsoft Windows Vista and later. Message sending is subject
	 *            to User Interface Privilege Isolation (UIPI). The thread of a
	 *            process can send messages only to message queues of threads in
	 *            processes of lesser or equal integrity level.
	 * 
	 * 
	 * @param Msg
	 *            Specifies the message to be sent.
	 * @param wParam
	 *            Specifies additional message-specific information.
	 * @param lParam
	 *            Specifies additional message-specific information.
	 * @return The return value specifies the result of the message processing;
	 *         it depends on the message sent.
	 */
	int SendMessageW(int hWnd, int Msg, int wParam, int lParam);

	/**
	 * The WM_GETICON message is sent to a window to retrieve a handle to the
	 * large or small icon associated with a window. The system displays the
	 * large icon in the ALT+TAB dialog, and the small icon in the window
	 * caption.
	 * <p>
	 * Parameters
	 * <p>
	 * wParam Specifies the type of icon being retrieved. This parameter can be
	 * one of the following values. ICON_BIG Retrieve the large icon for the
	 * window. ICON_SMALL Retrieve the small icon for the window. ICON_SMALL2
	 * Windows XP: Retrieves the small icon provided by the application. If the
	 * application does not provide one, the system uses the system-generated
	 * icon for that window. lParam This parameter is not used.
	 * <p>
	 * Return Value
	 * <p>
	 * The return value is a handle to the large or small icon, depending on the
	 * value of wParam. When an application receives this message, it can return
	 * a handle to a large or small icon, or pass the message to the
	 * DefWindowProc function.
	 */
	int WM_GETICON = 0x7F;
	/** Retrieve the large icon for the window. */
	int ICON_BIG = 1;
	/** Retrieve the small icon for the window. */
	int ICON_SMALL = 0;
	/** Windows XP: Retrieves the small icon provided by the application. If the application does not provide one, the system uses the system-generated icon for that window. */
	int ICON_SMALL2 = 2;
}