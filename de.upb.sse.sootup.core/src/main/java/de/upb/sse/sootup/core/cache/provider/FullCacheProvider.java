package de.upb.sse.sootup.core.cache.provider;

import de.upb.sse.sootup.core.cache.Cache;
import de.upb.sse.sootup.core.cache.FullCache;
import de.upb.sse.sootup.core.model.SootClass;

public class FullCacheProvider<S extends SootClass<?>> implements CacheProvider<S> {

  @Override
  public Cache<S> createCache() {
    return new FullCache<S>();
  }
}
