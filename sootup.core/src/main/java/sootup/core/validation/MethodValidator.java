package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Markus Schmidt and others
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
import org.jspecify.annotations.NonNull;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

public class MethodValidator implements BodyValidator {
  /**
   * Checks the following invariants on this Jimple body:
   *
   * <ol>
   *   <li>static initializer should have 'static' modifier
   * </ol>
   *
   * @return a list of found validation exceptions
   */
  @Override
  public List<ValidationException> validate(@NonNull Body body, @NonNull View view) {
    List<ValidationException> exceptions = new ArrayList<>();

    MethodSignature methodSignature = body.getMethodSignature();
    Optional<? extends SootMethod> optionalSootMethod = view.getMethod(methodSignature);
    if (!optionalSootMethod.isPresent()) {
      throw new IllegalStateException("The Method of this Body should be found in the View.");
    }

    SootMethod method = optionalSootMethod.get();
    if (method.isAbstract()) {
      throw new IllegalStateException("An abstract Method does not have Body.");
    }

    if (view.getIdentifierFactory()
            .isStaticInitializerSubSignature(methodSignature.getSubSignature())
        && !method.isStatic()) {
      exceptions.add(
          new ValidationException(
              method,
              methodSignature
                  + " should be static! Static initializer without 'static'('0x8') modifier"
                  + " will cause problem when running on android platform: "
                  + "\"<clinit> is not flagged correctly wrt/ static\"!"));
    }

    return exceptions;
  }
}
