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
import de.upb.soot.views.IView;

import java.util.EnumSet;

public class SootField extends ClassMember {

  /**
   * Soot's counterpart of the source language's field concept. Soot representation of a Java field. Can be declared to belong
   * to a SootClass.
   *
   * Modified by Linghui Luo
   **/
  private static final long serialVersionUID = -5101396409117866687L;

  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(IView view, SootClass klass, String name, Type type, EnumSet<Modifier> modifiers) {
    super(view, klass, name, type, modifiers);
  }

  public SootField(IView view, SootClass klass, SootField field) {
    this(view, klass, field.name, field.type, field.modifiers);
  }

  /** Constructs a Soot field with the given name, type and no modifiers. */
  public SootField(IView view, SootClass klass, String name, Type type) {
    this(view, klass, name, type, EnumSet.noneOf(Modifier.class));
  }

  @Override
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

  public String getSignature(SootClass cl, String name, Type type) {
    return getSignature(cl, getSubSignature(name, type));
  }

  @Override
  public String getSignature(SootClass cl, String subSignature) {
    StringBuilder buffer = new StringBuilder();

    buffer.append("<").append(this.getView().quotedNameOf(cl.getClassSignature().toString())).append(": ");
    buffer.append(subSignature).append(">");

    return buffer.toString();

  }

  @Override
  public String getSubSignature() {
    if (subSig == null) {
      synchronized (this) {
        if (subSig == null) {
          subSig = getSubSignature(getName(), getType());
        }
      }
    }
    return subSig;
  }

  private String getSubSignature(String name, Type type) {
    StringBuilder buffer = new StringBuilder();
    buffer.append(type.toQuotedString() + " " + this.getView().quotedNameOf(name));
    return buffer.toString();
  }

  public Type getType() {
    return type;
  }

  private String getOriginalStyleDeclaration() {
    String qualifiers = Modifier.toString(modifiers) + " " + type.toQuotedString();
    qualifiers = qualifiers.trim();

    if (qualifiers.isEmpty()) {
      return this.getView().quotedNameOf(name);
    } else {
      return qualifiers + " " + this.getView().quotedNameOf(name) + "";
    }

  }

  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }

}
