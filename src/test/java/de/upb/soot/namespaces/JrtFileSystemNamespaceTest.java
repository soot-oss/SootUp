package de.upb.soot.namespaces;

import categories.Java9Test;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.matchers.GreaterOrEqual;

@Category(Java9Test.class)
public class JrtFileSystemNamespaceTest extends AbstractNamespaceTest {

  @Test
  public void getClassSource() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    final JavaClassSignature sig = getSignatureFactory().getClassSignature("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  // FIXME findout why this test is slow > 1 sec
  public void getClassSourceModule() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    final JavaClassSignature sig =
        new ModuleSignatureFactory() {}.getClassSignature("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    ns.getClassSources(getSignatureFactory());
  }

  @Test
  public void getClassSourcesModulepath() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory() {};
  }

  @Test
  public void discoverModules() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    Collection<String> modules = ns.discoverModules();
    Assert.assertThat(modules.size(), new GreaterOrEqual<>(70));
  }
}
