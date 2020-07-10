package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareEnumWithConstructorTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootClass sc = loadClass(getDeclaredClassSignature());
    assertTrue(sc.isEnum());

    final Set<SootMethod> methods = sc.getMethods();
    assertTrue(methods.stream().anyMatch(m -> m.getSignature().getName().equals("getValue")));
  }
}
