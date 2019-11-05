package de.upb.swt.soot.core;

/**
 * This class is a container for language specific information
 *
 * @author Markus Schmidt
 */
public abstract class Language {
  public abstract String getName();

  public abstract IdentifierFactory getIdentifierFactory();
}
