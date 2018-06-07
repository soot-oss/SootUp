package de.upb.soot.ns;

import java.io.File;

import org.junit.Test;

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
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void twoDirs() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), String.format("target/classes%starget/classes", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void singleJar() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void twoJars() {
    final JavaCPNamespace javaCPNamespace = new JavaCPNamespace(getClassProvider(), String.format(
        "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar",
        File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void dirAndJar() {
    final JavaCPNamespace javaCPNamespace = new JavaCPNamespace(getClassProvider(),
        String.format("target/classes%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void correctAndInvalid() {
    final JavaCPNamespace javaCPNamespace
        = new JavaCPNamespace(getClassProvider(), String.format("target/classes%s;9tß2ng2nßg2ßgn", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaCPNamespace, sig, MIN_CLASSES_FOUND);
  }

}