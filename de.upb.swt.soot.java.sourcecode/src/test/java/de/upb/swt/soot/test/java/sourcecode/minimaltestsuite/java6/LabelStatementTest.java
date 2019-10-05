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
public class LabelStatementTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "labelStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "r0 := @this: LabelStatement",
            "$i0 = 20",
            "$i1 = 1",
            "$z0 = $i1 < $i0",
            "if $z0 == 0 goto return",
            "$i2 = $i1 % 10",
            "$z1 = $i2 == 0",
            "if $z1 == 0 goto $i3 = $i1",
            "goto [?= return]",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
            "goto [?= $z0 = $i1 < $i0]",
            "return")
        .collect(Collectors.toList());
  }
}
