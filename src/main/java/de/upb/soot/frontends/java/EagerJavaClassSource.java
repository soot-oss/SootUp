package de.upb.soot.frontends.java;

import com.google.common.base.Preconditions;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A class source for resolving from .java files using wala java source front-end.
 *
 * @author Linghui Luo
 */
public class EagerJavaClassSource extends ClassSource {

  private final JavaClassType superClass;
  private final Set<JavaClassType> interfaces;
  private final JavaClassType outerClass;
  private final Set<SootField> sootFields;
  private final Set<SootMethod> sootMethods;
  private final Position position;
  private final EnumSet<Modifier> modifiers;

  public EagerJavaClassSource(
      INamespace srcNamespace,
      Path sourcePath,
      JavaClassType classSignature,
      JavaClassType superClass,
      Set<JavaClassType> interfaces,
      JavaClassType outerClass,
      Set<SootField> sootFields,
      Set<SootMethod> sootMethods,
      Position position,
      EnumSet<Modifier> modifiers) {
    super(srcNamespace, sourcePath, classSignature);
    this.superClass = superClass;
    this.interfaces = interfaces;
    this.outerClass = outerClass;
    this.sootFields = sootFields;
    this.sootMethods = sootMethods;
    this.position = position;
    this.modifiers = modifiers;
  }

  @Override
  public IClassSourceContent getContent() {
    return new IClassSourceContent() {

      @Nonnull
      @Override
      public Iterable<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
          throws ResolveException {
        return sootMethods;
      }

      @Nonnull
      @Override
      public Iterable<SootField> resolveFields(@Nonnull JavaClassType signature)
          throws ResolveException {
        return sootFields;
      }

      @Override
      public Set<Modifier> resolveModifiers(JavaClassType type) {
        checkExpectedType(type);
        return modifiers;
      }

      @Override
      public Set<JavaClassType> resolveInterfaces(JavaClassType type) {
        checkExpectedType(type);
        return interfaces;
      }

      @Override
      public Optional<JavaClassType> resolveSuperclass(JavaClassType type) {
        checkExpectedType(type);
        return Optional.ofNullable(superClass);
      }

      @Override
      public Optional<JavaClassType> resolveOuterClass(JavaClassType type) {
        checkExpectedType(type);
        return Optional.ofNullable(outerClass);
      }

      @Override
      public Position resolvePosition(JavaClassType type) {
        checkExpectedType(type);
        return position;
      }

      private void checkExpectedType(JavaClassType type) {
        Preconditions.checkArgument(getClassType().equals(type), "Expected type " + getClassType());
      }
    };
  }
}
