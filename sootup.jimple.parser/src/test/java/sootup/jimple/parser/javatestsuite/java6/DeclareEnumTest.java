package sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.java.core.JavaIdentifierFactory;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
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
