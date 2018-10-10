package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/** @author Manuel Benz created on 06.06.18 */
@Category(Java8Test.class)
public class PathBasedNamespaceTest extends AbstractNamespaceTest {

  @Test(expected = IllegalArgumentException.class)
  public void failsOnFile() {
    // TODO adapt to new testing folder structure
    PathBasedNamespace
        .createForClassContainer(Paths.get("target/test-classes/de/upb/soot/namespaces/PathBasedNamespaceTest.class"));
  }

  @Test
  public void classNotFound() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(baseDir);
    final ClassSignature sig = getSignatureFactory().getClassSignature("NotExisting", "de.upb.soot.namespaces");
    final Optional<ClassSource> classSource = pathBasedNamespace.getClassSource(sig);
    Assert.assertFalse(classSource.isPresent());
  }

  @Test
  public void testFolder() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(baseDir);
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }

  @Test
  public void testJar() {
    // TODO adapt to new testing folder structure
    Path jar = Paths.get("target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(jar);
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(pathBasedNamespace, sig, CLASSES_IN_JAR);
  }
}
