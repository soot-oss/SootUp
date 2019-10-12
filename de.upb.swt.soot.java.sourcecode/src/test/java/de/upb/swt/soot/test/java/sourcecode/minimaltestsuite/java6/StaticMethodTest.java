/*Author Kaustubh Kelkar*/

package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class StaticMethodTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "staticMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    assertTrue(method.isStatic());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"static method\")",
            "return")
        .collect(Collectors.toList());
  }
}
