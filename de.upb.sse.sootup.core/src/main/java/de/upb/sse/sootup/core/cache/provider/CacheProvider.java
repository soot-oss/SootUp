package de.upb.sse.sootup.core.cache.provider;

import de.upb.sse.sootup.core.cache.Cache;
import de.upb.sse.sootup.core.model.SootClass;

public interface CacheProvider<S extends SootClass<?>> {
  Cache<S> createCache();
}
