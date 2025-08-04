package sootup.java.bytecode.frontend.minimaltestsuite.java8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;

public class PackageAnnotationTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void testPackageAnnotation() {
    ClassType packageInfoType = identifierFactory.getClassType("dummyPackage.package-info");
    JavaSootClass sootClass = loadClass(packageInfoType);
    ClassType annotationPackageType =
        identifierFactory.getClassType("dummyPackage.AnnotationPackage");
    assertEquals(
        Collections.singletonList(
            new AnnotationUsage(annotationPackageType, Collections.emptyMap())),
        sootClass.getAnnotations());
  }
}
