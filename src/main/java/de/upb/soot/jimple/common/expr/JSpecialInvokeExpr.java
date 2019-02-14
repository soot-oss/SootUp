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
import de.upb.soot.jimple.basic.ImmediateBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JSpecialInvokeExpr extends AbstractInstanceInvokeExpr {
  /**
   * 
   */
  private static final long serialVersionUID = 9170581307891035087L;

  /**
   * Stores the values of new ImmediateBox to the argBoxes array.
   */
  public JSpecialInvokeExpr(IView view, Local base, MethodSignature method, List<? extends Value> args) {
    super(view, Jimple.newLocalBox(base), method, new ImmediateBox[args.size()]);

    for (int i = 0; i < args.size(); i++) {
      this.argBoxes[i] = Jimple.newImmediateBox(args.get(i));
    }
  }

  @Override
  public Object clone() {
    List<Value> clonedArgs = new ArrayList<Value>(getArgCount());

    for (int i = 0; i < getArgCount(); i++) {
      clonedArgs.add(i, getArg(i));
    }

    return new JSpecialInvokeExpr(this.getView(), (Local) getBase(), methodSignature, clonedArgs);
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append(Jimple.SPECIALINVOKE + " " + baseBox.getValue().toString() + "." + methodSignature + "(");
    argBoxesToString(buffer);
    buffer.append(")");

    return buffer.toString();
  }

  /**
   * Converts a parameter of type StmtPrinter to a string literal.
   */
  @Override
  public void toString(IStmtPrinter up) {

    up.literal(Jimple.SPECIALINVOKE);
    up.literal(" ");
    baseBox.toString(up);
    up.literal(".");
    up.methodSignature(methodSignature);
    up.literal("(");

    if (argBoxes != null) {
      final int len = argBoxes.length;
      if (0 < len) {
        argBoxes[0].toString(up);
        for (int i = 1; i < len; i++) {
          up.literal(", ");
          argBoxes[i].toString(up);
        }
      }
    }
    up.literal(")");
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }

}
