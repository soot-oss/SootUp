package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Markus Schmidt and others
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

public class FieldRefValidator implements BodyValidator {

  /** Checks the consistency of field references. */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * SootMethod methodRef = body.getMethod(); if (methodRef.isAbstract()) { return; }
     *
     * Chain<Unit> units = body.getUnits().getNonPatchingChain();
     *
     * for (Unit unit : units) { Stmt s = (Stmt) unit; if (!s.containsFieldRef()) { continue; } JFieldRef fr =
     * s.getFieldRef();
     *
     * if (fr instanceof JStaticFieldRef) { JStaticFieldRef v = (JStaticFieldRef) fr; try { SootField field = v.getField();
     * if (field == null) { exceptions.add(new UnitValidationException(unit, body, "Resolved field is null: " +
     * fr.toString())); } else if (!field.isStatic() && !field.isPhantom()) { exceptions .add(new
     * UnitValidationException(unit, body, "Trying to get a static field which is non-static: " + v)); } } catch
     * (ResolutionFailedException e) { exceptions.add(new UnitValidationException(unit, body,
     * "Trying to get a static field which is non-static: " + v)); } } else if (fr instanceof InstanceFieldRef) {
     * InstanceFieldRef v = (InstanceFieldRef) fr;
     *
     * try { SootField field = v.getField(); if (field == null) { exceptions.add(new UnitValidationException(unit, body,
     * "Resolved field is null: " + fr.toString())); } else if (field.isStatic() && !field.isPhantom()) { exceptions.add(new
     * UnitValidationException(unit, body, "Trying to get an instance field which is static: " + v)); } } catch
     * (ResolutionFailedException e) { exceptions.add(new UnitValidationException(unit, body,
     * "Trying to get an instance field which is static: " + v)); } } else { throw new RuntimeException("unknown field ref");
     * } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
