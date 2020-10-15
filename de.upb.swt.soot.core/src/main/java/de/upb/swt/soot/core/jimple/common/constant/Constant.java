package de.upb.swt.soot.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Christian Brüggemann, Linghui Luo
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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;

public interface Constant extends Immediate {

  @Override
  default List<Value> getUses() {
    return Collections.emptyList();
  }

  @Override
  default boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseConstant(this, o);
  }

  /**
   * Returns a hash code consistent with structural equality for this object. For Constants,
   * equality is structural equality; we hope that each subclass defines hashCode() correctly.
   */
  @Override
  default int equivHashCode() {
    return hashCode();
  }

  @Override
  default void toString(StmtPrinter up) {
    up.constant(this);
  }
}
