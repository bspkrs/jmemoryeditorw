package hu.jmemoryeditorw.jna;

import com.sun.jna.Structure;

/** The LUID_AND_ATTRIBUTES structure represents a locally unique identifier (LUID) and its attributes. */
public class LUIDandAttributes extends Structure {
	/** Specifies an LUID value. */
	public LUID Luid;
	/** Specifies attributes of the LUID. This value contains up to 32 one-bit flags. Its meaning is dependent on the definition and use of the LUID. */
	public int Attributes;
}