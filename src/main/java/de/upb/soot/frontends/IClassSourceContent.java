package de.upb.soot.frontends;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.JavaClassSignature;
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
  default Iterable<SootMethod> resolveMethods(@Nonnull JavaClassSignature signature)
      throws ResolveException {
    return Collections.emptyList();
  }

  @Nonnull
  default Iterable<SootField> resolveFields(@Nonnull JavaClassSignature signature)
      throws ResolveException {
    return Collections.emptyList();
  }
}
