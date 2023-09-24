package sootup.core.cache;

import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Mutable version of the {@link FullCache} that additionally allows for a removal of cached
 * classes.
 */
public class MutableFullCache<S extends SootClass<?>> extends FullCache<S>
    implements MutableClassCache<S> {

  @Override
  public S removeClass(@Nonnull ClassType classType) {
    if (this.hasClass(classType)) {
      return cache.remove(classType);
    }
    return null;
  }
}
