/*
 * @author Linghui Luo
 */
package de.upb.swt.soot.java.core.language;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Language;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Language specific Configuration for Java.
 *
 * @author Markus Schmidt
 * @author Linghui Luo
 */
public class JavaLanguage extends Language {

  /** The identifier factory. */
  @Nonnull private final IdentifierFactory identifierFactory;

  /** The use java modules. */
  private final boolean useJavaModules;

  /**
   * Instantiates a new java language with given version e.g 8 or 9
   *
   * @param version the version
   */
  public JavaLanguage(int version) {
    if (version <= 8) {
      identifierFactory = JavaIdentifierFactory.getInstance();
      useJavaModules = false;
    } else {
      identifierFactory = ModuleIdentifierFactory.getInstance();
      useJavaModules = true;
    }
  }

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

  public boolean useJavaModules() {
    return useJavaModules;
  }
}
