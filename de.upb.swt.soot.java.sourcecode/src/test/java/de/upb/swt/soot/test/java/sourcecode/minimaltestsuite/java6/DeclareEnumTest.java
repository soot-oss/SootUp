/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class DeclareEnumTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootClass sc =
        loadClass(
            identifierFactory.getClassType(
                getDeclaredClassSignature().getFullyQualifiedName() + "$Type"));
    assertTrue(sc.isEnum());
  }
}
