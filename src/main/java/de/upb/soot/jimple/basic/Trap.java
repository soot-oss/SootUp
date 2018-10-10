package de.upb.soot.jimple.basic;

/**
 * A trap is an exception catcher.
 * 
 * @author Linghui Luo
 *
 */
public interface Trap extends StmtBoxOwner {

    /** Performs a shallow clone of this trap. */
    public Object clone();

}
