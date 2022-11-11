package sootup.java.bytecode.minimaltestsuite.java9;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class PrivateMethodInterfaceImplTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodInterfaceImpl", "void", Collections.emptyList());
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
