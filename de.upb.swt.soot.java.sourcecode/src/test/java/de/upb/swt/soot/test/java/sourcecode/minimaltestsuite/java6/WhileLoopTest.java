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
public class WhileLoopTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "whileLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "r0 := @this: WhileLoop",
            "$i0 = 10",
            "$i1 = 0",
            "$z0 = $i0 > $i1",
            "if $z0 == 0 goto return",
            "$i2 = $i0",
            "$i3 = $i0 - 1",
            "$i0 = $i3",
            "goto [?= $z0 = $i0 > $i1]",
            "return")
        .collect(Collectors.toList());
  }
}
