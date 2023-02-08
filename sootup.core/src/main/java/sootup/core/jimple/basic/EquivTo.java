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

package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Christian Brüggemann, Linghui Luo and others
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

import javax.annotation.Nonnull;

/**
 * An alternate equivalence relation between objects, not necessarily compliant with the contract
 * defined by {@link Object#equals(Object)}. The standard interpretation will be structural
 * equality. We also demand that if {@code x.equivTo(y)}, then {@code x.equivHashCode() ==
 * y.equivHashCode}.
 *
 * <p>See {@link JimpleComparator} for the detailed contract.
 */
public interface EquivTo {

  /**
   * Returns true if this object is equivalent to o. The contract is defined in {@link
   * JimpleComparator} and is not necessarily compliant with the contract * defined by {@link
   * Object#equals(Object)}.
   */
  default boolean equivTo(Object o) {
    return equivTo(o, JimpleComparator.getInstance());
  }

  /**
   * Returns a (not necessarily fixed) hash code for this object. This hash code coincides with
   * equivTo; it is undefined in the presence of mutable objects. The contract is defined in {@link
   * JimpleComparator}.
   */
  int equivHashCode();

  /** Returns true if this object is equivalent to o according to the given comparator. */
  boolean equivTo(Object o, @Nonnull JimpleComparator comparator);
}
