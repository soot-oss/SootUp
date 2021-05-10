package de.upb.swt.soot.java.bytecode.frontend;

import static org.junit.Assert.*;

import categories.Java9Test;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Collection;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java9Test.class)
public class JavaModuleViewTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

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
  public void testUnnamedModule() {

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addClassPath(
                new JavaClassPathAnalysisInputLocation(
                    "../shared-test-resources/miniTestSuite/java6/binary/"))
            .build();
    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass = JavaModuleIdentifierFactory.getInstance().getClassType("A", "", "");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    assertSame(
        ((ModulePackageName) aClass.get().getType().getPackageName()).getModuleSignature(),
        JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());

    JavaClassType targetClassWOModuleSig =
        JavaModuleIdentifierFactory.getInstance().getClassType("A", "");
    Optional<JavaSootClass> bClass = view.getClass(targetClassWOModuleSig);
    assertTrue(bClass.isPresent());
    assertSame(
        ((ModulePackageName) bClass.get().getType().getPackageName()).getModuleSignature(),
        JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());
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
    // modmain -> modb -> mod.annotations
    // transitive: modmain -> mod.annotations
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "annotations/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgb", "modb");
    ModulePackageName modAnnotations =
        JavaModuleIdentifierFactory.getInstance()
            .getPackageSignature("pkgannotations", "mod.annotations");

    assertTrue(view.getModuleInfo(modMain.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modB.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modAnnotations.getModuleSignature()).isPresent());

    JavaClassType ctAnno =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("CompileTimeAnnotation", "pkgannotations", "mod.annotations");
    JavaClassType customAnno =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ReallyCoolModule", "pkgannotations", "mod.annotations");
    JavaClassType rtAnno =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("RunTimeAnnotation", "pkgannotations", "mod.annotations");

    assertTrue(view.getClass(ctAnno).isPresent());
    assertTrue(view.getClass(customAnno).isPresent());
    assertTrue(view.getClass(rtAnno).isPresent());

    assertTrue(view.getClass(modAnnotations, ctAnno).isPresent());
    assertTrue(view.getClass(modB, ctAnno).isPresent());
    assertTrue(view.getClass(modMain, ctAnno).isPresent());
  }

  @Test
  public void testRequiresStatic() {
    // modmain -> modb -> modc
    // static trans: modmain -> modc [via modb]

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(
                new JavaModulePathAnalysisInputLocation(testPath + "requires-static/jar"))
            .addClassPath(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgb", "modb");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageSignature("pkgc", "modc");

    assertTrue(view.getModuleInfo(modMain.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modB.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modC.getModuleSignature()).isPresent());

    JavaClassType cClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("C", "pkgc", "modc");

    assertTrue(view.getClass(cClass).isPresent());

    assertTrue(view.getClass(modC, cClass).isPresent());
    assertTrue(view.getClass(modB, cClass).isPresent());
    assertTrue(view.getClass(modMain, cClass).isPresent());

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    assertTrue(view.getClass(targetClass).isPresent());
    assertTrue(view.getClass(modMain, targetClass).isPresent());
    assertTrue(view.getClass(modB, targetClass).isPresent());
    assertTrue(view.getClass(modC, targetClass).isPresent());
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
            .addModulePath(new JrtFileSystemAnalysisInputLocation())
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

    // modmain -> moda
    JavaClassType mainToAClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("A1", "pkga1", "moda");
    assertTrue(view.getClass(modA, mainToAClass).isPresent());
    assertTrue(view.getClass(modMain, mainToAClass).isPresent());

    // transitive: modmain -> modc
    assertTrue(view.getClass(modMain, targetClassC).isPresent());

    JavaClassType targetClassFromJavaBase =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    assertTrue(view.getClass(modMain, targetClassFromJavaBase).isPresent());
  }

  @Ignore
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

  @Ignore("to implement")
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

  @Ignore
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

  @Ignore("to implement")
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

  @Ignore("to implement")
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

  @Ignore("to implement")
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

  @Ignore("to implement")
  public void testSplitpackageAutomaticModules() {
    // A module must not requires 2 or more modules, which have/export the same package
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

  @Ignore("to implement")
  public void testSplitpackage() {
    // A module must not requires 2 or more modules, which have/export the same package
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

  @Ignore("to implement")
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

  @Ignore("to implement")
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

  @Ignore("to implement")
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

  @Ignore("to implement")
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
