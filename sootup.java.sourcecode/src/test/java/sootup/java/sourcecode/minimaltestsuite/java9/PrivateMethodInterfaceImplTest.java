package sootup.java.sourcecode.minimaltestsuite.java9;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class PrivateMethodInterfaceImplTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodInterfaceImpl", "void", Collections.emptyList());
  }

  @Ignore
  /** TODO WALA does not support Java9 constructs */
  public void ignoreTest() {
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
   *     public void methodInterfaceImpl(){
   * methodInterface(4,2);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: PrivateMethodInterfaceImpl",
            "interfaceinvoke r0.<PrivateMethodInterface: void methodInterface(int,int)>(4, 2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
