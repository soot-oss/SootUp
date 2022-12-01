package sootup.jimple.parser.javatestsuite.java9;

import static org.junit.Assert.assertTrue;

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
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class PrivateMethodInterfaceImplTest extends JimpleTestSuiteBase {

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

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: PrivateMethodInterfaceImpl",
            "virtualinvoke l0.<PrivateMethodInterfaceImpl: void methodInterface(int,int)>(4, 2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
