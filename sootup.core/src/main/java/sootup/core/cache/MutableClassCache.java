package sootup.core.cache;

import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Interface for caches which are mutable and allow classes to be removed from. */
public interface MutableClassCache<S extends SootClass<?>> extends ClassCache<S> {
  S removeClass(ClassType classType);

  default S replaceClass(
      @Nonnull ClassType oldType, @Nonnull ClassType newType, @Nonnull S newClass) {
    S oldClass = removeClass(oldType);
    putClass(newType, newClass);
    return oldClass;
  }
}
