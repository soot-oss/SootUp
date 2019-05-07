package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class WalaJavaClassProviderTest {

  @Test
  public void testCreateClassSource() {
    // TODO It's not ideal that we need to pass exclusionFilePath twice

    String srcDir = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    JavaSourcePathNamespace namespace =
        new JavaSourcePathNamespace(Utils.immutableSet(srcDir), exclusionFilePath);
    JavaClassType type = new JavaClassType("Array1", PackageSignature.DEFAULT_PACKAGE);

    WalaJavaClassProvider provider = new WalaJavaClassProvider(exclusionFilePath);
    ClassSource classSource = provider.createClassSource(namespace, Paths.get(srcDir), type);

    assertEquals(type, classSource.getClassType());

    IClassSourceContent content = classSource.getContent();
    assertNotNull(content);
    assertEquals(3, content.resolveMethods(type).size());
    assertEquals(0, content.resolveFields(type).size());

    assertEquals(content, provider.getContent(classSource));
  }

  @Test
  public void testGetHandledFileType() {
    assertEquals(FileType.JAVA, new WalaJavaClassProvider().getHandledFileType());
  }
}
