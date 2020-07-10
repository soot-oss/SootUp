package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class IfElseStatementTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void ifStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "label1:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "goto label2",
            "label1:",
            "l2 = 2",
            "label2:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseIfStatement() {
    // TODO: unnecessary variable self assignment in jimple
    SootMethod method = loadMethod(getMethodSignature("ifElseIfStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "goto label3",
            "label1:",
            "if l1 <= 123 goto label2",
            "l2 = 2",
            "goto label3",
            "label2:",
            "l2 = 3",
            "label3:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseCascadingStatement() {
    // TODO: (same) unnecessary variable self assignment in jimple
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label2",
            "if l1 >= 42 goto label1",
            "l2 = 11",
            "goto label3",
            "label1:",
            "l2 = 12",
            "goto label3",
            "label2:",
            "l2 = 3",
            "label3:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseCascadingInElseStatement() {
    // TODO: (same) unnecessary variable self assignment in jimple
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingInElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "goto label3",
            "label1:",
            "if l1 >= 42 goto label2",
            "l2 = 21",
            "goto label3",
            "label2:",
            "l2 = 22",
            "label3:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseCascadingElseIfStatement() {
    // TODO: (same?) unnecessary variable self assignment - multiple times - in jimple
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label3",
            "if l1 >= 42 goto label1",
            "l2 = 11",
            "goto label4",
            "label1:",
            "if l1 <= 123 goto label2",
            "l2 = 12",
            "goto label4",
            "label2:",
            "l2 = 13",
            "goto label4",
            "label3:",
            "l2 = 2",
            "label4:",
            "$stack3 = l2",
            "return $stack3"));
  }

  @Test
  public void ifElseCascadingElseIfInElseStatement() {
    // TODO: (same) unnecessary variable self assignment - multiple times - in jimple
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfInElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "goto label4",
            "label1:",
            "if l1 >= 42 goto label2",
            "l2 = 21",
            "goto label4",
            "label2:",
            "if l1 <= 123 goto label3",
            "l2 = 22",
            "goto label4",
            "label3:",
            "l2 = 23",
            "label4:",
            "$stack3 = l2",
            "return $stack3"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }
}
