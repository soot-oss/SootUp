package sootup.core.cache.provider;

import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;

/** Provides a new {@link FullCache} object. */
public class FullCacheProvider implements ClassCacheProvider {

  @Override
  public ClassCache createCache() {
    return new FullCache();
  }
}
