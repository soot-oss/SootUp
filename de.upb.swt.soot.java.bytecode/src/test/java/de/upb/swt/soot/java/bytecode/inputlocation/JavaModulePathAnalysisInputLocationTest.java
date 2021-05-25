package de.upb.swt.soot.java.bytecode.inputlocation;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;

public class JavaModulePathAnalysisInputLocationTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

  @Test
  public void testJarModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "uses-provides/jar/"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

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

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(((ModulePackageName) targetClass.getPackageName()).getModuleSignature());
    assertNotNull(moduleDescriptor);
  }

  @Test
  public void getModuleInfo() {

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

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
  public void getClassSource() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(" TODO ");
    final ClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");

    final Optional<? extends AbstractClassSource<JavaSootClass>> clazzOpt =
        inputLocation.getClassSource(sig);
    assertTrue(clazzOpt.isPresent());
    assertEquals(sig, clazzOpt.get().getClassType());
    assertEquals(sig, clazzOpt.get());
  }

  @Test
  public void getClassSources() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar");
    final Collection<? extends AbstractClassSource<?>> classSources =
        inputLocation.getClassSources(JavaModuleIdentifierFactory.getInstance());
    assertEquals(3, classSources.size());
  }

  @Test
  public void getModules() {
    JavaModulePathAnalysisInputLocation inputLocation =
        new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar");
    Collection<ModuleSignature> modules = inputLocation.getModules();
    assertEquals(3, modules.size());

    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modmain")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modb")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("modc")));
  }
}
