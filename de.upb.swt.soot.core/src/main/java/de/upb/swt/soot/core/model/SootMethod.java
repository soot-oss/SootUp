package de.upb.swt.soot.core.model;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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
import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.ImmutableUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Soot's counterpart of the source language's method concept. Soot representation of a Java method.
 * Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a
 * Body. The getBody() method points to the currently-active body.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootMethod extends SootClassMember<MethodSignature> implements Method, Copyable {

  /**
   * An array of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  @Nonnull protected final ImmutableList<Type> parameterTypes;

  /** Declared exceptions thrown by this methodRef. Created upon demand. */
  @Nonnull protected final ImmutableList<ClassType> exceptions;

  /** Tells this methodRef how to find out where its body lives. */
  @Nonnull protected final MethodSource methodSource;

  /** Constructs a SootMethod object with the given attributes. */
  public SootMethod(
      @Nonnull MethodSource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions) {
    super(methodSignature, modifiers);

    this.methodSource = source;
    this.parameterTypes = ImmutableUtils.immutableListOf(methodSignature.getParameterSignatures());
    this.exceptions = ImmutableUtils.immutableListOf(thrownExceptions);
  }

  @Nullable
  private Body lazyBodyInitializer() {
    if (!isConcrete()) return null;

    Body body;
    try {
      body = this.methodSource.resolveBody();

      if (body != null) {
        body.setMethod(this);
      }
    } catch (ResolveException e) {
      body = null;

      // TODO: [JMP] Exception handling
      e.printStackTrace();
    }

    return body;
  }

  @Nonnull
  @Override
  public MethodSubSignature getSubSignature() {
    return (MethodSubSignature) super.getSubSignature();
  }

  /** Returns true if this method is not abstract or native, i.e. this method can have a body. */
  public boolean isConcrete() {
    return !isAbstract() && !isNative();
  }

  public Type getReturnTypeSignature() {
    return this.getSignature().getType();
  }

  /** Returns the number of parameters taken by this method. */
  public int getParameterCount() {
    return parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this method. */
  public Type getParameterType(int n) {
    return parameterTypes.get(n);
  }

  /** Returns a read-only list of the parameter types of this methodRef. */
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

  private final @Nonnull Supplier<Body> _lazyBody = Suppliers.memoize(this::lazyBodyInitializer);

  /** Retrieves the active body for this methodRef. */
  @Nullable
  public Body getBody() {
    return this._lazyBody.get(); // TODO: [JMP] Refactor to return `.getAsOptional()`
  }

  /** Returns true if this method has an active body. */
  public boolean hasBody() {
    return this.getBody() != null;
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
        && this.getSubSignature().toString().equals("void main(java.lang.String[])");
  }

  /** We rely on the JDK class recognition to decide if a method is JDK method. */
  public boolean isBuiltInMethod() {
    return getSignature().getDeclClassType().isBuiltInClass();
  }

  /**
   * Returns the declaration of this method, as used at the top of textual body representations
   * (before the {}'s containing the code for representation.)
   */
  public String getDeclaration() {
    StringBuilder builder = new StringBuilder();

    // modifiers
    StringTokenizer st = new StringTokenizer(Modifier.toString(this.getModifiers()));
    if (st.hasMoreTokens()) {
      builder.append(st.nextToken());
    }

    while (st.hasMoreTokens()) {
      builder.append(" ").append(st.nextToken());
    }

    if (builder.length() != 0) {
      builder.append(" ");
    }

    // return type + name

    builder.append(this.getSubSignature().toString());

    // Print exceptions
    Iterator<ClassType> exceptionIt = this.getExceptionSignatures().iterator();

    if (exceptionIt.hasNext()) {
      builder.append(" throws ").append(exceptionIt.next());

      while (exceptionIt.hasNext()) {
        builder.append(", ").append(exceptionIt.next());
      }
    }

    return builder.toString().intern();
  }

  /**
   * Creates a new SootMethod based on a new {@link OverridingMethodSource}. This is useful to
   * change selected parts of a {@link SootMethod} without recreating a {@link MethodSource}
   * completely. {@link OverridingMethodSource} allows for replacing the body of a method.
   */
  @Nonnull
  public SootMethod withOverridingMethodSource(
      Function<OverridingMethodSource, OverridingMethodSource> overrider) {
    return new SootMethod(
        overrider.apply(new OverridingMethodSource(methodSource)),
        getSignature(),
        getModifiers(),
        exceptions);
  }

  @Nonnull
  public SootMethod withSource(MethodSource source) {
    return new SootMethod(source, getSignature(), getModifiers(), exceptions);
  }

  @Nonnull
  public SootMethod withModifiers(Iterable<Modifier> modifiers) {
    return new SootMethod(methodSource, getSignature(), getModifiers(), exceptions);
  }

  @Nonnull
  public SootMethod withThrownExceptions(Iterable<ClassType> thrownExceptions) {
    return new SootMethod(methodSource, getSignature(), getModifiers(), thrownExceptions);
  }

  @Nonnull
  public SootMethod withBody(@Nullable Body body) {
    return new SootMethod(
        new OverridingMethodSource(methodSource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions);
  }

  /** @see OverridingMethodSource#withBodyStmts(Consumer) */
  @Nonnull
  public SootMethod withBodyStmts(Consumer<List<Stmt>> stmtModifier) {
    return new SootMethod(
        new OverridingMethodSource(methodSource).withBodyStmts(stmtModifier),
        getSignature(),
        getModifiers(),
        exceptions);
  }

  /**
   * Creates a {@link SootMethod} builder.
   *
   * @return A {@link SootMethod} builder.
   */
  @Nonnull
  public static Builder.MethodSourceStep builder() {
    return new SootMethodBuilder();
  }

  /**
   * Defines a stepwise builder for the {@link SootMethod} class.
   *
   * @see #builder()
   * @author Jan Martin Persch
   */
  public interface Builder extends SootClassMember.Builder<MethodSignature, SootMethod> {
    interface MethodSourceStep {
      /**
       * Sets the {@link MethodSource}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      MethodSignatureStep withSource(@Nonnull MethodSource value);
    }

    interface MethodSignatureStep {
      /**
       * Sets the {@link MethodSignature}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ModifiersStep withSignature(@Nonnull MethodSignature value);
    }

    interface ModifiersStep extends SootClassMember.Builder.ModifiersStep<ThrownExceptionsStep> {}

    interface ThrownExceptionsStep extends Builder {
      /**
       * Sets the exceptions thrown by the method to build. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      Builder withThrownExceptions(@Nonnull Iterable<ClassType> value);
    }

    /**
     * Builds the {@link SootMethod}.
     *
     * @return The created {@link SootMethod}.
     * @throws BuilderException A build error occurred.
     */
    @Nonnull
    SootMethod build();
  }

  /**
   * Defines a {@link SootMethod} builder that provides a fluent API.
   *
   * @author Jan Martin Persch
   */
  protected static class SootMethodBuilder
      extends SootClassMemberBuilder<MethodSignature, SootMethod>
      implements Builder.MethodSourceStep,
          Builder.MethodSignatureStep,
          Builder.ModifiersStep,
          Builder.ThrownExceptionsStep,
          Builder {

    /** Creates a new instance of the {@link SootMethodBuilder} class. */
    SootMethodBuilder() {
      super(SootMethod.class);
    }

    @Nullable private MethodSource _source;

    /**
     * Gets the method source content.
     *
     * @return The value to get.
     */
    @Nonnull
    protected MethodSource getSource() {
      return ensureValue(this._source, "source");
    }

    /**
     * Sets the method source content.
     *
     * @param value The value to set.
     */
    @Nonnull
    public MethodSignatureStep withSource(@Nonnull MethodSource value) {
      this._source = value;

      return this;
    }

    private @Nullable MethodSignature _methodSignature;

    /**
     * Gets the method sub-signature.
     *
     * @return The value to get.
     */
    @Nonnull
    protected MethodSignature getSignature() {
      return ensureValue(this._methodSignature, "signature");
    }

    /**
     * Sets the method sub-signature.
     *
     * @param value The value to set.
     */
    @Nonnull
    public ModifiersStep withSignature(@Nonnull MethodSignature value) {
      this._methodSignature = value;

      return this;
    }

    @Nullable private Iterable<Modifier> _modifiers;

    /**
     * Gets the modifiers.
     *
     * @return The value to get.
     */
    @Nonnull
    protected Iterable<Modifier> getModifiers() {
      return ensureValue(this._modifiers, "modifiers");
    }

    /**
     * Sets the modifiers.
     *
     * @param value The value to set.
     */
    @Nonnull
    public ThrownExceptionsStep withModifiers(@Nonnull Iterable<Modifier> value) {
      this._modifiers = value;

      return this;
    }

    @Nullable private Iterable<ClassType> _thrownExceptions = Collections.emptyList();

    /**
     * Gets the thrown exceptions.
     *
     * @return The value to get.
     */
    @Nonnull
    protected Iterable<ClassType> getThrownExceptions() {
      return ensureValue(this._thrownExceptions, "thrownExceptions");
    }

    /**
     * Sets the thrown exceptions.
     *
     * @param value The value to set.
     */
    @Nonnull
    public Builder withThrownExceptions(@Nonnull Iterable<ClassType> value) {
      this._thrownExceptions = value;

      return this;
    }

    @Override
    @Nonnull
    public SootMethod build() {
      return new SootMethod(
          this.getSource(), this.getSignature(), this.getModifiers(), this.getThrownExceptions());
    }
  }
}
