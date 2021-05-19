package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.List;
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

  // assert whether two sets contain the same objects
  public static void assertSetsEquiv(Set expected, Set actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    if (expected.size() != actual.size()) {
      System.out.println("Expected size is not equal to actual size: ");
      System.out.println("expected size of set: " + expected.size());
      System.out.println("actual size of set: " + actual.size());
    }
    assertEquals(expected.size(), actual.size());
    boolean condition = true;
    for (Object o : actual) {
      if (!expected.contains(o)) {
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

  // assert whether two lists are equal
  public static void assertListsEquiv(List expected, List actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    if (expected.size() != actual.size()) {
      System.out.println("Expected size is not equal to actual size: ");
      System.out.println("expected size of list: " + expected.size());
      System.out.println("actual size of list: " + actual.size());
    }
    assertEquals(expected.size(), actual.size());
    boolean condition = true;
    for (Object o : actual) {
      int idx = actual.indexOf(o);
      if (!(expected.get(idx) == o)) {
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

  // assert whether two trap lists are equal
  public static void assertTrapsEquiv(List<Trap> expected, List<Trap> actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    if (expected.size() != actual.size()) {
      System.out.println("Expected size is not equal to actual size: ");
      System.out.println("expected size of list: " + expected.size());
      System.out.println("actual size of list: " + actual.size());
    }
    assertEquals(expected.size(), actual.size());
    boolean condition = true;
    for (Trap trap : actual) {
      boolean hasSameTrap = false;
      Stmt beginStmt = trap.getBeginStmt();
      Stmt endStmt = trap.getEndStmt();
      Stmt hanlderStmt = trap.getHandlerStmt();
      for (Trap anTrap : expected) {
        if (anTrap.getBeginStmt() == beginStmt
            && anTrap.getEndStmt() == endStmt
            && anTrap.getHandlerStmt() == hanlderStmt) {
          hasSameTrap = true;
          break;
        }
      }
      if (!hasSameTrap) {
        condition = false;
        break;
      }
    }
    if (!condition) {
      System.out.println("expected:");
      for (Trap trap : expected) {
        System.out.println("    " + trap);
      }
      System.out.println("actual:");
      for (Trap trap : actual) {
        System.out.println("    " + trap);
      }
    }
    assertTrue(condition);
  }
}
