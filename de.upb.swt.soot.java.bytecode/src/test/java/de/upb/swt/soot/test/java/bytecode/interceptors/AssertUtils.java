package de.upb.swt.soot.test.java.bytecode.interceptors;

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

  // assert whether two stmtsset contain the same stmts
  public static void assertStmtsSetEquiv(Set<Stmt> expected, Set<Stmt> actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    if (expected.size() != actual.size()) {
      System.out.println("expected size of set: " + expected.size());
      System.out.println("actual size of set: " + actual.size());
    }
    boolean condition = true;
    for (Stmt stmt : actual) {
      if (!expected.contains(stmt)) {
        condition = false;
        break;
      }
    }

    if (!condition) {
      System.out.println("expected:");
      System.out.println(expected);
      System.out.println("actual:");
      System.out.println(actual);
    }
    assertTrue(condition);
  }
}
