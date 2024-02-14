package sootup.jimple.parser.javatestsuite.java6;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaIdentifierFactory;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class DeclareEnumWithConstructorTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            JavaIdentifierFactory.getInstance()
                .getClassType(getDeclaredClassSignature().getFullyQualifiedName() + "$Number"));
    assertTrue(sc.isEnum());

    final Set<SootMethod> methods = (Set<SootMethod>) sc.getMethods();
    assertTrue(methods.stream().anyMatch(m -> m.getSignature().getName().equals("getValue")));
  }
}
