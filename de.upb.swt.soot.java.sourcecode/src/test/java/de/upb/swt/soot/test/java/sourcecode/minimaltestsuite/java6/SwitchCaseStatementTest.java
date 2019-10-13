/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "switchCaseStatement",
        getDeclaredClassSignature(),
        "java.lang.String",
        Collections.singletonList("java.lang.String"));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SwitchCaseStatement",
            "$r1 := @parameter0: java.lang.String",
            "$r2 = \"\"",
            "$r3 = staticinvoke <SwitchCaseStatement$Color: SwitchCaseStatement$Color valueOf(java.lang.String)>($r1)",
            "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "if $r3 == $r4 goto $r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "if $r3 == $r5 goto $r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "goto [?= $r2 = \"invalid color\"]",
            "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
            "$r2 = \"color red detected\"",
            "goto [?= return $r2]",
            "$r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
            "$r2 = \"color green detected\"",
            "goto [?= return $r2]",
            "$r2 = \"invalid color\"",
            "goto [?= return $r2]",
            "return $r2")
        .collect(Collectors.toList());
  }
}
