package sootup.core.cache;

import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Interface for caches which are mutable and allow classes to be removed from.
 */
public interface MutableCache<S extends SootClass<?>> extends Cache<S> {
  S removeClass(ClassType classType);
}
