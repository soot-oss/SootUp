/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class AbstractClassTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    // The SuperClass is the abstract one
    SootClass superClazz = loadClass(clazz.getSuperclass().get());
    assertTrue(superClazz.isAbstract());
    super.defaultTest();
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "abstractClass", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AbstractClass",
            "$r1 = new AbstractClass",
            "specialinvoke $r1.<AbstractClass: void <init>()>()",
            "virtualinvoke $r1.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
