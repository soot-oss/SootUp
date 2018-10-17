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
import de.upb.soot.util.Numberable;
import de.upb.soot.views.IView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 */
public abstract class ClassMember extends AbstractViewResident implements Numberable, Serializable {

  public abstract String getSubSignature();

  public abstract String getSignature(SootClass cl, String subSignature);

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

  /** True when some <code>SootClass</code> object declares this class member. */
  protected boolean isDeclared = false;
  /** Holds the <code>SootClass</code> which declares this class member. */
  protected SootClass declaringClass;
  /** Whether this class member is phantom. */
  protected boolean isPhantom = false;
  /** The return type of this class member. */
  protected Type type;
  /** The name of this class member. */
  protected String name;
  /** The Signature of this class member. */
  protected volatile String sig;
  /** The SubSignature of this class member. */
  protected volatile String subSig;
  /** Modifiers associated with this class member (e.g. private, protected, etc.). */
  EnumSet<Modifier> modifiers;

  /** Constructor. */
  ClassMember(IView view) {
    super(view);
  }

  /** Returns the name of this method. */
  public String getName() {
    return name;
  }

  /** Returns the SootClass declaring this one. */
  public SootClass getDeclaringClass() {
    if (!isDeclared) {
      throw new RuntimeException("not declared: " + getName() + " " + this.type);
    }

    return declaringClass;
  }

  /** Returns true when this object is from a phantom class. */
  public boolean isPhantom() {
    return isPhantom;
  }

  /** Sets the phantom flag. */
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

  /** Convenience method returning true if this class member is protected. */
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /** Convenience method returning true if this class member is private. */
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /** Convenience method returning true if this class member is public. */
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /** Convenience method returning true if this class member is static. */
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  /**
   * Convenience method returning true if this field is final.
   */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  /**
   * Gets the modifiers of this class member.
   *
   * @see de.upb.soot.core.Modifier
   */
  public EnumSet<Modifier> getModifiers() {
    return modifiers;
  }

  /**
   * Sets the modifiers of this class member.
   *
   * @see de.upb.soot.core.Modifier
   */
  public void setModifiers(EnumSet<Modifier> modifiers) {
    this.modifiers = modifiers;
  }

  /**
   * Sets the modifiers of this class member.
   *
   * @see de.upb.soot.core.Modifier
   */
  public void setModifiers(Modifier... modifiers) {
    setModifiers(EnumSet.copyOf(Arrays.asList(modifiers)));
  }

  /** Returns true when some SootClass object declares this object. */
  public boolean isDeclared() {
    return isDeclared;
  }

  public void setDeclared(boolean isDeclared) {
    this.isDeclared = isDeclared;
  }

  /**
   * Returns a hash code for this method consistent with structural equality.
   */
  // TODO: check whether modifiers.hashcode() does what its meant for; former: "modifiers"/int bit flags representing the set
  public int equivHashCode() {
    return type.hashCode() * 101 + modifiers.hashCode() * 17 + name.hashCode();
  }

  /** Returns the signature of this method. */
  public String toString() {
    return getSignature();
  }

  protected int number = 0;

  @Override
  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  public int getNumber() {
    return this.number;
  }

}