/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$r1 = \"RED\"",
            "$r2 = \"\"",
            "$r3 = staticinvoke <SwitchCaseStatement$Color: SwitchCaseStatement$Color valueOf(java.lang.String)>($r1)",
            "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "if $r3 == $r4 goto $r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "if $r3 == $r5 goto $r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "goto [?= $r2 = \"invalid color\"]",
            "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"color red detected\"",
            "goto [?= return]",
            "$r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "$r2 = \"color green detected\"",
            "goto [?= return]",
            "$r2 = \"invalid color\"",
            "goto [?= return]",
            "return"),
        getMethodSignature("switchCaseStatementEnum"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: SwitchCaseStatement",
            "$i0 = 2",
            "$r1 = null",
            "lookupswitch($i0) {     "
                + "case 1: goto goto [?= return];     "
                + "case 2: goto null;     "
                + "case 3: goto null;     "
                + "default: goto $r1 = \"number 1 detected\"; }",
            "$r1 = \"number 1 detected\"",
            "goto [?= return]",
            "$r1 = \"number 2 detected\"",
            "goto [?= return]",
            "$r1 = \"number 3 detected\"",
            "goto [?= return]",
            "return"),
        getMethodSignature("switchCaseStatementInt"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
