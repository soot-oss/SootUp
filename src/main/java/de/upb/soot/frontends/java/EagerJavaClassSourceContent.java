package de.upb.soot.frontends.java;

import com.google.common.base.Preconditions;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.types.JavaClassType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

class EagerJavaClassSourceContent implements IClassSourceContent {
  private final JavaClassType superClass;
  private final Set<JavaClassType> interfaces;
  private final JavaClassType outerClass;
  private final Set<SootField> sootFields;
  private final Set<SootMethod> sootMethods;
  private final Position position;
  private final EnumSet<Modifier> modifiers;
  private final JavaClassType classType;

  EagerJavaClassSourceContent(
      JavaClassType superClass,
      Set<JavaClassType> interfaces,
      JavaClassType outerClass,
      Set<SootField> sootFields,
      Set<SootMethod> sootMethods,
      Position position,
      EnumSet<Modifier> modifiers,
      JavaClassType classType) {
    this.superClass = superClass;
    this.interfaces = interfaces;
    this.outerClass = outerClass;
    this.sootFields = sootFields;
    this.sootMethods = sootMethods;
    this.position = position;
    this.modifiers = modifiers;
    this.classType = classType;
  }

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    return sootMethods;
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields(@Nonnull JavaClassType signature)
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
    Preconditions.checkArgument(classType.equals(type), "Expected type " + classType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EagerJavaClassSourceContent that = (EagerJavaClassSourceContent) o;
    return Objects.equals(superClass, that.superClass)
        && Objects.equals(interfaces, that.interfaces)
        && Objects.equals(outerClass, that.outerClass)
        && Objects.equals(sootFields, that.sootFields)
        && Objects.equals(sootMethods, that.sootMethods)
        && Objects.equals(position, that.position)
        && Objects.equals(modifiers, that.modifiers)
        && Objects.equals(classType, that.classType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        superClass,
        interfaces,
        outerClass,
        sootFields,
        sootMethods,
        position,
        modifiers,
        classType);
  }

  @Override
  public String toString() {
    return "EagerJavaClassSourceContent{"
        + "superClass="
        + superClass
        + ", interfaces="
        + interfaces
        + ", outerClass="
        + outerClass
        + ", sootFields="
        + sootFields
        + ", sootMethods="
        + sootMethods
        + ", position="
        + position
        + ", modifiers="
        + modifiers
        + ", classType="
        + classType
        + '}';
  }
}
