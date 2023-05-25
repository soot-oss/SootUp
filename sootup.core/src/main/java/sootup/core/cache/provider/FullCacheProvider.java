package sootup.core.cache.provider;

import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;
import sootup.core.model.SootClass;

/** Provides a new {@link FullCache} object. */
public class FullCacheProvider<S extends SootClass<?>> implements ClassCacheProvider<S> {

  @Override
  public ClassCache<S> createCache() {
    return new FullCache<S>();
  }
}
