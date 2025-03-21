package sootup.jimple.frontend.javatestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class DeclareEnumWithConstructorTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Number"));
    assertTrue(sc.isEnum());

    final Set<SootMethod> methods = (Set<SootMethod>) sc.getMethods();
    assertTrue(methods.stream().anyMatch(m -> m.getSignature().getName().equals("getValue")));
  }
}
