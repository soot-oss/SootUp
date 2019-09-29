package de.upb.soot.buildactor;

// TODO: adapt if still relevant
/*
import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.soot.ModuleIdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.inputlocation.EagerInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category(Java9Test.class)
public class ModuleBuilderActorTest {

  private View createNewScene() {

    final AnalysisInputLocation javaClassPathNamespace =
        new new JavaModulePathAnalysisInputLocation(("target/test-classes/de/upb/soot/namespaces/modules");

    Project<AnalysisInputLocation> project =
        new Project<>(javaClassPathNamespace, ModuleIdentifierFactory.getInstance());

    // de.upb.soot.views.JavaView view = new de.upb.soot.views.JavaView(project);

    // stuffAViewNeeds = new de.upb.soot.views.StuffAViewNeeds();
    // stuffAViewNeeds.namespaces = java.util.Collections.singleton(javaClassPathNamespace);
    // view.stuffAViewNeeds = stuffAViewNeeds;

    // 1. simple case

    return project.createOnDemandView();
  }

  @Test
  public void refiyMessageModuleInfoTest() {
    View view = createNewScene();

    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("module-info", "", "de.upb.mod");
    // Optional<ClassSource> source = stuffAViewNeeds.pollNamespaces(sig);

    // assertTrue(source.isPresent());

    // Resolve signature to `SootClass`
    Optional<AbstractClass<? extends AbstractClassSource>> result = view.getClass(sig);
    // stuffAViewNeeds.reifyClass(source.get(), view);

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

  @Test
  public void resolveMessageModuleInfoTest() {
    View view = createNewScene();

    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("module-info", "", "de.upb.mod");

    Optional<AbstractClass<? extends AbstractClassSource>> result = view.getClass(sig);
    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }
}
*/
