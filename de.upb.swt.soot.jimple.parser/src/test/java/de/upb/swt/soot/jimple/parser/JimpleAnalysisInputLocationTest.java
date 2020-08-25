package de.upb.swt.soot.jimple.parser;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
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
    final Optional<? extends AbstractClassSource> classSource1 =
        inputLocation1.getClassSource(onlyClassNameType);
    assertTrue(classSource1.isPresent());
    final Optional<? extends AbstractClassSource> classSource2 =
        inputLocation1.getClassSource(classType);
    assertFalse(classSource2.isPresent());

    // files in subdir structure
    final JimpleAnalysisInputLocation inputLocation2 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir));
    final Optional<? extends AbstractClassSource> classSource3 =
        inputLocation2.getClassSource(onlyClassNameType);
    assertFalse(classSource3.isPresent());

    final Optional<? extends AbstractClassSource> classSource4 =
        inputLocation2.getClassSource(classType);
    assertTrue(classSource4.isPresent());
  }
}
