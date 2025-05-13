package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.model.*;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.JimplePrinter;
import sootup.java.core.types.JavaClassType;

public class JavaSootClass implements SootClass, HasAnnotation {

  @NonNull SourceType sourceType;
  @NonNull JavaClassType classSignature;
  @NonNull JavaSootClassSource classSource;

  @NonNull Supplier<Set<? extends SootMethod>> _lazyMethods =
      Suppliers.memoize(this::lazyMethodInitializer);

  @NonNull Supplier<Set<? extends SootField>> _lazyFields =
      Suppliers.memoize(this::lazyFieldInitializer);

  Supplier<Set<ClassModifier>> lazyModifiers = Suppliers.memoize(classSource::resolveModifiers);

  Supplier<Set<? extends ClassType>> lazyInterfaces =
      Suppliers.memoize(classSource::resolveInterfaces);

  Supplier<Optional<? extends ClassType>> lazySuperclass =
      Suppliers.memoize(classSource::resolveSuperclass);

  Supplier<Optional<? extends ClassType>> lazyOuterClass =
      Suppliers.memoize(classSource::resolveOuterClass);

  Supplier<Position> lazyPosition = Suppliers.memoize(classSource::resolvePosition);

  public JavaSootClass(SootClassSource classSource, SourceType sourceType) {
    this.classSource = (JavaSootClassSource) classSource;
    this.sourceType = sourceType;
    this.classSignature = (JavaClassType) classSource.getClassType();
  }

  private JavaSootClass(
      JavaSootClassSource classSource,
      SourceType sourceType,
      Set<? extends SootMethod> methods,
      Set<? extends SootField> fields,
      Set<ClassModifier> modifiers,
      Set<? extends ClassType> interfaces,
      Optional<? extends ClassType> superclass,
      Optional<? extends ClassType> outerClass,
      Position position) {
    this.classSource = classSource;
    this.sourceType = sourceType;
    this.classSignature = (JavaClassType) classSource.getClassType();
    this._lazyMethods = Suppliers.ofInstance(methods);
    this._lazyFields = Suppliers.ofInstance(fields);
    this.lazyModifiers = Suppliers.ofInstance(modifiers);
    this.lazyInterfaces = Suppliers.ofInstance(interfaces);
    this.lazySuperclass = Suppliers.ofInstance(superclass);
    this.lazyOuterClass = Suppliers.ofInstance(outerClass);
    this.lazyPosition = Suppliers.ofInstance(position);
  }

  /** Returns the ClassSignature of this class. */
  @Override
  public ClassType getType() {
    return classSignature;
  }

  /**
   * Get all annotations that are directly attached to this class.
   *
   * <p>This includes "visible" and "invisible" annotations. Note that inherited annotations are not
   * part of this iterable.
   *
   * @return
   */
  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    // we should cache it in the future: for now, we do not cache it
    // because the underlying data structure might be mutable
    return classSource.resolveAnnotations();
  }

  @NonNull
  @Override
  public Set<JavaSootMethod> getMethods() {
    return this._lazyMethods.get().stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Set<JavaSootField> getFields() {
    return this._lazyFields.get().stream()
        .map(field -> (JavaSootField) field)
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Optional<JavaSootField> getField(@NonNull String name) {
    return SootClass.super.getField(name).map(field -> (JavaSootField) field);
  }

  @NonNull
  @Override
  public Optional<JavaSootField> getField(@NonNull FieldSubSignature subSignature) {
    return SootClass.super.getField(subSignature).map(field -> (JavaSootField) field);
  }

  @NonNull
  @Override
  public Optional<JavaSootMethod> getMethod(
      @NonNull String name, @NonNull Iterable<? extends Type> parameterTypes) {
    return SootClass.super.getMethod(name, parameterTypes).map(method -> (JavaSootMethod) method);
  }

  @NonNull
  @Override
  public Set<JavaSootMethod> getMethodsByName(@NonNull String name) {
    return SootClass.super.getMethodsByName(name).stream()
        .map(method -> (JavaSootMethod) method)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<ClassModifier> getModifiers() {
    return lazyModifiers.get();
  }

  @Override
  public Set<? extends ClassType> getInterfaces() {
    return lazyInterfaces.get();
  }

  @NonNull
  @Override
  public Optional<JavaSootMethod> getMethod(@NonNull MethodSubSignature subSignature) {
    return SootClass.super.getMethod(subSignature).map(method -> (JavaSootMethod) method);
  }

  @NonNull
  @Override
  public JavaSootClassSource getClassSource() {
    return classSource;
  }

  @NonNull
  public Optional<JavaClassType> getOuterClass() {
    return lazyOuterClass.get().map(ct -> (JavaClassType) ct);
  }

  @NonNull
  public Optional<JavaClassType> getSuperclass() {
    return lazySuperclass.get().map(ct -> (JavaClassType) ct);
  }

  // Convenience withers that delegate to an OverridingClassSource

  /**
   * Creates a new JavaSootClass based on a new {@link OverridingJavaClassSource}. This is useful to
   * change selected parts of a {@link SootClass} without recreating a {@link JavaSootClassSource}
   * completely. {@link OverridingJavaClassSource} allows for replacing specific parts of a class,
   * such as fields and methods.
   */
  @NonNull
  public JavaSootClass withOverridingClassSource(
      Function<OverridingJavaClassSource, OverridingJavaClassSource> overrider) {
    return new JavaSootClass(
        overrider.apply(new OverridingJavaClassSource(getClassSource())), sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedMethod(
      @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @NonNull
  public JavaSootClass withMethods(@NonNull Collection<JavaSootMethod> methods) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withMethods(methods), sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedField(
      @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedField(toReplace, replacement),
        sourceType);
  }

  @NonNull
  public JavaSootClass withFields(@NonNull Collection<JavaSootField> fields) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withFields(fields), sourceType);
  }

  @NonNull
  public JavaSootClass withModifiers(@NonNull Set<ClassModifier> modifiers) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withModifiers(modifiers), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withSuperclass(@NonNull Optional<JavaClassType> superclass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withSuperclass(superclass), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withOuterClass(@NonNull Optional<JavaClassType> outerClass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withOuterClass(outerClass), sourceType);
  }

  @NonNull
  public JavaSootClass withPosition(@Nullable Position position) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withPosition(position), sourceType);
  }

  /** Defines a {@link SootClass} builder. */
  public static class JavaSootClassBuilder {
    @Nullable private JavaSootClassSource classSource;
    @Nullable private SourceType sourceType;
    @Nullable private Set<? extends SootMethod> methods = ImmutableSet.of();
    @Nullable private Set<? extends SootField> fields = ImmutableSet.of();
    @Nullable private Set<ClassModifier> modifiers = ImmutableSet.of();
    @Nullable private Set<? extends ClassType> interfaces = ImmutableSet.of();
    @Nullable private Optional<? extends ClassType> superclass = Optional.empty();
    @Nullable private Optional<? extends ClassType> outerClass = Optional.empty();
    @Nullable private Position position;

    private JavaSootClassBuilder() {}

    public static ClassSourceStep builder() {
      return new Steps();
    }

    /** Step interface for setting the class source. */
    public interface ClassSourceStep {
      SourceTypeStep withClassSource(@NonNull SootClassSource classSource);
    }

    /** Step interface for setting the source type. */
    public interface SourceTypeStep {
      CompleteStep withSourceType(@NonNull SourceType sourceType);
    }

    /** Interface that accumulates all possible methods. */
    public interface CompleteStep
        extends InterfaceStep,
            MethodStep,
            FieldStep,
            ModifierStep,
            SuperclassStep,
            OuterClassStep,
            PositionStep,
            Build {}

    public interface MethodStep {
      CompleteStep withMethod(@NonNull SootMethod method);

      CompleteStep withMethods(@NonNull Set<? extends SootMethod> methods);
    }

    public interface FieldStep {
      CompleteStep withField(@NonNull SootField field);

      CompleteStep withFields(@NonNull Set<? extends SootField> fields);
    }

    public interface ModifierStep {
      CompleteStep withModifier(@NonNull ClassModifier modifier);

      CompleteStep withModifiers(@NonNull Set<ClassModifier> modifiers);
    }

    public interface InterfaceStep {
      CompleteStep withInterface(@NonNull ClassType interfaceType);

      CompleteStep withInterfaces(@NonNull Set<? extends ClassType> interfaceTypes);
    }

    public interface SuperclassStep {
      CompleteStep withSuperclass(@NonNull Optional<? extends ClassType> superclass);
    }

    public interface OuterClassStep {
      CompleteStep withOuterClass(@NonNull Optional<? extends ClassType> outerClass);
    }

    public interface PositionStep {
      CompleteStep withPosition(@NonNull Position position);
    }

    public interface Build {
      SootClass build();
    }

    /** Concrete implementation of the step builder. */
    private static class Steps implements ClassSourceStep, SourceTypeStep, CompleteStep {
      private final JavaSootClassBuilder instance = new JavaSootClassBuilder();

      @Override
      public SourceTypeStep withClassSource(@NonNull SootClassSource classSource) {
        instance.classSource = (JavaSootClassSource) classSource;
        return this;
      }

      @Override
      public CompleteStep withSourceType(@NonNull SourceType sourceType) {
        instance.sourceType = sourceType;
        return this;
      }

      @Override
      public CompleteStep withMethod(@NonNull SootMethod method) {
        instance.methods = ImmutableSet.<SootMethod>builder().add(method).build();
        return this;
      }

      @Override
      public CompleteStep withMethods(@NonNull Set<? extends SootMethod> methods) {
        instance.methods = ImmutableSet.<SootMethod>builder().addAll(methods).build();
        return this;
      }

      @Override
      public CompleteStep withField(@NonNull SootField field) {
        instance.fields = ImmutableSet.<SootField>builder().add(field).build();
        return this;
      }

      @Override
      public CompleteStep withFields(@NonNull Set<? extends SootField> fields) {
        instance.fields = ImmutableSet.<SootField>builder().addAll(fields).build();
        return this;
      }

      @Override
      public CompleteStep withModifier(@NonNull ClassModifier modifier) {
        instance.modifiers = ImmutableSet.<ClassModifier>builder().add(modifier).build();
        return this;
      }

      @Override
      public CompleteStep withModifiers(@NonNull Set<ClassModifier> modifiers) {
        instance.modifiers = ImmutableSet.<ClassModifier>builder().addAll(modifiers).build();
        return this;
      }

      @Override
      public CompleteStep withInterface(@NonNull ClassType interfaceType) {
        instance.interfaces = ImmutableSet.<ClassType>builder().add(interfaceType).build();
        return this;
      }

      @Override
      public CompleteStep withInterfaces(@NonNull Set<? extends ClassType> interfaceTypes) {
        instance.interfaces = ImmutableSet.<ClassType>builder().addAll(interfaceTypes).build();
        return this;
      }

      @Override
      public CompleteStep withSuperclass(@NonNull Optional<? extends ClassType> superclass) {
        instance.superclass = superclass;
        return this;
      }

      @Override
      public CompleteStep withOuterClass(@NonNull Optional<? extends ClassType> outerClass) {
        instance.outerClass = outerClass;
        return this;
      }

      @Override
      public CompleteStep withPosition(@NonNull Position position) {
        instance.position = position;
        return this;
      }

      @Override
      public SootClass build() {
        if (instance.classSource != null && instance.sourceType != null) {
          return new JavaSootClass(instance.classSource, instance.sourceType);
        }
        return new JavaSootClass(
            instance.classSource,
            instance.sourceType,
            instance.methods,
            instance.fields,
            instance.modifiers,
            instance.interfaces,
            instance.superclass,
            instance.outerClass,
            instance.position);
      }
    }
  }

  @NonNull
  public Set<? extends SootField> lazyFieldInitializer() {
    Set<SootField> fields;
    try {
      fields = ImmutableUtils.immutableSetOf(this.classSource.resolveFields());
    } catch (ResolveException e) {
      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return fields;
  }

  @NonNull
  public Set<? extends SootMethod> lazyMethodInitializer() {
    Set<SootMethod> methods;
    try {
      methods = ImmutableUtils.immutableSetOf(this.classSource.resolveMethods());
    } catch (ResolveException e) {
      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return methods;
  }

  /** Does this class directly implement the given interface? (see getInterfaceCount()) */
  public boolean implementsInterface(@NonNull ClassType classSignature) {
    for (ClassType sc : getInterfaces()) {
      if (sc.equals(classSignature)) {
        return true;
      }
    }
    return false;
  }

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Does this class have a
   * superclass? False implies that this is the java.lang.Object class. Note that interfaces are
   * subclasses of the java.lang.Object class.
   */
  public boolean hasSuperclass() {
    return lazySuperclass.get().isPresent();
  }

  public boolean hasOuterClass() {
    return lazyOuterClass.get().isPresent();
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Convenience method; returns true if this class is an interface. */
  public boolean isInterface() {
    return ClassModifier.isInterface(this.getModifiers());
  }

  /** Convenience method; returns true if this class is an enumeration. */
  public boolean isEnum() {
    return ClassModifier.isEnum(this.getModifiers());
  }

  /** Convenience method; returns true if this class is synchronized. */
  public boolean isSuper() {
    return ClassModifier.isSuper(this.getModifiers());
  }

  /** Returns true if this class is not an interface and not abstract. */
  public boolean isConcrete() {
    return !isInterface() && !isAbstract();
  }

  /** Convenience method; returns true if this class is public. */
  public boolean isPublic() {
    return ClassModifier.isPublic(this.getModifiers());
  }

  /** Returns the name of this class. */
  @Override
  @NonNull
  public String toString() {
    return classSignature.toString();
  }

  /** Returns the serialized Jimple of this SootClass as String */
  @NonNull
  public String print() {
    StringWriter output = new StringWriter();
    JimplePrinter p = new JimplePrinter();
    p.printTo(this, new PrintWriter(output));
    return output.toString();
  }

  /** Returns true if this class is an application class. */
  public boolean isApplicationClass() {
    return sourceType == SourceType.Application;
  }

  /** Returns true if this class is a library class. */
  public boolean isLibraryClass() {
    return sourceType == SourceType.Library;
  }

  /** Convenience method returning true if this class is private. */
  public boolean isPrivate() {
    return ClassModifier.isPrivate(this.getModifiers());
  }

  /** Convenience method returning true if this class is protected. */
  public boolean isProtected() {
    return ClassModifier.isProtected(this.getModifiers());
  }

  /** Convenience method returning true if this class is abstract. */
  public boolean isAbstract() {
    return ClassModifier.isAbstract(this.getModifiers());
  }

  /** Convenience method returning true if this class is final. */
  public boolean isFinal() {
    return ClassModifier.isFinal(this.getModifiers());
  }

  /** Convenience method returning true if this class is static. */
  public boolean isStatic() {
    return ClassModifier.isStatic(this.getModifiers());
  }

  public boolean isAnnotation() {
    return ClassModifier.isAnnotation(this.getModifiers());
  }

  @NonNull
  @Override
  public Position getPosition() {
    return lazyPosition.get();
  }

  @Override
  @NonNull
  public String getName() {
    return this.classSignature.getFullyQualifiedName();
  }

  @NonNull
  public SootClass withClassSource(@NonNull JavaSootClassSource classSource) {
    return new JavaSootClass(classSource, sourceType);
  }

  @NonNull
  public SootClass withSourceType(@NonNull SourceType sourceType) {
    return new JavaSootClass(classSource, sourceType);
  }
}
