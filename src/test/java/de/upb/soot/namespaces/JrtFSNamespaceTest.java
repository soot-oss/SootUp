package de.upb.soot.namespaces;

import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java9Test;

@Category(Java9Test.class)

public class JrtFSNamespaceTest extends AbstractNamespaceTest {

  @Test
  public void getClassSource() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    final ClassSignature sig = getSignatureFactory().getClassSignature("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  // todo findout why this test is slow, extremly > 1 sec
  public void getClassSourceModule() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    final ClassSignature sig = new ModuleSignatureFactory() {
    }.getClassSignature("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);

  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    ns.getClasses(getSignatureFactory());

  }

  @Test
  public void getClassSourcesModulepath() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory(){};
    ns.getClasses(signatureFactory);

  }
}