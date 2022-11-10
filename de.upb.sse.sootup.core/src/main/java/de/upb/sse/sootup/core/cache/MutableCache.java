package de.upb.sse.sootup.core.cache;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.types.ClassType;

public interface MutableCache<S extends SootClass<?>> extends Cache<S> {
  S removeClass(ClassType classType);
}
