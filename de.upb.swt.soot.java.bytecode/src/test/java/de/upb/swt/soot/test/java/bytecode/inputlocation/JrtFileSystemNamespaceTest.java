package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.fail;

import categories.Java9Test;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java9Test.class)
public class JrtFileSystemNamespaceTest extends AnalysisInputLocationTest {

  @Test
  public void getClassSource() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    final ClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourceModule() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    final JavaClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    ns.getClassSources(getIdentifierFactory());
  }

  @Test
  public void getClassSourcesModulePath() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    fail("implement");
  }

  @Test
  public void discoverModules() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    Collection<String> modules = ns.discoverModules();
    Assert.assertTrue(modules.size() > 65);
  }
}
