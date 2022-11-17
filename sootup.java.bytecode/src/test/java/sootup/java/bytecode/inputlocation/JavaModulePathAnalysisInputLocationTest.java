package sootup.java.bytecode.inputlocation;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.DefaultSourceTypeSpecifier;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.*;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaModuleView;

public class JavaModulePathAnalysisInputLocationTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

  @Test
  public void testJarModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "uses-provides/jar/"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

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
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "uses-provides/exploded_module/"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();
    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(((ModulePackageName) targetClass.getPackageName()).getModuleSignature());
    assertNotNull(moduleDescriptor);
  }

  @Test
  public void testGetModuleInfo() {

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

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
    JavaModuleProject project =
        new JavaModuleProject(
            new JavaLanguage(9),
            Collections.emptyList(),
            Collections.singletonList(inputLocation),
            DefaultSourceTypeSpecifier.getInstance());
    JavaModuleView view = project.createView();

    final ClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");

    final Optional<? extends AbstractClassSource<JavaSootClass>> clazzOpt =
        inputLocation.getClassSource(sig, view);
    assertTrue(clazzOpt.isPresent());
    AbstractClassSource<JavaSootClass> scs = clazzOpt.get();
    assertEquals(sig, scs.getClassType());
    assertEquals("modules/java.base/java/lang/String.class", scs.getSourcePath().toString());
    JavaSootClass javaSootClass = scs.buildClass(SourceType.Application);
    assertTrue(javaSootClass.getMethod("length", Collections.emptyList()).isPresent());
  }

  @Test
  public void testGetClassSources() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar");
    JavaModuleProject project =
        new JavaModuleProject(
            new JavaLanguage(9),
            Collections.emptyList(),
            Collections.singletonList(inputLocation),
            DefaultSourceTypeSpecifier.getInstance());
    JavaModuleView view = project.createView();

    final Collection<? extends AbstractClassSource<?>> classSources =
        inputLocation.getClassSources(view);
    assertEquals(3, classSources.size());
  }

  @Test
  public void testGetModules() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar");
    JavaModuleProject project =
        new JavaModuleProject(
            new JavaLanguage(9),
            Collections.emptyList(),
            Collections.singletonList(inputLocation),
            DefaultSourceTypeSpecifier.getInstance());
    JavaModuleView view = project.createView();
    Collection<ModuleSignature> modules = inputLocation.getModules(view);
    assertEquals(3, modules.size());

    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modmain")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modb")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modc")));
  }
}
