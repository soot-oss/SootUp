package de.upb.soot.namespaces;

import categories.Java9Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.types.JavaClassType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.matchers.GreaterOrEqual;

import java.util.Collection;

@Category(Java9Test.class)
public class JrtFileSystemNamespaceTest extends AbstractNamespaceTest {

  @Test
  public void getClassSource() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    final JavaClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  // FIXME findout why this test is slow > 1 sec
  public void getClassSourceModule() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    final JavaClassType sig =
        DefaultIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    ns.getClassSources(getIdentifierFactory());
  }

  @Test
  public void getClassSourcesModulePath() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
  }

  @Test
  public void discoverModules() {
    JrtFileSystemNamespace ns = new JrtFileSystemNamespace(getClassProvider());
    Collection<String> modules = ns.discoverModules();
    Assert.assertThat(modules.size(), new GreaterOrEqual<>(70));
  }
}
