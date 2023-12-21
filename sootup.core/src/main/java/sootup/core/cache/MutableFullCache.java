package sootup.core.cache;

import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Mutable version of the {@link FullCache} that additionally allows for a removal of cached
 * classes.
 */
public class MutableFullCache extends FullCache implements MutableClassCache {

  @Override
  public SootClass removeClass(@Nonnull ClassType classType) {
    if (this.hasClass(classType)) {
      return cache.remove(classType);
    }
    return null;
  }
}
