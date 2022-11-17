package sootup.core.cache.provider;

import sootup.core.cache.Cache;
import sootup.core.model.SootClass;

public interface CacheProvider<S extends SootClass<?>> {
  Cache<S> createCache();
}
