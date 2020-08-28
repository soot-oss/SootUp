package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class StaticInitializerTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodStaticInitializer", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        "<clinit>", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass clazz = loadClass(getDeclaredClassSignature());

    assertTrue(
        clazz.getFields().stream()
            .anyMatch(sootField -> sootField.getName().equals("i") && sootField.isStatic()));

    final SootMethod staticMethod = loadMethod(getStaticMethodSignature());
    assertTrue(staticMethod.isStatic());
    assertJimpleStmts(staticMethod, expectedBodyStmtsOfClinit());
  }

  public List<String> expectedBodyStmtsOfClinit() {
    return Stream.of(
            "<StaticInitializer: int i> = 5",
            "$stack0 = <StaticInitializer: int i>",
            "if $stack0 <= 4 goto label1",
            "<StaticInitializer: int i> = 4",
            "label1:",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "$stack0 = <StaticInitializer: int i>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(int)>($stack0)",
            "return")
        .collect(Collectors.toList());
  }
}
