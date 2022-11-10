package de.upb.sse.sootup.test.java.sourcecode.inputlocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.frontend.AbstractClassSource;
import de.upb.sse.sootup.core.frontend.SootClassSource;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.signatures.PackageName;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.core.util.ImmutableUtils;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.core.views.JavaView;
import de.upb.sse.sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class JavaSourcePathNamespaceTest {

  @Test
  public void testGetClassSource() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);
    JavaClassType type = new JavaClassType("Array1", PackageName.DEFAULT_PACKAGE);

    final JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(inputLocation).build();
    final JavaView view = project.createView();

    Optional<JavaSootClass> clazz = view.getClass(type);
    assertTrue(clazz.isPresent());
    AbstractClassSource<JavaSootClass> classSource = clazz.get().getClassSource();

    assertEquals(type, classSource.getClassType());

    AbstractClassSource<JavaSootClass> content = classSource;
    assertNotNull(content);
    assertTrue(content instanceof SootClassSource);
    assertEquals(3, ((SootClassSource<JavaSootClass>) content).resolveMethods().size());
    assertEquals(0, ((SootClassSource<JavaSootClass>) content).resolveFields().size());
  }

  @Ignore
  public void testGetClassSources() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);

    JavaIdentifierFactory defaultFactories = JavaIdentifierFactory.getInstance();

    final JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(inputLocation).build();
    final JavaView view = project.createView();

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
}
