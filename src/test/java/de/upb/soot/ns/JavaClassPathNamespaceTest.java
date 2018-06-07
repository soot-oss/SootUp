package de.upb.soot.ns;

import de.upb.soot.signatures.ClassSignature;

import java.io.File;

import org.junit.Test;

/**
 * @author Manuel Benz created on 07.06.18
 */
public class JavaClassPathNamespaceTest extends AbstractNamespaceTest {

  @Test(expected = JavaClassPathNamespace.InvalidClassPathException.class)
  public void empty() {
    new JavaClassPathNamespace(getClassProvider(), "");
  }

  @Test(expected = JavaClassPathNamespace.InvalidClassPathException.class)
  public void empty2() {
    new JavaClassPathNamespace(getClassProvider(), "/a/b:äüää3");
  }

  @Test(expected = JavaClassPathNamespace.InvalidClassPathException.class)
  public void empty3() {
    new JavaClassPathNamespace(getClassProvider(), ":");
  }

  @Test(expected = JavaClassPathNamespace.InvalidClassPathException.class)
  public void empty4() {
    new JavaClassPathNamespace(getClassProvider(), ";");
  }

  @Test
  public void singleDir() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(), "target/classes");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void twoDirs() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format("target/classes%starget/classes", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void singleJar() {
    final JavaClassPathNamespace javaClassPathNamespace
        = new JavaClassPathNamespace(getClassProvider(), "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void twoJars() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(), String.format(
        "target/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar",
        File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void dirAndJar() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format("target/classes%starget/test-classes/de/upb/soot/ns/Soot-4.0-SNAPSHOT.jar", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

  @Test
  public void correctAndInvalid() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format("target/classes%s;9tß2ng2nßg2ßgn", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void wildCard() {
    final JavaClassPathNamespace javaClassPathNamespace
        = new JavaClassPathNamespace(getClassProvider(), "target/test-classes/de/upb/soot/ns/*");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND);
  }

  @Test
  public void wildCard2() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format("target/test-classes/de/upb/soot/ns/*%starget/classes", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.ns");
    testClassReceival(javaClassPathNamespace, sig, MIN_CLASSES_FOUND * 2);
  }

}