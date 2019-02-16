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

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import de.upb.soot.frontends.IMethodSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.views.IView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

/**
 * Soot's counterpart of th import java.util.stream.Collectors;e source language's methodRef concept. Soot representation of
 * a Java methodRef. Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a Body. The
 * getActiveBody() methodRef points to the currently-active body.
 *
 * Modified by Linghui Luo
 *
 */

public class SootMethod extends SootClassMember implements IMethod {
  /**
   * 
   */
  private static final long serialVersionUID = -7438746401781827520L;

  private static final String constructorName = "<init>";
  private static final String staticInitializerName = "<clinit>";

  private final DebuggingInformation debugInfo;
  /**
   * An array of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  private final List<TypeSignature> parameterTypes;

  /** Declared exceptions thrown by this methodRef. Created upon demand. */
  protected final List<JavaClassSignature> exceptions;

  /** Active body associated with this methodRef. */
  protected final @Nullable Body activeBody;

  /** Tells this methodRef how to find out where its body lives. */
  private final IMethodSourceContent methodSource;

  // FIXME: remove Wala DebuggingInformation from this Class, IMHO it does not belong to a sootmethod
  /**
   * Constructs a SootMethod object with the given attributes. It contains no active body.
   */
  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSourceContent source,
      List<TypeSignature> parameterTypes, TypeSignature returnType, EnumSet<Modifier> modifiers,
      DebuggingInformation debugInfo) {
    this(view, declaringClass, source, source.getSignature(), modifiers, Collections.emptyList(), debugInfo);
  }

  /**
   * Constructs a SootMethod object with the given attributes. It contains no active body.
   */
  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSourceContent source,
      List<TypeSignature> parameterTypes, TypeSignature returnType, EnumSet<Modifier> modifiers) {
    this(view, declaringClass, source, source.getSignature(), modifiers, Collections.<JavaClassSignature>emptyList(), null);
  }

  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSourceContent source, MethodSignature signature,
      EnumSet<Modifier> modifiers) {
    this(view, declaringClass, source, signature, modifiers, Collections.<JavaClassSignature>emptyList(), null);
  }

  /**
   * Constructs a SootMethod object with the given attributes.
   */
  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSourceContent source,
      MethodSignature methodSignature, EnumSet<Modifier> modifiers, List<JavaClassSignature> thrownExceptions,
      DebuggingInformation debugInfo) {
    super(view, declaringClass, methodSignature, methodSignature.typeSignature, modifiers);
    Body myActiveBody = null;
    this.methodSource = source;
    this.parameterTypes = Collections.unmodifiableList(methodSignature.parameterSignatures);
    this.exceptions = Collections.unmodifiableList(thrownExceptions);
    this.debugInfo = debugInfo;
    try {

      // FIXME: error handling
      myActiveBody = source.getBody(this);
      if (myActiveBody != null) {
        myActiveBody.setMethod(this);
      }

    } catch (ResolveException e) {
      myActiveBody = null;
      e.printStackTrace();

    }
    activeBody = myActiveBody;

  }

  /**
   * Construct a SootMethod object with the attributes of given methodRef and activeBody.
   */
  public SootMethod(SootMethod method, Body activeBody) {
    super(method.getView(), method.getDeclaringClassSignature(), method.signature, method.typeSignature, method.modifiers);
    this.methodSource = method.methodSource;
    this.parameterTypes = Collections.unmodifiableList(method.parameterTypes);
    this.exceptions = Collections.unmodifiableList(method.exceptions);
    this.debugInfo = method.debugInfo;
    this.activeBody = activeBody;
    if (this.activeBody != null) {
      this.activeBody.setMethod(this);
    }
  }

  /**
   * Returns true if this methodRef is not phantom, abstract or native, i.e. this methodRef can have a body.
   */
  public boolean isConcrete() {
    return !isPhantom() && !isAbstract() && !isNative();
  }

  /** Returns the return type of this methodRef. */
  public Type getReturnType() {
    return this.getView().getType(this.typeSignature);
  }

  /** Returns the number of parameters taken by this methodRef. */
  public int getParameterCount() {
    return parameterTypes == null ? 0 : parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this methodRef. */
  public Type getParameterType(int n) {
    return this.getView().getType(parameterTypes.get(n));
  }

  /**
   * Returns a read-only list of the parameter types of this methodRef.
   */
  public Collection<Type> getParameterTypes() {
    List<Type> ret = new ArrayList<>();
    parameterTypes.forEach(t -> ret.add(this.getView().getType(t)));
    return ret;
  }

  /**
   * Retrieves the active body for this methodRef.
   */
  public Body getActiveBody() {
    return this.activeBody;
  }

  /** Returns true if this methodRef has an active body. */
  public boolean hasActiveBody() {
    return activeBody != null;
  }

  /** Returns true if this methodRef throws exception <code>e</code>. */
  public boolean throwsException(SootClass e) {
    // FIXME: [JMP] `exceptions` contain instances of type `JavaClassSignature`,
    // but `contains(…)` is called with `SootClass`
    return exceptions != null && exceptions.contains(e.getSignature());
  }

  /**
   * Returns a backed list of the exceptions thrown by this methodRef.
   */
  public Collection<SootClass> getExceptions() {
    // FIXME: `Collections.emptySet()` is immutable, this it can't be modified!
    Collection<SootClass> ret = new HashSet<>();
    exceptions.stream().filter(e -> this.getView().getClass(e).isPresent())
        .forEach(e -> ret.add((SootClass) this.getView().getClass(e).get()));
    return ret;
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
    if (isPublic() && isStatic()) {
      return this.getSubSignature().equals("void main(java.lang.String[])");
    }
    return false;
  }

  /**
   *
   * @return yes, if this function is a constructor. Please not that <clinit> methods are not treated as constructors in this
   *         methodRef.
   */
  public boolean isConstructor() {
    return this.signature.name.equals(constructorName);
  }

  /**
   *
   * @return yes, if this function is a static initializer.
   */
  public boolean isStaticInitializer() {
    return this.signature.name.equals(staticInitializerName);
  }

  /**
   * We rely on the JDK class recognition to decide if a methodRef is JDK methodRef.
   */
  public boolean isJavaLibraryMethod() {
    Optional<SootClass> op = getDeclaringClass();
    if (op.isPresent()) {
      SootClass cl = op.get();
      return cl.isJavaLibraryClass();
    } else {
      return false;
    }
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

    builder.append(this.getReturnType().toQuotedString()).append(" ");
    builder.append(this.getView().quotedNameOf(this.getSignature().name));

    builder.append("(");

    // parameters
    Iterator<Type> typeIt = this.getParameterTypes().iterator();
    // int count = 0;
    while (typeIt.hasNext()) {
      Type t = typeIt.next();
      builder.append(t.toQuotedString());
      if (typeIt.hasNext()) {
        builder.append(", ");
      }
    }
    builder.append(")");

    // Print exceptions
    if (exceptions != null) {
      Iterator<SootClass> exceptionIt = this.getExceptions().iterator();

      if (exceptionIt.hasNext()) {
        builder.append(" throws ").append(this.getView().quotedNameOf(exceptionIt.next().getSignature().toString()));

        while (exceptionIt.hasNext()) {
          builder.append(", ").append(this.getView().quotedNameOf(exceptionIt.next().getSignature().toString()));
        }
      }
    }

    return builder.toString().intern();
  }

  public int getJavaSourceStartLineNumber() {
    return debugInfo.getCodeBodyPosition().getFirstLine();
  }

  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
  }

}
