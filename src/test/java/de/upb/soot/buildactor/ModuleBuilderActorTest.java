package de.upb.soot.buildactor;

import categories.Java9Test;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.signatures.JavaClassSignature;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

@Category(Java9Test.class)

public class ModuleBuilderActorTest {

  private de.upb.soot.views.IView createNewScene() {

    final JavaModulePathNamespace javaClassPathNamespace
        = new JavaModulePathNamespace("target/test-classes/de/upb/soot/namespaces/modules");

    de.upb.soot.Project project = new de.upb.soot.Project(javaClassPathNamespace);

    // de.upb.soot.views.JavaView view = new de.upb.soot.views.JavaView(project);

    // stuffAViewNeeds = new de.upb.soot.views.StuffAViewNeeds();
    // stuffAViewNeeds.namespaces = java.util.Collections.singleton(javaClassPathNamespace);
    // view.stuffAViewNeeds = stuffAViewNeeds;

    // 1. simple case

    return project.createDemandView();
  }

  @Test
  public void refiyMessageModuleInfoTest() {
    de.upb.soot.views.IView iView = createNewScene();

    final JavaClassSignature sig
        = new de.upb.soot.signatures.ModuleSignatureFactory().getClassSignature("module-info", "", "de.upb.mod");
    // Optional<ClassSource> source = stuffAViewNeeds.pollNamespaces(sig);

    // assertTrue(source.isPresent());

    Optional<AbstractClass> result = iView.getClass(sig);
    // stuffAViewNeeds.reifyClass(source.get(), iView);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

  @Test
  public void resolveMessageModuleInfoTest() {
    de.upb.soot.views.IView iView = createNewScene();

    final JavaClassSignature sig
        = (new de.upb.soot.signatures.ModuleSignatureFactory()).getClassSignature("module-info", "", "de.upb.mod");

    Optional<AbstractClass> result = iView.getClass(sig);
    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

}