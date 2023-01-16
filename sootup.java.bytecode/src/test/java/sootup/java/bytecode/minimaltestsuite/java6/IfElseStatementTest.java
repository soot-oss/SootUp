package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class IfElseStatementTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void ifStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfStatement());
  }

  @Test
  public void ifElseStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseStatement());
  }

  @Test
  public void ifElseIfStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseIfStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseIfStatement());
  }

  @Test
  public void ifElseCascadingStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseCascadingStatement());
  }

  @Test
  public void ifElseCascadingInElseStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingInElseStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseCascadingInElseStatement());
  }

  @Test
  public void ifElseCascadingElseIfStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseCascadingElseIfStatement());
  }

  @Test
  public void ifElseCascadingElseIfInElseStatementTest() {
    SootMethod method = loadMethod(getMethodSignature("ifElseCascadingElseIfInElseStatement"));
    assertJimpleStmts(method, expectedBodyStmtsIfElseCascadingElseIfInElseStatement());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "int", Collections.singletonList("int"));
  }

  /**
   *
   *
   * <pre>
   *     public int ifStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             val = 1;
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfStatement() {
    return Stream.of(
            "l0 := @this: IfElseStatement",
            "l1 := @parameter0: int",
            "l2 = 0",
            "if l1 >= 42 goto label1",
            "l2 = 1",
            "label1:",
            "$stack3 = l2",
            "return $stack3")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *  public int ifElseStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             val = 1;
   *         }else{
   *             val = 2;
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public int ifElseIfStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             val = 1;
   *         }else if( a > 123){
   *             val = 2;
   *         }else{
   *             val = 3;
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseIfStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public int ifElseCascadingStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             if(a < 42){
   *                 val = 11;
   *             }else{
   *                 val = 12;
   *             }
   *         }else{
   *             val = 3;
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseCascadingStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public int ifElseCascadingInElseStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             val = 1;
   *         }else{
   *             if(a < 42){
   *                 val = 21;
   *             }else{
   *                 val = 22;
   *             }
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseCascadingInElseStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public int ifElseCascadingElseIfStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             if(a < 42){
   *                 val = 11;
   *             }else if(a > 123){
   *                 val = 12;
   *             }else{
   *                 val = 13;
   *             }
   *         }else{
   *             val = 2;
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseCascadingElseIfStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public int ifElseCascadingElseIfInElseStatement(int a){
   *         int val = 0;
   *         if(a < 42){
   *             val = 1;
   *         }else{
   *             if(a < 42){
   *                 val = 21;
   *             }else if(a > 123){
   *                 val = 22;
   *             }else{
   *                 val = 23;
   *             }
   *         }
   *         return val;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsIfElseCascadingElseIfInElseStatement() {
    return Stream.of(
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
        .collect(Collectors.toList());
  }
}
