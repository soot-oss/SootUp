package de.upb.swt.soot.java.bytecode.frontend;

import static org.junit.Assert.*;

import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.core.*;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Optional;
import org.junit.Test;

public class JavaModuleViewTest {

  private String testPath = "../shared-test-resources/jigsaw-examples/";

  @Test
  public void testClassFromJavaBase() {
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JrtFileSystemAnalysisInputLocation())
            .build();
    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("String", "java.lang");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());

    JavaModuleInfo moduleDescriptor = view.getModuleDescriptor(startModule);
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testRequiresStatic() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "requires-static"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testRequiresExport() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "requires_exports"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testReflection() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "reflection"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testUsesProvide() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "uses-provides"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testExceptions() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "exceptions"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testInterfaceCallback() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "interface-callback"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }

  @Test
  public void testSplitpackage() {
    // TODO: adapt
    JavaProject p =
        JavaProject.builder(new JavaLanguage(9))
            .addModulePath(new JavaModulePathAnalysisInputLocation(testPath + "splitpackage"))
            .build();

    JavaModuleView view = (JavaModuleView) p.createOnDemandView();
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
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
    ModuleSignature startModule = JavaModuleIdentifierFactory.getModuleSignature("java.base");
    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("java.base", "java.lang.String");
    Optional<JavaSootClass> aClass = view.getClass(startModule, targetClass);
    assertTrue(aClass.isPresent());
  }
}
