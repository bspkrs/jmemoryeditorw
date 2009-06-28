package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/** Locally Unique Identifier record. */
public class LUID extends Structure {
	public int LowPart;
	public int HiPart;
}