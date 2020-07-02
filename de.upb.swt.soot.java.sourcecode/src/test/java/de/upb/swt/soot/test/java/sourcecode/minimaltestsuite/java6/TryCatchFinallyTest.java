/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Javadoc;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class TryCatchFinallyTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "tryCatchFinally", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

//TODO automate like following for all tests.
  /**
  <pre>
  <code>
  public class TryCatchFinally {
    public void tryCatchFinally() {
        String str = "";
        try {
            str = "this is try block";
            int i = 0;
            i++;
            System.out.println(i);
        } catch (Exception e) {
            str = "this is catch block";
        } finally {
            str = "this is finally block";
        }
    }
  }
  <code>
  </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TryCatchFinally",
            "$r1 = \"\"",
            "$r1 = \"this is try block\"",
            "$i0 = 0",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i0)",
            "goto label1",
            "$r3 := @caughtexception",
            "$r4 = $r3",
            "$r1 = \"this is catch block\"",
            "label1:",
            "$r1 = \"this is finally block\"",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
