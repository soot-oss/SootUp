package de.upb.soot.types;

import javax.annotation.Nonnull;

public final class GlobalTypeScope implements JavaClassTypeScope {

  @Nonnull private static final GlobalTypeScope INSTANCE = new GlobalTypeScope();

  private GlobalTypeScope() {};

  public static GlobalTypeScope getInstance() {
    return INSTANCE;
  }

  @Override
  public GlobalTypeScope getScope() {
    return this;
  }
}
