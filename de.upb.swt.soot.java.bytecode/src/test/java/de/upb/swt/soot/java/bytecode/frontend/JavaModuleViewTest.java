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
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
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

    int size =
        view.getModuleClasses(
                ((ModulePackageName) targetClass.getPackageName()).getModuleSignature())
            .size();
    assertTrue(
        "actual: " + size,
        size > 5500
            && size < 6500); // ~amount of java.base classes -> depends on java implementation..
  }

  @Test
  public void testUnnamedModule() {

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
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

    Collection<JavaSootClass> classes =
        view.getModuleClasses(JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());
    assertTrue(
        classes.size() > 110
            && classes.size() < 150); // ~amount of files/testcases in the refered directory
  }

  @Test
  public void testAnnotation() {
    // modmain -> modb -> mod.annotations
    // transitive: modmain -> mod.annotations
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "annotations/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    ModulePackageName modAnnotations =
        JavaModuleIdentifierFactory.getInstance()
            .getPackageName("pkgannotations", "mod.annotations");

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
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "requires-static/jar"))
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgc", "modc");

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
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "requires_exports_requires-transitive_exports-to/jar"))
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModulePackageName modA =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkga", "moda");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgc", "modc");

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

  @Test
  public void testReflection() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "reflection/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> mainClassOpt = view.getClass(mainClass);
    assertTrue(mainClassOpt.isPresent());

    JavaClassType bClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    Optional<JavaSootClass> bClassOpt = view.getClass(bClass);
    assertTrue(bClassOpt.isPresent());

    JavaClassType b1Class =
        JavaModuleIdentifierFactory.getInstance().getClassType("B1", "pkgb1", "modb");
    Optional<JavaSootClass> b1ClassOpt = view.getClass(b1Class);
    assertTrue(b1ClassOpt.isPresent());

    JavaClassType biClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("InternalB", "pkgbinternal", "modb");
    Optional<JavaSootClass> biClassOpt = view.getClass(biClass);
    assertTrue(biClassOpt.isPresent());

    Optional<JavaModuleInfo> moduleInfoOpt =
        view.getModuleInfo(((ModulePackageName) bClass.getPackageName()).getModuleSignature());
    assertTrue(moduleInfoOpt.isPresent());
    // is it open
    assertTrue(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == bClass.getPackageName())
            .anyMatch(
                o ->
                    o.appliesTo(
                        ((ModulePackageName) mainClass.getPackageName()).getModuleSignature())));
    assertFalse(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == b1Class.getPackageName())
            .anyMatch(
                o ->
                    o.appliesTo(
                        ((ModulePackageName) mainClass.getPackageName()).getModuleSignature())));
    assertTrue(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == biClass.getPackageName())
            .anyMatch(
                o ->
                    o.appliesTo(
                        ((ModulePackageName) mainClass.getPackageName()).getModuleSignature())));

    // even if a module can access itself this returns false as this implicit rule is not explicitly
    // stated in the module-descriptor
    assertFalse(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == b1Class.getPackageName())
            .anyMatch(
                o ->
                    o.appliesTo(
                        ((ModulePackageName) mainClass.getPackageName()).getModuleSignature())));
  }

  @Test
  public void testUsesProvide() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "uses-provides/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType mainModmainSig =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> mainModmainClass = view.getClass(mainModmainSig);
    assertTrue(mainModmainClass.isPresent());

    JavaClassType serviceImplSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "com.service.impl", "modservice.impl.com");
    Optional<JavaSootClass> serviceImplClass = view.getClass(serviceImplSig);
    assertTrue(serviceImplClass.isPresent());

    JavaClassType serviceImplNetSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "net.service.impl", "modservice.impl.net");
    Optional<JavaSootClass> serviceImplNetClass = view.getClass(serviceImplNetSig);
    assertTrue(serviceImplNetClass.isPresent());

    JavaClassType serviceDefSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("IService", "myservice", "modservicedefinition");
    Optional<JavaSootClass> serviceDefClass = view.getClass(serviceDefSig);
    assertTrue(serviceDefClass.isPresent());

    view.getModuleInfo(((ModulePackageName) mainModmainSig.getPackageName()).getModuleSignature());
  }

  @Ignore
  @Test
  public void testUsesProvideInClient() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "uses-provides_uses-in-client/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Ignore("to implement")
  @Test
  public void testDerivedPrivatePackageProtected() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "derived_private-package-protected/jar"))
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
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "exceptions/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Ignore("to implement")
  @Test
  public void testInterfaceCallback() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "interface-callback/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  @Ignore
  public void testSplitpackageAutomaticModules() {
    // A module must not require 2 or more modules, which have/export the same package
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "splitpackage_automatic-modules/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    assertEquals(3, view.getNamedModules().size());

    ModulePackageName modmain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    JavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(mainClass).isPresent());

    JavaClassType v1Class =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("Version1", "pkgsplitted", "modauto1");
    assertTrue(view.getClass(v1Class).isPresent());

    JavaClassType v2Class =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("Version2", "pkgsplitted", "modauto2");
    assertTrue(view.getClass(v2Class).isPresent());

    assertTrue(view.getClass(modmain, v2Class).isPresent());
    assertTrue(view.getClass(modmain, v2Class).isPresent());

    fail("thats not how it goes");
  }

  @Ignore("to implement")
  @Test
  public void testSplitpackage() {
    // A module must not requires 2 or more modules, which have/export the same package
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "splitpackage"))
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
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "hiddenmain/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testAccessUnnamedModuleFromAutomaticModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "unnamed-module_access-from-automatic-module/jar/modmain.auto.jar"))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    testPath + "unnamed-module_access-from-automatic-module/jar/cpa.jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    JavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain.auto");
    assertTrue(view.getClass(mainClass).isPresent());

    JavaClassType aClass = JavaModuleIdentifierFactory.getInstance().getClassType("A", "pkga", "");
    assertTrue(view.getClass(aClass).isPresent());

    assertTrue(view.getClass(mainClass.getPackageName(), aClass).isPresent());
    assertTrue(view.getClass(aClass.getPackageName(), mainClass).isPresent());

    assertEquals(
        1,
        view.getModuleClasses(((ModulePackageName) mainClass.getPackageName()).getModuleSignature())
            .size());
    assertEquals(
        1,
        view.getModuleClasses(((ModulePackageName) aClass.getPackageName()).getModuleSignature())
            .size());
  }

  @Test
  public void testAccessUnnamedModuleFromModule() {

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "unnamed-module_access-from-explicit-module/jar/modb.jar"))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "unnamed-module_access-from-explicit-module/jar/modmain.jar"))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    testPath + "unnamed-module_access-from-explicit-module/jar/cpb.jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName cpb = JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "");
    JavaModuleInfo moduleInfo_cpb = view.getModuleInfo(cpb.getModuleSignature()).get();
    assertTrue(moduleInfo_cpb.isUnnamedModule());

    ModulePackageName pkgbModb =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    JavaModuleInfo moduleInfo_pkgbModb = view.getModuleInfo(pkgbModb.getModuleSignature()).get();

    assertFalse(moduleInfo_pkgbModb.isUnnamedModule());

    ModulePackageName modmain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgcpmain", "modmain");
    JavaModuleInfo moduleInfo_cpmain = view.getModuleInfo(modmain.getModuleSignature()).get();
    assertFalse(moduleInfo_cpmain.isUnnamedModule());

    JavaClassType BFromClasspath =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromClasspath", "pkgb", "");
    assertTrue(view.getClass(BFromClasspath).isPresent());
    assertFalse(view.getClass(modmain, BFromClasspath).isPresent());

    assertEquals(1, view.getModuleClasses(modmain.getModuleSignature()).size());
    assertEquals(
        3,
        view.getModuleClasses(JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature()).size());

    // TODO: check & test: compile and run option: "ALL-UNNAMED" which opens access from the named
    // module to the unnamed module

  }

  @Test
  public void testAccessModuleFromUnnamedModule() {
    //  if module class "covers" a class on classpath (i.e. in unnamed module) the one on module
    // path is taken

    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "unnamed-module_accessing-module-path/jar/modb.jar"))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    testPath + "unnamed-module_accessing-module-path/jar/cpb.jar"))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    testPath + "unnamed-module_accessing-module-path/jar/cpmain.jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();

    ModulePackageName pkgbModb =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    JavaModuleInfo moduleInfo_pkgbModb = view.getModuleInfo(pkgbModb.getModuleSignature()).get();
    System.out.println(moduleInfo_pkgbModb);
    assertFalse(moduleInfo_pkgbModb.isUnnamedModule());

    ModulePackageName cpb = JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "");
    JavaModuleInfo moduleInfo_cpb = view.getModuleInfo(cpb.getModuleSignature()).get();
    assertTrue(moduleInfo_cpb.isUnnamedModule());

    ModulePackageName cpmain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgcpmain", "");
    JavaModuleInfo moduleInfo_cpmain = view.getModuleInfo(cpmain.getModuleSignature()).get();
    assertTrue(moduleInfo_cpmain.isUnnamedModule());

    JavaClassType main =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgcpmain", "");
    assertTrue(view.getClass(main).isPresent());
    assertTrue(view.getClass(cpmain, main).isPresent());
    assertTrue(view.getClass(cpb, main).isPresent());
    assertFalse(view.getClass(pkgbModb, main).isPresent());

    JavaClassType BOnClasspath =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromClasspath", "pkgboncp", "");
    assertTrue(view.getClass(BOnClasspath).isPresent());
    assertTrue(view.getClass(cpb, BOnClasspath).isPresent());
    assertTrue(view.getClass(cpmain, BOnClasspath).isPresent());
    assertFalse(view.getClass(pkgbModb, BOnClasspath).isPresent());

    JavaClassType BFromClasspath =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromClasspath", "pkgb", "");
    assertTrue(view.getClass(BFromClasspath).isPresent());
    assertTrue(view.getClass(cpb, BFromClasspath).isPresent());
    assertTrue(view.getClass(cpmain, BFromClasspath).isPresent());
    assertFalse(view.getClass(pkgbModb, BFromClasspath).isPresent());

    JavaClassType BFromModule =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromModule", "pkgb", "modb");
    assertTrue(view.getClass(BFromModule).isPresent());
    assertTrue(view.getClass(pkgbModb, BFromModule).isPresent());
    assertTrue(view.getClass(cpb, BFromModule).isPresent());
    assertTrue(view.getClass(cpmain, BFromModule).isPresent());

    JavaClassType BModuleB =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    assertTrue(view.getClass(BModuleB).isPresent());
    assertTrue(view.getClass(pkgbModb, BModuleB).isPresent());
    assertTrue(view.getClass(cpb, BModuleB).isPresent());
    assertTrue(view.getClass(cpmain, BModuleB).isPresent());

    JavaClassType BFromModuleButInternal =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("BFromModuleButInternal", "pkgbinternal", "modb");
    assertTrue(view.getClass(BFromModuleButInternal).isPresent());
    assertTrue(view.getClass(pkgbModb, BFromModuleButInternal).isPresent());
    assertFalse(
        "unnamed module can only access exported packages!",
        view.getClass(cpmain, BFromModuleButInternal).isPresent());
    assertFalse(
        "unnamed module can only access exported packages!",
        view.getClass(cpb, BFromModuleButInternal).isPresent());
  }

  @Test
  @Ignore
  public void testModulePathEqualsClassPath() {
    // TODO: check what happens if modulepath == classpath
  }

  @Test
  @Ignore
  public void testEqualPackageNamesInInputLocations() {
    // TODO: implement test
  }

  @Test
  @Ignore
  public void testEqualModuleSignaturesInInputLocations() {
    // TODO: implement test
  }
}
