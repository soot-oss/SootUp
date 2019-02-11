package de.upb.soot.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.soot.core.Body;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;

import java.util.List;

public enum LocalsValidator implements BodyValidator {
  INSTANCE;

  public static LocalsValidator getInstance() {
    return INSTANCE;
  }

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    for (ValueBox vb : body.getUseBoxes()) {
      validateLocal(body, vb, exception);
    }
    for (ValueBox vb : body.getDefBoxes()) {
      validateLocal(body, vb, exception);
    }
  }

  private void validateLocal(Body body, ValueBox vb, List<ValidationException> exception) {
    Value value;
    if ((value = vb.getValue()) instanceof Local) {
      if (!body.getLocals().contains(value)) {
        exception.add(new ValidationException(value, "Local not in chain : " + value + " in " + body.getMethod()));
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
