package de.upb.swt.soot.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class StaticInitializerTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodStaticInitializer", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @org.junit.Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass clazz = loadClass(getDeclaredClassSignature());
    /** TODO assertTrue(method.isStaticInitializer()); */
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return sootField.getName().equals("i") && sootField.isStatic();
                }));
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = <StaticInitializer: int i>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toList());
  }
}
