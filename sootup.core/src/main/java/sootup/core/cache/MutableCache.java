package sootup.core.cache;

import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

public interface MutableCache<S extends SootClass<?>> extends Cache<S> {
  S removeClass(ClassType classType);
}
