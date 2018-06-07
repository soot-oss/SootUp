package de.upb.soot.ns;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

/**
 * @author Manuel Benz created on 07.06.18
 */
public class JavaCPNamespaceTest extends AbstractNamespaceTest {

  @Test(expected = JavaCPNamespace.InvalidClassPathException.class)
  public void empty() {
    new JavaCPNamespace(getClassProvider(), "");
  }

  @Test(expected = JavaCPNamespace.InvalidClassPathException.class)
  public void empty2() {
    new JavaCPNamespace(getClassProvider(), "/a/b:äüää3");
  }

  @Test(expected = JavaCPNamespace.InvalidClassPathException.class)
  public void empty3() {
    new JavaCPNamespace(getClassProvider(), ":");
  }

  @Test(expected = JavaCPNamespace.InvalidClassPathException.class)
  public void empty4() {
    new JavaCPNamespace(getClassProvider(), ";");
  }

  @Test
  public void singleDir() {
    final JavaCPNamespace javaCPNamespace = new JavaCPNamespace(getClassProvider(), "target/classes");

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertTrue(classSources.size() > 20);
  }

  @Test
  public void twoDirs() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), String.format("target/classes%starget/classes", File.pathSeparator));

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertTrue(classSources.size() > 40);
  }

  @Test
  public void singleJar() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar");

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertEquals(20, classSources.size());
  }

  @Test
  public void twoJars() {
    final JavaCPNamespace javaCPNamespace = new JavaCPNamespace(getClassProvider(), String.format(
        "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar",
        File.pathSeparator));

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertEquals(40, classSources.size());
  }

  @Test
  public void dirAndJar() {
    final JavaCPNamespace javaCPNamespace = new JavaCPNamespace(getClassProvider(),
        String.format("target/classes%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar", File.pathSeparator));

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertTrue(classSources.size() > 40);
  }

  @Test
  public void correctAndInvalid() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), String.format("target/classes%s;9tß2ng2nßg2ßgn", File.pathSeparator));

    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");

    final Optional<ClassSource> clazz = javaCPNamespace.getClassSource(sig);
    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = javaCPNamespace.getClassSources();

    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertTrue(classSources.size() > 20);
  }

}