/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.util.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Defines the base class for lazies that provides a method to get the value wrapped into an
 * {@link Optional}. The {@link Optional} instance is cached for performance reasons.
 *
 * @author Jan Martin Persch
 */
public abstract class LazyBase<T> implements Lazy<T> {
  
  // region Fields
  
  // endregion /Fields/
  
  // endregion /Properties/
  
  // region Methods
  
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nullable private transient Optional<T> _cachedOptional;
  
  @SuppressWarnings("OptionalAssignedToNull")
  @Override
  @Nonnull
  public Optional<T> getAsOptional() {
    Optional<T> o = this._cachedOptional;
    
    if(o != null) {
      return o;
    }
    
    this._cachedOptional = o = Optional.ofNullable(this.get());
    
    return o;
  }
  
  @Override
  @Nonnull
  public String toString() {
    return this.isInitialized()
            ? this.get().toString()
            : "Lazy value not initialized yet.";
  }
  
  // endregion /Methods/
}
