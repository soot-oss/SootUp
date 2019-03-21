package de.upb.soot.core;

import de.upb.soot.signatures.ISignature;
import javax.annotation.Nonnull;

/** Defines the resolving levels. */
public enum ResolvingLevel {
  /** The {@link ISignature signature} is known */
  DANGLING(0),

  /** Super and sub classes are known */
  HIERARCHY(1),

  /** Methods and fields are known */
  SIGNATURES(2),

  /** Method bodies are loaded */
  BODIES(3);

  // region Constructor

  ResolvingLevel(int level) {
    this._level = level;
  }

  // endregion /Constructor/

  // region Properties

  private final int _level;

  /**
   * Gets the resolving level.
   *
   * @return The value to get.
   */
  public int getLevel() {
    return this._level;
  }

  // endregion /Properties/

  // region Methods

  /**
   * Gets a value, indicating whether this resolving level is lower than the specified resolving
   * level.
   *
   * @param other An other {@link ResolvingLevel} to compare.
   * @return <tt>true</tt>, if this resolving level is lower than the <i>other</i> resolving level;
   *     otherwise, <tt>false</tt>.
   */
  public boolean isLowerThan(@Nonnull ResolvingLevel other) {
    return this.getLevel() < other.getLevel();
  }

  // endregion /Methods/
}
