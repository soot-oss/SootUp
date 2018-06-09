package de.upb.soot.namespaces;

import categories.Java8Test;
import categories.Java9Test;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java9Test.class)

public class JrtFSNamespaceTest extends AbstractNamespaceTest {

  @Test
  public void getClassSource() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    final ClassSignature sig = getSignatureFactory().getClassSignature("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  //todo findout why this test is slow, extremly > 1 sec
  public void getClassSourceModule() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    final ClassSignature sig = new ModuleSignatureFactory() {
    }.getClassSignature("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);

  }

  @Test
  public void getClassSources() {
    JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
    ns.getClasses(getSignatureFactory());

  }
}