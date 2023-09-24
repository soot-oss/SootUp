package sootup.java.bytecode.minimaltestsuite.java8;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class RepeatingAnnotationsTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "annotaionMethod", "void", Collections.emptyList());
  }

  @Ignore
  public void annotationTest() {
    // TODO: after annotations are implemented
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: RepeatingAnnotations", "$r1 = \"\"", "$r2 = \"\"", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
