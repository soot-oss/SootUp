package de.upb.sse.sootup.test.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.sse.sootup.core.frontend.SootClassSource;
import de.upb.sse.sootup.core.signatures.PackageName;
import de.upb.sse.sootup.core.util.ImmutableUtils;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.sourcecode.frontend.WalaJavaClassProvider;
import de.upb.sse.sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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

    WalaJavaClassProvider provider = new WalaJavaClassProvider(srcDir, exclusionFilePath);
    SootClassSource classSource =
        provider.createClassSource(inputLocation, Paths.get(srcDir), type);

    assertEquals(type, classSource.getClassType());

    SootClassSource content = classSource;
    assertNotNull(content);
    assertEquals(3, content.resolveMethods().size());
    assertEquals(0, content.resolveFields().size());

    assertEquals(content, (classSource));
  }
}
