package sootup.core.jimple.common.expr;

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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.ClassType;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** An expression that creates a new instance of a class. */
public final class JNewExpr implements Expr, Copyable {

  @Nonnull private final ClassType type;

  public JNewExpr(@Nonnull ClassType type) {
    this.type = type;
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseNewExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return Jimple.NEW + " " + type;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.NEW);
    up.literal(" ");
    up.typeSignature(type);
  }

  @Nonnull
  @Override
  public ClassType getType() {
    return type;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseNewExpr(this);
  }

  @Nonnull
  public JNewExpr withType(@Nonnull ClassType type) {
    return new JNewExpr(type);
  }
}
