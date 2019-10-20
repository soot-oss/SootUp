package de.upb.swt.soot.test.java.sourcecode.inputlocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.DefaultIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.frontend.WalaJavaClassProvider;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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

    Optional<? extends AbstractClassSource> classSourceOptional =
        inputLocation.getClassSource(type);
    assertTrue(classSourceOptional.isPresent());
    AbstractClassSource classSource = classSourceOptional.get();

    assertEquals(type, classSource.getClassType());

    AbstractClassSource content = classSource;
    assertNotNull(content);
    assertTrue(content instanceof ClassSource);
    assertEquals(3, ((ClassSource) content).resolveMethods().size());
    assertEquals(0, ((ClassSource) content).resolveFields().size());
  }

  @Test
  public void testGetClassProvider() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);

    ClassProvider classProvider = inputLocation.getClassProvider();
    assertTrue(classProvider instanceof WalaJavaClassProvider);
  }

  @Test
  public void testGetClassSources() {
    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    AnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);

    DefaultIdentifierFactory defaultFactories = DefaultIdentifierFactory.getInstance();
    Collection<? extends AbstractClassSource> classSources =
        inputLocation.getClassSources(defaultFactories);

    JavaClassType type = new JavaClassType("Array1", PackageName.DEFAULT_PACKAGE);
    Optional<JavaClassType> optionalFoundType =
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
