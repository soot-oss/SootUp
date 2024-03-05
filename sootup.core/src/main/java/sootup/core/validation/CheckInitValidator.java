package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Markus Schmidt, Akshita Dubey
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/** This validator checks whether local variables are defined before they are used. */
public class CheckInitValidator implements BodyValidator {
  /**
   * Checks that local variables present in method body are defined before use.
   *
   * @param body the body to check
   * @param view the view
   * @return
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {

    List<ValidationException> validationException = new ArrayList<>();

    List<LValue> localList = new ArrayList<>();

    for (Stmt s : body.getStmts()) {
      // Add locals defined in the statement to list.
      s.getDef().ifPresent(localList::add);
      for (Iterator<Value> iterator = s.getUses().iterator(); iterator.hasNext(); ) {
        Value v = iterator.next();
        if (v instanceof Local) {
          Local l = (Local) v;
          if (!localList.contains(l)) {
            validationException.add(
                new ValidationException(
                    l,
                    "Local variable $1 is not definitively defined at this point"
                        .replace("$1", l.getName()),
                    "Warning: Local variable "
                        + l
                        + " not definitely defined at "
                        + s
                        + " in "
                        + body.getMethodSignature()));
          }
        }
      }
    }
    return validationException;
  }
}
