package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SynchronizedMethodTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Ignore
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    // FIXME [ms] method is not synchronized
    // 1. check bytecode if the compiler optimized it away
    // 2. hopefully no second
    assertTrue(method.isSynchronized());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SynchronizedMethod",
            "$stack2 = l0.<SynchronizedMethod: SenderMethod sender>",
            "$stack1 = l0.<SynchronizedMethod: java.lang.String msg>",
            "virtualinvoke $stack2.<SenderMethod: void send(java.lang.String)>($stack1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
