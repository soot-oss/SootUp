package de.upb.soot.buildactor;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.signatures.JavaClassType;
import de.upb.soot.signatures.ModuleSignatureFactory;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java9Test.class)
public class ModuleBuilderActorTest {

  private de.upb.soot.views.IView createNewScene() {

    final JavaModulePathNamespace javaClassPathNamespace =
        new JavaModulePathNamespace("target/test-classes/de/upb/soot/namespaces/modules");
    ModuleSignatureFactory moduleSignatureFactory = new ModuleSignatureFactory();

    Project project = new Project(javaClassPathNamespace, moduleSignatureFactory);

    // de.upb.soot.views.JavaView view = new de.upb.soot.views.JavaView(project);

    // stuffAViewNeeds = new de.upb.soot.views.StuffAViewNeeds();
    // stuffAViewNeeds.namespaces = java.util.Collections.singleton(javaClassPathNamespace);
    // view.stuffAViewNeeds = stuffAViewNeeds;

    // 1. simple case

    return project.createOnDemandView();
  }

  @Test
  @Ignore
  public void refiyMessageModuleInfoTest() {
    de.upb.soot.views.IView iView = createNewScene();

    final JavaClassType sig =
        new de.upb.soot.signatures.ModuleSignatureFactory()
            .getClassType("module-info", "", "de.upb.mod");
    // Optional<ClassSource> source = stuffAViewNeeds.pollNamespaces(sig);

    // assertTrue(source.isPresent());

    // Resolve signature to `SootClass`
    Optional<AbstractClass> result = iView.getClass(sig);
    // stuffAViewNeeds.reifyClass(source.get(), iView);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

  @Test
  @Ignore
  public void resolveMessageModuleInfoTest() {
    de.upb.soot.views.IView iView = createNewScene();

    final JavaClassType sig =
        (new de.upb.soot.signatures.ModuleSignatureFactory())
            .getClassType("module-info", "", "de.upb.mod");

    Optional<AbstractClass> result = iView.getClass(sig);
    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }
}
