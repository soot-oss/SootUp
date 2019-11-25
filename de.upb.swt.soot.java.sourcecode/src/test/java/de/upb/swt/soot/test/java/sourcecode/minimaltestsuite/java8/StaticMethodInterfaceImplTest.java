package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class StaticMethodInterfaceImplTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodStaticMethodInterfaceImpl",
        getDeclaredClassSignature(),
        "void",
        Collections.emptyList());
  }

  private MethodSignature getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        "initStatic", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    loadMethod(expectedBodyStmts1(), getStaticMethodSignature());
    SootMethod staticMethod = loadMethod(expectedBodyStmts1(), getStaticMethodSignature());
    assertTrue(staticMethod.isStatic() && staticMethod.getName().equals("initStatic"));
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equals("StaticMethodInterface");
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StaticMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside display - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"Inside initStatic - StaticmethodInterfaceImpl\")",
            "return")
        .collect(Collectors.toList());
  }
}
