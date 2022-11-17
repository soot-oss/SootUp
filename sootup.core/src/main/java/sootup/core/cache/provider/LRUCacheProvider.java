package sootup.core.cache.provider;

import sootup.core.cache.Cache;
import sootup.core.cache.LRUCache;
import sootup.core.model.SootClass;

public class LRUCacheProvider<S extends SootClass<?>> implements CacheProvider<S> {
  private final int cacheSize;

  public LRUCacheProvider() {
    this(100);
  }

  public LRUCacheProvider(int cacheSize) {
    if (cacheSize < 1) throw new IllegalArgumentException("Cache size has to be at least 1");
    this.cacheSize = cacheSize;
  }

  @Override
  public Cache<S> createCache() {
    return new LRUCache<>(cacheSize);
  }
}
