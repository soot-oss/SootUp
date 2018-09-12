package de.upb.soot.core;

import de.upb.soot.jimple.common.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Soot's counterpart of the source language's method concept.
 * 
 * @author Linghui Luo
 *
 */
public class SootMethod {
  /** Name of the current method. */
  private String name;

  /**
   * A list of parameter types taken by this <code>SootMethod</code> object, in declaration order.
   */
  private List<Type> parameterTypes;

  /** The return type of this object. */
  private Type returnType;

  /** Modifiers associated with this SootMethod (e.g. private, protected, etc.) */
  private int modifiers;

  /** Declared exceptions thrown by this method. Created upon demand. */
  private List<SootClass> exceptions = null;

  /** Holds the class which declares this <code>SootClass</code> method. */
  private SootClass declaringClass;

  /** Active body associated with this method. */
  private volatile Body activeBody;

  /**
   * Constructs a SootMethod with the given name, parameter types, return type, and list of thrown exceptions.
   */
  public SootMethod(String name, List<Type> parameterTypes, Type returnType, int modifiers,
      List<SootClass> thrownExceptions) {
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

  }
  public int equivHashCode() {
    // TODO Auto-generated method stub
    return 0;
  }

  public static Object getSubSignature(String string, List<Type> parameterTypes, Type returnType) {
    // TODO Auto-generated method stub
    return null;
  }

  public SootMethod resolve() {
    // TODO Auto-generated method stub
    return null;
  }

  public Type returnType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSignature() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object name() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Type> parameterTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isStatic() {
    // TODO Auto-generated method stub
    return false;
  }

  public SootClass declaringClass() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sets the active body for this method.
   */
  public void setActiveBody(Body body) {
    if ((declaringClass != null) && declaringClass.isPhantom()) {
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

  private boolean isConcrete() {
    // TODO Auto-generated method stub
    return false;
  }

}
