package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class DeclareFieldTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "display", "void", Collections.emptyList());
  }

  public MethodSignature getStaticMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "staticDisplay", "void", Collections.emptyList());
  }

  @org.junit.Test
  public void test() {
    SootMethod method1 = loadMethod(getMethodSignature());
    assertJimpleStmts(method1, expectedBodyStmts());
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getStaticMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts1());
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getModifiers().contains(FieldModifier.PRIVATE)
                        && sootField.getModifiers().contains(FieldModifier.STATIC)
                        && sootField.getName().equals("i")));
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getModifiers().contains(FieldModifier.PUBLIC)
                        && sootField.getModifiers().contains(FieldModifier.FINAL)
                        && sootField.getName().equals("s")));
  }

  /**
   *
   *
   * <pre>
   *     public void display(){
   *         System.out.println(s);
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareField",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$r2 = r0.<DeclareField: java.lang.String s>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Java\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public void staticDisplay(){
   *         System.out.println(i);
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DeclareField",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = <DeclareField: int i>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
