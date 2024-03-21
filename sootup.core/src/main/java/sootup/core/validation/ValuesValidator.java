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

import static java.util.Collections.newSetFromMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

public class ValuesValidator implements BodyValidator {

  /**
   * Verifies that a Value is not used in more than one place.
   *
   * @return
   */
  @Override
  public List<ValidationException> validate(Body body, View view) {

    List<ValidationException> validationException = new ArrayList<>();

    Set<Value> valueSet = newSetFromMap(new IdentityHashMap<Value, Boolean>());
    Collection<LValue> defs = body.getDefs();
    List<Value> values = body.getUses().collect(Collectors.toList());
    for (Stmt s : body.getStmts()) {
      for (Value v : s.getUsesAndDefs().collect(Collectors.toList())) {
        if (valueSet.add(v)) {
          continue;
        }
        validationException.add(
            new ValidationException(
                v, "Aliased value : " + v + " in " + body.getMethodSignature()));
        System.err.println(s);
      }
    }
    return validationException;
  }
}
