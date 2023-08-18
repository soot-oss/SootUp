package sootup.jimple.parser;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.jimple.parser.categories.Java8Test;

@Category(Java8Test.class)
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

    final ClassType classTypeFake =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

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
    JimpleView jv1 = new JimpleProject(inputLocation1).createView();
    final Optional<SootClass<?>> classSource1 = jv1.getClass(onlyClassNameType);
    assertTrue(classSource1.isPresent());
    final Optional<SootClass<?>> classSource2 = jv1.getClass(classType);
    assertFalse(classSource2.isPresent());
    final Optional<SootClass<?>> classSourceNon = jv1.getClass(classTypeFake);
    assertFalse(classSourceNon.isPresent());

    // files in subdir structure
    final JimpleAnalysisInputLocation inputLocation2 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir));
    JimpleView jv2 = new JimpleProject(inputLocation2).createView();
    final Optional<SootClass<?>> classSource3 = jv2.getClass(onlyClassNameType);
    assertFalse(classSource3.isPresent());

    final Optional<SootClass<?>> classSource4 = jv2.getClass(classType);
    assertTrue(classSource4.isPresent());
  }

  /**
   * Test for JimpleAnalysisInputLocation. Specifying jimple file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputJimplePath() {
    String classPath = "src/test/java/resources/jimple";
    AnalysisInputLocation<JavaSootClass> jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Library);
    JimpleView view = new JimpleProject(jimpleInputLocation).createOnDemandView();

    Collection<SootClass<?>> classes = new HashSet<>(); // Set to track the classes to check

    for (SootClass<?> aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }
}
