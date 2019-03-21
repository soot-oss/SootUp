/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.util.concurrent;

import de.upb.soot.util.Utils;
import java.util.function.Supplier;

/**
 * Represents a serializable value that has been lazy initialized. You can use this class, if you
 * have to provide an already existing value lazy.
 *
 * <p><i>Info: This class is adopted from the <a
 * href="https://kotlinlang.org/api/latest/jvm/stdlib/index.html">Kotlin Standard Library</a> (
 * <code>kotlin.util.Lazy</code> and <code>kotlin.util.LazyJVM</code>).</i>
 *
 * @author Jan Martin Persch
 * @see Utils#initializedLazy(Object)
 * @see SynchronizedLazy
 * @see Utils#synchronizedLazy(Supplier)
 * @see Utils#synchronizedLazy(Object, Supplier)
 */
public class InitializedLazy<T> extends LazyBase<T> {
  // region Fields

  // endregion /Fields/

  // region Constructor

  /**
   * Creates a new instance of the {@link InitializedLazy} class.
   *
   * @param value A value.
   */
  public InitializedLazy(T value) {
    this._value = value;
  }

  // endregion /Constructor/

  // region Properties

  private final T _value;

  @Override
  public T get() {
    return this._value;
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  // endregion /Properties/

  // region Methods

  // endregion /Methods/
}
