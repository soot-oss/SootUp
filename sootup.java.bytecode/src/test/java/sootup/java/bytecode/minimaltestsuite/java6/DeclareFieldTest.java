package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareFieldTest extends MinimalBytecodeTestSuiteBase {

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
            "l0 := @this: DeclareField",
            "$stack1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"Java\")",
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
            "l0 := @this: DeclareField",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "$stack1 = <DeclareField: int i>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(int)>($stack1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
