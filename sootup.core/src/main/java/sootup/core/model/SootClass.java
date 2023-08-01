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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
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
public class SootClass<S extends SootClassSource<? extends SootClass<S>>> extends AbstractClass<S> {

  @Nonnull protected final SourceType sourceType;
  @Nonnull protected final ClassType classSignature;

  public SootClass(@Nonnull S classSource, @Nonnull SourceType sourceType) {
    super(classSource);
    this.sourceType = sourceType;
    this.classSignature = classSource.getClassType();
  }

  @Nonnull
  private Set<? extends SootField> lazyFieldInitializer() {
    Set<? extends SootField> fields;

    try {
      fields = ImmutableUtils.immutableSetOf(this.classSource.resolveFields());
    } catch (ResolveException e) {
      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return fields;
  }

  @Nonnull
  private Set<SootMethod> lazyMethodInitializer() {
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

  @Nonnull
  private final Supplier<Set<? extends SootMethod>> _lazyMethods =
      Suppliers.memoize(this::lazyMethodInitializer);

  /** Gets the {@link Method methods} of this {@link SootClass} in an immutable set. */
  @Nonnull
  public Set<? extends SootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  @Nonnull
  private final Supplier<Set<? extends SootField>> _lazyFields =
      Suppliers.memoize(this::lazyFieldInitializer);

  /** Gets the {@link Field fields} of this {@link SootClass} in an immutable set. */
  @Override
  @Nonnull
  public Set<? extends SootField> getFields() {
    return this._lazyFields.get();
  }

  private final Supplier<Set<ClassModifier>> lazyModifiers =
      Suppliers.memoize(classSource::resolveModifiers);

  /** Returns the modifiers of this class in an immutable set. */
  @Nonnull
  public Set<ClassModifier> getModifiers() {
    return lazyModifiers.get();
  }

  private final Supplier<Set<? extends ClassType>> lazyInterfaces =
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
  public boolean implementsInterface(@Nonnull ClassType classSignature) {
    for (ClassType sc : getInterfaces()) {
      if (sc.equals(classSignature)) {
        return true;
      }
    }
    return false;
  }

  private final Supplier<Optional<? extends ClassType>> lazySuperclass =
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

  private final Supplier<Optional<? extends ClassType>> lazyOuterClass =
      Suppliers.memoize(classSource::resolveOuterClass);

  public boolean hasOuterClass() {
    return lazyOuterClass.get().isPresent();
  }

  /** This method returns the outer class. */
  @Nonnull
  public Optional<? extends ClassType> getOuterClass() {
    return lazyOuterClass.get();
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Returns the ClassSignature of this class. */
  @Nonnull
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
  @Nonnull
  public String toString() {
    return classSignature.toString();
  }

  /** Returns the serialized Jimple of this SootClass as String */
  @Nonnull
  public String print() {
    StringWriter output = new StringWriter();
    JimplePrinter p = new JimplePrinter();
    p.printTo(this, new PrintWriter(output));
    return output.toString();
  }

  /** Returns true if this class is an application class. */
  public boolean isApplicationClass() {
    return sourceType.equals(SourceType.Application);
  }

  /** Returns true if this class is a library class. */
  public boolean isLibraryClass() {
    return sourceType.equals(SourceType.Library);
  }

  /** Returns true if this class is a phantom class. */
  public boolean isPhantomClass() {
    return sourceType.equals(SourceType.Phantom);
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

  private final Supplier<Position> lazyPosition = Suppliers.memoize(classSource::resolvePosition);

  @Nonnull
  public Position getPosition() {
    return lazyPosition.get();
  }

  @Nonnull
  @Override
  public S getClassSource() {
    return classSource;
  }

  @Override
  @Nonnull
  public String getName() {
    return this.classSignature.getFullyQualifiedName();
  }

  @Nonnull
  public SootClass<S> withClassSource(@Nonnull S classSource) {
    return new SootClass<S>(classSource, sourceType);
  }

  @Nonnull
  public SootClass<S> withSourceType(@Nonnull SourceType sourceType) {
    return new SootClass<S>(classSource, sourceType);
  }
}
