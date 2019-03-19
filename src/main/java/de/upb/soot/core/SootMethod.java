package de.upb.soot.core;
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

import static de.upb.soot.util.Utils.immutableListOf;
import static de.upb.soot.util.Utils.initializedLazy;
import static de.upb.soot.util.Utils.synchronizedLazy;

import com.google.common.collect.ImmutableList;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import de.upb.soot.frontends.IMethodSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.util.builder.BuilderException;
import de.upb.soot.util.concurrent.Lazy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Soot's counterpart of th import java.util.stream.Collectors;e source language's methodRef concept. Soot representation of
 * a Java methodRef. Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a Body. The
 * getActiveBody() methodRef points to the currently-active body.
 *
 * Modified by Linghui Luo
 * @author Jan Martin Persch
 *
 */

public class SootMethod extends SootClassMember implements IMethod {
  /**
   * 
   */
  private static final long serialVersionUID = -7438746401781827520L;
  
  @Nonnull private static final String CONSTRUCTOR_NAME = "<init>";
  @Nonnull private static final String STATIC_INITIALIZER_NAME = "<clinit>";

  @Nullable private final DebuggingInformation debugInfo;
  /**
   * An array of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  @Nonnull private final ImmutableList<TypeSignature> parameterTypes;

  /** Declared exceptions thrown by this methodRef. Created upon demand. */
  @Nonnull protected final ImmutableList<JavaClassSignature> exceptions;

  /** Tells this methodRef how to find out where its body lives. */
  @Nonnull private final IMethodSourceContent methodSource;

  /**
   * Constructs a SootMethod object with the given attributes.
   */
  public SootMethod(
    @Nonnull IMethodSourceContent source,
    @Nonnull MethodSignature methodSignature,
    @Nonnull Iterable<Modifier> modifiers,
    @Nonnull Iterable<JavaClassSignature> thrownExceptions,
    @Nullable DebuggingInformation debugInfo // FIXME: remove Wala DebuggingInformation from this Class, IMHO it does not belong to a sootmethod
  ) {
    this(
        source,
        methodSignature,
        modifiers,
        thrownExceptions,
        null,
        debugInfo);
  }

  /**
   * Constructs a SootMethod object with the given attributes.
   */
  public SootMethod(
    @Nonnull IMethodSourceContent source,
    @Nonnull MethodSignature methodSignature,
    @Nonnull Iterable<Modifier> modifiers,
    @Nonnull Iterable<JavaClassSignature> thrownExceptions,
    @Nullable Body activeBody,
    @Nullable DebuggingInformation debugInfo // FIXME: remove Wala DebuggingInformation from this Class, IMHO it does not belong to a sootmethod
  ) {
    super(methodSignature, modifiers);
    
    this.methodSource = source;
    this.parameterTypes = immutableListOf(methodSignature.getParameterSignatures());
    this.exceptions = immutableListOf(thrownExceptions);
    this.debugInfo = debugInfo;
    
    if (activeBody != null) {
      //noinspection ThisEscapedInObjectConstruction
      activeBody.setMethod(this);
      this._lazyBody = initializedLazy(activeBody);
    } else {
      this._lazyBody = synchronizedLazy(this::lazyBodyInitializer);
    }
  }
  
  @Nullable
  private Body lazyBodyInitializer() {
    Body body;

    try {
      body = this.methodSource.resolveBody(this);

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
  
  /**
   * Returns true if this methodRef is not phantom, abstract or native, i.e. this methodRef can have a body.
   */
  public boolean isConcrete() {
    return !isPhantom() && !isAbstract() && !isNative();
  }
  
  public TypeSignature getReturnTypeSignature() {
    return this.getSignature().getSignature();
  }

  /** Returns the number of parameters taken by this methodRef. */
  public int getParameterCount() {
    return parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this methodRef. */
  public TypeSignature getParameterType(int n) {
    return parameterTypes.get(n);
  }

  /**
   * Returns a read-only list of the parameter types of this methodRef.
   */
  public List<TypeSignature> getParameterTypes() {
    return parameterTypes;
  }

  private final @Nonnull Lazy<Body> _lazyBody;
  
  /**
   * Retrieves the active body for this methodRef.
   */
  @Nullable
  public Body getActiveBody() {
    return this._lazyBody.get(); // TODO: [JMP] Refactor to return `.getAsOptional()`
  }

  /** Returns true if this methodRef has an active body. */
  public boolean hasActiveBody() {
    return this.getActiveBody() != null;
  }

  @Nonnull
  public List<JavaClassSignature> getExceptionSignatures() {
    return exceptions;
  }

  /**
   * Convenience methodRef returning true if this methodRef is abstract.
   */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /**
   * Convenience methodRef returning true if this methodRef is native.
   */
  public boolean isNative() {
    return Modifier.isNative(this.getModifiers());
  }

  /**
   * Convenience methodRef returning true if this methodRef is synchronized.
   */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(this.getModifiers());
  }

  /**
   *
   * @return yes if this is the main methodRef
   */
  public boolean isMain() {
    return isPublic() && isStatic() && this.getSubSignature().toString().equals("void main(java.lang.String[])");
  }

  /**
   *
   * @return yes, if this function is a constructor. Please not that &lt;clinit&gt; methods are not treated as constructors in this
   *         methodRef.
   */
  public boolean isConstructor() {
    return this.getSignature().getName().equals(CONSTRUCTOR_NAME);
  }

  /**
   *
   * @return yes, if this function is a static initializer.
   */
  public boolean isStaticInitializer() {
    return this.getSignature().getName().equals(STATIC_INITIALIZER_NAME);
  }

  /**
   * We rely on the JDK class recognition to decide if a methodRef is JDK methodRef.
   */
  public boolean isJavaLibraryMethod() {
    return this.getDeclaringClass().isJavaLibraryClass();
  }

  /**
   * Returns the declaration of this methodRef, as used at the top of textual body representations (before the {}'s
   * containing the code for representation.)
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

    builder.append(this.getReturnTypeSignature().toQuotedString()).append(" ");
    builder.append(this.getSignature().toQuotedString());

    builder.append("(");

    // parameters
    Iterator<TypeSignature> typeIt = this.getParameterTypes().iterator();
    // int count = 0;
    while (typeIt.hasNext()) {
      TypeSignature t = typeIt.next();
      builder.append(t.toQuotedString());
      if (typeIt.hasNext()) {
        builder.append(", ");
      }
    }
    builder.append(")");

    // Print exceptions
    Iterator<JavaClassSignature> exceptionIt = this.getExceptionSignatures().iterator();

    if (exceptionIt.hasNext()) {
      builder.append(" throws ").append(exceptionIt.next().toQuotedString());

      while (exceptionIt.hasNext()) {
        builder.append(", ").append(exceptionIt.next().toQuotedString());
      }
    }

    return builder.toString().intern();
  }

  public int getJavaSourceStartLineNumber() {
    return debugInfo.getCodeBodyPosition().getFirstLine();
  }

  @Nullable
  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
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
  public interface Builder extends SootClassMember.Builder<SootMethod> {
    interface MethodSourceStep {
      /**
       * Sets the {@link IMethodSourceContent}.
       * 
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      MethodSignatureStep withSource(@Nonnull IMethodSourceContent value);
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
    
    interface ModifiersStep extends SootClassMember.Builder.ModifiersStep<ThrownExceptionsStep> {
    }
    
    interface ThrownExceptionsStep extends ActiveBodyStep {
      /**
       * Sets the exceptions thrown by the method to build. This step is optional.
       * 
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ActiveBodyStep withThrownExceptions(@Nonnull Iterable<JavaClassSignature> value);
    }
    
    interface ActiveBodyStep extends DebugStep {
      /**
       * Sets the {@link Body active body}. This step is optional.
       * 
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      DebugStep withActiveBody(@Nullable Body value);
    }
    
    interface DebugStep extends Builder {
      /**
       * Sets debugging information. This step is optional.
       * 
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      Builder withDebugInfo(@Nullable DebuggingInformation value);
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
      extends SootClassMemberBuilder<SootMethod>
      implements Builder.MethodSourceStep,
                     Builder.MethodSignatureStep,
                     Builder.ModifiersStep,
                     Builder.ThrownExceptionsStep,
                     Builder.DebugStep,
                     Builder
  {
    // region Fields
    
    // endregion /Fields/
    
    // region Constructor
    
    /**
     * Creates a new instance of the {@link SootMethodBuilder} class.
     */
    protected SootMethodBuilder() {
      super(SootMethod.class);
    }
    
    // endregion /Constructor/
    
    // region Properties
    
    @Nullable private IMethodSourceContent _source;
    
    /**
     * Gets the method source content.
     *
     * @return The value to get.
     */
    @Nonnull
    protected IMethodSourceContent getSource() {
      return ensureValue(this._source, "source");
    }
    
    /**
     * Sets the method source content.
     *
     * @param value The value to set.
     */
    @Nonnull
    public MethodSignatureStep withSource(@Nonnull IMethodSourceContent value) {
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
  
    @Nullable private Iterable<JavaClassSignature> _thrownExceptions = Collections.emptyList();
    
    /**
     * Gets the thrown exceptions.
     *
     * @return The value to get.
     */
    @Nonnull
    protected Iterable<JavaClassSignature> getThrownExceptions() {
      return ensureValue(this._thrownExceptions, "thrownExceptions");
    }
    
    /**
     * Sets the thrown exceptions.
     *
     * @param value The value to set.
     */
    @Nonnull
    public ActiveBodyStep withThrownExceptions(@Nonnull Iterable<JavaClassSignature> value) {
      this._thrownExceptions = value;
      
      return this;
    }
    
    @Nullable private Body _activeBody;
    
    /**
     * Gets the active body.
     *
     * @return The value to get.
     */
    @Nullable
    protected Body getActiveBody() {
      return this._activeBody;
    }
    
    /**
     * Sets the active body.
     *
     * @param value The value to set.
     */
    @Nonnull
    public DebugStep withActiveBody(@Nullable Body value) {
      this._activeBody = value;
      
      return this;
    }
    
    @Nullable private DebuggingInformation _debugInfo;
    
    /**
     * Gets the debugging information.
     *
     * @return The value to get.
     */
    @Nullable
    protected DebuggingInformation getDebugInfo() {
      return this._debugInfo;
    }
    
    /**
     * Sets the debugging information.
     */
    @Nonnull
    public Builder withDebugInfo(@Nullable DebuggingInformation value) {
      this._debugInfo = value;
      
      return this;
    }
    
    // endregion /Properties/
    
    // region Methods
  
    @Override
    @Nonnull
    protected SootMethod make() {
      return
          new SootMethod(
              this.getSource(),
              this.getSignature(),
              this.getModifiers(),
              this.getThrownExceptions(),
              this.getActiveBody(),
              this.getDebugInfo());
    }
    
    // endregion /Methods/
  }
}
