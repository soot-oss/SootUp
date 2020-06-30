/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class IfElseStatementTest extends MinimalSourceTestSuiteBase {

  @Test
  public void ifStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label1",
            "label1:",
            "return $i1"));
  }

  @Test
  public void ifElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label2",
            "label1:",
            "$i1 = 2",
            "label2:",
            "return $i1"));
  }

  @Test
  public void ifElseIfStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseIfStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label3",
            "label1:",
            "$z1 = $i0 > 123",
            "if $z1 == 0 goto label2",
            "$i1 = 2",
            "goto label3",
            "label2:",
            "$i1 = 3",
            "label3:",
            "return $i1"));
  }

  @Test
  public void ifElseCascadingStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label3",
            "$z1 = $i0 < 42",
            "if $z1 == 0 goto label1",
            "$i1 = 11",
            "goto label2",
            "label1:",
            "$i1 = 12",
            "label2:",
            "goto label4",
            "label3:",
            "$i1 = 3",
            "label4:",
            "return $i1"));
  }

  @Test
  public void ifElseCascadingInElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingInElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label3",
            "label1:",
            "$z1 = $i0 < 42",
            "if $z1 == 0 goto label2",
            "$i1 = 21",
            "goto label3",
            "label2:",
            "$i1 = 22",
            "label3:",
            "return $i1"));
  }

  @Test
  public void ifElseCascadingElseIfStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label4",
            "$z1 = $i0 < 42",
            "if $z1 == 0 goto label1",
            "$i1 = 11",
            "goto label3",
            "label1:",
            "$z2 = $i0 > 123",
            "if $z2 == 0 goto label2",
            "$i1 = 12",
            "goto label3",
            "label2:",
            "$i1 = 13",
            "label3:",
            "goto label5",
            "label4:",
            "$i1 = 2",
            "label5:",
            "return $i1"));
  }

  @Test
  public void ifElseCascadingElseIfInElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfInElseStatement"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label4",
            "label1:",
            "$z1 = $i0 < 42",
            "if $z1 == 0 goto label2",
            "$i1 = 21",
            "goto label4",
            "label2:",
            "$z2 = $i0 > 123",
            "if $z2 == 0 goto label3",
            "$i1 = 22",
            "goto label4",
            "label3:",
            "$i1 = 23",
            "label4:",
            "return $i1"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }
}
