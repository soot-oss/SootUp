/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class IfElseStatementTest extends MinimalSourceTestSuiteBase {

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
            "r0 := @this: IfElseStatement",
            "$i0 := @parameter0: int",
            "$i1 = 0",
            "$z0 = $i0 < 42",
            "if $z0 == 0 goto label1",
            "$i1 = 1",
            "goto label1",
            "label1:",
            "return $i1")
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
            "return $i1")
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
            "return $i1")
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
            "return $i1")
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
            "return $i1")
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
            "return $i1")
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
            "return $i1")
        .collect(Collectors.toList());
  }
}
