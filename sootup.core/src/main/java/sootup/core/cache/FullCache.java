package sootup.core.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Cache that stores any class that has been resolved. */
public class FullCache implements ClassCache {

  protected final Map<ClassType, SootClass> cache = new HashMap<>();

  @Override
  public synchronized SootClass getClass(ClassType classType) {
    return cache.get(classType);
  }

  @Nonnull
  @Override
  public synchronized Collection<SootClass> getClasses() {
    return cache.values();
  }

  @Override
  public void putClass(ClassType classType, SootClass sootClass) {
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
