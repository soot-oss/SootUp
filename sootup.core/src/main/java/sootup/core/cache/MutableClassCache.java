package sootup.core.cache;

import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Interface for caches which are mutable and allow classes to be removed from. */
public interface MutableClassCache extends ClassCache {
  SootClass removeClass(ClassType classType);

  default SootClass replaceClass(
      @Nonnull ClassType oldType, @Nonnull ClassType newType, @Nonnull SootClass newClass) {
    SootClass oldClass = removeClass(oldType);
    putClass(newType, newClass);
    return oldClass;
  }
}
