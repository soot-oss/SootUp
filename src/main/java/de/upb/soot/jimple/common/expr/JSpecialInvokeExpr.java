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
import de.upb.soot.jimple.common.ref.SootMethodRef;

import java.util.ArrayList;
import java.util.List;

public class JSpecialInvokeExpr extends AbstractSpecialInvokeExpr {
  /**
   * Stores the values of new ImmediateBox to the argBoxes array.
   */
  public JSpecialInvokeExpr(Local base, SootMethodRef methodRef, List<? extends Value> args) {
    super(Jimple.getInstance().newLocalBox(base), methodRef, new ImmediateBox[args.size()]);

    for (int i = 0; i < args.size(); i++) {
      this.argBoxes[i] = Jimple.getInstance().newImmediateBox(args.get(i));
    }
  }

  @Override
  public Object clone() {
    List<Value> clonedArgs = new ArrayList<Value>(getArgCount());

    for (int i = 0; i < getArgCount(); i++) {
      clonedArgs.add(i, getArg(i));
    }

    return new JSpecialInvokeExpr((Local) getBase(), methodRef, clonedArgs);
  }
}
