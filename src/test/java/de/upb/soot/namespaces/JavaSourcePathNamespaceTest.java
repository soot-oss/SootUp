package de.upb.soot.namespaces;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.java.WalaJavaClassProvider;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(Java8Test.class)
public class JavaSourcePathNamespaceTest {

  @Test
  public void testGetClassSource() {
    String srcDir = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    INamespace namespace =
        new JavaSourcePathNamespace(Utils.immutableSet(srcDir), exclusionFilePath);
    JavaClassType type = new JavaClassType("Array1", PackageSignature.DEFAULT_PACKAGE);

    Optional<? extends AbstractClassSource> classSourceOptional = namespace.getClassSource(type);
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
    String srcDir = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    INamespace namespace =
        new JavaSourcePathNamespace(Utils.immutableSet(srcDir), exclusionFilePath);

    IClassProvider classProvider = namespace.getClassProvider();
    assertTrue(classProvider instanceof WalaJavaClassProvider);
  }

  @Test
  public void testGetClassSources() {
    String srcDir = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    INamespace namespace =
        new JavaSourcePathNamespace(Utils.immutableSet(srcDir), exclusionFilePath);

    DefaultFactories defaultFactories = DefaultFactories.create();
    Collection<? extends AbstractClassSource> classSources =
        namespace.getClassSources(
            defaultFactories.getSignatureFactory(), defaultFactories.getTypeFactory());

    JavaClassType type = new JavaClassType("Array1", PackageSignature.DEFAULT_PACKAGE);
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
