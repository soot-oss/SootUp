package de.upb.soot.javasourcecodefrontend.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.soot.core.frontend.ClassSource;
import de.upb.soot.core.inputlocation.FileType;
import de.upb.soot.core.signatures.PackageName;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.util.ImmutableUtils;
import de.upb.soot.javasourcecodefrontend.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.nio.file.Paths;
import org.junit.Assert;
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

    WalaJavaClassProvider provider = new WalaJavaClassProvider(exclusionFilePath);
    ClassSource classSource = provider.createClassSource(inputLocation, Paths.get(srcDir), type);

    Assert.assertEquals(type, classSource.getClassType());

    ClassSource content = classSource;
    assertNotNull(content);
    assertEquals(3, content.resolveMethods().size());
    assertEquals(0, content.resolveFields().size());

    assertEquals(content, (classSource));
  }

  @Test
  public void testGetHandledFileType() {
    Assert.assertEquals(FileType.JAVA, new WalaJavaClassProvider().getHandledFileType());
  }
}
