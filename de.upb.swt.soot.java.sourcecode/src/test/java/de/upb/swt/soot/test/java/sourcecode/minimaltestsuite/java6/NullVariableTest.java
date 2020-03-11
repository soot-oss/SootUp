/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.Utils;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NullVariableTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "nullVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: NullVariable", "$r1 = null", "return")
        .collect(Collectors.toList());
  }

  @Ignore
  public void test() {
    // FIXME see InstructionConverter.convertUnaryOpInstruction(...)
    SootMethod method = loadMethod(getMethodSignature());
    assertEquals(
        "[java.lang.String $r1, NullVariable r0, r0 := @this: NullVariable, $r1 = null, return]",
        Utils.filterJimple(method.getBody().toString()));
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
