package de.upb.swt.soot.test.java.bytecode.inputlocation;

import categories.Java9Test;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java9Test.class)
// TODO: [ms] unignore when JavaModules are implemented
@Ignore("implement JavaModules")
public class JrtFileSystemNamespaceTest extends AnalysisInputLocationTest {

  @Test
  public void getClassSource() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    final ClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(ns, sig, 1);
  }

  @Test
  // FIXME [AD] find out why this test is slow > 1 sec
  public void getClassSourceModule() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");
    testClassReceival(ns, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    final JavaProject project = JavaProject.builder(new JavaLanguage(8)).addClassPath(ns).build();
    final JavaView view = project.createFullView();

    view.getClasses().stream().map(JavaSootClass::getClassSource);
  }

  @Test
  public void getClassSourcesModulePath() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
  }

  @Test
  public void discoverModules() {
    JrtFileSystemAnalysisInputLocation ns = new JrtFileSystemAnalysisInputLocation();
    Collection<String> modules = ns.discoverModules();
    Assert.assertTrue(modules.size() > 65);
  }
}
