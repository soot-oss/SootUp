package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.language.SootLanguage;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import javax.annotation.Nonnull;

/**
 * Language specific Configuration for Java
 *
 * @author Markus Schmidt
 */
public class Java extends SootLanguage {

  @Nonnull
  private static final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @Override
  @Nonnull
  public String getName() {
    return "Java";
  }

  @Override
  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }
}
