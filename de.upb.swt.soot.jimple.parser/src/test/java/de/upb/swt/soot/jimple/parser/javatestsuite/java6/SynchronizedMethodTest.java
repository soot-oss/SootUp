package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class SynchronizedMethodTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(method.isSynchronized());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SynchronizedMethod",
            "$r1 = r0.<SynchronizedMethod: SenderMethod sender>",
            "$r2 = r0.<SynchronizedMethod: java.lang.String msg>",
            "virtualinvoke $r1.<SenderMethod: void send(java.lang.String)>($r2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
