package de.upb.swt.soot.test.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Zun Wang
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

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.Set;

/** @author Zun Wang */
public class AssertUtils {

  // assert whether two bodys have the same locals
  public static void assertLocalsEquiv(Body expected, Body actual) {
    Set<Local> expected_locals = expected.getLocals();
    Set<Local> actual_locals = actual.getLocals();
    assertNotNull(expected_locals);
    assertNotNull(actual_locals);
    assertEquals(expected_locals.size(), actual_locals.size());
    boolean isEqual = true;
    for (Local local : actual_locals) {
      if (!expected_locals.contains(local)) {
        isEqual = false;
        break;
      }
    }
    assertTrue(isEqual);
  }

  // assert whether two bodys have the same stmtGraphs
  public static void assertStmtGraphEquiv(Body expected, Body actual) {
    StmtGraph expected_SG = expected.getStmtGraph();
    StmtGraph actual_SG = actual.getStmtGraph();
    assertNotNull(expected_SG);
    assertNotNull(actual_SG);
    final boolean condition = expected_SG.equivTo(actual_SG);
    if (!condition) {
      System.out.println("expected:");
      System.out.println(Lists.newArrayList(expected_SG.iterator()));
      System.out.println("actual:");
      System.out.println(Lists.newArrayList(actual_SG.iterator()) + "\n");

      for (Stmt s : expected_SG) {
        System.out.println(s + " => " + expected_SG.successors(s));
      }
      System.out.println();
      for (Stmt s : actual_SG) {
        System.out.println(s + " => " + actual_SG.successors(s));
      }
    }
    assertTrue(condition);
  }
}
