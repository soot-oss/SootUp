package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Akshita Dubey and others
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
import sootup.core.model.SootMethod;
import sootup.core.types.NullType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;

/**
 * Validates classes to make sure that all method signatures are valid and does not contain
 * impossible method modifier combinations
 *
 * @author Akshita Dubey
 */
public class MethodDeclarationValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {

    for (SootMethod sm : sc.getMethods()) {
      if (sc.isConcrete()) {
        List<Type> parameterTypes = sm.getParameterTypes();
        for (Type tp : parameterTypes) {
          if (tp instanceof NullType) {
            exceptions.add(new ValidationException(sm, "Null parameter types are invalid"));
          }
          if (tp instanceof VoidType) {
            exceptions.add(new ValidationException(sm, "Void parameter types are invalid"));
          }
        }
      }
      if (sm.isAbstract()) {
        if (sm.isFinal()) {
          exceptions.add(new ValidationException(sm, "Method cannot be Abstract and Final"));
        }
        if (sm.isNative()) {
          exceptions.add(new ValidationException(sm, "Method cannot be Abstract and Native"));
        }
        if (sm.isPrivate()) {
          exceptions.add(new ValidationException(sm, "Method cannot be Abstract and Private"));
        }
        if (sm.isStatic()) {
          exceptions.add(new ValidationException(sm, "Method cannot be Abstract and Static"));
        }
        if (sm.isSynchronized()) {
          exceptions.add(new ValidationException(sm, "Method cannot be Abstract and Synchronized"));
        }
      }

      if (sc.isInterface()) {
        if (sm.isProtected()) {
          exceptions.add(
              new ValidationException(sm, "Method cannot be an interface and protected"));
        }
        if (sm.isSynchronized()) {
          exceptions.add(
              new ValidationException(sm, "Method cannot be an interface and synchronized"));
        }
        if (sm.isFinal()) {
          exceptions.add(new ValidationException(sm, "Method cannot be an interface and final"));
        }
        if (sm.isNative()) {
          exceptions.add(new ValidationException(sm, "Method cannot be an interface and native"));
        }
      }

      if ((sm.isPrivate() || sm.isProtected()) && (sm.isPublic()) || sm.isProtected()) {
        exceptions.add(
            new ValidationException(sm, "Method can only be either Public, Protected or Private"));
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
