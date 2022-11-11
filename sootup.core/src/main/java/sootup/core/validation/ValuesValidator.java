package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo and others
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

import java.util.List;
import sootup.core.model.Body;

public class ValuesValidator implements BodyValidator {

  /** Verifies that a Value is not used in more than one place. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    // TODO: check code from old soot below
    /*
     * Set<ValueBox> set = newSetFromMap(new IdentityHashMap<ValueBox, Boolean>());
     *
     * for (ValueBox vb : body.getUseAndDefBoxes()) { if (set.add(vb)) { continue; }
     *
     * exception.add(new ValidationException(vb, "Aliased value box : " + vb + " in " + body.getMethod()));
     *
     * for (Unit u : body.getUnits()) { System.err.println(u); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
