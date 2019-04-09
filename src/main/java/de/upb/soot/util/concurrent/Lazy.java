/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.util.concurrent;

import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Represents a serializable value with lazy initialization.
 *
 * <p><i>Info: This class is adopted from the <a
 * href="https://kotlinlang.org/api/latest/jvm/stdlib/index.html">Kotlin Standard Library</a> (
 * <code>kotlin.util.Lazy</code> and <code>kotlin.util.LazyJVM</code>).</i>
 *
 * @author Jan Martin Persch
 * @see SynchronizedLazy
 * @see InitializedLazy
 */
public interface Lazy<T> {

  /**
   * Gets the lazily initialized value of the current Lazy instance.
   *
   * @return The value to get;
   */
  T get();

  /**
   * Gets a value, indicating whether the this instance has been initialized.
   *
   * @return <tt>true</tt>, if this instance has been initialized; otherwise, <tt>false</tt>.
   */
  boolean isInitialized();

  @Nonnull
  Optional<T> getAsOptional();
}
