package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.SootClassSource;
import sootup.core.signatures.PackageName;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

@Category(Java8Test.class)
public class WalaJavaClassProviderTest {

  @Test
  public void testCreateClassSource() {
    // TODO It's not ideal that we need to pass exclusionFilePath twice

    String srcDir = "../shared-test-resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    JavaSourcePathAnalysisInputLocation inputLocation =
        new JavaSourcePathAnalysisInputLocation(
            ImmutableUtils.immutableSet(srcDir), exclusionFilePath);
    JavaClassType type = new JavaClassType("Array1", PackageName.DEFAULT_PACKAGE);
    JavaClassType faketype = new JavaClassType("FakeJava", PackageName.DEFAULT_PACKAGE);

    WalaJavaClassProvider provider = new WalaJavaClassProvider(srcDir, exclusionFilePath);

    Optional<SootClassSource<JavaSootClass>> opFakeClass =
        provider.createClassSource(inputLocation, Paths.get(srcDir), faketype);
    assertFalse(opFakeClass.isPresent());

    Optional<SootClassSource<JavaSootClass>> opClass =
        provider.createClassSource(inputLocation, Paths.get(srcDir), type);
    assertTrue(opClass.isPresent());
    SootClassSource classSource = opClass.get();

    assertEquals(type, classSource.getClassType());

    SootClassSource content = classSource;
    assertNotNull(content);
    assertEquals(3, content.resolveMethods().size());
    assertEquals(0, content.resolveFields().size());

    assertEquals(content, (classSource));
  }
}
