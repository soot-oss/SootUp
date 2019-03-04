package de.upb.soot.core;

public enum ResolvingLevel {
  DANGLING(0),
  HIERARCHY(1),
  SIGNATURES(2),
  BODIES(3);

  private final int level;

  ResolvingLevel(int level) {
    this.level = level;
  }

  public boolean isLoweverLevel(de.upb.soot.core.ResolvingLevel other) {
    return this.level < other.level;
  }
}
