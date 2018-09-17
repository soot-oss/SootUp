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
import de.upb.soot.jimple.common.type.VoidType;

import java.util.List;

public enum CheckVoidLocalesValidator implements BodyValidator {
  INSTANCE;

  public static CheckVoidLocalesValidator getInstance() {
    return INSTANCE;
  }

  @Override
  public void validate(Body body, List<ValidationException> exception) {
    for (Local l : body.getLocals()) {
      if (l.getType() instanceof VoidType) {
        exception.add(new ValidationException(l, "Local " + l + " in " + body.getMethod() + " defined with void type"));
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
