package de.upb.swt.soot.java.bytecode.frontend.apk.tag;

/**
 * Represents a tag; these get attached to implementations of Host.
 */

public interface Tag {
    /** Returns the tag name. */
    public String getName();

    /** Returns the tag raw data. */
    public byte[] getValue();
}