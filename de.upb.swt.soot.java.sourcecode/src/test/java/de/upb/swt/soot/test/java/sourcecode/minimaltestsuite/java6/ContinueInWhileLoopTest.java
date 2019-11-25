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
public class ContinueInWhileLoopTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "continueInWhileLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ContinueInWhileLoop",
            "$i0 = 0",
            "label1:",
            "$z0 = $i0 < 10",
            "if $z0 == 0 goto label4",
            "$z1 = $i0 == 5",
            "if $z1 == 0 goto label2",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "goto label3",
            "label2:",
            "$i3 = $i0",
            "$i4 = $i0 + 1",
            "$i0 = $i4",
            "label3:",
            "goto label1",
            "label4:",
            "return")
        .collect(Collectors.toList());
  }
}
