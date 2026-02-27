package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;

@Slf4j
@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PAGStmtVisitor extends AbstractStmtVisitor {

  PAG PAG;

  @Override
  public void caseAssignStmt(JAssignStmt stmt) {
    val left = stmt.getLeftOp();
    val right = stmt.getRightOp();
    val leftNode = NodeFactory.createNode(left);
    val rightNode = NodeFactory.createNode(right);
    if (leftNode.isPresent() && rightNode.isPresent()) {
      val source = rightNode.get();
      val target = leftNode.get();
      PAG.addEdge(source, target);
    } else {
      log.warn("Missing nodes for left: {} <- right: {}", left, right);
    }
  }
}
