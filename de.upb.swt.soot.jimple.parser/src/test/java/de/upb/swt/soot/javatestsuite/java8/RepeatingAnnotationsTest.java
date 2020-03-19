package de.upb.swt.soot.javatestsuite.java8;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class RepeatingAnnotationsTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "annotaionMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void defaultTest() {}

  @Ignore
  public void annotationTest() {
    super.defaultTest();
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: RepeatingAnnotations", "$r1 = \"\"", "$r2 = \"\"", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
