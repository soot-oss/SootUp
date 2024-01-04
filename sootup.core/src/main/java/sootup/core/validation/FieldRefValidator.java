package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann, Markus Schmidt, Akshita Dubey and others
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
import java.util.List;
import java.util.Optional;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

public class FieldRefValidator implements BodyValidator {

  // Checks the consistency of field references.
  @Override
  public List<ValidationException> validate(Body body, View<?> view) {

    List<ValidationException> validationException = new ArrayList<>();

    SootMethod sootMethod = view.getMethod(body.getMethodSignature()).get();
    if (sootMethod.isAbstract()) {
      validationException.add(new ValidationException(sootMethod, "The method is abstract"));
      return validationException;
    }

    List<Stmt> stmts = body.getStmts();

    for (Stmt stmt : stmts) {
      if (!stmt.containsFieldRef()) {
        continue;
      }
      JFieldRef fr = stmt.getFieldRef();

      if (fr instanceof JStaticFieldRef) {
        JStaticFieldRef v = (JStaticFieldRef) fr;
        try {
          Optional<? extends SootField> fieldOpt = view.getField(v.getFieldSignature());
          if (!fieldOpt.isPresent()) {
            validationException.add(new ValidationException(v, "Resolved field is empty: "));
          } else {
            SootField field = fieldOpt.get();
            if (!field.isStatic()) {
              validationException.add(
                  new ValidationException(
                      fr, "Trying to get a static field which is non-static: " + v));
            }
          }
        } catch (Exception e) {
          validationException.add(
              new ValidationException(
                  fr,
                  "Trying to get a static field which is non-static: " + v + " " + e.getMessage()));
        }
      } else if (fr instanceof JInstanceFieldRef) {
        JInstanceFieldRef v = (JInstanceFieldRef) fr;

        try {
          Optional<? extends SootField> fieldOpt = view.getField(v.getFieldSignature());
          if (!fieldOpt.isPresent()) {
            validationException.add(
                new ValidationException(fr, "Resolved field is null: " + fr.toString()));
          } else {
            SootField field = fieldOpt.get();
            if (field.isStatic()) {
              validationException.add(
                  new ValidationException(
                      fr, "Trying to get an instance field which is static: " + v));
            }
          }
        } catch (Exception e) {
          validationException.add(
              new ValidationException(
                  fr,
                  "Trying to get an instance field which is static: " + v + " " + e.getMessage()));
        }
      } else {
        throw new RuntimeException("unknown field ref");
      }
    }
    return validationException;
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
