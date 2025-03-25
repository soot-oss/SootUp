package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Marcus Nachtigall and others
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
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.views.View;

public class LocalsValidator implements BodyValidator {

  /**
   * Verifies that each Local of getUses() and getDefs() belongs to this body's locals.
   *
   * @return
   */
  @Override
  public List<ValidationException> validate(@NonNull Body body, @NonNull View view) {
    List<ValidationException> exception = new ArrayList<>();

    final Set<Local> locals = body.getLocals();

    body.getUses()
        .filter(value -> value instanceof Local && !locals.contains(value))
        .forEach(
            value ->
                exception.add(
                    new ValidationException(
                        value,
                        "Local is not in the StmtGraph : "
                            + value
                            + " in "
                            + body.getMethodSignature())));

    body.getDefs().stream()
        .filter(value -> value instanceof Local && !locals.contains(value))
        .forEach(
            value ->
                exception.add(
                    new ValidationException(
                        value,
                        "Local is not in the StmtGraph : "
                            + value
                            + " in "
                            + body.getMethodSignature())));

    return exception;
  }
}
