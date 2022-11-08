package de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import org.junit.Test;

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
