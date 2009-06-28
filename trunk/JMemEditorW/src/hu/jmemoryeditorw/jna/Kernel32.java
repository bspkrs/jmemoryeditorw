/*
 * Classname            : jna.Kernel32
 * Version information  : 1.0
 * Date                 : 2008.12.16.
 * Copyright notice     : Karnok David
 */

package hu.jmemoryeditorw.jna;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {
	/**
	 * Retrieves the calling thread's last-error code value. The last-error code
	 * is maintained on a per-thread basis. Multiple threads do not overwrite
	 * each other's last-error code.
	 * 
	 * @return The return value is the calling thread's last-error code.
	 */
	int GetLastError();

	/**
	 * Formats a message string. The function requires a message definition as
	 * input. The message definition can come from a buffer passed into the
	 * function. It can come from a message table resource in an already-loaded
	 * module. Or the caller can ask the function to search the system's message
	 * table resource(s) for the message definition. The function finds the
	 * message definition in a message table resource based on a message
	 * identifier and a language identifier. The function copies the formatted
	 * message text to an output buffer, processing any embedded insert
	 * sequences if requested. For more information, see <a
	 * href='http://msdn.microsoft.com/en-us/library/ms679351.aspx'>MSDN</a>.
	 * 
	 * @param dwFlags
	 *            The formatting options, and how to interpret the lpSource
	 *            parameter. The low-order byte of dwFlags specifies how the
	 *            function handles line breaks in the output buffer. The
	 *            low-order byte can also specify the maximum width of a
	 *            formatted output line. See the FORMAT_MESSAGE_* constants.
	 * @param lpSource
	 *            [optional] The location of the message definition. The type of
	 *            this parameter depends upon the settings in the dwFlags
	 *            parameter.
	 * @param dwMessageId
	 *            The message identifier for the requested message. This
	 *            parameter is ignored if dwFlags includes
	 *            FORMAT_MESSAGE_FROM_STRING.
	 * 
	 * 
	 * @param dwLanguageId
	 *            The language identifier for the requested message. This
	 *            parameter is ignored if dwFlags includes
	 *            FORMAT_MESSAGE_FROM_STRING.
	 * @param lpBuffer
	 *            A pointer to a buffer that receives the null-terminated string
	 *            that specifies the formatted message. If dwFlags includes
	 *            FORMAT_MESSAGE_ALLOCATE_BUFFER, the function allocates a
	 *            buffer using the LocalAlloc function, and places the pointer
	 *            to the buffer at the address specified in lpBuffer.
	 * 
	 *            This buffer cannot be larger than 64K bytes.
	 * 
	 * @param nSize
	 *            If the FORMAT_MESSAGE_ALLOCATE_BUFFER flag is not set, this
	 *            parameter specifies the size of the output buffer, in TCHARs.
	 *            If FORMAT_MESSAGE_ALLOCATE_BUFFER is set, this parameter
	 *            specifies the minimum number of TCHARs to allocate for an
	 *            output buffer.
	 * 
	 *            The output buffer cannot be larger than 64K bytes.
	 * 
	 * 
	 * @param args
	 *            An array of values that are used as insert values in the
	 *            formatted message. A %1 in the format string indicates the
	 *            first value in the Arguments array; a %2 indicates the second
	 *            argument; and so on.
	 * @return If the function succeeds, the return value is the number of
	 *         TCHARs stored in the output buffer, excluding the terminating
	 *         null character.
	 * 
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 */
	int FormatMessageW(int dwFlags, Pointer lpSource, int dwMessageId,
			int dwLanguageId, Memory lpBuffer, int nSize, Pointer args);

	/**
	 * Frees the specified local memory object and invalidates its handle.
	 * 
	 * @param hMem
	 *            A handle to the local memory object.
	 * @return If the function succeeds, the return value is NULL. If the
	 *         function fails, the return value is equal to a handle to the
	 *         local memory object. To get extended error information, call
	 *         GetLastError.
	 */
	Pointer LocalFree(Pointer hMem);

	/**
	 * Opens an existing local process object. When you are finished with the
	 * handle, be sure to close it using the CloseHandle function.
	 * 
	 * @param dwDesiredAccess
	 *            The access to the process object. This access right is checked
	 *            against the security descriptor for the process. This
	 *            parameter can be one or more of the process access rights.
	 * 
	 *            If the caller has enabled the SeDebugPrivilege privilege, the
	 *            requested access is granted regardless of the contents of the
	 *            security descriptor. See the PROCESS_* constants.
	 * @param bInheritHandle
	 *            If this value is TRUE, processes created by this process will
	 *            inherit the handle. Otherwise, the processes do not inherit
	 *            this handle.
	 * @param dwProcessId
	 *            The identifier of the local process to be opened.
	 * @return the function succeeds, the return value is an open handle to the
	 *         specified process.
	 * 
	 *         If the function fails, the return value is NULL. To get extended
	 *         error information, call GetLastError.
	 */
	int OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);

	/**
	 * Reads data from an area of memory in a specified process. The entire area
	 * to be read must be accessible or the operation fails.
	 * 
	 * @param hProcess
	 *            A handle to the process with memory that is being read. The
	 *            handle must have PROCESS_VM_READ access to the process.
	 * @param lpBaseAddress
	 *            A pointer to the base address in the specified process from
	 *            which to read. Before any data transfer occurs, the system
	 *            verifies that all data in the base address and memory of the
	 *            specified size is accessible for read access, and if it is not
	 *            accessible the function fails.
	 * @param lpBuffer
	 *            A pointer to a buffer that receives the contents from the
	 *            address space of the specified process.
	 * @param nSize
	 *            The number of bytes to be read from the specified process.
	 * @param lpNumberOfBytesRead
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified buffer. If lpNumberOfBytesRead
	 *            is NULL, the parameter is ignored.
	 * @return If the function succeeds, the return value is nonzero. If the
	 *         function fails, the return value is 0 (zero). To get extended
	 *         error information, call GetLastError. The function fails if the
	 *         requested read operation crosses into an area of the process that
	 *         is inaccessible.
	 */
	boolean ReadProcessMemory(int hProcess, int lpBaseAddress, byte[] lpBuffer,
			int nSize, IntByReference lpNumberOfBytesRead);

	/**
	 * Writes data to an area of memory in a specified process. The entire area
	 * to be written to must be accessible or the operation fails.
	 * 
	 * @param hProcess
	 *            A handle to the process memory to be modified. The handle must
	 *            have PROCESS_VM_WRITE and PROCESS_VM_OPERATION access to the
	 *            process.
	 * @param lpBaseAddress
	 *            A pointer to the base address in the specified process to
	 *            which data is written. Before data transfer occurs, the system
	 *            verifies that all data in the base address and memory of the
	 *            specified size is accessible for write access, and if it is
	 *            not accessible, the function fails.
	 * @param lpBuffer
	 *            A pointer to the buffer that contains data to be written in
	 *            the address space of the specified process.
	 * @param nSize
	 *            The number of bytes to be written to the specified process.
	 * @param lpNumberOfBytesRead
	 *            A pointer to a variable that receives the number of bytes
	 *            transferred into the specified process. This parameter is
	 *            optional. If lpNumberOfBytesWritten is NULL, the parameter is
	 *            ignored.
	 * @return If the function succeeds, the return value is nonzero. If the
	 *         function fails, the return value is 0 (zero). To get extended
	 *         error information, call GetLastError. The function fails if the
	 *         requested write operation crosses into an area of the process
	 *         that is inaccessible.
	 */
	boolean WriteProcessMemory(int hProcess, int lpBaseAddress,
			byte[] lpBuffer, int nSize, IntByReference lpNumberOfBytesRead);

	/**
	 * Closes an open object handle.
	 * @param hObject
	 *            A valid handle to an open object.
	 * @return If the function succeeds, the return value is nonzero.
	 *         If the function fails, the return value is zero. To get extended
	 *         error information, call GetLastError.
	 *         If the application is running under a debugger, the function will
	 *         throw an exception if it receives either a handle value that is
	 *         not valid or a pseudo-handle value. This can happen if you close
	 *         a handle twice, or if you call CloseHandle on a handle returned
	 *         by the FindFirstFile function instead of calling the FindClose
	 *         function.
	 */
	boolean CloseHandle(int hObject);
	/** Contains all standard rights. */
	int STANDARD_RIGHTS_REQUIRED = 0xF0000;
	/** Required to wait for the process to terminate using the wait functions. */
	int SYNCHRONIZE = 0x100000;
	/** All possible access rights for a process object. */
	int PROCESS_ALL_ACCESS = STANDARD_RIGHTS_REQUIRED | SYNCHRONIZE | 0xFFFF;
	/**
	 * The function allocates a buffer large enough to hold the formatted
	 * message, and places a pointer to the allocated buffer at the address
	 * specified by lpBuffer.
	 */
	int FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x100;
	/**
	 * The function should search the system message-table resource(s) for the
	 * requested message.
	 */
	int FORMAT_MESSAGE_FROM_SYSTEM = 0x1000;
	/** Unspecified custom locale language. */
	int LANG_NEUTRAL = 0x1000;
	/** The unsynchronized instance of the library. */
	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32",
			Kernel32.class);
	/** The synchronized instance of the library. */
	Kernel32 SINSTANCE = (Kernel32) Native.synchronizedLibrary(INSTANCE);
	/**
	 * Indicates committed pages for which physical storage has been allocated,
	 * either in memory or in the paging file on disk. Constant for
	 * MEMORY_BASIC_INFORMATION.State field.
	 */
	int MEM_COMMIT = 0x1000;
	/**
	 * Indicates free pages not accessible to the calling process and available
	 * to be allocated. For free pages, the information in the AllocationBase,
	 * AllocationProtect, Protect, and Type members is undefined. Constant for
	 * MEMORY_BASIC_INFORMATION.State field.
	 */
	int MEM_FREE = 0x10000;
	/**
	 * Indicates reserved pages where a range of the process's virtual address
	 * space is reserved without any physical storage being allocated. For
	 * reserved pages, the information in the Protect member is undefined.
	 * Constant for MEMORY_BASIC_INFORMATION.State field.
	 */
	int MEM_RESERVE = 0x2000;
	/**
	 * Indicates that the memory pages within the region are mapped into the
	 * view of an image section. Constant for MEMORY_BASIC_INFORMATION.Type
	 * field.
	 */
	int MEM_IMAGE = 0x1000000;
	/**
	 * Indicates that the memory pages within the region are mapped into the
	 * view of a section. Constant for MEMORY_BASIC_INFORMATION.Type field.
	 */
	int MEM_MAPPED = 0x40000;
	/**
	 * Indicates that the memory pages within the region are private (that is,
	 * not shared by other processes). Constant for
	 * MEMORY_BASIC_INFORMATION.Type field.
	 */
	int MEM_PRIVATE = 0x20000;
	/**
	 * Enables execute access to the committed region of pages. An attempt to
	 * read from or write to the committed region results in an access
	 * violation. Constant for MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_EXECUTE = 0x10;
	/**
	 * Enables execute, read-only, or copy-on-write access to the committed
	 * region of pages. An attempt to write to the committed region results in
	 * an access violation. Constant for MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_EXECUTE_READ = 0x20;
	/**
	 * Enables execute, read-only, read/write, or copy-on-write access to the
	 * committed region of pages. Constant for MEMORY_BASIC_INFORMATION.Protect
	 * field.
	 */
	int PAGE_EXECUTE_READWRITE = 0x40;
	/**
	 * Enables execute, read-only, or copy-on-write access to the committed
	 * region of image file code pages. This value is equivalent to
	 * PAGE_EXECUTE_READ. Constant for MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_EXECUTE_WRITECOPY = 0x80;
	/**
	 * Disables all access to the committed region of pages. An attempt to read
	 * from, write to, or execute the committed region results in an access
	 * violation exception, called a general protection (GP) fault. Constant for
	 * MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_NOACCESS = 0x1;
	/**
	 * Enables read-only or copy-on-write access to the committed region of
	 * pages. An attempt to write to the committed region results in an access
	 * violation. If the system differentiates between read-only access and
	 * execute access, an attempt to execute code in the committed region
	 * results in an access violation. Constant for
	 * MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_READONLY = 0x2;
	/**
	 * Enables read-only, read/write, or copy-on-write access to the committed
	 * region of pages. Constant for MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_READWRITE = 0x4;
	/**
	 * Enables read-only or copy-on-write access to the committed region of
	 * pages. This value is equivalent to PAGE_READONLY. Constant for
	 * MEMORY_BASIC_INFORMATION.Protect field.
	 */
	int PAGE_WRITECOPY = 0x8;
	/**
	 * Pages in the region become guard pages. Any attempt to access a guard
	 * page causes the system to raise a STATUS_GUARD_PAGE_VIOLATION exception
	 * and turn off the guard page status. Guard pages thus act as a one-time
	 * access alarm. For more information, see Creating Guard Pages.
	 */
	int PAGE_GUARD = 0x100;
	/**
	 * Does not allow caching of the committed regions of pages in the CPU
	 * cache. The hardware attributes for the physical memory should be
	 * specified as "no cache." This is not recommended for general usage. It is
	 * useful for device drivers, for example, mapping a video frame buffer with
	 * no caching.
	 */
	int PAGE_NOCACHE = 0x200;
	/**
	 * Enables write-combined memory accesses. When enabled, the processor
	 * caches memory write requests to optimize performance. Thus, if two
	 * requests are made to write to the same memory address, only the more
	 * recent write may occur.
	 */
	int PAGE_WRITECOMBINE = 0x400;

	/**
	 * Retrieves information about a range of pages within the virtual address
	 * space of a specified process.
	 */
	int VirtualQueryEx(int hProcess, int lpAddress,
			MemoryBasicInformation lpBuffer, int dwLength);

	/** Not all privileges or groups referenced are assigned to the caller. */
	int ERROR_NOT_ALL_ASSIGNED = 1300;
	/** Mask for the predefined standard access types. */
	int DELETE = 0x00010000;
	/** Mask for the predefined standard access types. */
	int READ_CONTROL = 0x00020000;
	/** Mask for the predefined standard access types. */
	int WRITE_DAC = 0x00040000;
	/** Mask for the predefined standard access types. */
	int WRITE_OWNER = 0x00080000;
	/** Mask for the predefined standard access types. */
	int STANDARD_RIGHTS_READ = READ_CONTROL;
	/** Mask for the predefined standard access types. */
	int STANDARD_RIGHTS_WRITE = READ_CONTROL;
	/** Mask for the predefined standard access types. */
	int STANDARD_RIGHTS_EXECUTE = READ_CONTROL;
	/** Mask for the predefined standard access types. */
	int STANDARD_RIGHTS_ALL = 0x001F0000;
	/** Mask for the predefined standard access types. */
	int SPECIFIC_RIGHTS_ALL = 0x0000FFFF;

	/** Retrieves a pseudo handle for the current process. */
	int GetCurrentProcess();

	/**
	 * Required to retrieve certain information about a process, such as its
	 * token, exit code, and priority class (see OpenProcessToken,
	 * GetExitCodeProcess, GetPriorityClass, and IsProcessInJob).
	 */
	int PROCESS_QUERY_INFORMATION = 0x400;
	/** Required to read memory in a process using ReadProcessMemory. */
	int PROCESS_VM_READ = 0x10;
	/** Required to write to memory in a process using WriteProcessMemory. */
	int PROCESS_VM_WRITE = 0x0020;
	/**
	 * Required to perform an operation on the address space of a process (see
	 * VirtualProtectEx and WriteProcessMemory).
	 */
	int PROCESS_VM_OPERATION = 0x8;

}