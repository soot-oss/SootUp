/** @author: Markus Schmidt */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java7;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SwitchCaseStatementWithStringTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "switchCaseStatementString", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    // TODO: INFO: the generated jimple contains a bug - $i1,$i1,$i3 are undefined/not set
    return Stream.of(
            "r0 := @this: SwitchCaseStatementWithString",
            "$r1 = \"something\"",
            "$i0 = 0",
            "if $r1 == $i1 goto $i0 = 1",
            "if $r1 == $i2 goto $i0 = 2",
            "if $r1 == $i3 goto $i0 = 3",
            "goto [?= $i4 = 0 - 1]",
            "$i0 = 1",
            "goto [?= return]",
            "$i0 = 2",
            "goto [?= return]",
            "$i0 = 3",
            "goto [?= return]",
            "$i4 = 0 - 1",
            "$i0 = $i4",
            "return")
        .collect(Collectors.toList());
  }
}
