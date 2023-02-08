package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Steven Arzt, Linghui Luo
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
import sootup.core.model.SootClass;

/**
 * Validates classes to make sure that the outer class chain is not recursive
 *
 * @author Steven Arzt
 */
public class OuterClassValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {

    // TODO: check code from old soot in the comment

    /*
     * Set<SootClass> outerClasses = new HashSet<SootClass>(); SootClass curClass = sc; while (curClass != null) { if
     * (!outerClasses.add(curClass)) { exceptions.add(new ValidationException(curClass, "Circular outer class chain"));
     * break; } curClass = curClass.hasOuterClass() ? curClass.getOuterClass() : null; }
     */
  }

  @Override
  public boolean isBasicValidator() {
    // TODO: check code from old soot n the comment
    return true;
  }
}
