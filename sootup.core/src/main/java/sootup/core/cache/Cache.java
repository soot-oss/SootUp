package sootup.core.cache;

import java.util.Collection;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Interface for different caching strategies of resolved classes.
 */
public interface Cache<S extends SootClass<?>> {

  S getClass(ClassType classType);

  @Nonnull
  Collection<S> getClasses();

  @Nonnull
  void putClass(ClassType classType, S sootClass);

  @Nonnull
  boolean hasClass(ClassType classType);
}
