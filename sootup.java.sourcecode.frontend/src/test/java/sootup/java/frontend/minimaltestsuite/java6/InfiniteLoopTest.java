package sootup.java.frontend.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Markus Schmidt */
@Tag("Java8")
// FIXME: [ms] rename test (and in bytecodefrontend too) as it is not an infinite loop!
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
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
