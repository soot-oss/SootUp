package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class StaticInitializerTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodStaticInitializer", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass clazz = loadClass(getDeclaredClassSignature());

    assertTrue(
        clazz.getFields().stream()
            .anyMatch(sootField -> sootField.getName().equals("i") && sootField.isStatic()));

    final SootMethod staticMethod =
        loadMethod(identifierFactory.getStaticInitializerSignature(getDeclaredClassSignature()));
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

  /**
   *
   *
   * <pre>
   * static void methodStaticInitializer(){
   * System.out.println(i);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "$stack0 = <StaticInitializer: int i>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(int)>($stack0)",
            "return")
        .collect(Collectors.toList());
  }
}
