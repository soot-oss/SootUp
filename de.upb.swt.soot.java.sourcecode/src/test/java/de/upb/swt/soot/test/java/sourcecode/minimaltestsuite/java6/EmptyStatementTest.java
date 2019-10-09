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
public class EmptyStatementTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "emptyStatement", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of("r0 := @this: EmptyStatement", "$i0 = 5", "return")
        .collect(Collectors.toList());
  }
}
