package sootup.core.cache;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

public class LRUCache<S extends SootClass<?>> implements Cache<S> {
  private final int cacheSize;
  @Nonnull private final Map<ClassType, S> cache = new HashMap<>();
  @Nonnull private final LinkedList<ClassType> accessOrder = new LinkedList<>();

  public LRUCache(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  @Override
  public synchronized S getClass(ClassType classType) {
    S sootClass = cache.get(classType);
    if (sootClass != null) {
      accessOrder.remove(classType);
      accessOrder.addFirst(classType);
    }

    return sootClass;
  }

  @Nonnull
  @Override
  public synchronized Collection<S> getClasses() {
    return null;
  }

  @Nonnull
  @Override
  public void putClass(ClassType classType, S sootClass) {
    if (accessOrder.size() >= cacheSize) {
      ClassType leastAccessed = accessOrder.removeLast();
      cache.remove(leastAccessed);
    }

    accessOrder.addFirst(classType);
    cache.putIfAbsent(classType, sootClass);
  }

  @Nonnull
  @Override
  public boolean hasClass(ClassType classType) {
    return cache.containsKey(classType);
  }
}
