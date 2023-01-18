package sootup.core.cache.provider;

import sootup.core.cache.ClassCache;
import sootup.core.cache.MutableFullCache;
import sootup.core.model.SootClass;

/** Provides a new {@link MutableFullCache} object. */
public class MutableFullCacheProvider<S extends SootClass<?>> implements ClassCacheProvider<S> {

  @Override
  public ClassCache<S> createCache() {
    return new MutableFullCache<>();
  }
}
