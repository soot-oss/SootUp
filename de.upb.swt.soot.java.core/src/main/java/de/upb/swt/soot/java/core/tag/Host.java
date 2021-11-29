package de.upb.swt.soot.java.core.tag;

import java.util.List;

/**
 * A "taggable" object. Implementing classes can have arbitrary labelled data attached to them.
 *
 * One example of a tag would be to store Boolean values, associated with array accesses, indicating whether bounds checks
 * can be omitted.
 *
 * @see Tag
 */
public interface Host {
    /** Gets a list of tags associated with the current object. */
    public List<Tag> getTags();

    /** Returns the tag with the given name. */
    public Tag getTag(String aName);

    /** Adds a tag. */
    public void addTag(Tag t);

    /** Removes the first tag with the given name. */
    public void removeTag(String name);

    /** Returns true if this host has a tag with the given name. */
    public boolean hasTag(String aName);

    /** Removes all the tags from this host. */
    public void removeAllTags();

    /** Adds all the tags from h to this host. */
    public void addAllTagsOf(Host h);

    /**
     * Returns the Java source line number if available. Returns -1 if not.
     */
    public int getJavaSourceStartLineNumber();

    /**
     * Returns the Java source line column if available. Returns -1 if not.
     */
    public int getJavaSourceStartColumnNumber();
}