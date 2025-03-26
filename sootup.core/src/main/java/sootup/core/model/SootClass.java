package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Jan Martin Persch and others
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.JimplePrinter;

/**
 * Soot's counterpart of the source languages class concept. Soot representation of a Java class.
 * They are usually created by a Scene, but can also be constructed manually through the given
 * constructors.
 *
 * @author Manuel Benz
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootClass extends AbstractClass implements HasPosition {

  @NonNull protected final SourceType sourceType;
  @NonNull protected final ClassType classSignature;

  public SootClass(@NonNull SootClassSource classSource, @NonNull SourceType sourceType) {
    super(classSource);
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
  }

  private SootClass(
      SootClassSource classSource,
      SourceType sourceType,
      Set<? extends SootMethod> methods,
      Set<? extends SootField> fields,
      Set<ClassModifier> modifiers,
      Set<? extends ClassType> interfaces,
      Optional<? extends ClassType> superclass,
      Optional<? extends ClassType> outerClass,
      Position position) {
    super(classSource);
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
    this._lazyMethods = Suppliers.ofInstance(methods);
    this._lazyFields = Suppliers.ofInstance(fields);
    this.lazyModifiers = Suppliers.ofInstance(modifiers);
    this.lazyInterfaces = Suppliers.ofInstance(interfaces);
    this.lazySuperclass = Suppliers.ofInstance(superclass);
    this.lazyOuterClass = Suppliers.ofInstance(outerClass);
    this.lazyPosition = Suppliers.ofInstance(position);
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
  private Set<? extends SootMethod> lazyMethodInitializer() {
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

  @NonNull
  private Supplier<Set<? extends SootMethod>> _lazyMethods =
      Suppliers.memoize(this::lazyMethodInitializer);

  /** Gets the {@link Method methods} of this {@link SootClass} in an immutable set. */
  @NonNull
  public Set<? extends SootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  @NonNull
  private Supplier<Set<? extends SootField>> _lazyFields =
      Suppliers.memoize(this::lazyFieldInitializer);

  /** Gets the {@link Field fields} of this {@link SootClass} in an immutable set. */
  @Override
  @NonNull
  public Set<? extends SootField> getFields() {
    return this._lazyFields.get();
  }

  private Supplier<Set<ClassModifier>> lazyModifiers =
      Suppliers.memoize(classSource::resolveModifiers);

  /** Returns the modifiers of this class in an immutable set. */
  @NonNull
  public Set<ClassModifier> getModifiers() {
    return lazyModifiers.get();
  }

  private Supplier<Set<? extends ClassType>> lazyInterfaces =
      Suppliers.memoize(classSource::resolveInterfaces);

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

  private Supplier<Optional<? extends ClassType>> lazySuperclass =
      Suppliers.memoize(classSource::resolveSuperclass);

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
  public Optional<? extends ClassType> getSuperclass() {
    return lazySuperclass.get();
  }

  private Supplier<Optional<? extends ClassType>> lazyOuterClass =
      Suppliers.memoize(classSource::resolveOuterClass);

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
  @NonNull
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

  private Supplier<Position> lazyPosition = Suppliers.memoize(classSource::resolvePosition);

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
  public SootClass withClassSource(@NonNull SootClassSource classSource) {
    return new SootClass(classSource, sourceType);
  }

  @NonNull
  public SootClass withSourceType(@NonNull SourceType sourceType) {
    return new SootClass(classSource, sourceType);
  }

  /** Defines a {@link SootClass} builder. */
  public static class SootClassBuilder {
    @Nullable private SootClassSource classSource;
    @Nullable private SourceType sourceType;
    @Nullable private Set<? extends SootMethod> methods = ImmutableSet.of();
    @Nullable private Set<? extends SootField> fields = ImmutableSet.of();
    @Nullable private Set<ClassModifier> modifiers = ImmutableSet.of();
    @Nullable private Set<? extends ClassType> interfaces = ImmutableSet.of();
    @Nullable private Optional<? extends ClassType> superclass = Optional.empty();
    @Nullable private Optional<? extends ClassType> outerClass = Optional.empty();
    @Nullable private Position position;

    private SootClassBuilder() {}

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
      private final SootClassBuilder instance = new SootClassBuilder();

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
          return new SootClass(instance.classSource, instance.sourceType);
        }
        return new SootClass(
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
}
