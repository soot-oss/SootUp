package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java9;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class PrivateMethodInterfaceImplTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodInterfaceImpl", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(
        sootClass.getInterfaces().stream()
            .anyMatch(
                javaClassType ->
                    javaClassType.getClassName().equalsIgnoreCase("PrivateMethodInterface")));
  }

  /**
   *
   *
   * <pre>
   *
   * </pre>
   */
  /**
   *
   *
   * <pre>
   * public default void methodInterface(int a, int b) {
   * add(a, b);
   * sub(a, b);
   * System.out.println("methodInterface() in PrivateMethodInterface");
   * }
   * private void add(int a, int b){
   * System.out.println(a+b);
   * }
   * private static void sub(int a, int b){
   * System.out.println(a-b);
   * };
   *
   * </pre>
   */
  /**
   *
   *
   * <pre>
   * public void methodInterfaceImpl(){
   * methodInterface(4,2);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: PrivateMethodInterfaceImpl",
            "virtualinvoke l0.<PrivateMethodInterfaceImpl: void methodInterface(int,int)>(4, 2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
