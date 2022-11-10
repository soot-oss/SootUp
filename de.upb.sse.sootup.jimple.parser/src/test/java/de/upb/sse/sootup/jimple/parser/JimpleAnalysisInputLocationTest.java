package de.upb.sse.sootup.jimple.parser;

import static org.junit.Assert.*;

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.signatures.PackageName;
import de.upb.sse.sootup.core.types.ClassType;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;

public class JimpleAnalysisInputLocationTest {

  @Test
  public void testClassResolving() {

    ClassType onlyClassNameType =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

          @Override
          public String getFullyQualifiedName() {
            return "A";
          }

          @Override
          public String getClassName() {
            return "A";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("");
          }
        };

    final ClassType classType =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

          @Override
          public String getFullyQualifiedName() {
            return "jimple.A";
          }

          @Override
          public String getClassName() {
            return "A";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("jimple");
          }
        };

    final String resourceDir = "src/test/java/resources/";

    // files direct in dir
    final JimpleAnalysisInputLocation inputLocation1 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir + "/jimple/"));
    JimpleView jv1 = new JimpleProject(inputLocation1).createView();
    final Optional<SootClass<?>> classSource1 = jv1.getClass(onlyClassNameType);
    assertTrue(classSource1.isPresent());
    final Optional<SootClass<?>> classSource2 = jv1.getClass(classType);
    assertFalse(classSource2.isPresent());

    // files in subdir structure
    final JimpleAnalysisInputLocation inputLocation2 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir));
    JimpleView jv2 = new JimpleProject(inputLocation2).createView();
    final Optional<SootClass<?>> classSource3 = jv2.getClass(onlyClassNameType);
    assertFalse(classSource3.isPresent());

    final Optional<SootClass<?>> classSource4 = jv2.getClass(classType);
    assertTrue(classSource4.isPresent());
  }
}
