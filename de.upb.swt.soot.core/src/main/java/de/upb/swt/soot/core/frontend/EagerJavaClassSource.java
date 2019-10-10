package de.upb.swt.soot.core.frontend;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A class source for resolving classes that are batchparsed like .java files using wala java source
 * frontend or in tests where all information is already existing.
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
    super(srcNamespace, classType, sourcePath);

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
  public EnumSet<Modifier> resolveModifiers() {
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
    return "frontend.EagerJavaClassSource{"
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
