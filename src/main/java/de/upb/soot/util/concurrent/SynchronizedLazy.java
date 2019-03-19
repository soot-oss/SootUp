/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.util.concurrent;

import de.upb.soot.util.Utils;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a serializable value with synchronized lazy initialization.
 * <p>
 * <i>Info: This class is adopted from the
 * <a href="https://kotlinlang.org/api/latest/jvm/stdlib/index.html">Kotlin Standard Library</a>
 * (<code>kotlin.util.Lazy</code> and <code>kotlin.util.LazyJVM</code>).</i>
 *
 * @author Jan Martin Persch
 * @see Utils#synchronizedLazy(Supplier)
 * @see Utils#synchronizedLazy(Object, Supplier)
 * @see InitializedLazy
 * @see Utils#initializedLazy(Object)
 */
public class SynchronizedLazy<T> extends LazyBase<T> {
    // region Fields
    
    @Nonnull
    private static final Object UNINITIALIZED_VALUE = new Object();
    
    @Nonnull
    private final Object mutex;
    
    @Nonnull
    private final Supplier<T> initializer;
    
    @SuppressWarnings("unchecked")
    @Nullable
    private volatile T value = (T) UNINITIALIZED_VALUE;
    
    // endregion /Fields/
    
    // region Constructor
    
    /**
     * Creates a new instance of the {@link SynchronizedLazy} class.
     *
     * @param initializer The initializer for the lazy value.
     */
    public SynchronizedLazy(@Nonnull Supplier<T> initializer) {
        this(null, initializer);
    }
    
    /**
     * Creates a new instance of the {@link SynchronizedLazy} class.
     *
     * @param mutex A mutex object for synchronization.
     * @param initializer The initializer for the lazy value.
     */
    public SynchronizedLazy(@Nullable Object mutex, @Nonnull Supplier<T> initializer) {
        this.mutex = mutex == null ? this : mutex;
        this.initializer = initializer;
    }
    
    // endregion /Constructor/
    
    // region Properties
    
    @Override
    public boolean isInitialized() {
        return this.value != UNINITIALIZED_VALUE;
    }
    
    // endregion /Properties/
    
    // region Methods
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public T get() {
        T v1 = this.value;
        
        if (v1 != UNINITIALIZED_VALUE) {
            return v1;
        }
        
        synchronized (this.mutex) {
            T v2 = value;
    
            if (v2 != UNINITIALIZED_VALUE) {
                return v2;
            }
            
            T newValue = this.initializer.get();
            
            this.value = newValue;
            
            return newValue;
        }
    }
    
    // endregion /Methods/
}
