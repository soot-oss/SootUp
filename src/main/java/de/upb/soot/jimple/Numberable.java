package de.upb.soot.jimple;

/**
 * /** A class that numbers objects, so they can be placed in bitsets.
 *
 * @author Ondrej Lhotak
 */
public interface Numberable {
  public void setNumber(int number);
  public int getNumber();
}