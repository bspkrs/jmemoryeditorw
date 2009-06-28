package hu.jmemoryeditorw.jna;


import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/** AdvAp32.dll library. */
public interface AdvApi32 extends StdCallLibrary {
	/** The library instance. */
	AdvApi32 INSTANCE = (AdvApi32)Native.loadLibrary("advapi32", AdvApi32.class);
	/** The LookupPrivilegeValue function retrieves the locally unique identifier (LUID) used on a specified system to locally represent the specified privilege name. */
	boolean LookupPrivilegeValueW(WString lpSystemName, WString lpName, LUID lpLuid);
	/** Required to debug and adjust the memory of a process owned by another account. */
	String SE_DEBUG_NAME = "SeDebugPrivilege";
	int SE_PRIVILEGE_ENABLED = 2;
	boolean AdjustTokenPrivileges(int TokenHandle, boolean DisableAllPrivileges, TokenPrivileges NewState,
			int BufferLength, TokenPrivileges PreviousState, IntByReference ReturnLength);
	/** The OpenProcessToken function opens the access token associated with a process. */
	boolean OpenProcessToken(int ProcessHandle, int DesiredAccess, IntByReference TokenHandle);
	int TOKEN_ASSIGN_PRIMARY = 0x0001;
	int TOKEN_DUPLICATE = 0x0002;
	int TOKEN_IMPERSONATE = 0x0004;
	int TOKEN_QUERY = 0x0008;
	int TOKEN_QUERY_SOURCE = 0x0010;
	int TOKEN_ADJUST_PRIVILEGES = 0x0020;
	int TOKEN_ADJUST_GROUPS =    0x0040;
	int TOKEN_ADJUST_DEFAULT = 0x0080;
	int TOKEN_ADJUST_SESSIONID = 0x0100;

	int TOKEN_ALL_ACCESS = Kernel32.STANDARD_RIGHTS_REQUIRED  |
	                          TOKEN_ASSIGN_PRIMARY      |
	                          TOKEN_DUPLICATE           |
	                          TOKEN_IMPERSONATE         |
	                          TOKEN_QUERY               |
	                          TOKEN_QUERY_SOURCE        |
	                          TOKEN_ADJUST_PRIVILEGES   |
	                          TOKEN_ADJUST_GROUPS       |
	                          TOKEN_ADJUST_DEFAULT;
	int TOKEN_READ = Kernel32.STANDARD_RIGHTS_READ | TOKEN_QUERY;
	int TOKEN_WRITE = Kernel32.STANDARD_RIGHTS_WRITE | TOKEN_ADJUST_PRIVILEGES |
            TOKEN_ADJUST_GROUPS       |
            TOKEN_ADJUST_DEFAULT;

	int TOKEN_EXECUTE = Kernel32.STANDARD_RIGHTS_EXECUTE;
}