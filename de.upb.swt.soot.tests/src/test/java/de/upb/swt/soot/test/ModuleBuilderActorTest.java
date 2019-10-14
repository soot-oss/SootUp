package de.upb.swt.soot.test;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.ModuleIdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.frontend.modules.SootModuleInfo;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java9Test.class)
public class ModuleBuilderActorTest {

  private View createNewScene() {

    final AnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules",
            new DefaultSourceTypeSpecifier(),
            new AsmJavaClassProvider());

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
