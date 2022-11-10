package de.upb.sse.sootup.core.cache;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.types.ClassType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class FullCache<S extends SootClass<?>> implements Cache<S> {

  @Nonnull protected final Map<ClassType, S> cache = new HashMap<>();

  @Override
  public synchronized S getClass(ClassType classType) {
    return cache.get(classType);
  }

  @Nonnull
  @Override
  public synchronized Collection<S> getClasses() {
    return cache.values();
  }

  @Nonnull
  @Override
  public void putClass(ClassType classType, S sootClass) {
    cache.putIfAbsent(classType, sootClass);
  }

  @Nonnull
  @Override
  public boolean hasClass(ClassType classType) {
    return cache.containsKey(classType);
  }
}
