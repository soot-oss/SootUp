package de.upb.swt.soot.test.java.bytecode.inputlocation;

import categories.Java9Test;
import de.upb.swt.soot.core.ModuleIdentifierFactory;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.matchers.GreaterOrEqual;

@Category(Java9Test.class)
public class JrtFileSystemNamespaceTest extends AbstractAnalysisInputLocationTest {

  @Test
  public void getClassSource() {
    JrtFileSystemAnalysisInputLocation ns =
        new JrtFileSystemAnalysisInputLocation(
            getClassProvider(), new DefaultSourceTypeSpecifier());
    final JavaClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  // FIXME findout why this test is slow > 1 sec
  public void getClassSourceModule() {
    JrtFileSystemAnalysisInputLocation ns =
        new JrtFileSystemAnalysisInputLocation(
            getClassProvider(), new DefaultSourceTypeSpecifier());
    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemAnalysisInputLocation ns =
        new JrtFileSystemAnalysisInputLocation(
            getClassProvider(), new DefaultSourceTypeSpecifier());
    ns.getClassSources(getIdentifierFactory());
  }

  @Test
  public void getClassSourcesModulePath() {
    JrtFileSystemAnalysisInputLocation ns =
        new JrtFileSystemAnalysisInputLocation(
            getClassProvider(), new DefaultSourceTypeSpecifier());
  }

  @Test
  public void discoverModules() {
    JrtFileSystemAnalysisInputLocation ns =
        new JrtFileSystemAnalysisInputLocation(
            getClassProvider(), new DefaultSourceTypeSpecifier());
    Collection<String> modules = ns.discoverModules();
    Assert.assertThat(modules.size(), new GreaterOrEqual<>(70));
  }
}
