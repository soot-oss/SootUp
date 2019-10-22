package de.upb.swt.soot.core.language;

import de.upb.swt.soot.core.IdentifierFactory;

/**
 * This class is a container for language specific information
 *
 * @author Markus Schmidt
 */
public abstract class SootLanguage {
  public abstract String getName();

  public abstract IdentifierFactory getIdentifierFactory();
}
