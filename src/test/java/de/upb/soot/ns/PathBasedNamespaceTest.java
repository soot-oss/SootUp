package de.upb.soot.ns;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        return new ClassSource(ns, PathUtils.pathFromSignature(sourcePath, signatureFac)) {
        };
      }

      @Override
      public boolean handlesType(Path fileName) {
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
    Path baseDir = Paths.get("target/test-classes/");
    PathBasedNamespace pathBasedNamespace = new PathBasedNamespace(dummyProvider, baseDir);
    final ClassSignature sig = signatureFac.getClassSignature("PathBasedNamespaceTest", "de.upb.soot.ns");
    final ClassSource clazz = pathBasedNamespace.getClassSource(sig);

    Assert.assertNotNull(clazz);
    Assert.assertEquals(clazz.getClassSignature(), sig);
  }
}
