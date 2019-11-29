/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class AbstractClassTest extends MinimalBytecodeTestSuiteBase {

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
            "l0 := @this: AbstractClass",
            "$stack2 = new AbstractClass",
            "specialinvoke $stack2.<AbstractClass: void <init>()>()",
            "l1 = $stack2",
            "virtualinvoke l1.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
