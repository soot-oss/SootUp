package de.upb.soot.ns;

import com.sun.nio.zipfs.ZipPath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

/** @author Manuel Benz created on 06.06.18 */
public class PathBasedNamespaceTest {

  private SignatureFactory signatureFac;

  private IClassProvider dummyProvider;

  @Before
  public void setUp() throws Exception {
    signatureFac = new SignatureFactory() {
    };
    dummyProvider = new IClassProvider() {
      @Override
      public ClassSource getClass(INamespace ns, Path sourcePath) throws SootClassNotFoundException {
        Path sigPath = null;
        if (sourcePath instanceof ZipPath) {
          sigPath = sourcePath.getRoot().relativize(sourcePath);
        } else {
          sigPath = Paths.get("target/classes").relativize(sourcePath);
        }

        return new ClassSource(ns, PathUtils.signatureFromPath(sigPath, signatureFac)) {
        };
      }

      @Override
      public boolean handlesFile(Path fileName) {
        return PathUtils.hasExtension(fileName, "class");
      }
    };
  }

  @Test(expected = IllegalArgumentException.class)
  public void failsOnFile() {
    // TODO adapt to new testing folder structure
    new PathBasedNamespace(dummyProvider, Paths.get("target/test-classes/de/upb/soot/ns/PathBasedNamespaceTest.class"));
  }

  @Test(expected = SootClassNotFoundException.class)
  public void failsOnClassNotFound() throws SootClassNotFoundException {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedNamespace pathBasedNamespace = new PathBasedNamespace(dummyProvider, baseDir);
    final ClassSignature sig = signatureFac.getClassSignature("NotExisting", "de.upb.soot.ns");
    pathBasedNamespace.getClassSource(sig);
  }

  @Test
  public void testFolder() throws SootClassNotFoundException, IOException {
    // TODO adapt to new testing folder structure
    Path baseDir = Paths.get("target/classes/");
    PathBasedNamespace pathBasedNamespace = new PathBasedNamespace(dummyProvider, baseDir);
    final ClassSignature sig = signatureFac.getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    final ClassSource clazz = pathBasedNamespace.getClassSource(sig);

    Assert.assertNotNull(clazz);
    Assert.assertEquals(sig, clazz.getClassSignature());

    final Collection<ClassSource> classSources = pathBasedNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertEquals(20, classSources.size());
  }

  @Test
  public void testJar() throws SootClassNotFoundException, IOException {
    // TODO adapt to new testing folder structure
    Path jar = Paths.get("target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar");
    PathBasedNamespace pathBasedNamespace = new PathBasedNamespace(dummyProvider, jar);
    final ClassSignature sig = signatureFac.getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final ClassSource clazz = pathBasedNamespace.getClassSource(sig);
    Assert.assertNotNull(clazz);
    Assert.assertEquals(sig, clazz.getClassSignature());

    final Collection<ClassSource> classSources = pathBasedNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertEquals(20, classSources.size());
  }
}
