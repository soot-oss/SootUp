package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.transform.BodyInterceptor;
import sootup.core.validation.ClassValidator;
import sootup.core.validation.ValidationException;
import sootup.core.views.View;

public class ClassValidationInterceptor implements BodyInterceptor {

  List<ClassValidator> classValidators = ClassValidators.Default.getClassValidators();

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    for (ClassValidator classValidator : classValidators) {
      try {
        Optional<? extends SootMethod> sootMethod =
            view.getMethod(builder.build().getMethodSignature());
        if (sootMethod.isPresent()) {
          Optional<? extends SootClass> declSootClass =
              view.getClass(sootMethod.get().getDeclClassType());
          if (declSootClass.isPresent()) {
            List<ValidationException> validationExceptionList = Collections.emptyList();
            classValidator.validate(declSootClass.get(), validationExceptionList, view);
          }
        }
      } catch (Exception e) {
        throw new IllegalStateException("Failed to apply " + classValidator + " to " + builder, e);
      }
    }
  }
}
