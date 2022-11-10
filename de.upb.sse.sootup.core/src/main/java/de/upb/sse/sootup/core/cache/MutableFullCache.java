package de.upb.sse.sootup.core.cache;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.types.ClassType;

public class MutableFullCache<S extends SootClass<?>> extends FullCache<S>
    implements MutableCache<S> {

  @Override
  public S removeClass(ClassType classType) {
    if (this.hasClass(classType)) {
      return cache.remove(classType);
    }
    return null;
  }
}
