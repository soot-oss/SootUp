/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.pag;

import sootup.core.model.SootMethod;
import sootup.core.types.Type;

/**
 * Represents a simple variable node in the pointer assignment graph that is not associated with any
 * particular method invocation.
 *
 * @author Ondrej Lhotak
 */
public class GlobalVarNode extends VarNode {
  public GlobalVarNode(Object variable, Type t) {
    super(variable, t);
  }

  @Override
  public VarNode base() {
    return this;
  }

  @Override
  public SootMethod getMethod() {
    return null;
  }

  public String toString() {
    return "GlobalVarNode " + getNumber() + " " + variable;
  }
}
