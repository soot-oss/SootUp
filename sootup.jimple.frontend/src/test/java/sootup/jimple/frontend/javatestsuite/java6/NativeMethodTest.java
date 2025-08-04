package sootup.jimple.frontend.javatestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class NativeMethodTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "returnResult", "int", Collections.singletonList("int"));
  }

  @Test
  public void nativeMethod() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertTrue(sootMethod.isNative());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: NativeMethod",
            "specialinvoke l0.<java.lang.Object: void <init>()>();",
            "return")
        .collect(Collectors.toList());
  }
}
