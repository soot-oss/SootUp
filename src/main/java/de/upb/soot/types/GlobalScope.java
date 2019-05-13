package de.upb.soot.types;

import javax.annotation.Nonnull;

public final class GlobalScope implements JavaClassTypeScope {

  @Nonnull private static final GlobalScope INSTANCE = new GlobalScope();

  private GlobalScope() {};

  public static GlobalScope getInstance() {
    return INSTANCE;
  }

  @Override
  public GlobalScope getScope() {
    return this;
  }
}
