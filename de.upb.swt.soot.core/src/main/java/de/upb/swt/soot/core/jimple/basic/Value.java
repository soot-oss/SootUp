/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

import de.upb.swt.soot.core.jimple.visitor.Acceptor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;

/**
 * Data used as, for instance, arguments to instructions; typical implementations are constants or
 * expressions.
 *
 * <p>Values are typed, clonable and must declare which other Values they use (contain).
 */
public interface Value extends Acceptor, EquivTo {
  /**
   * Returns a List of boxes corresponding to Values which are used by (ie contained within) this
   * Value.
   */
  List<ValueBox> getUseBoxes();

  /** Returns the Soot type of this Value. */
  Type getType();

  void toString(StmtPrinter up);
}
