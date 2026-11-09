package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class SynchronizedBlockTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "run", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  /**  <pre>
   * public void run()
   * {
   * synchronized(msg)
   * {
   * System.out.println(msg);
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: SynchronizedBlock",
            "l1 = this.<SynchronizedBlock: java.lang.String msg>",
            "entermonitor l1",
            "label1:",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = this.<SynchronizedBlock: java.lang.String msg>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack3)",
            "exitmonitor l1",
            "label2:",
            "goto label5",
            "label3:",
            "$stack5 := @caughtexception",
            "l2 = $stack5",
            "exitmonitor l1",
            "label4:",
            "throw l2",
            "label5:",
            "return",
            "catch java.lang.Throwable from label1 to label2 with label3",
            "catch java.lang.Throwable from label3 to label4 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
