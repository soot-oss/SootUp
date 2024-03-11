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

import java.util.Objects;
import soot.MethodOrMethodContext;
import sootup.core.jimple.common.stmt.Stmt;

public class ExceptionThrowSite {
  private final Stmt unit;
  private final VarNode throwNode;
  private final MethodOrMethodContext container;

  public ExceptionThrowSite(VarNode throwNode, Stmt unit, MethodOrMethodContext container) {
    this.unit = unit;
    this.container = container;
    this.throwNode = throwNode;
  }

  public MethodOrMethodContext container() {
    return container;
  }

  public VarNode getThrowNode() {
    return throwNode;
  }

  public Stmt getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExceptionThrowSite that = (ExceptionThrowSite) o;
    return Objects.equals(unit, that.unit)
        && Objects.equals(throwNode, that.throwNode)
        && Objects.equals(container, that.container);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unit, throwNode, container);
  }
}
