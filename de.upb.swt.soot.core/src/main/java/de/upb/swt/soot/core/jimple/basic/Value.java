package de.upb.swt.soot.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Markus Schmidt and others
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

import de.upb.swt.soot.core.jimple.visitor.Acceptor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Data used as, for instance, arguments to instructions; typical implementations are constants or
 * expressions.
 *
 * <p>Values are typed, clonable and must declare which other Values they use (contain).
 */
public interface Value extends Acceptor, EquivTo {

  /**
   * Returns a List of Locals,FieldRefs,ArrayRefs which are used by (ie contained within) this
   * Expression or Reference.
   *
   * @return
   */
  List<Value> getUses();

  /** Returns the Soot type of this Value. */
  Type getType();

  void toString(@Nonnull StmtPrinter up);
}
