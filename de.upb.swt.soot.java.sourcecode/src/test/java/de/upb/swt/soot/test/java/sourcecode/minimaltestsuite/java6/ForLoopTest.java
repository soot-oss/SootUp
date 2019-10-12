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
public class ForLoopTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "forLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ForLoop",
            "$i0 = 10", // j
            "$i1 = 0", // num
            "$i2 = 0", // i
            "$z0 = $i2 < $i0",
            "if $z0 == 0 goto return",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "$i5 = $i2",
            "$i6 = $i2 + 1",
            "$i2 = $i6",
            "goto [?= $z0 = $i2 < $i0]",
            "return")
        .collect(Collectors.toList());
  }
}
