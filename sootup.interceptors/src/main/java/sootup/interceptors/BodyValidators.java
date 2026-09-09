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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.validation.*;

/** Built-in sets of {@link BodyValidators} */
public enum BodyValidators {
  Default(
      new CheckInitValidator(),
      new CheckTypesValidator(),
      new FieldRefValidator(),
      new IdentityStmtsValidator(),
      new InvokeArgumentValidator(),
      new JimpleTrapValidator(),
      new LocalsValidator(),
      new MethodValidator(),
      new NewValidator(),
      new TrapsValidator(),
      new TypesValidator(),
      new UsesValidator(),
      new CheckEscapingValidator(),
      new CheckVoidLocalesValidator(),
      new InvokeValidator(),
      new ReturnStatementsValidator());

  @NonNull private final List<BodyValidator> bodyValidators;

  BodyValidators(BodyValidator... bodyValidators) {
    this.bodyValidators = Collections.unmodifiableList(Arrays.asList(bodyValidators));
  }

  @NonNull
  public List<BodyValidator> getBodyValidators() {
    return bodyValidators;
  }
}
