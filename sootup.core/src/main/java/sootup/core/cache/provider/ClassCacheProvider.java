package sootup.core.cache.provider;

import sootup.core.cache.ClassCache;

/** Interface for cache providers. */
public interface ClassCacheProvider {

  /** Create and return a new cache object. */
  ClassCache createCache();
}
