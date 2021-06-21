package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Language;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;

public class JimpleLanguage extends Language {

  private static JimpleLanguage INSTANCE = new JimpleLanguage();

  public static JimpleLanguage getInstance() {
    return INSTANCE;
  }

  @Override
  public String getName() {
    return "Jimple";
  }

  @Override
  public int getVersion() {
    return -1; // there is no real versioning other than "old" Soot and FutureSoot at the moment
  }

  @Override
  public IdentifierFactory getIdentifierFactory() {
    // FIXME [ms] ?
    return JavaIdentifierFactory.getInstance();
  }
}
