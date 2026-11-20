package sootup.core.inputlocation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2026 SootUp contributors
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

import sootup.core.model.LocalVariableScope;

/** Extended analysis scope options for an {@link AnalysisInputLocation}. */
public enum AnalysisExtendedScope {
  /**
   * Preserves LocalVariableTable debug metadata from bytecode, creating statement-level {@link
   * LocalVariableScope} metadata on Jimple statements.
   */
  LocalVariableTable
}
