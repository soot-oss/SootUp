package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class αρετηTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "αρετηAsClassName", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public class αρετη {
   *     public void αρετηAsClassName(){
   *         System.out.println("this is αρετη class");
   *     }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    // TODO: likely the Jimple lines need a change when it works until here
    return Stream.of(
            "l0 := @this: \\u03b1\\u03c1\\u03b5\\u03c4\\u03b7",
            "$stack1 = <java.lang.System: java.io.PrintStream; out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"this is \\u03b1\\u03c1\\u03b5\\u03c4\\u03b7 class\")",
            "return")
        .collect(Collectors.toList());
  }

  @Ignore
  public void test() {
    // fails due to missing unicode support in some filesystems
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
