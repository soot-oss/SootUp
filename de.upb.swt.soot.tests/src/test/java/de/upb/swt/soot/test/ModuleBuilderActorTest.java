package de.upb.swt.soot.test;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java9Test.class)
public class ModuleBuilderActorTest {

  private JavaModuleView createNewScene() {

    final AnalysisInputLocation javaClassPathNamespace =
        new JavaModulePathAnalysisInputLocation(
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules");

    JavaProject project =
        JavaProject.builder(new JavaLanguage(9)).addClassPath(javaClassPathNamespace).build();

    // de.upb.soot.views.JavaView view = new de.upb.soot.views.JavaView(project);

    // stuffAViewNeeds = new de.upb.soot.views.StuffAViewNeeds();
    // stuffAViewNeeds.namespaces = java.util.Collections.singleton(javaClassPathNamespace);
    // view.stuffAViewNeeds = stuffAViewNeeds;

    // 1. simple case

    // TODO Why do we have a separate JavaModuleView? Can't we put its features into JavaView?
    return (JavaModuleView) project.createOnDemandView();
  }

  @Test
  public void buildMessageModuleInfoTest() {
    JavaModuleView view = createNewScene();

    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("module-info", "", "de.upb.mod");
    // Optional<ClassSource> source = stuffAViewNeeds.pollNamespaces(sig);

    // assertTrue(source.isPresent());

    // Resolve signature
    Optional<JavaModuleInfo> result = view.getModuleInfo(sig);
    // stuffAViewNeeds.reifyClass(source.get(), view);

    assertTrue(result.isPresent());
  }

  @Test
  public void resolveMessageModuleInfoTest() {
    JavaModuleView view = createNewScene();

    final JavaClassType sig =
        ModuleIdentifierFactory.getInstance().getClassType("module-info", "", "de.upb.mod");

    Optional<JavaModuleInfo> result = view.getModuleInfo(sig);
    assertTrue(result.isPresent());
  }
}
