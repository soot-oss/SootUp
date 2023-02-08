package sootup.jimple.parser.javatestsuite.java6;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class IfElseStatementTest extends JimpleTestSuiteBase {

  @Test
  public void ifStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
                "l0 := @this: IfElseStatement",
                "l1 := @parameter0: int",
                "l2 = 0",
                "if l1 >= 42 goto label1",
                "l2 = 1",
                "label1:",
                "$stack3 = l2",
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseIfStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseIfStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseCascadingStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseCascadingInElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingInElseStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseCascadingElseIfStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  @Test
  public void ifElseCascadingElseIfInElseStatement() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfInElseStatement"));
    assertJimpleStmts(
        method,
        Stream.of(
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
                "return $stack3")
            .collect(Collectors.toList()));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "int", Collections.singletonList("int"));
  }
}
