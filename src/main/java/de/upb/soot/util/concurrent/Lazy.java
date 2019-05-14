/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.util.concurrent;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   * Creates a {@link SynchronizedLazy} instance.
   *
   * @param initializer The initializer for the lazy value.
   * @param <T> The type of the lazy value.
   * @return The created {@link SynchronizedLazy} instance.
   */
  @Nonnull
  static <T> Lazy<T> synchronizedLazy(@Nonnull Supplier<T> initializer) {
    return synchronizedLazy(null, initializer);
  }

  /**
   * Creates a {@link SynchronizedLazy} instance.
   *
   * @param mutex A mutex object for synchronization.
   * @param initializer The initializer for the lazy value.
   * @param <T> The type of the lazy value.
   * @return The created {@link SynchronizedLazy} instance.
   */
  @Nonnull
  static <T> Lazy<T> synchronizedLazy(@Nullable Object mutex, @Nonnull Supplier<T> initializer) {
    return new SynchronizedLazy<>(mutex, initializer);
  }

  /**
   * Creates a {@link InitializedLazy} instance.
   *
   * @param value The value.
   * @param <T> The type of the lazy value.
   * @return The created {@link InitializedLazy} instance.
   */
  @Nonnull
  static <T> Lazy<T> initializedLazy(@Nonnull T value) {
    return new InitializedLazy<>(value);
  }

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
