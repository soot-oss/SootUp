package de.upb.soot.namespaces;

import de.upb.soot.signatures.ClassSignature;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 * @author Manuel Benz created on 07.06.18
 */
@Category(Java8Test.class)
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
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(), TARGET_CLASSES_DIR);
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR);
  }

  @Test
  public void twoDirs() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_CLASSES_DIR + "%s" + TARGET_TEST_CLASSES_DIR, File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, (int) (CLASSES_IN_JAR * 1.2f));
  }

  @Test
  public void singleJar() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR, CLASSES_IN_JAR);
  }

  @Test
  public void twoJars() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar%s" + TARGET_TEST_CLASSES_DIR
            + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.copy.jar", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR * 2, CLASSES_IN_JAR * 2);
  }

  @Test
  public void dirAndJar() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_CLASSES_DIR + "%s" + TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar",
            File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR * 2);
  }

  @Test
  public void correctAndInvalid() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_CLASSES_DIR + "%s;9tß2ng2nßg2ßgn", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR);
  }

  @Test
  public void wildCard() {
    final JavaClassPathNamespace javaClassPathNamespace
        = new JavaClassPathNamespace(getClassProvider(), TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/*");
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR * 2, CLASSES_IN_JAR * 2);
  }

  @Test
  public void wildCard2() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/*%s" + TARGET_CLASSES_DIR, File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR * 3);
  }

  @Test
  public void duplicateEntry() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar%s" + TARGET_TEST_CLASSES_DIR
            + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar", File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR, CLASSES_IN_JAR);
  }

  @Test
  public void duplicateEntry2() {
    final JavaClassPathNamespace javaClassPathNamespace = new JavaClassPathNamespace(getClassProvider(),
        String.format(TARGET_TEST_CLASSES_DIR + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar%s" + TARGET_TEST_CLASSES_DIR
            + "/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar%s" + TARGET_TEST_CLASSES_DIR
            + "/de/upb/../upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar", File.pathSeparator, File.pathSeparator));
    final ClassSignature sig = getSignatureFactory().getClassSignature("PathBasedNamespace", "de.upb.soot.namespaces");
    testClassReceival(javaClassPathNamespace, sig, CLASSES_IN_JAR, CLASSES_IN_JAR);
  }
}