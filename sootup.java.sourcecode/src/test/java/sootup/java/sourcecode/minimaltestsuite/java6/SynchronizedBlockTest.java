package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class SynchronizedBlockTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "run", "void", Collections.emptyList());
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
            "r0 := @this: SynchronizedBlock",
            "label1:",
            "$r1 = r0.<SynchronizedBlock: java.lang.String msg>",
            "entermonitor $r1",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$r3 = r0.<SynchronizedBlock: java.lang.String msg>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto label3",
            "label2:",
            "$r4 := @caughtexception",
            "exitmonitor $r1",
            "throw $r4",
            "label3:",
            "exitmonitor $r1",
            "return",
            "catch java.lang.Throwable from label1 to label2 with label2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  @Ignore("FIXME: ms: wala does not convert traps correctly.")
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
