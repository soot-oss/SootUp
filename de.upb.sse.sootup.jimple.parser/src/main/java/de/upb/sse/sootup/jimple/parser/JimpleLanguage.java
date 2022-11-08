package de.upb.sse.sootup.jimple.parser;

import de.upb.sse.sootup.core.IdentifierFactory;
import de.upb.sse.sootup.core.Language;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;

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
