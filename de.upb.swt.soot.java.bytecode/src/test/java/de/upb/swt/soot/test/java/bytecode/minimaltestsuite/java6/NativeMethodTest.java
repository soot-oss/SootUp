package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class NativeMethodTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "returnResult", getDeclaredClassSignature(), "int", Collections.singletonList("int"));
  }

  @Test
  public void defaultTest() {
    /**
     * Can not pass assertJimpleStmts() as body for native method is empty and current check does nt
     * allow that
     */
  }

  @Ignore
  public void ignoreTest() {
    /** Can not check Native code feature */
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
    assertTrue(sootMethod.isNative());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: NativeMethod",
            "l1 := @parameter0: int",
            "$stack2 = l1 + l1",
            "return $stack2")
        .collect(Collectors.toList());
  }
}
