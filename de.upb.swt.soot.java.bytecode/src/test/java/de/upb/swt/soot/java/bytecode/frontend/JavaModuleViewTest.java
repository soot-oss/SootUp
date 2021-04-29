package de.upb.swt.soot.java.bytecode.frontend;

import static org.junit.Assert.*;

import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
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
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "requires_exports"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
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
