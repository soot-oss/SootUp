/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.visitor.JimpleValueVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Local variable in {@link Body}. Use {@link LocalGenerator} to generate locals.
 *
 * <p>Prefer to use the factory methods in {@link de.upb.swt.soot.core.jimple.Jimple}.
 *
 * @author Linghui Luo
 */
public final class Local implements Value, Immediate, Copyable {
  // This class is final since it implements equals and hashCode

  @Nonnull private final String name;
  @Nonnull private final Type type;

  /** Constructs a JimpleLocal of the given name and type. */
  public Local(@Nonnull String name, @Nonnull Type type) {
    this.name = name.intern();
    this.type = type;
  }

  // Can be safely suppressed, JimpleComparator performs this check
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object o) {
    return equivTo(o);
  }

  @Override
  public int hashCode() {
    return equivHashCode();
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseLocal(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(name, type);
  }

  /** Returns the name of this object. */
  @Nonnull
  public String getName() {
    return name;
  }

  /** Returns the type of this local. */
  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.local(this);
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public void accept(Visitor sw) {
    ((JimpleValueVisitor) sw).caseLocal(this);
  }

  @Nonnull
  public Local withName(String name) {
    return new Local(name, type);
  }

  @Nonnull
  public Local withType(Type type) {
    return new Local(name, type);
  }
}
