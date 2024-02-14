package sootup.jimple.parser.javatestsuite.java6;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.java.core.JavaIdentifierFactory;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class DeclareEnumTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            JavaIdentifierFactory.getInstance()
                .getClassType(getDeclaredClassSignature().getFullyQualifiedName() + "$Type"));
    assertTrue(sc.isEnum());
  }
}
