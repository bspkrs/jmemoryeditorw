/*
 * Classname            : jna.Psapi
 * Version information  : 1.0
 * Date                 : 2008.12.17.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Process related API functions of windows.
 * 
 * @author karnokd, 2008.12.17.
 * @version $Revision 1.0$
 */
public interface Psapi extends StdCallLibrary {
	/** The library instance. */
	Psapi INSTANCE = (Psapi) Native.loadLibrary("psapi", Psapi.class);

	/**
	 * Retrieves the name of the executable file for the specified process.
	 * 
	 * @param hProcess
	 *            A handle to the process. The handle must have the
	 *            PROCESS_QUERY_INFORMATION or PROCESS_QUERY_LIMITED_INFORMATION
	 *            access right. For more information, see Process Security and
	 *            Access Rights.
	 * 
	 *            Windows Server 2003 and Windows XP: The handle must have the
	 *            PROCESS_QUERY_INFORMATION access right.
	 * @param lpImageFileName
	 *            A pointer to a buffer that receives the full path to the
	 *            executable file.
	 * 
	 * 
	 * @param nSize
	 *            The size of the lpImageFileName buffer, in characters.
	 * @return If the function succeeds, the return value specifies the length
	 *         of the string copied to the buffer.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int GetProcessImageFileNameW(int hProcess, char[] lpImageFileName, int nSize);

	/**
	 * Retrieves the base name of the specified module.
	 * 
	 * @param hProcess
	 *            handle to the process that contains the module.
	 * 
	 *            The handle must have the PROCESS_QUERY_INFORMATION and
	 *            PROCESS_VM_READ access rights. For more information, see
	 *            Process Security and Access Rights.
	 * 
	 * 
	 * @param hModule
	 *            A handle to the module. If this parameter is NULL, this
	 *            function returns the name of the file used to create the
	 *            calling process.
	 * @param lpBaseName
	 *            A pointer to the buffer that receives the base name of the
	 *            module. If the base name is longer than maximum number of
	 *            characters specified by the nSize parameter, the base name is
	 *            truncated.
	 * @param nSize
	 *            The size of the lpBaseName buffer, in characters.
	 * @return If the function succeeds, the return value specifies the length
	 *         of the string copied to the buffer, in characters.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int GetModuleBaseNameW(int hProcess, int hModule, char[] lpBaseName,
			int nSize);

	/**
	 * Retrieves the fully-qualified path for the file containing the specified
	 * module.
	 * 
	 * @param hProcess
	 *            A handle to the process that contains the module.
	 * 
	 *            The handle must have the PROCESS_QUERY_INFORMATION and
	 *            PROCESS_VM_READ access rights. For more information, see
	 *            Process Security and Access Rights.
	 * 
	 *            The GetModuleFileNameEx function does not retrieve the path
	 *            for modules that were loaded using the
	 *            LOAD_LIBRARY_AS_DATAFILE flag. For more information, see
	 *            LoadLibraryEx.
	 * 
	 * 
	 * @param hModule
	 *            A handle to the module. If this parameter is NULL,
	 *            GetModuleFileNameEx returns the path of the executable file of
	 *            the process specified in hProcess.
	 * 
	 * 
	 * @param lpFilename
	 *            A pointer to a buffer that receives the fully-qualified path
	 *            to the module. If the size of the file name is larger than the
	 *            value of the nSize parameter, the function succeeds but the
	 *            file name is truncated and null terminated.
	 * @param nSize
	 *            The size of the lpFilename buffer, in characters.
	 * @return If the function succeeds, the return value specifies the length
	 *         of the string copied to the buffer.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int GetModuleFileNameExW(int hProcess, int hModule, char[] lpFilename,
			int nSize);
}
