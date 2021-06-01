package de.upb.swt.soot.core.model;

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
import com.google.common.collect.ImmutableList;
import de.upb.swt.soot.core.frontend.BodySource;
import de.upb.swt.soot.core.frontend.OverridingBodySource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Soot's counterpart of the source language's method concept. Soot representation of a Java method.
 * Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a
 * Body.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootMethod extends SootClassMember<MethodSignature> implements Method, Copyable {

  /**
   * An array of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  @Nonnull protected final ImmutableList<Type> parameterTypes;

  /** Declared exceptions thrown by this method. Created upon demand. */
  @Nonnull protected final ImmutableList<ClassType> exceptions;

  /** Tells this method how to find out where its body lives. */
  @Nonnull protected final BodySource bodySource;

  /** Constructs a SootMethod object with the given attributes. */
  public SootMethod(
      @Nonnull BodySource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      @Nonnull Position position) {
    super(methodSignature, modifiers, position);

    this.bodySource = source;
    this.parameterTypes = ImmutableUtils.immutableListOf(methodSignature.getParameterTypes());
    this.exceptions = ImmutableUtils.immutableListOf(thrownExceptions);
  }

  @Nonnull
  private Body lazyBodyInitializer() {
    if (!isConcrete()) {
      throw new ResolveException(
          "There is no corresponding body if the method is not concrete i.e."
              + getSignature()
              + " is abstract or native.",
          Paths.get(""));
    }

    try {
      return bodySource.resolveBody(getModifiers());
    } catch (ResolveException | IOException e) {
      throw new ResolveException(
          "Could not resolve a corresponding body for " + getSignature(), Paths.get(""), e);
    }
  }

  /** Returns true if this method is not abstract or native, i.e. this method can have a body. */
  public boolean isConcrete() {
    return !isAbstract() && !isNative();
  }

  @Nonnull
  public Type getReturnType() {
    return getSignature().getType();
  }

  /** Returns the number of parameters taken by this method. */
  public int getParameterCount() {
    return parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this method. */
  @Nonnull
  public Type getParameterType(int n) {
    return parameterTypes.get(n);
  }

  /** Returns a read-only list of the parameter types of this method. */
  @Nonnull
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

  @Nonnull private final Supplier<Body> _lazyBody = Suppliers.memoize(this::lazyBodyInitializer);

  /** Retrieves the active body for this method. */
  @Nonnull
  public Body getBody() {
    return this._lazyBody.get();
  }

  /** Returns true if this method has a body. */
  public boolean hasBody() {
    return isConcrete();
  }

  @Nonnull
  public List<ClassType> getExceptionSignatures() {
    return exceptions;
  }

  /** Convenience method returning true if this method is abstract. */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /** Convenience method returning true if this method is native. */
  public boolean isNative() {
    return Modifier.isNative(this.getModifiers());
  }

  /** Convenience method returning true if this method is synchronized. */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(this.getModifiers());
  }

  /** @return yes if this is the main method */
  public boolean isMain() {
    return isPublic()
        && isStatic()
        && getSignature().getSubSignature().toString().equals("void main(java.lang.String[])");
  }

  /** We rely on the JDK class recognition to decide if a method is JDK method. */
  public boolean isBuiltInMethod() {
    return getSignature().getDeclClassType().isBuiltInClass();
  }

  /**
   * Returns the declaration of this method, as used at the top of textual body representations
   * (before the {}'s containing the code for representation.)
   */
  public void toString(@Nonnull StmtPrinter printer) {

    // print modifiers
    final Set<Modifier> modifiers = getModifiers();
    printer.modifier(Modifier.toString(modifiers));
    if (modifiers.size() != 0) {
      printer.literal(" ");
    }

    // print returnType + name + ( parameterList )
    final MethodSubSignature subSignature = getSignature().getSubSignature();
    subSignature.toString(printer);

    // Print exceptions
    Iterator<ClassType> exceptionIt = getExceptionSignatures().iterator();
    if (exceptionIt.hasNext()) {
      printer.literal(" throws ");
      printer.typeSignature(exceptionIt.next());

      while (exceptionIt.hasNext()) {
        printer.literal(", ");
        printer.typeSignature(exceptionIt.next());
      }
    }
  }

  /**
   * Creates a new SootMethod based on a new {@link OverridingBodySource}. This is useful to change
   * selected parts of a {@link SootMethod} without recreating a {@link BodySource} completely.
   * {@link OverridingBodySource} allows for replacing the body of a method.
   */
  @Nonnull
  public SootMethod withOverridingMethodSource(
      Function<OverridingBodySource, OverridingBodySource> overrider) {
    return new SootMethod(
        overrider.apply(new OverridingBodySource(bodySource)),
        getSignature(),
        getModifiers(),
        exceptions,
        getPosition());
  }

  @Nonnull
  public SootMethod withSource(BodySource source) {
    return new SootMethod(source, getSignature(), getModifiers(), exceptions, getPosition());
  }

  @Nonnull
  public SootMethod withModifiers(Iterable<Modifier> modifiers) {
    return new SootMethod(
        bodySource, getSignature(), modifiers, getExceptionSignatures(), getPosition());
  }

  @Nonnull
  public SootMethod withThrownExceptions(Iterable<ClassType> thrownExceptions) {
    return new SootMethod(
        bodySource, getSignature(), getModifiers(), thrownExceptions, getPosition());
  }

  @Nonnull
  public SootMethod withBody(@Nonnull Body body) {
    return new SootMethod(
        new OverridingBodySource(bodySource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions,
        getPosition());
  }

  /**
   * Creates a builder for {@link SootMethod}s.
   *
   * @return A {@link SootMethodBuilder}.
   */
  @Nonnull
  public static MethodSourceStep builder() {
    return new SootMethodBuilder();
  }

  public interface MethodSourceStep {
    @Nonnull
    SignatureStep withSource(@Nonnull BodySource value);
  }

  public interface SignatureStep {
    @Nonnull
    ModifierStep withSignature(@Nonnull MethodSignature value);
  }

  public interface ModifierStep {
    @Nonnull
    ThrownExceptionsStep withModifier(@Nonnull Iterable<Modifier> modifier);

    @Nonnull
    default ThrownExceptionsStep withModifiers(@Nonnull Modifier first, @Nonnull Modifier... rest) {
      return withModifier(EnumSet.of(first, rest));
    }
  }

  public interface ThrownExceptionsStep {
    @Nonnull
    BuildStep withThrownExceptions(@Nonnull Iterable<ClassType> value);

    @Nonnull
    SootMethod build();
  }

  public interface BuildStep {
    @Nonnull
    SootMethod build();

    @Nonnull
    BuildStep withPosition(Position position);
  }

  /**
   * Defines a {@link SootMethod} builder that provides a fluent API.
   *
   * @author Jan Martin Persch
   */
  public static class SootMethodBuilder
      implements MethodSourceStep, SignatureStep, ModifierStep, ThrownExceptionsStep, BuildStep {

    @Nullable private BodySource source;
    @Nonnull private Iterable<Modifier> modifiers = Collections.emptyList();
    @Nullable private MethodSignature methodSignature;
    @Nonnull private Iterable<ClassType> thrownExceptions = Collections.emptyList();
    @Nonnull private Position position = NoPositionInformation.getInstance();

    @Nonnull
    protected Iterable<Modifier> getModifiers() {
      return modifiers;
    }

    @Nullable
    protected BodySource getSource() {
      return source;
    }

    @Nullable
    protected MethodSignature getSignature() {
      return methodSignature;
    }

    @Nonnull
    public Position getPosition() {
      return position;
    }

    @Nonnull
    protected Iterable<ClassType> getThrownExceptions() {
      return thrownExceptions;
    }

    @Override
    @Nonnull
    public SignatureStep withSource(@Nonnull BodySource source) {
      this.source = source;
      return this;
    }

    @Override
    @Nonnull
    public ModifierStep withSignature(@Nonnull MethodSignature methodSignature) {
      this.methodSignature = methodSignature;
      return this;
    }

    @Override
    @Nonnull
    public ThrownExceptionsStep withModifier(@Nonnull Iterable<Modifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    @Nonnull
    public BuildStep withThrownExceptions(@Nonnull Iterable<ClassType> thrownExceptions) {
      this.thrownExceptions = thrownExceptions;
      return this;
    }

    @Nonnull
    public BuildStep withPosition(@Nonnull Position position) {
      this.position = position;
      return this;
    }

    @Override
    @Nonnull
    public SootMethod build() {
      // nonnull is enforced by stepwise builder pattern - at least if s.o. doesn't force a null
      // value as parameter
      return new SootMethod(
          getSource(), getSignature(), getModifiers(), getThrownExceptions(), position);
    }
  }
}
