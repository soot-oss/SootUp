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
import de.upb.soot.util.Numberable;
import de.upb.soot.util.NumberedString;
import de.upb.soot.views.IView;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Soot's counterpart of the source language's method concept. Soot representation of a Java method. Can be declared to
 * belong to a SootClass. Does not contain the actual code, which belongs to a Body. The getActiveBody() method points to the
 * currently-active body.
 *
 * Modified by Linghui Luo
 *
 */

public class SootMethod extends AbstractViewResident implements ClassMember, Numberable,Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -7438746401781827520L;
  private DebuggingInformation debugInfo;
  private static final String constructorName = "<init>";
  private static final String staticInitializerName = "<clinit>";
  /** Name of the current method. */
  protected String name;

  /**
   * An array of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  private List<Type> parameterTypes;

  /** The return type of this object. */
  protected Type returnType;

  /**
   * True when some <code>SootClass</code> object declares this <code>SootMethod</code> object.
   */
  protected boolean isDeclared;

  /** Holds the class which declares this <code>SootClass</code> method. */
  protected SootClass declaringClass;

  /**
   * Modifiers associated with this SootMethod (e.g. private, protected, etc.)
   */
  protected EnumSet<Modifier> modifiers;

  /** Is this method a phantom method? */
  protected boolean isPhantom = false;

  /** Declared exceptions thrown by this method. Created upon demand. */
  protected List<SootClass> exceptions = null;

  /** Active body associated with this method. */
  protected volatile Body activeBody;

  /** Tells this method how to find out where its body lives. */
  protected volatile IMethodSource ms;

  protected volatile String sig;
  protected volatile String subSig;

  /**
   * Constructs a SootMethod with the given name, parameter types and return type.
   */
  public SootMethod(IView view, String name, List<Type> parameterTypes, Type returnType) {
    this(view, name, parameterTypes, returnType, EnumSet.noneOf(Modifier.class), Collections.<SootClass>emptyList());
  }

  /**
   * Constructs a SootMethod with the given name, parameter types, return type and modifiers.
   */
  public SootMethod(IView view, String name, List<Type> parameterTypes, Type returnType, EnumSet<Modifier> modifiers) {
    this(view, name, parameterTypes, returnType, modifiers, Collections.<SootClass>emptyList());
  }

  /**
   * Constructs a SootMethod with the given name, parameter types, return type, and list of thrown exceptions.
   */
  public SootMethod(IView view, String name, List<Type> parameterTypes, Type returnType, EnumSet<Modifier> modifiers,
      List<SootClass> thrownExceptions) {
    super(view);
    this.name = name;
    this.parameterTypes = new ArrayList<Type>();
    this.parameterTypes.addAll(parameterTypes);
    this.parameterTypes = Collections.unmodifiableList(this.parameterTypes);

    this.returnType = returnType;
    this.modifiers = modifiers;

    if (exceptions == null && !thrownExceptions.isEmpty()) {
      exceptions = new ArrayList<SootClass>();
      this.exceptions.addAll(thrownExceptions);
    }
    subsignature = this.getView().getSubSigNumberer().findOrAdd(getSubSignature());
  }

  /**
   * Returns a hash code for this method consistent with structural equality.
   */
  // TODO: check whether modifiers.hashcode (former: "modifiers" representing the set) does what its meant for
  public int equivHashCode() {
    return returnType.hashCode() * 101 + modifiers.hashCode() * 17 + name.hashCode();
  }

  /** Returns the name of this method. */
  public String getName() {
    return name;
  }

  /** Sets the name of this method. */
  public synchronized void setName(String name) {
    boolean wasDeclared = isDeclared;
    SootClass oldDeclaringClass = declaringClass;
    if (wasDeclared) {
      oldDeclaringClass.removeMethod(this);
    }
    this.name = name;
    subSig = null;
    sig = null;
    subsignature = this.getView().getSubSigNumberer().findOrAdd(getSubSignature());
    if (wasDeclared) {
      oldDeclaringClass.addMethod(this);
    }
  }

  /** Sets the declaring class */
  public synchronized void setDeclaringClass(SootClass declClass) {
    // There is nothing to stop this field from being null except when it actually gets in
    // other classes such as SootMethodRef (when it tries to resolve the method). However, if
    // the method is not declared, it should not be trying to resolve it anyways. So I see no
    // problem with having it able to be null.
    if (declClass != null) {
      this.getView().getMethodNumberer().add(this);
    }
    // We could call setDeclared here, however, when SootClass adds a method, it checks isDeclared
    // and throws an exception if set. So we currently cannot call setDeclared here.
    declaringClass = declClass;
    sig = null;
  }

  /** Returns the class which declares the current <code>SootMethod</code>. */
  @Override
  public SootClass getDeclaringClass() {
    if (!isDeclared) {
      throw new RuntimeException("not declared: " + getName());
    }

    return declaringClass;
  }

  public void setDeclared(boolean isDeclared) {
    this.isDeclared = isDeclared;
  }

  /**
   * Returns true when some <code>SootClass</code> object declares this <code>SootMethod</code> object.
   */
  @Override
  public boolean isDeclared() {
    return isDeclared;
  }

  /** Returns true when this <code>SootMethod</code> object is phantom. */
  @Override
  public boolean isPhantom() {
    return isPhantom;
  }

  /**
   * Returns true if this method is not phantom, abstract or native, i.e. this method can have a body.
   */

  public boolean isConcrete() {
    return !isPhantom() && !isAbstract() && !isNative();
  }

  /** Sets the phantom flag on this method. */
  @Override
  public void setPhantom(boolean value) {
    if (value) {
      if (!this.getView().allowsPhantomRefs()) {
        throw new RuntimeException("Phantom refs not allowed");
      }
      if (!this.getView().getOptions().allow_phantom_elms() && declaringClass != null && !declaringClass.isPhantomClass()) {
        throw new RuntimeException("Declaring class would have to be phantom");
      }
    }
    isPhantom = value;
  }

  /**
   * Gets the modifiers of this method.
   *
   * @see de.upb.soot.core.Modifier
   */
  @Override
  public EnumSet<Modifier> getModifiers() {
      return modifiers;
  }

  /**
   * Sets the modifiers of this method.
   *
   * @see de.upb.soot.core.Modifier
   */
  @Override
  public void setModifiers( EnumSet<Modifier> modifiers) {
    this.modifiers = modifiers;
  }

  /** Returns the return type of this method. */
  public Type getReturnType() {
    return returnType;
  }

  /** Sets the return type of this method. */
  public synchronized void setReturnType(Type t) {
    boolean wasDeclared = isDeclared;
    SootClass oldDeclaringClass = declaringClass;
    if (wasDeclared) {
      oldDeclaringClass.removeMethod(this);
    }
    returnType = t;
    subSig = null;
    sig = null;
    subsignature = this.getView().getSubSigNumberer().findOrAdd(getSubSignature());
    if (wasDeclared) {
      oldDeclaringClass.addMethod(this);
    }
  }

  /** Returns the number of parameters taken by this method. */
  public int getParameterCount() {
    return parameterTypes == null ? 0 : parameterTypes.size();
  }

  /** Gets the type of the <i>n</i>th parameter of this method. */
  public Type getParameterType(int n) {
    return parameterTypes.get(n);
  }

  /**
   * Returns a read-only list of the parameter types of this method.
   */
  public List<Type> getParameterTypes() {
    return parameterTypes == null ? Collections.<Type>emptyList() : parameterTypes;
  }

  /**
   * Changes the set of parameter types of this method.
   */
  public synchronized void setParameterTypes(List<Type> l) {
    boolean wasDeclared = isDeclared;
    SootClass oldDeclaringClass = declaringClass;
    if (wasDeclared) {
      oldDeclaringClass.removeMethod(this);
    }
    this.parameterTypes = l;
    subSig = null;
    sig = null;
    subsignature = this.getView().getSubSigNumberer().findOrAdd(getSubSignature());
    if (wasDeclared) {
      oldDeclaringClass.addMethod(this);
    }
  }

  /** Returns the MethodSource of the current SootMethod. */
  public IMethodSource getSource() {
    return ms;
  }

  /** Sets the MethodSource of the current SootMethod. */
  public synchronized void setSource(IMethodSource ms) {
    this.ms = ms;
  }

  /**
   * Retrieves the active body for this method.
   */
  public Body getActiveBody() {
    // Retrieve the active body so thread changes do not affect the
    // synchronization between if the body exists and the returned body.
    // This is a quick check just in case the activeBody exists.
    Body activeBody = this.activeBody;
    if (activeBody != null) {
      return activeBody;
    }

    // Synchronize because we are operating on two fields that may be updated
    // separately otherwise.
    synchronized (this) {
      // Re-check the activeBody because things might have changed
      activeBody = this.activeBody;
      if (activeBody != null) {
        return activeBody;
      }

      if (declaringClass != null) {
        declaringClass.checkLevel(SootClass.BODIES);
      }
      if ((declaringClass != null && declaringClass.isPhantomClass()) || isPhantom()) {
        throw new RuntimeException("cannot get active body for phantom method: " + getSignature());
      }

      // ignore empty body exceptions if we are just computing coffi metrics
      /*
       * TODO: sth if (!soot.jbco.Main.metrics) { throw new RuntimeException("no active body present for method " +
       * getSignature()); }
       */
      return null;
    }
  }

  /**
   * Sets the active body for this method.
   */
  public void setActiveBody(Body body) {
    if ((declaringClass != null) && declaringClass.isPhantomClass()) {
      throw new RuntimeException("cannot set active body for phantom class! " + this);
    }

    if (!isConcrete()) {
      throw new RuntimeException("cannot set body for non-concrete method! " + this);
    }

    if (body != null && body.getMethod() != this) {
      body.setMethod(this);
    }

    activeBody = body;
  }

  /**
   * Returns the active body if present, else constructs an active body and returns that.
   *
   * If you called Scene.getInstance().loadClassAndSupport() for a class yourself, it will not be an application class, so
   * you cannot get retrieve its active body. Please call setApplicationClass() on the relevant class.
   */
  public Body retrieveActiveBody() {
    // Retrieve the active body so thread changes do not affect the
    // synchronization between if the body exists and the returned body.
    // This is a quick check just in case the activeBody exists.
    Body activeBody = this.activeBody;
    if (activeBody != null) {
      return activeBody;
    }

    // Synchronize because we are operating on multiple fields that may be updated
    // separately otherwise.
    synchronized (this) {
      // Re-check the activeBody because things might have changed
      activeBody = this.activeBody;
      if (activeBody != null) {
        return activeBody;
      }

      if (declaringClass != null) {
        declaringClass.checkLevel(SootClass.BODIES);
      }
      if ((declaringClass != null && declaringClass.isPhantomClass()) || isPhantom()) {
        throw new RuntimeException("cannot get resident body for phantom method : " + this);
      }

      if (ms == null) {
        throw new RuntimeException("No method source set for method " + this);
      }

      // Method sources are not expected to be thread safe
      activeBody = ms.getBody(this, "jb");
      setActiveBody(activeBody);

      // If configured, we drop the method source to save memory
      if (this.getView().getOptions().drop_bodies_after_load()) {
        ms = null;
      }
      return activeBody;
    }
  }

  /** Returns true if this method has an active body. */
  public boolean hasActiveBody() {
    return activeBody != null;
  }

  /** Releases the active body associated with this method. */
  public synchronized void releaseActiveBody() {
    activeBody = null;
  }

  /**
   * Adds the given exception to the list of exceptions thrown by this method unless the exception is already in the list.
   */
  public void addExceptionIfAbsent(SootClass e) {
    if (!throwsException(e)) {
      addException(e);
    }
  }

  /**
   * Adds the given exception to the list of exceptions thrown by this method.
   */
  public void addException(SootClass e) {
    if (exceptions == null) {
      exceptions = new ArrayList<SootClass>();
    } else if (exceptions.contains(e)) {
      throw new RuntimeException("already throws exception " + e.getName());
    }

    exceptions.add(e);
  }

  /**
   * Removes the given exception from the list of exceptions thrown by this method.
   */
  public void removeException(SootClass e) {
    if (exceptions == null) {
      throw new RuntimeException("does not throw exception " + e.getName());
    }

    if (!exceptions.contains(e)) {
      throw new RuntimeException("does not throw exception " + e.getName());
    }

    exceptions.remove(e);
  }

  /** Returns true if this method throws exception <code>e</code>. */
  public boolean throwsException(SootClass e) {
    return exceptions != null && exceptions.contains(e);
  }

  public void setExceptions(List<SootClass> exceptions) {
    if (exceptions != null && !exceptions.isEmpty()) {
      this.exceptions = new ArrayList<SootClass>(exceptions);
    } else {
      this.exceptions = null;
    }
  }

  /**
   * Returns a backed list of the exceptions thrown by this method.
   */

  public List<SootClass> getExceptions() {
    if (exceptions == null) {
      exceptions = new ArrayList<SootClass>();
    }

    return exceptions;
  }

  public List<SootClass> getExceptionsUnsafe() {
    return exceptions;
  }

  /**
   * Convenience method returning true if this method is static.
   */
  @Override
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is private.
   */
  @Override
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is public.
   */
  @Override
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is protected.
   */
  @Override
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is abstract.
   */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /**
   * Convenience method returning true if this method is final.
   */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
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
      NumberedString main_sig = this.getView().getSubSigNumberer().findOrAdd("void main(java.lang.String[])");
      if (main_sig.equals(subsignature)) {
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
    return name.equals(constructorName);
  }

  /**
   *
   * @return yes, if this function is a static initializer.
   */
  public boolean isStaticInitializer() {
    return name.equals(staticInitializerName);
  }

  /**
   * @return yes, if this is a class initializer or main function.
   */
  public boolean isEntryMethod() {
    if (isStatic() && subsignature.equals(this.getView().getSubSigNumberer().findOrAdd("void <clinit>()"))) {
      return true;
    }
    return isMain();
  }

  /**
   * We rely on the JDK class recognition to decide if a method is JDK method.
   */
  public boolean isJavaLibraryMethod() {
    SootClass cl = getDeclaringClass();
    return cl.isJavaLibraryClass();
  }

  /**
   * Returns the parameters part of the signature in the format in which it appears in bytecode.
   */
  public String getBytecodeParms() {
    StringBuffer buffer = new StringBuffer();
    for (Iterator<Type> typeIt = getParameterTypes().iterator(); typeIt.hasNext();) {
      final Type type = typeIt.next();
    }
    return buffer.toString().intern();
  }

  /**
   * Returns the signature of this method in the format in which it appears in bytecode (eg. [Ljava/lang/Object instead of
   * java.lang.Object[]).
   */
  public String getBytecodeSignature() {
    String name = getName();

    StringBuffer buffer = new StringBuffer();
    buffer.append("<" + this.getView().quotedNameOf(getDeclaringClass().getName()) + ": ");
    buffer.append(name);
    // TODO: sth: AbstractJasminClass
    // buffer.append(AbstractJasminClass.jasminDescriptorOf(makeRef()));
    buffer.append(">");

    return buffer.toString().intern();
  }

  /**
   * Returns the Soot signature of this method. Used to refer to methods unambiguously.
   */
  public String getSignature() {
    if (sig == null) {
      synchronized (this) {
        if (sig == null) {
          sig = getSignature(getDeclaringClass(), getSubSignature());
        }
      }
    }
    return sig;
  }

  public String getSignature(SootClass cl, String name, List<Type> params, Type returnType) {
    return getSignature(cl, getSubSignatureImpl(name, params, returnType));
  }

  public String getSignature(SootClass cl, String subSignature) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<");
    buffer.append(this.getView().quotedNameOf(cl.getName()));
    buffer.append(": ");
    buffer.append(subSignature);
    buffer.append(">");

    return buffer.toString();
  }

  /**
   * Returns the Soot subsignature of this method. Used to refer to methods unambiguously.
   */
  public String getSubSignature() {
    if (subSig == null) {
      synchronized (this) {
        if (subSig == null) {
          subSig = getSubSignatureImpl(getName(), getParameterTypes(), getReturnType());
        }
      }
    }
    return subSig;
  }

  public String getSubSignature(String name, List<Type> params, Type returnType) {
    return getSubSignatureImpl(name, params, returnType);
  }

  private String getSubSignatureImpl(String name, List<Type> params, Type returnType) {
    StringBuilder buffer = new StringBuilder();

    buffer.append(returnType.toQuotedString());

    buffer.append(" ");
    buffer.append(this.getView().quotedNameOf(name));
    buffer.append("(");

    if (params != null) {
      for (int i = 0; i < params.size(); i++) {
        buffer.append(params.get(i).toQuotedString());
        if (i < params.size() - 1) {
          buffer.append(",");
        }
      }
    }
    buffer.append(")");

    return buffer.toString();
  }

  protected NumberedString subsignature;

  public NumberedString getNumberedSubSignature() {
    return subsignature;
  }

  /** Returns the signature of this method. */
  @Override
  public String toString() {
    return getSignature();
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
    buffer.append(this.getView().quotedNameOf(this.getName()));

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
        buffer.append(" throws " + this.getView().quotedNameOf(exceptionIt.next().getName()));

        while (exceptionIt.hasNext()) {
          buffer.append(", " + this.getView().quotedNameOf(exceptionIt.next().getName()));
        }
      }
    }

    return buffer.toString().intern();
  }

  @Override
  public final int getNumber() {
    return number;
  }

  @Override
  public final void setNumber(int number) {
    this.number = number;
  }

  protected int number = 0;

  public SootMethod method() {
    return this;
  }

  public int getJavaSourceStartLineNumber() {
    return debugInfo.getCodeBodyPosition().getFirstLine();
  }

  public Type returnType() {
    return this.returnType;
  }

  public String name() {
    return this.name;
  }

  public List<Type> parameterTypes() {
    return this.parameterTypes;
  }

  public SootClass declaringClass() {
    return this.declaringClass;
  }

  public void setDebugInfo(DebuggingInformation debugInfo) {
    this.debugInfo = debugInfo;
  }

  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;
  }
}
