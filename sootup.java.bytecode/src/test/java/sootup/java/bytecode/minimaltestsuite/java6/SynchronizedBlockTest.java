package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
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
            "l0 := @this: SynchronizedBlock",
            "$stack3 = l0.<SynchronizedBlock: java.lang.String msg>",
            "l1 = $stack3",
            "entermonitor $stack3",
            "label1:",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "$stack4 = l0.<SynchronizedBlock: java.lang.String msg>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.String)>($stack4)",
            "$stack6 = l1",
            "exitmonitor $stack6",
            "label2:",
            "goto label5",
            "label3:",
            "$stack7 := @caughtexception",
            "l2 = $stack7",
            "$stack8 = l1",
            "exitmonitor $stack8",
            "label4:",
            "throw l2",
            "label5:",
            "return",
            "catch java.lang.Throwable from label1 to label2 with label3",
            "catch java.lang.Throwable from label3 to label4 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
