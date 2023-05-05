package sootup.java.bytecode.frontend;

import static org.junit.Assert.*;

import categories.Java9Test;
import java.util.Collection;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.types.ModuleJavaClassType;
import sootup.java.core.views.JavaModuleView;

@Category(Java9Test.class)
public class JavaModuleViewTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

  @Test
  public void testGeneralClassReceivalFromModule() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JrtFileSystemAnalysisInputLocation())
            .build();
    JavaModuleView view = (JavaModuleView) p.createView();
    ModuleJavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());

    ModuleJavaClassType notExistingClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Panty", "java.lang", "java.base");
    assertFalse(view.getClass(notExistingClass).isPresent());

    ModuleJavaClassType notExistingPackage =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.kurz", "java.base");
    assertFalse(view.getClass(notExistingPackage).isPresent());

    ModuleJavaClassType notExistingModule =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("String", "java.lang", "non.existent");
    assertFalse(view.getClass(notExistingModule).isPresent());

    Optional<JavaModuleInfo> moduleDescriptor =
        view.getModuleInfo(targetClass.getPackageName().getModuleSignature());
    assertTrue(moduleDescriptor.isPresent());

    int size = view.getModuleClasses(targetClass.getPackageName().getModuleSignature()).size();
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
            .enableModules()
            .build();
    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("A", "", "");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    assertSame(
        ((ModulePackageName) aClass.get().getType().getPackageName()).getModuleSignature(),
        JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());

    ModuleJavaClassType targetClassWOModuleSig =
        JavaModuleIdentifierFactory.getInstance().getClassType("A", "");
    Optional<JavaSootClass> bClass = view.getClass(targetClassWOModuleSig);
    assertTrue(bClass.isPresent());
    assertSame(
        ((ModulePackageName) bClass.get().getType().getPackageName()).getModuleSignature(),
        JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());

    Collection<JavaSootClass> classes =
        view.getModuleClasses(JavaModuleInfo.getUnnamedModuleInfo().getModuleSignature());

    // ~amount of files/testcases in the referred directory
    assertTrue(classes.size() > 110);
    assertTrue(classes.size() < 150);
  }

  @Test
  public void testAnnotation() {
    // modmain -> modb -> mod.annotations
    // transitive: modmain -> mod.annotations
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "annotations/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

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

    ModuleJavaClassType ctAnno =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("CompileTimeAnnotation", "pkgannotations", "mod.annotations");
    ModuleJavaClassType customAnno =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ReallyCoolModule", "pkgannotations", "mod.annotations");
    ModuleJavaClassType rtAnno =
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

    JavaModuleView view = (JavaModuleView) p.createView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModulePackageName modB =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    ModulePackageName modC =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgc", "modc");

    assertTrue(view.getModuleInfo(modMain.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modB.getModuleSignature()).isPresent());
    assertTrue(view.getModuleInfo(modC.getModuleSignature()).isPresent());

    ModuleJavaClassType cClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("C", "pkgc", "modc");

    assertTrue(view.getClass(cClass).isPresent());

    assertTrue(view.getClass(modC, cClass).isPresent());
    assertTrue(view.getClass(modB, cClass).isPresent());
    assertTrue(view.getClass(modMain, cClass).isPresent());

    ModuleJavaClassType targetClass =
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

    Collection<JavaModuleInfo.ModuleReference> requiresOfMain = moduleInfoMain.get().requires();
    assertTrue(
        requiresOfMain.stream()
            .anyMatch(reqs -> reqs.getModuleSignature().equals(modB.getModuleSignature())));

    ModuleJavaClassType targetClassMain =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(modMain, targetClassMain).isPresent());

    // ModB
    Collection<JavaModuleInfo.ModuleReference> requiresOfB = moduleInfoB.get().requires();
    assertTrue(
        requiresOfB.stream()
            .anyMatch(reqs -> reqs.getModuleSignature().equals(modC.getModuleSignature())));

    ModuleJavaClassType targetClassB =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    assertTrue(view.getClass(modB, targetClassB).isPresent());
    assertTrue(view.getClass(modMain, targetClassB).isPresent());

    // ModC
    ModuleJavaClassType targetClassC =
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

    JavaModuleView view = (JavaModuleView) p.createView();

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

    ModuleJavaClassType targetClassMain =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(modMain, targetClassMain).isPresent());

    // ModC
    ModuleJavaClassType targetClassC =
        JavaModuleIdentifierFactory.getInstance().getClassType("C", "pkgc", "modc");
    assertTrue(view.getClass(modC, targetClassC).isPresent());
    // A -> C
    assertTrue(view.getClass(modA, targetClassC).isPresent());

    // modmain -> moda
    ModuleJavaClassType mainToAClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("A1", "pkga1", "moda");
    assertTrue(view.getClass(modA, mainToAClass).isPresent());
    assertTrue(view.getClass(modMain, mainToAClass).isPresent());

    // transitive: modmain -> modc
    assertTrue(view.getClass(modMain, targetClassC).isPresent());

    ModuleJavaClassType targetClassFromJavaBase =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    assertTrue(view.getClass(modMain, targetClassFromJavaBase).isPresent());
  }

  @Test
  public void testReflection() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "reflection/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> mainClassOpt = view.getClass(mainClass);
    assertTrue(mainClassOpt.isPresent());

    ModuleJavaClassType bClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    Optional<JavaSootClass> bClassOpt = view.getClass(bClass);
    assertTrue(bClassOpt.isPresent());

    ModuleJavaClassType b1Class =
        JavaModuleIdentifierFactory.getInstance().getClassType("B1", "pkgb1", "modb");
    Optional<JavaSootClass> b1ClassOpt = view.getClass(b1Class);
    assertTrue(b1ClassOpt.isPresent());

    ModuleJavaClassType biClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("InternalB", "pkgbinternal", "modb");
    Optional<JavaSootClass> biClassOpt = view.getClass(biClass);
    assertTrue(biClassOpt.isPresent());

    Optional<JavaModuleInfo> moduleInfoOpt =
        view.getModuleInfo(bClass.getPackageName().getModuleSignature());
    assertTrue(moduleInfoOpt.isPresent());
    // is it open
    assertTrue(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == bClass.getPackageName())
            .anyMatch(o -> o.appliesTo(mainClass.getPackageName().getModuleSignature())));
    assertFalse(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == b1Class.getPackageName())
            .anyMatch(o -> o.appliesTo(mainClass.getPackageName().getModuleSignature())));
    assertTrue(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == biClass.getPackageName())
            .anyMatch(o -> o.appliesTo(mainClass.getPackageName().getModuleSignature())));

    // even if a module can access itself this returns false as this implicit rule is not explicitly
    // stated in the module-descriptor
    assertFalse(
        moduleInfoOpt.get().opens().stream()
            .filter(pckg -> pckg.getPackageName() == b1Class.getPackageName())
            .anyMatch(o -> o.appliesTo(mainClass.getPackageName().getModuleSignature())));
  }

  @Test
  public void testUsesProvide() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "uses-provides/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainModmainSig =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> mainModmainClass = view.getClass(mainModmainSig);
    assertTrue(mainModmainClass.isPresent());

    ModuleJavaClassType serviceDefSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("IService", "myservice", "modservicedefinition");
    Optional<JavaSootClass> serviceDefClass = view.getClass(serviceDefSig);
    assertTrue(serviceDefClass.isPresent());
    Optional<JavaSootClass> serviceDefAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceDefSig);
    assertTrue(serviceDefAccessClass.isPresent());
    assertEquals(serviceDefAccessClass.get(), serviceDefClass.get());

    ModuleJavaClassType serviceImplSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "com.service.impl", "modservice.impl.com");
    Optional<JavaSootClass> serviceImplClass = view.getClass(serviceImplSig);
    assertTrue(serviceImplClass.isPresent());
    Optional<JavaSootClass> serviceImplAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceImplSig);
    assertTrue(serviceImplAccessClass.isPresent());
    assertEquals(serviceImplAccessClass.get(), serviceImplClass.get());

    ModuleJavaClassType serviceImplNetSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "net.service.impl", "modservice.impl.net");
    Optional<JavaSootClass> serviceImplNetClass = view.getClass(serviceImplNetSig);
    assertTrue(serviceImplNetClass.isPresent());
    Optional<JavaSootClass> serviceImplNetAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceImplNetSig);
    assertTrue(serviceImplNetAccessClass.isPresent());
    assertEquals(serviceImplNetAccessClass.get(), serviceImplNetClass.get());
    assertNotEquals(serviceImplNetClass.get(), serviceImplClass.get());

    view.getModuleInfo(mainModmainSig.getPackageName().getModuleSignature());
  }

  @Test
  public void testUsesProvideInClient() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "uses-provides_uses-in-client/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();
    ModuleJavaClassType mainModmainSig =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    Optional<JavaSootClass> mainModmainClass = view.getClass(mainModmainSig);
    assertTrue(mainModmainClass.isPresent());

    ModuleJavaClassType serviceDefSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("IService", "myservice", "modservicedefinition");
    Optional<JavaSootClass> serviceDefClass = view.getClass(serviceDefSig);
    assertTrue(serviceDefClass.isPresent());
    Optional<JavaSootClass> serviceDefAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceDefSig);
    assertTrue(serviceDefAccessClass.isPresent());
    assertEquals(serviceDefAccessClass.get(), serviceDefClass.get());

    ModuleJavaClassType serviceImplSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "com.service.impl", "modservice.impl.com");
    Optional<JavaSootClass> serviceImplClass = view.getClass(serviceImplSig);
    assertTrue(serviceImplClass.isPresent());
    Optional<JavaSootClass> serviceImplAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceImplSig);
    assertTrue(serviceImplAccessClass.isPresent());
    assertEquals(serviceImplAccessClass.get(), serviceImplClass.get());

    ModuleJavaClassType serviceImplNetSig =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ServiceImpl", "net.service.impl", "modservice.impl.net");
    Optional<JavaSootClass> serviceImplNetClass = view.getClass(serviceImplNetSig);
    assertTrue(serviceImplNetClass.isPresent());
    Optional<JavaSootClass> serviceImplNetAccessClass =
        view.getClass(mainModmainSig.getPackageName(), serviceImplNetSig);
    assertTrue(serviceImplNetAccessClass.isPresent());
    assertEquals(serviceImplNetAccessClass.get(), serviceImplNetClass.get());
    assertNotEquals(serviceImplNetClass.get(), serviceImplClass.get());

    view.getModuleInfo(mainModmainSig.getPackageName().getModuleSignature());
  }

  @Test
  public void testDerivedPrivatePackageProtected() {
    // static vs. dynamic type
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "derived_private-package-protected/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(mainClass).isPresent());

    ModuleJavaClassType bClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    assertTrue(view.getClass(bClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), bClass).isPresent());

    ModuleJavaClassType dataClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Data", "pkgb", "modb");
    assertTrue(view.getClass(dataClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), dataClass).isPresent());

    ModuleJavaClassType dataFactoryClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("DataFactory", "pkgb", "modb");
    assertTrue(view.getClass(dataFactoryClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), dataFactoryClass).isPresent());

    ModuleJavaClassType internalBHelperClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("InternalBHelper", "pkgbinternal", "modb");
    assertTrue(view.getClass(internalBHelperClass).isPresent());
    assertFalse(view.getClass(mainClass.getPackageName(), internalBHelperClass).isPresent());

    ModuleJavaClassType internalBSuperClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("InternalBSuperClass", "pkgbinternal", "modb");
    assertTrue(view.getClass(internalBSuperClass).isPresent());
    assertFalse(view.getClass(mainClass.getPackageName(), internalBSuperClass).isPresent());

    ModuleJavaClassType InternalDataClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("InternalData", "pkgbinternal", "modb");
    assertTrue(view.getClass(InternalDataClass).isPresent());
    assertFalse(view.getClass(mainClass.getPackageName(), InternalDataClass).isPresent());
  }

  @Test
  public void testExceptions() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "exceptions/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(mainClass).isPresent());

    ModuleJavaClassType exceptionClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("MyException", "pkgb", "modb");
    assertTrue(view.getClass(exceptionClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), exceptionClass).isPresent());

    ModuleJavaClassType internalExceptionClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("MyInternalException", "pkgbinternal", "modb");
    assertTrue(view.getClass(internalExceptionClass).isPresent());
    assertFalse(view.getClass(mainClass.getPackageName(), internalExceptionClass).isPresent());

    ModuleJavaClassType runtimeExceptionClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("MyRuntimeException", "pkgb", "modb");
    assertTrue(view.getClass(runtimeExceptionClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), runtimeExceptionClass).isPresent());

    ModuleJavaClassType internalRuntimeExceptionClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("MyInternalRuntimeException", "pkgbinternal", "modb");
    assertTrue(view.getClass(internalRuntimeExceptionClass).isPresent());
    assertFalse(
        view.getClass(mainClass.getPackageName(), internalRuntimeExceptionClass).isPresent());
  }

  @Test
  public void testInterfaceCallback() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "interface-callback/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(mainClass).isPresent());

    ModuleJavaClassType calleeClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Callee", "pkgcallee", "modcallee");
    assertTrue(view.getClass(calleeClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), calleeClass).isPresent());

    ModuleJavaClassType iCallbackClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("ICallback", "pkgcallee", "modcallee");
    assertTrue(view.getClass(iCallbackClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), iCallbackClass).isPresent());
    assertTrue(view.getClass(calleeClass.getPackageName(), iCallbackClass).isPresent());

    ModuleJavaClassType handlerClass =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("MyCallbackImpl", "pkgcallbackhandler", "modcallbackhandler");
    assertTrue(view.getClass(handlerClass).isPresent());
    assertTrue(view.getClass(mainClass.getPackageName(), handlerClass).isPresent());
    assertFalse(view.getClass(iCallbackClass.getPackageName(), handlerClass).isPresent());
    assertFalse(view.getClass(calleeClass.getPackageName(), handlerClass).isPresent());
  }

  @Test
  @Ignore
  public void testSplitpackageAutomaticModules() {
    // A module must not require 2 or more modules, which contain the same package - export is *not*
    // even necessary.
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(
                    testPath + "splitpackage_automatic-modules/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    assertEquals(3, view.getNamedModules().size());

    ModulePackageName modmain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(mainClass).isPresent());

    ModuleJavaClassType v1Class =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("Version1", "pkgsplitted", "modauto1");
    assertTrue(view.getClass(v1Class).isPresent());

    ModuleJavaClassType v2Class =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("Version2", "pkgsplitted", "modauto2");
    assertTrue(view.getClass(v2Class).isPresent());

    assertTrue(view.getClass(modmain, v1Class).isPresent());
    assertTrue(view.getClass(modmain, v2Class).isPresent());

    fail("we should complain about the split package in modules..");
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

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    Optional<JavaSootClass> aClass = view.getClass(targetClass);
    assertTrue(aClass.isPresent());
    fail("test module descriptor/rights");
  }

  @Test
  public void testHiddenMain() {
    // i.e. main is in non exported package
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(new JavaModulePathAnalysisInputLocation(testPath + "hiddenmain/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType targetClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(targetClass).isPresent());

    ModuleJavaClassType hiddenMain =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("HiddenMain", "pkgmainhidden", "modmain");
    assertTrue(view.getClass(hiddenMain).isPresent());
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

    JavaModuleView view = (JavaModuleView) p.createView();

    ModuleJavaClassType mainClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain.auto");
    assertTrue(view.getClass(mainClass).isPresent());

    ModuleJavaClassType aClass =
        JavaModuleIdentifierFactory.getInstance().getClassType("A", "pkga", "");
    assertTrue(view.getClass(aClass).isPresent());

    assertTrue(view.getClass(mainClass.getPackageName(), aClass).isPresent());
    assertTrue(view.getClass(aClass.getPackageName(), mainClass).isPresent());

    assertEquals(1, view.getModuleClasses(mainClass.getPackageName().getModuleSignature()).size());
    assertEquals(1, view.getModuleClasses(aClass.getPackageName().getModuleSignature()).size());
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

    JavaModuleView view = (JavaModuleView) p.createView();

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

    ModuleJavaClassType BFromClasspath =
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

    JavaModuleView view = (JavaModuleView) p.createView();

    ModulePackageName pkgbModb =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "modb");
    JavaModuleInfo moduleInfo_pkgbModb = view.getModuleInfo(pkgbModb.getModuleSignature()).get();

    assertFalse(moduleInfo_pkgbModb.isUnnamedModule());

    ModulePackageName cpb = JavaModuleIdentifierFactory.getInstance().getPackageName("pkgb", "");
    JavaModuleInfo moduleInfo_cpb = view.getModuleInfo(cpb.getModuleSignature()).get();
    assertTrue(moduleInfo_cpb.isUnnamedModule());

    ModulePackageName cpmain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgcpmain", "");
    JavaModuleInfo moduleInfo_cpmain = view.getModuleInfo(cpmain.getModuleSignature()).get();
    assertTrue(moduleInfo_cpmain.isUnnamedModule());

    ModuleJavaClassType main =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgcpmain", "");
    assertTrue(view.getClass(main).isPresent());
    assertTrue(view.getClass(cpmain, main).isPresent());
    assertTrue(view.getClass(cpb, main).isPresent());
    assertFalse(view.getClass(pkgbModb, main).isPresent());

    ModuleJavaClassType BOnClasspath =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromClasspath", "pkgboncp", "");
    assertTrue(view.getClass(BOnClasspath).isPresent());
    assertTrue(view.getClass(cpb, BOnClasspath).isPresent());
    assertTrue(view.getClass(cpmain, BOnClasspath).isPresent());
    assertFalse(view.getClass(pkgbModb, BOnClasspath).isPresent());

    ModuleJavaClassType BFromClasspath =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromClasspath", "pkgb", "");
    assertTrue(view.getClass(BFromClasspath).isPresent());
    assertTrue(view.getClass(cpb, BFromClasspath).isPresent());
    assertTrue(view.getClass(cpmain, BFromClasspath).isPresent());
    assertFalse(view.getClass(pkgbModb, BFromClasspath).isPresent());

    ModuleJavaClassType BFromModule =
        JavaModuleIdentifierFactory.getInstance().getClassType("BFromModule", "pkgb", "modb");
    assertTrue(view.getClass(BFromModule).isPresent());
    assertTrue(view.getClass(pkgbModb, BFromModule).isPresent());
    assertTrue(view.getClass(cpb, BFromModule).isPresent());
    assertTrue(view.getClass(cpmain, BFromModule).isPresent());

    ModuleJavaClassType BModuleB =
        JavaModuleIdentifierFactory.getInstance().getClassType("B", "pkgb", "modb");
    assertTrue(view.getClass(BModuleB).isPresent());
    assertTrue(view.getClass(pkgbModb, BModuleB).isPresent());
    assertTrue(view.getClass(cpb, BModuleB).isPresent());
    assertTrue(view.getClass(cpmain, BModuleB).isPresent());

    ModuleJavaClassType BFromModuleButInternal =
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
  public void testEqualModuleSignaturesInInputLocations() {}

  @Test
  public void testEqualModulePath() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar"))
            .addInputLocation(
                new JavaModulePathAnalysisInputLocation(testPath + "requires_exports/jar"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createView();

    ModulePackageName modMain =
        JavaModuleIdentifierFactory.getInstance().getPackageName("pkgmain", "modmain");
    ModuleJavaClassType targetClassMain =
        JavaModuleIdentifierFactory.getInstance().getClassType("Main", "pkgmain", "modmain");
    assertTrue(view.getClass(modMain, targetClassMain).isPresent());
    // should we detect that in general? it doenst lead to errors.. just unnecessary overhead while
    // resolving..
  }
}
