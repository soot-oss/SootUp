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
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.views.IView;

import java.util.EnumSet;

public class SootField extends SootClassMember implements IField {

  /**
   * Soot's counterpart of the source language's field concept. Soot representation of a Java field. Can be declared to
   * belong to a SootClass.
   *
   * Modified by Linghui Luo
   **/
  private static final long serialVersionUID = -5101396409117866687L;

  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(IView view, JavaClassSignature declaringClass, FieldSignature signature, TypeSignature type,
      EnumSet<Modifier> modifiers) {
    super(view, declaringClass, signature, type, modifiers);
  }

  public SootField(IView view, SootField field) {
    this(view, field.getDeclaringClassSignature(), (FieldSignature) field.signature, field.typeSignature, field.modifiers);
  }

  /** Constructs a Soot field with the given name, type and no modifiers. */
  public SootField(IView view, JavaClassSignature declaringClass, FieldSignature signature, TypeSignature type) {
    this(view, declaringClass, signature, type, EnumSet.noneOf(Modifier.class));
  }

  public Type getType() {
    return this.getView().getType(typeSignature);
  }

  private String getOriginalStyleDeclaration() {
    if (modifiers.isEmpty()) {
      return signature.getSubSignature();
    } else {
      return Modifier.toString(modifiers) + ' ' + this.signature.getSubSignature();
    }
  }

  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }

  @Override
  public String toString() {
    return this.signature.toString();
  }
}
