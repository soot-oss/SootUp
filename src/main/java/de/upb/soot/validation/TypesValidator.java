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

import java.util.List;

/**
 * Checks whether the types used for locals, method parameters, and method return values are allowed in final Jimple code.
 * This reports an error if a method uses e.g., null_type.
 */
public class TypesValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    /*
     * SootMethod method = body.getMethod();
     * 
     * if (method != null) { if (!method.getReturnType().isAllowedInFinalCode()) { exceptions.add(new
     * ValidationException(method, "Return type not allowed in final code: " + method.getReturnType(),
     * "return type not allowed in final code:" + method.getReturnType() + "\n method: " + method)); } for (Type t :
     * method.getParameterTypes()) { if (!t.isAllowedInFinalCode()) { exceptions.add(new ValidationException(method,
     * "Parameter type not allowed in final code: " + t, "parameter type not allowed in final code:" + t + "\n method: " +
     * method)); } } } for (Local l : body.getLocals()) { Type t = l.getType(); if (!t.isAllowedInFinalCode()) {
     * exceptions.add(new ValidationException(l, "Local type not allowed in final code: " + t, "(" + method +
     * ") local type not allowed in final code: " + t + " local: " + l)); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
