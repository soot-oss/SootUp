package de.upb.swt.soot.java.bytecode.frontend;

import static org.junit.Assert.*;

import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Optional;
import org.junit.Test;

/*
* Tests module-info properties in a JavaModule
 [] requires
 [] requires transitive
 [] exports
 [] exports ... to ...
 [] uses
 [] provides ... with ...
 [] allow reflection (nicetohave):

 [] open module
 [] opens directive
 [] opens ... to ... directive
* */

public class AsmModuleSourceTest {

  private final String testPath = "../shared-test-resources/jigsaw-examples/";

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

    fail("implement property check");
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

    fail("implement property check");
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

    fail("implement property check");
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

    fail("implement property check");
  }
}
