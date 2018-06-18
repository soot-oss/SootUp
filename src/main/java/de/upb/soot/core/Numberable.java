package de.upb.soot.core;

/**
 * /** A class that numbers objects, so they can be placed in bitsets.
 *
 * @author Ondrej Lhotak
 */
public interface Numberable {
  public void setNumber(int number);
  public int getNumber();
}