package sootup.codepropertygraph.propertygraph.nodes;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import java.util.Objects;
import sootup.core.jimple.common.stmt.Stmt;

public class StmtGraphNode extends PropertyGraphNode {
  private final Stmt stmt;

  public StmtGraphNode(Stmt stmt) {
    this.stmt = stmt;
  }

  public Stmt getStmt() {
    return stmt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StmtGraphNode that = (StmtGraphNode) o;
    return stmt.equivTo(that.getStmt())
        && Objects.equals(this.stmt.getPositionInfo(), that.getStmt().getPositionInfo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(stmt.toString(), stmt.getPositionInfo());
  }

  @Override
  public String toString() {
    return stmt.toString();
  }
}
