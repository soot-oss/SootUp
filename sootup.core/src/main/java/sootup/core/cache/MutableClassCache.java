package sootup.core.cache;

import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Interface for caches which are mutable and allow classes to be removed from. */
public interface MutableClassCache<S extends SootClass<?>> extends ClassCache<S> {
  S removeClass(ClassType classType);
}
