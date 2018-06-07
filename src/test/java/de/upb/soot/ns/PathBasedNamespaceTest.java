package de.upb.soot.ns;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 06.06.18 */
public class PathBasedNamespaceTest extends AbstractNamespaceTest {

  @Test(expected = IllegalArgumentException.class)
  public void failsOnFile() {
    // TODO adapt to new testing folder structure
    PathBasedNamespace.createForClassContainer(getClassProvider(),
        Paths.get("target/test-classes/de/upb/soot/ns/PathBasedNamespaceTest.class"));
  }

  public void classNotFound() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(getClassProvider(), baseDir);
    final ClassSignature sig = getSignatureFactory().getClassSignature("NotExisting", "de.upb.soot.ns");
    final Optional<ClassSource> classSource = pathBasedNamespace.getClassSource(sig);
    Assert.assertFalse(classSource.isPresent());
  }

  @Test
  public void testFolder() {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/classes/");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(getClassProvider(), baseDir);
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    final Optional<ClassSource> clazz = pathBasedNamespace.getClassSource(sig);

    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = pathBasedNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    // Since we continuously add new classes, we can just state a lower bound here
    Assert.assertTrue(classSources.size() > 20);
  }

  @Test
  public void testJar() {
    // TODO adapt to new testing folder structure
    Path jar = Paths.get("target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar");
    PathBasedNamespace pathBasedNamespace = PathBasedNamespace.createForClassContainer(getClassProvider(), jar);
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = pathBasedNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = pathBasedNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertEquals(20, classSources.size());
  }

}
