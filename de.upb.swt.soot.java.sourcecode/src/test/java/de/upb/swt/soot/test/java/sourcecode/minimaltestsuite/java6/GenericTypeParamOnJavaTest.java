package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class GenericTypeParamOnJavaTest extends MinimalTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "genericTypeParamOnJava", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: GenericTypeParamOnJava",
            "$r1 = new GenericTypeParamOnJava$A",
            "specialinvoke $r1.<GenericTypeParamOnJava$A: void <init>()>()",
            "specialinvoke $r1.<GenericTypeParamOnJava$A: void set(java.lang.Object)>(5)",
            "$r2 = virtualinvoke $r1.<GenericTypeParamOnJava$A: java.lang.Object get()>()",
            "$r3 = (java.lang.Integer) $r2",
            "return")
        .collect(Collectors.toList());
  }
}
