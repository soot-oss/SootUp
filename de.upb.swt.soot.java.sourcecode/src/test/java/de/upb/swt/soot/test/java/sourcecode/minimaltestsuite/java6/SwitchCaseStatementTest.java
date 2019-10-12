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
    // TODO: [ms] error in generated jimple:
    // 1) the locals: $i0, $i1 are not set
    // 2) check: is null a valid jimple type for an uninitialized local?
    return Stream.of(
            "r0 := @this: SwitchCaseStatement",
            "$r1 := @parameter0: java.lang.String",
            "$r2 = null",
            "if $r1 == $i0 goto $r2 = \"color red detected\"",
            "if $r1 == $i1 goto $r2 = \"color green detected\"",
            "goto [?= $r2 = \"invalid color\"]",
            "$r2 = \"color red detected\"",
            "goto [?= return $r2]",
            "$r2 = \"color green detected\"",
            "goto [?= return $r2]",
            "$r2 = \"invalid color\"",
            "goto [?= return $r2]",
            "return $r2")
        .collect(Collectors.toList());
  }
}
