package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo and others
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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that creates a new instance of a class. */
public final class JNewExpr implements Expr, Copyable {

  private final ReferenceType type;

  public JNewExpr(ReferenceType type) {
    this.type = type;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseNewExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return Jimple.NEW + " " + type.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.NEW);
    up.literal(" ");
    up.typeSignature(type);
  }

  // TODO: duplicate getter? ->getType()
  public ReferenceType getBaseType() {
    return type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseNewExpr(this);
  }

  @Nonnull
  public JNewExpr withType(ReferenceType type) {
    return new JNewExpr(type);
  }
}
