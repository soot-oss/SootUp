/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.javatestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class BooleanOperatorsTest extends MinimalTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("relationalOpEqual"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));

    method = loadMethod(getMethodSignature("relationalOpNotEqual"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));

    method = loadMethod(getMethodSignature("complementOp"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: BooleanOperators",
            "$z0 = 1",
            "if $z0 == 0 goto label1",
            "$z1 = neg $z0",
            "$z0 = $z1",
            "goto label1",
            "label1:",
            "return"));

    method = loadMethod(getMethodSignature("logicalOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));

    method = loadMethod(getMethodSignature("logicalOpOr"));

    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));

    method = loadMethod(getMethodSignature("logicalOpXor"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));

    method = loadMethod(getMethodSignature("ConditionalOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));
    method = loadMethod(getMethodSignature("conditionalOpOr"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));
    method = loadMethod(getMethodSignature("conditionalOp"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
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
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
