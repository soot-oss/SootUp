package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import categories.Java9Test;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collection;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java9Test.class)
public class JrtFileSystemNamespaceTest extends AnalysisInputLocationTest {

  @Test
  public void getClassSource() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    final ClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(inputLocation, sig, 1);
  }

  @Test
  public void getClassSourcesClasspath() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    Collection<? extends AbstractClassSource<JavaSootClass>> classSources =
        inputLocation.getClassSources(getIdentifierFactory());

    final ClassType sig = getIdentifierFactory().getClassType("java.lang.System");
    testClassReceival(inputLocation, sig, 1);
  }

  @Test
  public void getClassSourceModule() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    final JavaClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");
    testClassReceival(inputLocation, sig, 1);
  }

  @Test
  public void getClassSourcesModulePath() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    fail("implement");
  }

  @Test
  public void discoverModules() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    Collection<ModuleSignature> modules = inputLocation.discoverModules();
    assertTrue(modules.size() > 65);
  }
}
