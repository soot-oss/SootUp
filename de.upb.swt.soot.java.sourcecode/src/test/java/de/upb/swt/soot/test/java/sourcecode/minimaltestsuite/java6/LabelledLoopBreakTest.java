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
public class LabelledLoopBreakTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "labelledLoopBreak", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelledLoopBreak",
            "$i0 = 0",
            "$z0 = $i0 < 5",
            "if $z0 == 0 goto return",
            "$i1 = 0",
            "$z1 = $i1 < 5",
            "if $z1 == 0 goto $i4 = $i0",
            "$z2 = $i0 == 1",
            "if $z2 == 0 goto $i2 = $i1",
            "goto [?= return]",
            "$i2 = $i1",
            "$i3 = $i1 + 1",
            "$i1 = $i3",
            "goto [?= $z1 = $i1 < 5]",
            "$i4 = $i0",
            "$i5 = $i0 + 1",
            "$i0 = $i5",
            "goto [?= $z0 = $i0 < 5]",
            "return")
        .collect(Collectors.toList());
  }
}
