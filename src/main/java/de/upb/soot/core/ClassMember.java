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

import de.upb.soot.util.Numberable;
import de.upb.soot.views.IView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 */
public abstract class ClassMember extends AbstractViewResident implements Numberable, Serializable {

  /**
   * Modifiers associated with this SootMethod (e.g. private, protected, etc.).
   */
  EnumSet<Modifier> modifiers;

  /** Constructor. */
  ClassMember(IView view) {
    super(view);
  }

  /** Returns the SootClass declaring this one. */
  public abstract SootClass getDeclaringClass();

  /** Returns true when some SootClass object declares this object. */
  public abstract boolean isDeclared();

  /** Returns true when this object is from a phantom class. */
  public abstract boolean isPhantom();

  /** Sets the phantom flag. */
  public abstract void setPhantom(boolean value);

  /** Convenience method returning true if this class member is protected. */
  public abstract boolean isProtected();

  /** Convenience method returning true if this class member is private. */
  public abstract boolean isPrivate();

  /** Convenience method returning true if this class member is public. */
  public abstract boolean isPublic();

  /** Convenience method returning true if this class member is static. */
  public abstract boolean isStatic();

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
  public void setModifiers(Modifier... modifiers) {
    setModifiers(EnumSet.copyOf(Arrays.asList(modifiers)));
  }

  /**
   * Sets the modifiers of this class member.
   *
   * @see de.upb.soot.core.Modifier
   */
  public void setModifiers(EnumSet<Modifier> modifiers) {
    this.modifiers = modifiers;
  }

}