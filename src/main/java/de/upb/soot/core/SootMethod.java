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

import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.classprovider.IMethodSource;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.views.IView;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

/**
 * Soot's counterpart of th import java.util.stream.Collectors;e source language's method concept. Soot representation of a
 * Java method. Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a Body. The
 * getActiveBody() method points to the currently-active body.
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

  /** Declared exceptions thrown by this method. Created upon demand. */
  protected final List<JavaClassSignature> exceptions;

  /** Active body associated with this method. */
  protected final Body activeBody;

  /** Tells this method how to find out where its body lives. */
  private final IMethodSource methodSource;

  /**
   * Constructs a SootMethod object with the given attributes. It contains no active body.
   */
  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSource source, List<TypeSignature> parameterTypes,
      TypeSignature returnType, EnumSet<Modifier> modifiers, DebuggingInformation debugInfo) {
    this(view, declaringClass, source, parameterTypes, returnType, modifiers, Collections.<JavaClassSignature>emptyList(),
        debugInfo);
  }

  /**
   * Constructs a SootMethod object with the given attributes.
   */
  public SootMethod(IView view, JavaClassSignature declaringClass, IMethodSource source, List<TypeSignature> parameterTypes,
      TypeSignature returnType, EnumSet<Modifier> modifiers, List<JavaClassSignature> thrownExceptions,
      DebuggingInformation debugInfo) {
    super(view, declaringClass, source.getSignature(), returnType, modifiers);
    this.methodSource = source;
    this.parameterTypes = Collections.unmodifiableList(parameterTypes);
    this.exceptions = Collections.unmodifiableList(thrownExceptions);
    this.debugInfo = debugInfo;
    this.activeBody = source.getBody(this);
    if (this.activeBody != null) {
      this.activeBody.setMethod(this);
    }
  }

  /**
   * Construct a SootMethod object with the attributes of given method and activeBody.
   *
   * @param method
   * @param activeBody
   */
  public SootMethod(SootMethod method, Body activeBody) {
    super(method.getView(), method.getDeclaringClassSignature(), method.signature, method.typeSignature,
        method.modifiers);
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
   * Returns true if this method is not phantom, abstract or native, i.e. this method can have a body.
   */
  public boolean isConcrete() {
    return !isPhantom() && !isAbstract() && !isNative();
  }

  /** Returns the return type of this method. */
  public Type getReturnType() {
    return this.getView().getType(this.typeSignature);
  }

  /** Returns the number of parameters taken by this method. */
  public int getParameterCount() {
    return parameterTypes == null ? 0 : parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this method. */
  public Type getParameterType(int n) {
    return this.getView().getType(parameterTypes.get(n));
  }

  /**
   * Returns a read-only list of the parameter types of this method.
   */
  public Collection<Type> getParameterTypes() {
    List<Type> ret = new ArrayList<Type>();
    parameterTypes.forEach(t -> ret.add(this.getView().getType(t)));
    return ret;
  }

  /**
   * Retrieves the active body for this method.
   */
  public Body getActiveBody() {
    return this.activeBody;
  }

  /** Returns true if this method has an active body. */
  public boolean hasActiveBody() {
    return activeBody != null;
  }

  /** Returns true if this method throws exception <code>e</code>. */
  public boolean throwsException(SootClass e) {
    return exceptions != null && exceptions.contains(e);
  }

  /**
   * Returns a backed list of the exceptions thrown by this method.
   */

  public Collection<SootClass> getExceptions() {
    Collection<SootClass> ret = Collections.emptySet();
    exceptions.stream().filter(e -> this.getView().getClass(e).isPresent())
        .forEach(e -> ret.add((SootClass) this.getView().getClass(e).get()));
    return ret;
  }

  public Collection<JavaClassSignature> getExceptionSignatures() {
    return exceptions;
  }

  /**
   * Convenience method returning true if this method is abstract.
   */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is native.
   */
  public boolean isNative() {
    return Modifier.isNative(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is synchronized.
   */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(this.getModifiers());
  }

  /**
   *
   * @return yes if this is the main method
   */
  public boolean isMain() {
    if (isPublic() && isStatic()) {
      if (this.getSubSignature().equals("void main(java.lang.String[])")) {
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @return yes, if this function is a constructor. Please not that <clinit> methods are not treated as constructors in this
   *         method.
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
   * We rely on the JDK class recognition to decide if a method is JDK method.
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
   * Returns the declaration of this method, as used at the top of textual body representations (before the {}'s containing
   * the code for representation.)
   */
  public String getDeclaration() {
    StringBuffer buffer = new StringBuffer();

    // modifiers
    StringTokenizer st = new StringTokenizer(Modifier.toString(this.getModifiers()));
    if (st.hasMoreTokens()) {
      buffer.append(st.nextToken());
    }

    while (st.hasMoreTokens()) {
      buffer.append(" " + st.nextToken());
    }

    if (buffer.length() != 0) {
      buffer.append(" ");
    }

    // return type + name

    buffer.append(this.getReturnType().toQuotedString() + " ");
    buffer.append(this.getView().quotedNameOf(this.getSignature().name));

    buffer.append("(");

    // parameters
    Iterator<Type> typeIt = this.getParameterTypes().iterator();
    // int count = 0;
    while (typeIt.hasNext()) {
      Type t = typeIt.next();
      buffer.append(t.toQuotedString());
      if (typeIt.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(")");

    // Print exceptions
    if (exceptions != null) {
      Iterator<SootClass> exceptionIt = this.getExceptions().iterator();

      if (exceptionIt.hasNext()) {
        buffer.append(" throws " + this.getView().quotedNameOf(exceptionIt.next().getSignature().toString()));

        while (exceptionIt.hasNext()) {
          buffer.append(", " + this.getView().quotedNameOf(exceptionIt.next().getSignature().toString()));
        }
      }
    }

    return buffer.toString().intern();
  }

  public int getJavaSourceStartLineNumber() {
    return debugInfo.getCodeBodyPosition().getFirstLine();
  }


  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
  }

}
