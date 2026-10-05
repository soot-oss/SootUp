package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2026 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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
import org.jspecify.annotations.NonNull;

/** An immutable snapshot of debug variable bindings active at a statement. */
public final class LocalVariableScope {
  private static final LocalVariableScope EMPTY = new LocalVariableScope(List.of());
  @NonNull private final List<LocalVariableInfo> variables;

  private LocalVariableScope(List<LocalVariableInfo> variables) {
    this.variables = List.copyOf(variables);
  }

  @NonNull
  public static LocalVariableScope empty() {
    return EMPTY;
  }

  @NonNull
  public static LocalVariableScope of(@NonNull List<LocalVariableInfo> variables) {
    return variables.isEmpty() ? EMPTY : new LocalVariableScope(variables);
  }

  @NonNull
  public List<LocalVariableInfo> getVariables() {
    return variables;
  }
}
