package sootup.core.cache.provider;

import sootup.core.cache.Cache;
import sootup.core.cache.MutableFullCache;
import sootup.core.model.SootClass;

public class MutableFullCacheProvider<S extends SootClass<?>> implements CacheProvider<S> {

  @Override
  public Cache<S> createCache() {
    return new MutableFullCache<>();
  }
}
