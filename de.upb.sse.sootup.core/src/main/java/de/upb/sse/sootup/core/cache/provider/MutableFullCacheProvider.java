package de.upb.sse.sootup.core.cache.provider;

import de.upb.sse.sootup.core.cache.Cache;
import de.upb.sse.sootup.core.cache.MutableFullCache;
import de.upb.sse.sootup.core.model.SootClass;

public class MutableFullCacheProvider<S extends SootClass<?>> implements CacheProvider<S> {

  @Override
  public Cache<S> createCache() {
    return new MutableFullCache<>();
  }
}
