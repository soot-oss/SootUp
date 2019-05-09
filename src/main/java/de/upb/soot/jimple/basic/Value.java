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

package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.visitor.IAcceptor;
import de.upb.soot.types.Type;
import de.upb.soot.util.printer.IStmtPrinter;
import java.io.Serializable;
import java.util.List;

/**
 * Data used as, for instance, arguments to instructions; typical implementations are constants or
 * expressions.
 *
 * <p>Values are typed, clonable and must declare which other Values they use (contain).
 */
public interface Value extends IAcceptor, EquivTo, Serializable {
  /**
   * Returns a List of boxes corresponding to Values which are used by (ie contained within) this
   * Value.
   */
  List<ValueBox> getUseBoxes();

  /** Returns the Soot type of this Value. */
  Type getType();

  /** Returns a clone of this Value. */
  Object clone();

  void toString(IStmtPrinter up);
}
