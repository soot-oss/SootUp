package de.upb.soot.frontends;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.IView;
import java.util.Collections;
import javax.annotation.Nonnull;

/**
 * Converts a single source into Soot IR (Jimple).
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Manuel Benz
 */
public interface IClassSourceContent {

  @Nonnull
  AbstractClass resolveClass(@Nonnull ResolvingLevel level, @Nonnull IView view)
      throws ResolveException;

  @Nonnull
  default Iterable<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    // TODO: Not sure whether this should even have a default implementation
    return Collections.emptyList();
  }

  @Nonnull
  default Iterable<SootField> resolveFields(@Nonnull JavaClassType signature) throws ResolveException {
    // TODO: Not sure whether this should even have a default implementation
    return Collections.emptyList();
  }
}
