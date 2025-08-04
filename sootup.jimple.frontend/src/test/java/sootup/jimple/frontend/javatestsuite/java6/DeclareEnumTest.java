package sootup.jimple.frontend.javatestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.jimple.frontend.javatestsuite.JimpleTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class DeclareEnumTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Type"));
    assertTrue(sc.isEnum());
  }
}
