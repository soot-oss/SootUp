package de.upb.soot.frontends;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.types.JavaClassType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Converts a single source into Soot IR (Jimple).
 *
 * @author Andreas Dann
 */
public interface IModuleClassSourceContent extends IClassSourceContent {

  @Nonnull
  default Iterable<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    return Collections.emptyList();
  }

  @Nonnull
  default Iterable<SootField> resolveFields(@Nonnull JavaClassType signature)
      throws ResolveException {
    return Collections.emptyList();
  }

  default Set<JavaClassType> resolveInterfaces(JavaClassType type) {
    return Collections.emptySet();
  }

  default Optional<JavaClassType> resolveSuperclass(JavaClassType type) {
    return Optional.empty();
  }

  default Optional<JavaClassType> resolveOuterClass(JavaClassType type) {
    return Optional.empty();
  }

  String getModuleName();

  Collection<SootModuleInfo.ModuleReference> requires();

  Collection<SootModuleInfo.PackageReference> exports();

  Collection<SootModuleInfo.PackageReference> opens();

  Collection<JavaClassType> provides();

  Collection<JavaClassType> uses();

  Set<Modifier> resolveModifiers(JavaClassType type);

  Position resolvePosition(JavaClassType type);
}
