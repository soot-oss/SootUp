package de.upb.swt.soot.core.validation;

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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class LocalsValidator implements BodyValidator {

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    for (Value v : body.getUses()) {
      validateLocal(body, v, exception);
    }
    for (Value v : body.getDefs()) {
      validateLocal(body, v, exception);
    }
  }

  private void validateLocal(Body body, Value v, List<ValidationException> exception) {
    Value value;
    if ((value = v) instanceof Local) {
      if (!body.getLocals().contains(value)) {
        exception.add(
            new ValidationException(
                value, "Local not in chain : " + value + " in " + body.getMethodSignature()));
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
