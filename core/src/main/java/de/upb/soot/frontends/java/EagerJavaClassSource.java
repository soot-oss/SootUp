package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
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
      AnalysisInputLocation srcNamespace,
      Path sourcePath,
      JavaClassType classType,
      JavaClassType superClass,
      Set<JavaClassType> interfaces,
      JavaClassType outerClass,
      Set<SootField> sootFields,
      Set<SootMethod> sootMethods,
      Position position,
      EnumSet<Modifier> modifiers) {
    super(srcNamespace, sourcePath, classType);

    this.superClass = superClass;
    this.interfaces = interfaces;
    this.outerClass = outerClass;
    this.sootFields = sootFields;
    this.sootMethods = sootMethods;
    this.position = position;
    this.modifiers = modifiers;
  }

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    return sootMethods;
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    return sootFields;
  }

  @Nonnull
  @Override
  public Set<Modifier> resolveModifiers() {
    return modifiers;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> resolveInterfaces() {
    return interfaces;
  }

  @Nonnull
  @Override
  public Optional<JavaClassType> resolveSuperclass() {
    return Optional.ofNullable(superClass);
  }

  @Nonnull
  @Override
  public Optional<JavaClassType> resolveOuterClass() {
    return Optional.ofNullable(outerClass);
  }

  @Override
  public Position resolvePosition() {
    return position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EagerJavaClassSource that = (EagerJavaClassSource) o;
    return Objects.equals(superClass, that.superClass)
        && Objects.equals(interfaces, that.interfaces)
        && Objects.equals(outerClass, that.outerClass)
        && Objects.equals(sootFields, that.sootFields)
        && Objects.equals(sootMethods, that.sootMethods)
        && Objects.equals(position, that.position)
        && Objects.equals(modifiers, that.modifiers)
        && Objects.equals(classSignature, that.classSignature);
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
        classSignature);
  }

  @Override
  public String toString() {
    return "EagerJavaClassSource{"
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
        + classSignature
        + '}';
  }
}
