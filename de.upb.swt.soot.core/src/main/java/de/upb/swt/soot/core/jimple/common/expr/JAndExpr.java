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

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

/** An expression that computes a binary AND of two operands. */
public final class JAndExpr extends AbstractIntLongBinopExpr implements Copyable {

  public JAndExpr(Value op1, Value op2) {
    super(op1, op2);
  }

  @Override
  public final String getSymbol() {
    return " & ";
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseAndExpr(this);
  }

  @Nonnull
  public JAndExpr withOp1(Value op1) {
    return new JAndExpr(op1, getOp2());
  }

  @Nonnull
  public JAndExpr withOp2(Value op2) {
    return new JAndExpr(getOp1(), op2);
  }
}
