package sootup.java.bytecode.frontend.inputlocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.*;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaModuleView;

public class JavaModulePathAnalysisInputLocationTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

  @Test
  public void testJarModule() {
    List<AnalysisInputLocation> inputLocations =
        Collections.singletonList(
            new JavaModulePathAnalysisInputLocation(Paths.get(testPath + "uses-provides/jar/")));
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.emptyList();
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(((ModulePackageName) targetClass.getPackageName()).getModuleSignature());
    assertNotNull(moduleDescriptor);
  }

  @Test
  public void testExplodedModule() {
    List<AnalysisInputLocation> inputLocations =
        Collections.singletonList(
            new JavaModulePathAnalysisInputLocation(
                Paths.get(testPath + "uses-provides/exploded_module/")));
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.emptyList();
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(((ModulePackageName) targetClass.getPackageName()).getModuleSignature());
    assertNotNull(moduleDescriptor);
  }

  @Test
  @Disabled("// FIXME does not find moduleInfo's ")
  public void testGetModuleInfo() {
    List<AnalysisInputLocation> inputLocations =
        Collections.singletonList(
            new JavaModulePathAnalysisInputLocation(Paths.get(testPath + "requires_exports/jar")));
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.emptyList();
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgc", "modc");

    Optional<JavaModuleInfo> moduleInfoMain = view.getModuleInfo(modMain.getModuleSignature());
    assertTrue(moduleInfoMain.isPresent());

    Optional<JavaModuleInfo> moduleInfoB = view.getModuleInfo(modB.getModuleSignature());
    assertTrue(moduleInfoB.isPresent());

    Optional<JavaModuleInfo> moduleInfoC = view.getModuleInfo(modC.getModuleSignature());
    assertTrue(moduleInfoC.isPresent());
  }

  @Test
  public void testGetClassSource() {
    ModuleInfoAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();

    List<AnalysisInputLocation> inputLocations = Collections.emptyList();
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.singletonList(inputLocation);
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);

    final ClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");

    final Optional<? extends SootClassSource> clazzOpt = inputLocation.getClassSource(sig, view);
    assertTrue(clazzOpt.isPresent());
    AbstractClassSource scs = clazzOpt.get();
    assertEquals(sig, scs.getClassType());
    assertEquals("modules/java.base/java/lang/String.class", scs.getSourcePath().toString());
    JavaSootClass javaSootClass = (JavaSootClass) scs.buildClass(SourceType.Application);
    assertTrue(javaSootClass.getMethod("length", Collections.emptyList()).isPresent());
  }

  @Test
  public void testGetClassSources() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(Paths.get(testPath + "requires_exports/jar"));
    List<AnalysisInputLocation> inputLocations = Collections.emptyList();
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.singletonList(inputLocation);
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);

    assertEquals(3, inputLocation.getClassSources(view).count());
  }

  @Test
  public void testGetModules() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(Paths.get(testPath + "requires_exports/jar"));
    List<AnalysisInputLocation> inputLocations = Collections.emptyList();
    List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocations =
        Collections.singletonList(inputLocation);
    JavaModuleView view = new JavaModuleView(inputLocations, moduleInfoAnalysisInputLocations);
    Collection<ModuleSignature> modules = inputLocation.getModules(view);
    assertEquals(3, modules.size());

    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modmain")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modb")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modc")));
  }
}
