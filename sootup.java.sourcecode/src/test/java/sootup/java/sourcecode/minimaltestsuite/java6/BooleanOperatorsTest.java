package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class BooleanOperatorsTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("relationalOpEqual"));
    assertJimpleStmts(method, expectedBodyStmtsRelationalOpEqual());

    method = loadMethod(getMethodSignature("relationalOpNotEqual"));
    assertJimpleStmts(method, expectedBodyStmtsRelationalOpNotEqual());

    method = loadMethod(getMethodSignature("complementOp"));
    assertJimpleStmts(method, expectedBodyStmtsComplementOp());

    method = loadMethod(getMethodSignature("logicalOpAnd"));
    assertJimpleStmts(method, expectedBodyStmtsLogicalOpAnd());

    method = loadMethod(getMethodSignature("logicalOpOr"));

    assertJimpleStmts(method, expectedBodyStmtsLogicalOpOr());

    method = loadMethod(getMethodSignature("logicalOpXor"));
    assertJimpleStmts(method, expectedBodyStmtsLogicalOpXor());

    method = loadMethod(getMethodSignature("ConditionalOpAnd"));
    assertJimpleStmts(method, expectedBodyStmtsConditionalOpAnd());
    method = loadMethod(getMethodSignature("conditionalOpOr"));
    assertJimpleStmts(method, expectedBodyStmtsConditionalOpOr());
    method = loadMethod(getMethodSignature("conditionalOp"));
    assertJimpleStmts(method, expectedBodyStmtsConditionalOp());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void relationalOpEqual(){
   *         int i =  0;
   *         while (i<=10){
   *             i++;
   *             if (i==5){
   *                 break;
   *             }
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsRelationalOpEqual() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$i0 = 0",
            "label1:",
            "$z0 = $i0 <= 10",
            "if $z0 == 0 goto label3",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "$z1 = $i0 == 5",
            "if $z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void relationalOpNotEqual(){
   *         int i =  0;
   *         String str = "";
   *         while (i<10){
   *             i++;
   *             if (i!=5){
   *                 str = "i != 5";
   *             }
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsRelationalOpNotEqual() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$i0 = 0",
            "$r1 = \"\"",
            "label1:",
            "$z0 = $i0 < 10",
            "if $z0 == 0 goto label3",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "$z1 = $i0 != 5",
            "if $z1 == 0 goto label2",
            "$r1 = \"i != 5\"",
            "goto label2",
            "label2:",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void complementOp(){
   *         boolean b = true;
   *         if(b){
   *             b = !b;
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsComplementOp() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "if $z0 == 0 goto label1",
            "$z1 = neg $z0",
            "$z0 = $z1",
            "goto label1",
            "label1:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void logicalOpAnd(){
   *         boolean a = true;
   *         boolean b = true;
   *         boolean c = false;
   *         boolean d = false;
   *         String str = "";
   *
   *         if(a & b){
   *             str = "A";
   *         }
   *
   *         if (c & d){
   *             str = "B";
   *         }
   *
   *         if (a & c){
   *             str = "C";
   *         }
   *
   *         if (d & b){
   *             str = "D";
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLogicalOpAnd() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "$z1 = 1",
            "$z2 = 0",
            "$z3 = 0",
            "$r1 = \"\"",
            "$i0 = (int) $z0",
            "$i1 = (int) $z1",
            "$i2 = $i0 & $i1",
            "if $i2 == 0 goto label1",
            "$r1 = \"A\"",
            "goto label1",
            "label1:",
            "$i3 = (int) $z2",
            "$i4 = (int) $z3",
            "$i5 = $i3 & $i4",
            "if $i5 == 0 goto label2",
            "$r1 = \"B\"",
            "goto label2",
            "label2:",
            "$i6 = (int) $z0",
            "$i7 = (int) $z2",
            "$i8 = $i6 & $i7",
            "if $i8 == 0 goto label3",
            "$r1 = \"C\"",
            "goto label3",
            "label3:",
            "$i9 = (int) $z3",
            "$i10 = (int) $z1",
            "$i11 = $i9 & $i10",
            "if $i11 == 0 goto label4",
            "$r1 = \"D\"",
            "goto label4",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void logicalOpOr(){
   *         boolean a = true;
   *         boolean b = true;
   *         boolean c = false;
   *         boolean d = false;
   *         String str = "";
   *
   *         if(a | b){
   *             str = "A";
   *         }
   *
   *         if (c | d){
   *             str = "B";
   *         }
   *
   *         if (a | c){
   *             str = "C";
   *         }
   *
   *         if (d | b){
   *             str = "D";
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLogicalOpOr() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "$z1 = 1",
            "$z2 = 0",
            "$z3 = 0",
            "$r1 = \"\"",
            "$i0 = (int) $z0",
            "$i1 = (int) $z1",
            "$i2 = $i0 | $i1",
            "if $i2 == 0 goto label1",
            "$r1 = \"A\"",
            "goto label1",
            "label1:",
            "$i3 = (int) $z2",
            "$i4 = (int) $z3",
            "$i5 = $i3 | $i4",
            "if $i5 == 0 goto label2",
            "$r1 = \"B\"",
            "goto label2",
            "label2:",
            "$i6 = (int) $z0",
            "$i7 = (int) $z2",
            "$i8 = $i6 | $i7",
            "if $i8 == 0 goto label3",
            "$r1 = \"C\"",
            "goto label3",
            "label3:",
            "$i9 = (int) $z3",
            "$i10 = (int) $z1",
            "$i11 = $i9 | $i10",
            "if $i11 == 0 goto label4",
            "$r1 = \"D\"",
            "goto label4",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void logicalOpXor(){
   *         boolean a = true;
   *         boolean b = true;
   *         boolean c = false;
   *         boolean d = false;
   *         String str = "";
   *
   *         if(a ^ b){
   *             str = "A";
   *         }
   *
   *         if (c ^ d){
   *             str = "B";
   *         }
   *
   *         if (a ^ c){
   *             str = "C";
   *         }
   *
   *         if (d ^ b){
   *             str = "D";
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsLogicalOpXor() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "$z1 = 1",
            "$z2 = 0",
            "$z3 = 0",
            "$r1 = \"\"",
            "$i0 = (int) $z0",
            "$i1 = (int) $z1",
            "$i2 = $i0 ^ $i1",
            "if $i2 == 0 goto label1",
            "$r1 = \"A\"",
            "goto label1",
            "label1:",
            "$i3 = (int) $z2",
            "$i4 = (int) $z3",
            "$i5 = $i3 ^ $i4",
            "if $i5 == 0 goto label2",
            "$r1 = \"B\"",
            "goto label2",
            "label2:",
            "$i6 = (int) $z0",
            "$i7 = (int) $z2",
            "$i8 = $i6 ^ $i7",
            "if $i8 == 0 goto label3",
            "$r1 = \"C\"",
            "goto label3",
            "label3:",
            "$i9 = (int) $z3",
            "$i10 = (int) $z1",
            "$i11 = $i9 ^ $i10",
            "if $i11 == 0 goto label4",
            "$r1 = \"D\"",
            "goto label4",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void ConditionalOpAnd(){
   *         boolean a = true;
   *         boolean b = true;
   *         boolean c = false;
   *         boolean d = false;
   *         String str = "";
   *
   *         if(a && b){
   *             str = "A";
   *         }
   *
   *         if (c && d){
   *             str = "B";
   *         }
   *
   *         if (a && c){
   *             str = "C";
   *         }
   *
   *         if (d && b){
   *             str = "D";
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsConditionalOpAnd() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "$z1 = 1",
            "$z2 = 0",
            "$z3 = 0",
            "$r1 = \"\"",
            "if $z0 == 0 goto label01",
            "$z4 = $z1",
            "goto label02",
            "label01:",
            "$z4 = 0",
            "label02:",
            "if $z4 == 0 goto label03",
            "$r1 = \"A\"",
            "goto label03",
            "label03:",
            "if $z2 == 0 goto label04",
            "$z5 = $z3",
            "goto label05",
            "label04:",
            "$z5 = 0",
            "label05:",
            "if $z5 == 0 goto label06",
            "$r1 = \"B\"",
            "goto label06",
            "label06:",
            "if $z0 == 0 goto label07",
            "$z6 = $z2",
            "goto label08",
            "label07:",
            "$z6 = 0",
            "label08:",
            "if $z6 == 0 goto label09",
            "$r1 = \"C\"",
            "goto label09",
            "label09:",
            "if $z3 == 0 goto label10",
            "$z7 = $z1",
            "goto label11",
            "label10:",
            "$z7 = 0",
            "label11:",
            "if $z7 == 0 goto label12",
            "$r1 = \"D\"",
            "goto label12",
            "label12:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void conditionalOpOr(){
   *         boolean a = true;
   *         boolean b = true;
   *         boolean c = false;
   *         boolean d = false;
   *         String str = "";
   *
   *         if(a || b){
   *             str = "A";
   *         }
   *
   *         if (c || d){
   *             str = "B";
   *         }
   *
   *         if (a || c){
   *             str = "C";
   *         }
   *
   *         if (d || b){
   *             str = "D";
   *         }
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsConditionalOpOr() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "$z1 = 1",
            "$z2 = 0",
            "$z3 = 0",
            "$r1 = \"\"",
            "if $z0 == 0 goto label01",
            "$z4 = 1",
            "goto label02",
            "label01:",
            "$z4 = $z1",
            "label02:",
            "if $z4 == 0 goto label03",
            "$r1 = \"A\"",
            "goto label03",
            "label03:",
            "if $z2 == 0 goto label04",
            "$z5 = 1",
            "goto label05",
            "label04:",
            "$z5 = $z3",
            "label05:",
            "if $z5 == 0 goto label06",
            "$r1 = \"B\"",
            "goto label06",
            "label06:",
            "if $z0 == 0 goto label07",
            "$z6 = 1",
            "goto label08",
            "label07:",
            "$z6 = $z2",
            "label08:",
            "if $z6 == 0 goto label09",
            "$r1 = \"C\"",
            "goto label09",
            "label09:",
            "if $z3 == 0 goto label10",
            "$z7 = 1",
            "goto label11",
            "label10:",
            "$z7 = $z1",
            "label11:",
            "if $z7 == 0 goto label12",
            "$r1 = \"D\"",
            "goto label12",
            "label12:",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void conditionalOp(){
   *         int i = 5;
   *         String str = "";
   *         str = i <10 ? "i less than 10" : "i greater than 10";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsConditionalOp() {
    return Stream.of(
            "r0 := @this: BooleanOperators",
            "$i0 = 5",
            "$r1 = \"\"",
            "$z0 = $i0 < 10",
            "if $z0 == 0 goto label1",
            "$r2 = \"i less than 10\"",
            "goto label2",
            "label1:",
            "$r2 = \"i greater than 10\"",
            "label2:",
            "$r1 = $r2",
            "return")
        .collect(Collectors.toList());
  }
}
