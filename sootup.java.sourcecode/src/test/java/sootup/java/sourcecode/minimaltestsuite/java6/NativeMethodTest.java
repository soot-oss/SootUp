package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class NativeMethodTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "returnResult", "int", Collections.singletonList("int"));
  }

  @Test
  public void nativeMethod() {
    SootMethod method = loadMethod(getMethodSignature());
    assertTrue(method.isNative());
  }
}
