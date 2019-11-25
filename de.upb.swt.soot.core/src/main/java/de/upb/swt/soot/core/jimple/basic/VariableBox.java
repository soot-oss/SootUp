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

package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.ref.ConcreteRef;

/**
 * Contains a {@link Local} or a {@link ConcreteRef}.
 *
 * <p>Prefer to use the factory methods in {@link de.upb.swt.soot.core.jimple.Jimple}.
 */
public class VariableBox extends ValueBox {

  public VariableBox(Value value) {
    super(value);
  }

  @Override
  public boolean canContainValue(Value value) {
    return value instanceof Local || value instanceof ConcreteRef;
  }
}
