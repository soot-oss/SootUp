package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class DeclareInnerClassTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodDisplayOuter", "void", Collections.emptyList());
  }

  public MethodSignature getInnerMethodSignature() {
    return identifierFactory.getMethodSignature(
        identifierFactory.getClassType(
            getDeclaredClassSignature().getFullyQualifiedName() + "$InnerClass"),
        "methodDisplayInner",
        "void",
        Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());

    method = loadMethod(getInnerMethodSignature());
    assertJimpleStmts(method, expectedInnerClassBodyStmts());
  }

  /**
   *
   *
   * <pre>
   *     public void methodDisplayOuter(){
   *         System.out.println("methodDisplayOuter");
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareInnerClass",
            "r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke r1.<java.io.PrintStream: void println(java.lang.String)>(\"methodDisplayOuter\")",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void methodDisplayInner(){
   *             System.out.println("methodDisplayInner");
   *         }
   * </pre>
   */
  public List<String> expectedInnerClassBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareInnerClass$InnerClass",
            "r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke r1.<java.io.PrintStream: void println(java.lang.String)>(\"methodDisplayInner\")",
            "return")
        .collect(Collectors.toList());
  }
}
