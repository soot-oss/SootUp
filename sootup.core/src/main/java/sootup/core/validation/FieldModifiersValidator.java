package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Akshita Dubey
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
import sootup.core.model.SootField;

/** Validator that checks for impossible combinations of field modifiers */
public class FieldModifiersValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {
    for (SootField sf : sc.getFields()) {
      if ((sf.isPrivate() || sf.isProtected()) && (sf.isPublic()) || sf.isProtected()) {
        exceptions.add(
            new ValidationException(
                sc,
                "Field $1 can only be either public, protected or private"
                    .replace("$1", sf.getName())));
      }

      if (sc.isInterface()) {
        if (!sf.isPublic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and public".replace("$1", sf.getName())));
        }
        if (!sf.isStatic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and static".replace("$1", sf.getName())));
        }
        if (!sf.isFinal()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and final".replace("$1", sf.getName())));
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
