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
import java.util.EnumSet;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 */
public abstract class ClassMember extends AbstractViewResident implements Numberable, Serializable {
  protected boolean isDeclared = false;
  protected boolean isPhantom = false;

  protected final SootClass declaringClass;
  protected final Type type;
  protected final String name;

  protected volatile String sig;
  protected volatile String subSig;
  /** Modifiers associated with this class member (e.g. private, protected, etc.). */
  protected final EnumSet<Modifier> modifiers;

  public abstract String getSubSignature();

  public abstract String getSignature(SootClass cl, String subSignature);

  /** Constructor. */
  public ClassMember(IView view, SootClass klass, String name, Type type, EnumSet<Modifier> modifiers) {
    super(view);
    this.declaringClass = klass;
    this.name = name;
    this.type = type;
    this.modifiers = modifiers;

  }

  /** Returns the name of this method. */
  public String getName() {
    return name;
  }

  /** Returns the SootClass declaring this one. */
  public SootClass getDeclaringClass() {
    return declaringClass;
  }

  /** Returns true when this object is from a phantom class. */
  public boolean isPhantom() {
    return isPhantom;
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
  @Override
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

}