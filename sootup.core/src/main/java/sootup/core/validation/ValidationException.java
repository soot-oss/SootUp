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

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;

public class ValidationException extends RuntimeException {

  public ValidationException(SootMethod sm, String void_parameter_types_are_invalid) {
    // TODO: auto generated stub

  }

  public ValidationException(SootClass curClass, String circular_outer_class_chain) {
    // TODO: auto generated stub

  }

  public ValidationException(Local ls, String s) {
    // TODO: auto generated stub

  }

  public ValidationException(Value value, String s) {
    // TODO: auto generated stub

  }

  public ValidationException(Local l, String s, String s1) {
    // TODO: auto generated stub

  }

  public ValidationException(SootMethod method, String s, String s1) {
    // TODO: auto generated stub

  }
}
