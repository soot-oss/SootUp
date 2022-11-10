package de.upb.sse.sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.jimple.parser.categories.Java8Test;
import de.upb.sse.sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
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
