package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Markus Schmidt, Linghui Luo and others
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
import sootup.core.views.View;

public class TrapsValidator implements BodyValidator {

  /**
   * Verifies that the begin, end and handler units of each trap are in this body.
   *
   * @return
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {

    // TODO: check code from old soot below
    /*
     * PatchingChain<Unit> units = body.getUnits();
     *
     * for (Trap t : body.getTraps()) { if (!units.contains(t.getBeginUnit())) { exception.add(new
     * ValidationException(t.getBeginUnit(), "begin not in chain" + " in " + body.getMethod())); }
     *
     * if (!units.contains(t.getEndUnit())) { exception.add(new ValidationException(t.getEndUnit(), "end not in chain" +
     * " in " + body.getMethod())); }
     *
     * if (!units.contains(t.getHandlerUnit())) { exception.add(new ValidationException(t.getHandlerUnit(),
     * "handler not in chain" + " in " + body.getMethod())); } }
     */
    return null;
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
