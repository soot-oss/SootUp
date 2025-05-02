package sootup.jimple.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.interceptors.CopyPropagator;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class JimpleAnalysisInputLocationTest {

  @Test
  public void testClassResolving() {

    ClassType onlyClassNameType =
        new ClassType() {

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

    final ClassType classTypeFake =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return "jimple.FakeJimple";
          }

          @Override
          public String getClassName() {
            return "FakeJimple";
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
    JavaView jv1 = new JavaView(inputLocation1);
    final Optional<JavaSootClass> classSource1 = jv1.getClass(onlyClassNameType);
    assertTrue(classSource1.isPresent());
    final Optional<JavaSootClass> classSource2 = jv1.getClass(classType);
    assertFalse(classSource2.isPresent());
    final Optional<JavaSootClass> classSourceNon = jv1.getClass(classTypeFake);
    assertFalse(classSourceNon.isPresent());

    // files in subdir structure
    final JimpleAnalysisInputLocation inputLocation2 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir));
    JavaView jv2 = new JavaView(inputLocation2);
    final Optional<JavaSootClass> classSource3 = jv2.getClass(onlyClassNameType);
    assertFalse(classSource3.isPresent());

    final Optional<JavaSootClass> classSource4 = jv2.getClass(classType);
    assertTrue(classSource4.isPresent());
  }

  @Test
  public void testIfBodyInterceptorsApplied() {
    final String resourceDir = "src/test/java/resources/";
    final JimpleAnalysisInputLocation inputLocation =
        new JimpleAnalysisInputLocation(
            Paths.get(resourceDir + "/jimple/testbodyinterceptorsinjimpleinputlocation"),
            SourceType.Application,
            Arrays.asList(new CopyPropagator()));
    JavaView jv1 = new JavaView(inputLocation);
    List<SootClass> applicationClasses = jv1.getClasses().collect(Collectors.toList());
    applicationClasses.forEach(
        cls -> {
          cls.getMethods()
              .forEach(
                  m -> {
                    if (m.getSignature().getName().contains("tc1")) {
                      String s = m.getBody().toString();
                    }
                  });
        });
  }
}
