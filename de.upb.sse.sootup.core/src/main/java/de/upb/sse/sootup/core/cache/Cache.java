package de.upb.sse.sootup.core.cache;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.types.ClassType;
import java.util.Collection;
import javax.annotation.Nonnull;

public interface Cache<S extends SootClass<?>> {

  S getClass(ClassType classType);

  @Nonnull
  Collection<S> getClasses();

  @Nonnull
  void putClass(ClassType classType, S sootClass);

  @Nonnull
  boolean hasClass(ClassType classType);
}
