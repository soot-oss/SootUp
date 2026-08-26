package sootup.apk.frontend.entrypoint;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * The name, return type and parameter types of a single Android framework-invoked callback (e.g.
 * {@code Activity#onCreate(android.os.Bundle):void}), in the string form accepted by {@link
 * sootup.core.IdentifierFactory#getMethodSignature(sootup.core.types.ClassType, String, String,
 * List)}.
 */
public final class LifecycleMethod {

  @NonNull private final String name;
  @NonNull private final String returnType;
  @NonNull private final List<String> parameterTypes;

  public LifecycleMethod(
      @NonNull String name, @NonNull String returnType, @NonNull List<String> parameterTypes) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(parameterTypes));
  }

  @NonNull
  public String getName() {
    return name;
  }

  @NonNull
  public String getReturnType() {
    return returnType;
  }

  @NonNull
  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public String toString() {
    return returnType + " " + name + parameterTypes;
  }
}
