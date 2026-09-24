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
import com.google.common.collect.Iterables;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

  final SootClassSource classSource;
  final SourceType sourceType;
  final ClassType classSignature;

  private final Supplier<Set<ClassModifier>> lazyModifiers;
  private final Supplier<Set<? extends ClassType>> lazyInterfaces;
  private final Supplier<Optional<? extends ClassType>> lazySuperclass;
  private final Supplier<Optional<? extends ClassType>> lazyOuterClass;
  private final Supplier<Position> lazyPosition;

  @NonNull private final Supplier<Set<JavaSootMethod>> _lazyMethods;

  @NonNull private final Supplier<Set<? extends SootField>> _lazyFields;

  @NonNull
  private final Supplier<Map<MethodSubSignature, JavaSootMethod>> _lazyMethodBySubSignature;

  public JavaSootClass(SootClassSource classSource, SourceType sourceType) {
    this.classSource = classSource;
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
    this.lazyModifiers = Suppliers.memoize(classSource::resolveModifiers);
    this.lazyInterfaces = Suppliers.memoize(classSource::resolveInterfaces);
    this.lazySuperclass = Suppliers.memoize(classSource::resolveSuperclass);
    this.lazyOuterClass = Suppliers.memoize(classSource::resolveOuterClass);
    this.lazyPosition = Suppliers.memoize(classSource::resolvePosition);
    this._lazyMethods = Suppliers.memoize(this::lazyMethodInitializer);
    this._lazyFields = Suppliers.memoize(this::lazyFieldInitializer);
    this._lazyMethodBySubSignature =
        Suppliers.memoize(() -> buildMethodBySubSignatureMap(getMethods()));
  }

  public JavaSootClass(
      SootClassSource classSource,
      SourceType sourceType,
      Set<JavaSootMethod> methods,
      Set<? extends SootField> fields,
      Set<ClassModifier> modifiers,
      Set<? extends ClassType> interfaces,
      Optional<? extends ClassType> superclass,
      Optional<? extends ClassType> outerClass,
      Position position) {
    this.classSource = classSource;
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
    this._lazyMethods = Suppliers.ofInstance(methods);
    this._lazyFields = Suppliers.ofInstance(fields);
    this.lazyModifiers = Suppliers.ofInstance(modifiers);
    this.lazyInterfaces = Suppliers.ofInstance(interfaces);
    this.lazySuperclass = Suppliers.ofInstance(superclass);
    this.lazyOuterClass = Suppliers.ofInstance(outerClass);
    this.lazyPosition = Suppliers.ofInstance(position);
    this._lazyMethodBySubSignature = Suppliers.memoize(() -> buildMethodBySubSignatureMap(methods));
  }

  @NonNull
  private static Map<MethodSubSignature, JavaSootMethod> buildMethodBySubSignatureMap(
      @NonNull Set<JavaSootMethod> methods) {
    Map<MethodSubSignature, JavaSootMethod> map = new HashMap<>(methods.size() * 2);
    for (JavaSootMethod method : methods) {
      map.put(method.getSignature().getSubSignature(), method);
    }
    return Collections.unmodifiableMap(map);
  }

  @Override
  @NonNull
  public Optional<JavaSootMethod> getMethod(@NonNull MethodSubSignature subSignature) {
    return Optional.ofNullable(_lazyMethodBySubSignature.get().get(subSignature));
  }

  @Override
  @NonNull
  public Optional<JavaSootField> getField(@NonNull FieldSubSignature subSignature) {
    return getFields().stream()
        .filter(f -> f.getSignature().getSubSignature().equals(subSignature))
        .findAny();
  }

  @Override
  @NonNull
  public Optional<JavaSootField> getField(@NonNull String name) {
    return getFields().stream()
        .filter(field -> field.getSignature().getName().equals(name))
        .reduce(
            (l, r) -> {
              throw new ResolveException(
                  "ambiguous field: " + name + " in " + getClassSource().getClassType(),
                  getClassSource().getSourcePath());
            });
  }

  @Override
  @NonNull
  public Optional<JavaSootMethod> getMethod(
      @NonNull String name, @NonNull Iterable<? extends Type> parameterTypes) {
    return this.getMethods().stream()
        .filter(
            method ->
                method.getSignature().getName().equals(name)
                    && Iterables.elementsEqual(parameterTypes, method.getParameterTypes()))
        .reduce(
            (l, r) -> {
              throw new ResolveException(
                  "ambiguous method: " + name + " in " + getClassSource().getClassType(),
                  getClassSource().getSourcePath());
            });
  }

  @Override
  @NonNull
  public Set<JavaSootMethod> getMethodsByName(@NonNull String name) {
    return this.getMethods().stream()
        .filter(m -> m.getSignature().getName().equals(name))
        .collect(Collectors.toSet());
  }

  @NonNull
  private Set<? extends SootField> lazyFieldInitializer() {
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
  private Set<JavaSootMethod> lazyMethodInitializer() {
    Set<JavaSootMethod> methods;

    try {
      methods =
          (Set<JavaSootMethod>) ImmutableUtils.immutableSetOf(this.classSource.resolveMethods());
    } catch (ResolveException e) {
      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return methods;
  }

  /** Gets the {@link Method methods} of this {@link SootClass} in an immutable set. */
  @NonNull
  public Set<JavaSootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  /** Gets the {@link Field fields} of this {@link SootClass} in an immutable set. */
  @Override
  @NonNull
  public Set<JavaSootField> getFields() {
    return this._lazyFields.get().stream()
        .map(sootField -> (JavaSootField) sootField)
        .collect(Collectors.toSet());
  }

  /** Returns the modifiers of this class in an immutable set. */
  @NonNull
  public Set<ClassModifier> getModifiers() {
    return lazyModifiers.get();
  }

  /**
   * Returns a backed Chain of the interfaces that are directly implemented by this class. Note that
   * direct implementation corresponds to an "implements" keyword in the Java class file and that
   * this class may still be implementing additional interfaces in the usual sense by being a
   * subclass of a class which directly implements some interfaces.
   */
  public Set<? extends ClassType> getInterfaces() {
    return lazyInterfaces.get();
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

  /**
   * WARNING: interfaces in Java are subclasses of the java.lang.Object class! Returns the
   * superclass of this class. (see hasSuperclass())
   */
  public Optional<JavaClassType> getSuperclass() {
    return (Optional<JavaClassType>) lazySuperclass.get();
  }

  public boolean hasOuterClass() {
    return lazyOuterClass.get().isPresent();
  }

  /** This method returns the outer class. */
  @NonNull
  public Optional<? extends ClassType> getOuterClass() {
    return lazyOuterClass.get();
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Returns the ClassSignature of this class. */
  @Override
  public ClassType getType() {
    return classSignature;
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

  @NonNull
  @Override
  public SootClassSource getClassSource() {
    return classSource;
  }

  @Override
  @NonNull
  public String getName() {
    return this.classSignature.getFullyQualifiedName();
  }

  @NonNull
  public JavaSootClass withClassSource(@NonNull SootClassSource classSource) {
    return new JavaSootClass(classSource, sourceType);
  }

  @NonNull
  public JavaSootClass withSourceType(@NonNull SourceType sourceType) {
    return new JavaSootClass(classSource, sourceType);
  }

  @NonNull
  public JavaSootClass withMethods(@NonNull Collection<JavaSootMethod> methods) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource()).withMethods(methods),
        sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedField(
      @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withReplacedField(toReplace, replacement),
        sourceType);
  }

  @NonNull
  public JavaSootClass withFields(@NonNull Collection<JavaSootField> fields) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource()).withFields(fields),
        sourceType);
  }

  @NonNull
  public JavaSootClass withModifiers(@NonNull Set<ClassModifier> modifiers) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withModifiers(modifiers),
        sourceType);
  }

  @NonNull
  public JavaSootClass withReplacedMethod(
      @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withSuperclass(@NonNull Optional<JavaClassType> superclass) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withSuperclass(superclass),
        sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public JavaSootClass withOuterClass(@NonNull Optional<JavaClassType> outerClass) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withOuterClass(outerClass),
        sourceType);
  }

  @NonNull
  public JavaSootClass withPosition(@Nullable Position position) {
    return new JavaSootClass(
        new OverridingJavaClassSource((JavaSootClassSource) getClassSource())
            .withPosition(position),
        sourceType);
  }

  /**
   * Get all annotations that are directly attached to this class.
   *
   * <p>This includes "visible" and "invisible" annotations. Note that inherited annotations are not
   * part of this iterable.
   */
  @Override
  public Iterable<AnnotationUsage> getAnnotations() {
    // we should cache it in the future: for now, we do not cache it
    // because the underlying data structure might be mutable
    return ((JavaSootClassSource) classSource).resolveAnnotations();
  }

  /**
   * Resturns the default values of methods in Annotations
   *
   * @return a Map mapping the method name to the default value if the SootClass is no Annotation,
   *     it will return an empty map.
   */
  @NonNull
  public Map<String, Object> getAnnotationDefaultValues() {
    if (isAnnotation()) {
      return getMethods().stream()
          .<ImmutablePair<String, Object>>mapMulti(
              (javaSootMethod, consumer) ->
                  javaSootMethod
                      .getDefaultValue()
                      .ifPresent(
                          o -> consumer.accept(new ImmutablePair<>(javaSootMethod.getName(), o))))
          .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
    }
    return Map.of();
  }

  /** Defines a {@link SootClass} builder. */
  public static class JavaSootClassBuilder {
    @Nullable private SootClassSource classSource;
    @Nullable private SourceType sourceType;
    @Nullable private ClassType classSignature;
    @Nullable private Set<JavaSootMethod> methods = ImmutableSet.of();
    @Nullable private Set<JavaSootField> fields = ImmutableSet.of();
    @Nullable private Set<ClassModifier> modifiers = ImmutableSet.of();
    @Nullable private Set<? extends ClassType> interfaces = ImmutableSet.of();
    @Nullable private Optional<? extends ClassType> superclass = Optional.empty();
    @Nullable private Optional<? extends ClassType> outerClass = Optional.empty();
    @Nullable private Position position;

    private JavaSootClassBuilder() {}

    public static ClassSourceStep builder() {
      return new JavaSootClassBuilder.Steps();
    }

    /** Step interface for setting the class source. */
    public interface ClassSourceStep {
      SourceTypeStep withClassSource(@NonNull SootClassSource classSource);
    }

    /** Step interface for setting the source type. */
    public interface SourceTypeStep {
      CompleteStep withSourceType(@NonNull SourceType sourceType);
    }

    public interface ClassTypeStep {
      CompleteStep withClassType(@NonNull ClassType classType);
    }

    public interface MethodStep {
      CompleteStep withMethod(@NonNull JavaSootMethod method);

      CompleteStep withMethods(@NonNull Set<JavaSootMethod> methods);
    }

    public interface FieldStep {
      CompleteStep withField(@NonNull JavaSootField field);

      CompleteStep withFields(@NonNull Set<JavaSootField> fields);
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
      JavaSootClass build();
    }

    /** Interface that accumulates all possible methods. */
    public interface CompleteStep
        extends ClassTypeStep,
            InterfaceStep,
            MethodStep,
            FieldStep,
            ModifierStep,
            SuperclassStep,
            OuterClassStep,
            PositionStep,
            Build {}

    /** Concrete implementation of the step builder. */
    private static class Steps implements ClassSourceStep, SourceTypeStep, CompleteStep {
      private final JavaSootClassBuilder instance = new JavaSootClassBuilder();

      @Override
      public SourceTypeStep withClassSource(@NonNull SootClassSource classSource) {
        instance.classSource = classSource;
        return this;
      }

      @Override
      public CompleteStep withSourceType(@NonNull SourceType sourceType) {
        instance.sourceType = sourceType;
        return this;
      }

      @Override
      public CompleteStep withMethod(@NonNull JavaSootMethod method) {
        instance.methods = ImmutableSet.<JavaSootMethod>builder().add(method).build();
        return this;
      }

      public CompleteStep withMethods(@NonNull Set<JavaSootMethod> methods) {
        instance.methods = ImmutableSet.<JavaSootMethod>builder().addAll(methods).build();
        return this;
      }

      @Override
      public CompleteStep withField(@NonNull JavaSootField field) {
        instance.fields = ImmutableSet.<JavaSootField>builder().add(field).build();
        return this;
      }

      @Override
      public CompleteStep withFields(@NonNull Set<JavaSootField> fields) {
        instance.fields = ImmutableSet.<JavaSootField>builder().addAll(fields).build();
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
      public CompleteStep withClassType(@NonNull ClassType classType) {
        instance.classSignature = classType;
        return this;
      }

      @Override
      public JavaSootClass build() {
        if (instance.classSource != null && instance.sourceType != null) {
          return new JavaSootClass(instance.classSource, instance.sourceType);
        }
        assert instance.classSource != null;
        return new JavaSootClass(
            instance.classSource,
            null,
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
}
