package sootup.java.bytecode.interceptors.typeresolving;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2023 Raja Vallée-Rai and others
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

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;

public class StmtLocalPair {
  private final Stmt stmt;
  private final Local local;

  public StmtLocalPair(Stmt stmt, Local local) {
    this.stmt = stmt;
    this.local = local;
  }

  public Stmt getStmt() {
    return this.stmt;
  }

  public Local getLocal() {
    return this.local;
  }

  @Override
  public String toString() {
    return "(" + stmt + "," + local + ")";
  }
}
