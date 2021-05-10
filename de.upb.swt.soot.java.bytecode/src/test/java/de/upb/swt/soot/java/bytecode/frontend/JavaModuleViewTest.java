package de.upb.swt.soot.java.bytecode.frontend;

import static org.junit.Assert.*;

import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;

public class JavaModuleViewTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

  // TODO: test an aggregator module (no own content just dependencies to modules)

  @Test
  public void testGeneralClassReceivalFromModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JrtFileSystemAnalysisInputLocation())
            .build();
    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    JavaClassType notExistingClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Panty", "java.lang", "java.base");
    assertFalse(view.getClass(notExistingClass).isPresent());

    JavaClassType notExistingPackage =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.kurz", "java.base");
    assertFalse(view.getClass(notExistingPackage).isPresent());

    JavaClassType notExistingModule =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("String", "java.lang", "non.existent");
    assertFalse(view.getClass(notExistingModule).isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(((ModulePackageName) targetClass.getPackageName()).getModuleSignature());
    assertTrue(moduleDescriptor.isPresent());
  }

  @Test
  public void testJarModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "uses-provides/jar/"))
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
            .addModulePath(
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
  public void testAnnotation() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "annotations"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testRequiresStatic() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "requires-static"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testRequiresExport() {

    // requires: modmain -> modb -> modc
    // transitive: all to java.base

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgb", "modb");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgc", "modc");

    Optional<JavaModuleInfo> moduleInfoMain = view.getModuleInfo(modMain.getModuleSignature());
    assertTrue(moduleInfoMain.isPresent());

    Optional<JavaModuleInfo> moduleInfoB = view.getModuleInfo(modB.getModuleSignature());
    assertTrue(moduleInfoB.isPresent());

    Collection<JavaModuleInfo.ModuleReference> requiresOfMain = moduleInfoMain.get().requires();
    assertTrue(
        requiresOfMain.stream()
            .anyMatch(reqs -> reqs.getModuleSignature().equals(modB.getModuleSignature())));

    System.out.println("found modules:" + view.getModules());
    System.out.println(moduleInfoMain.get());
    System.out.println(moduleInfoB.get());
    System.out.println(view.getModuleInfo(modC.getModuleSignature()).get());

    JavaClassType targetClassMain =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(modMain, targetClassMain).isPresent());

    // ModB
    Collection<JavaModuleInfo.ModuleReference> requiresOfB = moduleInfoB.get().requires();
    assertTrue(
        requiresOfB.stream()
            .anyMatch(reqs -> reqs.getModuleSignature().equals(modC.getModuleSignature())));

    JavaClassType targetClassB =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    assertTrue(view.getClass(modB, targetClassB).isPresent());
    assertTrue(view.getClass(modMain, targetClassB).isPresent());

    // ModC
    JavaClassType targetClassC =
        JavaModuleIdentifierFactory.getInstance().getClassType("C", "pkgc", "modc");
    assertTrue(view.getClass(modC, targetClassC).isPresent());

    // test transitive
    JavaClassType targetClassFromJavaBase =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    assertTrue(view.getClass(modMain, targetClassFromJavaBase).isPresent());
  }

  @Test
  public void testRequiresTransitiveExport() {

    // req: modmain -> moda , ...
    // transitive: a -> c

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "requires_exports_requires-transitive_exports-to/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgmain", "modmain");
    ModulePackageName modA =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkga", "moda");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgc", "modc");

    // ModMain
    Optional<JavaModuleInfo> moduleInfoMain = view.getModuleInfo(modMain.getModuleSignature());
    assertTrue(moduleInfoMain.isPresent());
    Collection<JavaModuleInfo.ModuleReference> requiresOfMain = moduleInfoMain.get().requires();
    assertTrue(
        requiresOfMain.stream()
            .anyMatch(reqs -> reqs.getModuleSignature().equals(modA.getModuleSignature())));

    JavaClassType targetClassMain =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(modMain, targetClassMain).isPresent());

    // ModC
    JavaClassType targetClassC =
        JavaModuleIdentifierFactory.getInstance().getClassType("C", "pkgc", "modc");
    assertTrue(view.getClass(modC, targetClassC).isPresent());
    // A -> C
    assertTrue(view.getClass(modA, targetClassC).isPresent());

    // test transitive: modmain -> modc
    JavaClassType targetClassTransitive =
        JavaModuleIdentifierFactory.getInstance().getClassType("A1", "pkga", "moda");
    assertTrue(view.getClass(modMain, targetClassTransitive).isPresent());

    JavaClassType targetClassFromJavaBase =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    assertTrue(view.getClass(modMain, targetClassFromJavaBase).isPresent());
  }

  @Test
  public void testReflection() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "reflection"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testUsesProvide() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "uses-provides"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testUsesProvideInClient() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(testPath + "uses-provides_uses-in-client"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testDerivedPrivatePackageProtected() {
    // derived_private-package-protected
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "derived_private-package-protected"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testExceptions() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "exceptions"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testInterfaceCallback() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "interface-callback"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testSplitpackageAutomaticModules() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(testPath + "splitpackage_automatic-module"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testSplitpackage() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "splitpackage"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testHiddenMain() {
    // i.e. main is in non exported package
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "hiddenmain"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testAccessUnnamedModuleFromAutomaticModule() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "module_access-from-automatic-module"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testAccessUnnamedModuleFromModule() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "module_access-from-explicit-module"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testAccessModuleFromUnnamedModule() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(testPath + "module_accessing_module_path"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }
}
