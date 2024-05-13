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
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;

public class ValidationException extends RuntimeException {

  public ValidationException(SootMethod sm, String void_parameter_types_are_invalid) {
    super(void_parameter_types_are_invalid);
  }

  public ValidationException(SootClass curClass, String circular_outer_class_chain) {
    super(circular_outer_class_chain);
  }

  public ValidationException(Local ls, String s) {
    super(s);
  }

  public ValidationException(Value value, String s) {
    super(s);
  }

  public ValidationException(Local l, String s, String s1) {
    super(l + s + s1);
  }

  public ValidationException(SootMethod method, String s, String s1) {
    super(method + s + s1);
  }

  public ValidationException(Stmt stmt, String s) {
    super(stmt + s);
  }

  public ValidationException(Body body, String s) {
    super(body + s);
  }

  public ValidationException(MethodSignature methodSignature, String s) {
    super(methodSignature + s);
  }
}
