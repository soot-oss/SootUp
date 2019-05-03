package de.upb.soot.frontends;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.types.JavaClassType;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
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
  default Iterable<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    // TODO: Not sure whether this should even have a default implementation
    return Collections.emptyList();
  }

  @Nonnull
  default Iterable<SootField> resolveFields(@Nonnull JavaClassType signature)
      throws ResolveException {
    // TODO: Not sure whether this should even have a default implementation
    return Collections.emptyList();
  }

  Set<Modifier> resolveModifiers(JavaClassType type);

  Set<JavaClassType> resolveInterfaces(JavaClassType type);

  Optional<JavaClassType> resolveSuperclass(JavaClassType type);

  Optional<JavaClassType> resolveOuterClass(JavaClassType type);

  Position resolvePosition(JavaClassType type);
}
