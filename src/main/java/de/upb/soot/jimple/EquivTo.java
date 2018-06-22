package de.upb.soot.jimple;

/**
 * An alternate equivalence relation between objects. The standard interpretation will be structural equality. We also demand
 * that if x.equivTo(y), then x.equivHashCode() == y.equivHashCode.
 */
public interface EquivTo {
  /** Returns true if this object is equivalent to o. */
  public boolean equivTo(Object o);

  /**
   * Returns a (not necessarily fixed) hash code for this object. This hash code coincides with equivTo; it is undefined in
   * the presence of mutable objects.
   */
  public int equivHashCode();
}
