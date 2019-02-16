/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
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

package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.symbolicreferences.MethodRef;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JStaticInvokeExpr extends AbstractInvokeExpr {
  /**
   * 
   */
  private static final long serialVersionUID = -8705816067828505717L;

  /**
   * Stores the values of new ImmediateBox to the argBoxes array.
   */
  public JStaticInvokeExpr(IView view, MethodRef method, List<? extends Value> args) {
    super(view, method, new ValueBox[args.size()]);
    for (int i = 0; i < args.size(); i++) {
      this.argBoxes[i] = Jimple.newImmediateBox(args.get(i));
    }

  }

  @Override
  public Object clone() {
    List<Value> clonedArgs = new ArrayList<>(getArgCount());

    for (int i = 0; i < getArgCount(); i++) {
      clonedArgs.add(i, getArg(i));
    }
    return new JStaticInvokeExpr(this.getView(), method, clonedArgs);
  }

  /**
   * Returns true if object o is an instance of AbstractStaticInvokeExpr else returns false.
   */
  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JStaticInvokeExpr) {
      JStaticInvokeExpr ie = (JStaticInvokeExpr) o;
      if (!(getMethod().equals(ie.getMethod())
          && (argBoxes == null ? 0 : argBoxes.length) == (ie.argBoxes == null ? 0 : ie.argBoxes.length))) {
        return false;
      }
      if (argBoxes != null) {
        for (int i = 0; i < argBoxes.length; i++) {
          if (!(argBoxes[i]).getValue().equivTo(ie.argBoxes[i].getValue())) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Returns a hash code for this object, consistent with structural equality.
   */
  @Override
  public int equivHashCode() {
    return getMethod().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(Jimple.STATICINVOKE + " ").append(method).append("(");
    argBoxesToString(builder);
    builder.append(")");

    return builder.toString();
  }

  /**
   * Converts a parameter of type StmtPrinter to a string literal.
   */
  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.STATICINVOKE);
    up.literal(" ");
    up.methodSignature(method.getSignature());
    up.literal("(");

    argBoxesToPrinter(up);

    up.literal(")");
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseStaticInvokeExpr(this);
  }

  @Override
  public boolean equivTo(Object o, Comparator<Object> comparator) {
    return comparator.compare(this, o) == 0;
  }

}
