package sootup.java.sourcecode.inputlocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class JavaSourcePathNamespaceTest {

  @Test
  public void testGetClassSource() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);
    JavaClassType type = new JavaClassType("Array1", PackageName.DEFAULT_PACKAGE);

    final JavaView view = new JavaView(inputLocation);

    Optional<JavaSootClass> clazz = view.getClass(type);
    assertTrue(clazz.isPresent());
    JavaSootClassSource classSource = clazz.get().getClassSource();

    assertEquals(type, classSource.getClassType());

    JavaSootClassSource content = classSource;
    assertNotNull(content);
    assertEquals(3, content.resolveMethods().size());
    assertEquals(0, content.resolveFields().size());
  }

  @Ignore
  public void testGetClassSources() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);

    JavaIdentifierFactory defaultFactories = JavaIdentifierFactory.getInstance();

    final JavaView view = new JavaView(inputLocation);

    Collection<? extends AbstractClassSource> classSources =
        view.getClasses().stream().map(jsc -> jsc.getClassSource()).collect(Collectors.toList());

    ClassType type = new JavaClassType("Array1", PackageName.DEFAULT_PACKAGE);
    Optional<ClassType> optionalFoundType =
        classSources.stream()
            .filter(classSource -> classSource.getClassType().equals(type))
            .map(AbstractClassSource::getClassType)
            .findFirst();
    assertTrue(optionalFoundType.isPresent() && optionalFoundType.get().equals(type));

    // Also check that there are more classes than just this one.
    // We don't check for a specific number here to avoid breaking tests
    // Whenever we add a test class.
    assertTrue(classSources.size() > 1);
  }

  /**
   * Test for JavaSourcePathAnalysisInputLocation. Specifying all input source files with source
   * type as Library. Expected - All input classes are of source type Library.
   */
  @Test
  public void testInputSourcePathLibraryMode() {

    String classPath = "../shared-test-resources/java-miniapps/src/";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(SourceType.Library, classPath);
    JavaView view = new JavaView(inputLocation);

    Set<SootClass> classes = new HashSet<>(); // Set to track the classes to check
    for (SootClass aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        classes.add(aClass);
      }
    }

    TestCase.assertEquals("User Defined class found, expected none", 0, classes.size());
  }
}
