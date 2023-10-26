package sootup.core.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann and others
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

import java.io.IOException;
import javax.annotation.Nonnull;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;

/**
 * A class which holds the information of a methods body and knows how to produce a Body for a
 * SootMethod.
 */
public interface BodySource {

  /**
   * Returns a filled-out body for the given SootMethod. This may be an expensive operation.
   *
   * @param modifiers The collection of modifiers which are needed by BodyInterceptors to modify the
   *     body accordingly.
   */
  @Nonnull
  Body resolveBody(@Nonnull Iterable<MethodModifier> modifiers)
      throws ResolveException, IOException;

  /** @return returns the default value of the Annotation for this method */
  Object resolveAnnotationsDefaultValue();

  @Nonnull
  MethodSignature getSignature();
}
