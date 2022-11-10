package de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.util.Utils;
import de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt */
@Category(Java8Test.class)
// FIXME: [ms] rename test (and in bytecodefrontend too)
public class InfiniteLoopTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "stmtLoop", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *   void stmtLoop(){
   * infloop:
   * break infloop;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: InfiniteLoop", "goto label1", "label1:", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    Utils.printJimpleForTest(method);
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
