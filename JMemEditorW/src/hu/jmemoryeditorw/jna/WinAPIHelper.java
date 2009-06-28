package hu.jmemoryeditorw.jna;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

/**
 * Complex methods to accomplish tasks using the Windows API.
 * @author karnokd, 2008.12.15.
 * @version $Revision 1.0$
 */
public final class WinAPIHelper {
	/** Private constructor. */
	private WinAPIHelper() {
		// utility class
	}
	public static String getLastErrorStr(int errorCode) {
		Memory mem = new Memory(Memory.SIZE);
		Kernel32.INSTANCE.FormatMessageW(Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
				| Kernel32.FORMAT_MESSAGE_ALLOCATE_BUFFER, null, errorCode,
				Kernel32.LANG_NEUTRAL, mem, 0, null);
		String result = mem.getPointer(0).getString(0, true);
		Kernel32.INSTANCE.LocalFree(mem.getPointer(0));
		return result;
	}

	public static class ProcessData {
		public String WindowText;
		public String ClassName;
		public TagWndClassExW ClassWindowEx = new TagWndClassExW();
		public int Error;
		public String ErrorStr;
		public int Handle;
		public int Instance;
		public int ProcessID;
		/** The associated 16x16 application icon. */
		public ImageIcon icon16;
		/** The path to the executable file. */
		public String exeFile;
	}

	public static List<ProcessData> getProcesses() {
		List<ProcessData> result = new ArrayList<ProcessData>();
		User32 u32 = User32.INSTANCE;
		Kernel32 k32 = Kernel32.INSTANCE;
		int nxt = u32.GetTopWindow(0);
		do {
			if (nxt != 0 && u32.IsWindowVisible(nxt)) {
				ProcessData pd = new ProcessData();
				pd.Handle = nxt;
				char[] strOut = new char[512];
				u32.GetWindowTextW(nxt, strOut, strOut.length);
				pd.WindowText = Native.toString(strOut);
				if (pd.WindowText.length() > 0) {
					u32.GetClassNameW(nxt, strOut, strOut.length);
					pd.ClassName = Native.toString(strOut);
					pd.Instance = u32.GetWindowLongW(nxt, User32.GWL_HINSTANCE);
					if (!u32.GetClassInfoExW(pd.Instance, new WString(
							pd.ClassName), pd.ClassWindowEx)) {
						pd.Error = k32.GetLastError();
						pd.ErrorStr = getLastErrorStr(pd.Error);
					}
					IntByReference iref = new IntByReference();
					u32.GetWindowThreadProcessId(nxt, iref);
					pd.ProcessID = iref.getValue();
					int hIconSm = u32.SendMessageW(nxt, User32.WM_GETICON, User32.ICON_SMALL2, 0);
					if (hIconSm == 0) {
 						u32.SendMessageW(nxt, User32.WM_GETICON, User32.ICON_SMALL, 0);
					}
					boolean destroyIcon = false;
					if (hIconSm == 0) {
						Psapi ps = Psapi.INSTANCE;
						Arrays.fill(strOut, '\0');
						int hProcess = k32.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION, false, pd.ProcessID);
						if (hProcess != 0) {
							ps.GetModuleFileNameExW(hProcess, 0, strOut, strOut.length);
							k32.CloseHandle(hProcess);
							pd.exeFile = Native.toString(strOut);
							int[] ints = new int[1];
							if (Shell32.INSTANCE.ExtractIconExW(new WString(pd.exeFile), 0, null, ints, 1) == 1) {
								hIconSm = ints[0];
								destroyIcon = true;
							}
						} else {
							printLastError();
						}
					}
					int size = 16;
					int[] iconBits = getIconBits(hIconSm, size);
					if (iconBits != null) {
						BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
						bi.setRGB(0, 0, size, size, iconBits, 0, size);
						pd.icon16 = new ImageIcon(bi);
					}
					if (destroyIcon) {
						u32.DestroyIcon(hIconSm);
					}
					result.add(pd);
				}
			}
			nxt = u32.GetWindow(nxt, User32.GW_HWNDNEXT);
		} while (nxt != 0);
		return result;
	}

	/** Enables or disables a privilege by its name. */
	public static boolean SetPrivilege(int hToken, String lpszPrivilege,
			boolean bEnablePrivilege) {
		LUID luid = new LUID();
		AdvApi32 aa32 = AdvApi32.INSTANCE;
		if (!aa32.LookupPrivilegeValueW(null, new WString(lpszPrivilege), luid)) {
			printLastError();
			return false;
		}
		TokenPrivileges tp = new TokenPrivileges(1);
		tp.Privileges[0].Luid = luid;
		tp.Privileges[0].Attributes = bEnablePrivilege ? AdvApi32.SE_PRIVILEGE_ENABLED
				: 0;
		if (!aa32.AdjustTokenPrivileges(hToken, false, tp, tp.size(), null,
				null)) {
			printLastError();
			return false;
		}
		if (Kernel32.INSTANCE.GetLastError() == Kernel32.ERROR_NOT_ALL_ASSIGNED) {
			printLastError();
			return false;
		}
		return true;
	}

	/** Enables the current process the debug privilege. */
	public static boolean enableProcessDebug() {
		AdvApi32 aa32 = AdvApi32.INSTANCE;
		Kernel32 k32 = Kernel32.INSTANCE;
		IntByReference hToken = new IntByReference();
		int hProcess = k32.GetCurrentProcess();
		boolean result = false;
		if (aa32.OpenProcessToken(hProcess, AdvApi32.TOKEN_ALL_ACCESS, hToken)) {
			result = SetPrivilege(hToken.getValue(), AdvApi32.SE_DEBUG_NAME,
					true);
			k32.CloseHandle(hToken.getValue());
		} else {
			printLastError();
		}
		return result;
	}

	public static boolean readProcess(ProcessData pd, int base, int count,
			byte[] buffer) {
		boolean result = false;
		Kernel32 k32 = Kernel32.INSTANCE;
		enableProcessDebug();
		int pHandle = k32.OpenProcess(Kernel32.PROCESS_VM_READ, false,
				pd.ProcessID);
		printLastError();
		if (pHandle != 0) {
			result = k32.ReadProcessMemory(pHandle, base, buffer, count, null);
		}
		printLastError();
		k32.CloseHandle(pHandle);
		return result;
	}

	public static boolean writeProcess(ProcessData pd, int base, int count,
			byte[] buffer) {
		boolean result = false;
		Kernel32 k32 = Kernel32.INSTANCE;
		enableProcessDebug();
		int pHandle = k32.OpenProcess(Kernel32.PROCESS_VM_WRITE | Kernel32.PROCESS_VM_OPERATION, false,
				pd.ProcessID);
		if (pHandle != 0) {
			result = k32.WriteProcessMemory(pHandle, base, buffer, count, null);
		}
		printLastError();
		k32.CloseHandle(pHandle);
		return result;
	}

	public static void printLastError() {
		int err = Kernel32.INSTANCE.GetLastError();
		if (err != 0) {
			System.err.printf("%d - %s%n", err, getLastErrorStr(err));
		}
	}

	public static List<MemoryBasicInformation> queryProcessMemory(ProcessData pd) {
		Kernel32 k32 = Kernel32.INSTANCE;
		List<MemoryBasicInformation> result = new ArrayList<MemoryBasicInformation>();
		int base = 0;
		int bread = 0;
		enableProcessDebug();
		int pHandle = k32.OpenProcess(Kernel32.PROCESS_ALL_ACCESS, false,
				pd.ProcessID);
		if (pHandle == 0) {
			printLastError();
			return result;
		}
		do {
			MemoryBasicInformation meminfo = new MemoryBasicInformation();
			int size = meminfo.size();
			bread = k32.VirtualQueryEx(pHandle, base, meminfo, size);
			//printLastError();
			if (bread > 0) {
				if (meminfo.State == Kernel32.MEM_COMMIT
						&& (((meminfo.Protect & Kernel32.PAGE_READWRITE) != 0)
						|| (meminfo.Protect & Kernel32.PAGE_EXECUTE_READWRITE) == 0)) {
					result.add(meminfo);
				}
			}
			base += meminfo.RegionSize;
		} while (bread != 0 && base < 0x7FFF0000);
		k32.CloseHandle(pHandle);
		return result;
	}

	public static void main(String[] args) {
		List<ProcessData> data = getProcesses();
		for (ProcessData pd : data) {
			System.out.printf("%d - %s%n", pd.ProcessID, pd.WindowText);
			for (MemoryBasicInformation meminfo : queryProcessMemory(pd)) {
				System.out.printf(", %d", meminfo.RegionSize);
			}
			System.out.printf("%n");
		}
	}

	/**
	 * Returns the icon bits of the given HICON windows object.
	 * 
	 * @param hIcon
	 *            the HICON identifier of the icon
	 * @param size
	 *            the icon width and height
	 * @return the data ints or null if the icon is inaccessible
	 */
	public static int[] getIconBits(int hIcon, int size) {
		User32 u32 = User32.INSTANCE;
		GDI32 g32 = GDI32.INSTANCE;
		IconInfo iconinfo = new IconInfo();
		// get icon information
		if (!u32.GetIconInfo(hIcon, iconinfo)) {
			return null;
		}
		int dc = u32.GetDC(0);
		if (dc == 0) {
			g32.DeleteObject(iconinfo.hbmColor);
			g32.DeleteObject(iconinfo.hbmMask);
			return null;
		}
		int nBits = size * size * 4;
		BitmapInfo bmi = new BitmapInfo(1);
		bmi.bmiHeader.biWidth = size;
		bmi.bmiHeader.biHeight = -size;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = GDI32.BI_RGB;
		Memory colorBitsMem = new Memory(nBits);
		// Extract the color bitmap
		g32.GetDIBits(dc, iconinfo.hbmColor, 0, size, colorBitsMem, bmi,
				GDI32.DIB_RGB_COLORS);
		int[] colorBits = colorBitsMem.getIntArray(0, size * size);
		Memory maskBitsMem = new Memory(nBits);
		// Extract the mask bitmap
		g32.GetDIBits(dc, iconinfo.hbmMask, 0, size, maskBitsMem, bmi,
				GDI32.DIB_RGB_COLORS);
		int[] maskBits = maskBitsMem.getIntArray(0, size * size);
		// Copy the mask alphas into the color bits
		for (int i = 0; i < colorBits.length; i++) {
			colorBits[i] = colorBits[i] | (maskBits[i] != 0 ? 0 : 0xFF000000);
		}
		// Release DC
		u32.ReleaseDC(0, dc);

		// Release bitmap handle in icon info
		g32.DeleteObject(iconinfo.hbmColor); // add
		g32.DeleteObject(iconinfo.hbmMask); // add

		return colorBits;
	}
}
