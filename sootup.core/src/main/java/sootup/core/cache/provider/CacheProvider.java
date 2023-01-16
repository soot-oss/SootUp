package sootup.core.cache.provider;

import sootup.core.cache.Cache;
import sootup.core.model.SootClass;

/**
 * Interface for cache providers.
 */
public interface CacheProvider<S extends SootClass<?>> {

  /**
   * Create and return a new cache object.
   */
  Cache<S> createCache();
}
