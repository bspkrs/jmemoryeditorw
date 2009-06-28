package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/**
 * Contains information about a range of pages in the virtual address space of a process. The VirtualQuery and VirtualQueryEx functions use this structure.
 */
public class MemoryBasicInformation extends Structure {
	public int BaseAddress;
	public int AllocationBase;
	public int AllocationProtect;
	public int RegionSize;
	public int State;
	public int Protect;
	public int Type;
}