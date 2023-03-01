package sootup.core.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Cache that stores any class that has been resolved. */
public class FullCache<S extends SootClass<?>> implements ClassCache<S> {

  protected final Map<ClassType, S> cache = new HashMap<>();

  @Override
  public synchronized S getClass(ClassType classType) {
    return cache.get(classType);
  }

  @Nonnull
  @Override
  public synchronized Collection<S> getClasses() {
    return cache.values();
  }

  @Override
  public void putClass(ClassType classType, S sootClass) {
    cache.putIfAbsent(classType, sootClass);
  }

  @Override
  public boolean hasClass(ClassType classType) {
    return cache.containsKey(classType);
  }

  @Override
  public int size() {
    return cache.size();
  }
}
