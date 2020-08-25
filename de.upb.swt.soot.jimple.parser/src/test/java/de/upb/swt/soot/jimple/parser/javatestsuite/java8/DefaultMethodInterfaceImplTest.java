package de.upb.swt.soot.jimple.parser.javatestsuite.java8;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class DefaultMethodInterfaceImplTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "interfaceMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getDefaultMethodSignature() {
    return identifierFactory.getMethodSignature(
        "defaultInterfaceMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DefaultMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Method interfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DefaultMethodInterfaceImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Method defaultInterfaceMethod() is implemented\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** TODO Update the source code once default methods in WALA are supported */
  @Ignore
  public void test() {

    assertJimpleStmts(loadMethod(getMethodSignature()), expectedBodyStmts());
    assertJimpleStmts(loadMethod(getMethodSignature()), expectedBodyStmts());
    assertJimpleStmts(loadMethod(getDefaultMethodSignature()), expectedBodyStmts1());

    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getInterfaces().stream()
            .anyMatch(
                javaClassType ->
                    javaClassType.getClassName().equalsIgnoreCase("DefaultMethodInterface")));
  }
}
