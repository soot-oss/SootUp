package sootup.core.cache.provider;

import sootup.core.cache.ClassCache;
import sootup.core.cache.MutableFullCache;
import sootup.core.model.SootClass;

/** Provides a new {@link MutableFullCache} object. */
public class MutableFullCacheProvider implements ClassCacheProvider {

  @Override
  public ClassCache createCache() {
    return new MutableFullCache();
  }
}
