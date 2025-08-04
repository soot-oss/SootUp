package sootup.java.bytecode.frontend.inputlocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.SootClassSource;
import sootup.core.types.ClassType;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.views.JavaModuleView;

/**
 * @author Andreas Dann, Markus Schmidt
 */
public class JrtFileSystemAnalysisInputLocationTest {

  @Test
  public void getClassSource() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();

    JavaModuleView view =
        new JavaModuleView(Collections.emptyList(), Collections.singletonList(inputLocation));

    final ClassType sig =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");

    final Optional<? extends SootClassSource> clazz = inputLocation.getClassSource(sig, view);
    assertTrue(clazz.isPresent());
    assertEquals(sig, clazz.get().getClassType());
  }

  @Test
  public void getClassSources() {
    // hint: quite expensive as it loads **all** Runtime modules!
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    JavaModuleView view =
        new JavaModuleView(Collections.emptyList(), Collections.singletonList(inputLocation));

    final ClassType sig1 =
        JavaModuleIdentifierFactory.getInstance().getClassType("String", "java.lang", "java.base");
    final ClassType sig2 =
        JavaModuleIdentifierFactory.getInstance().getClassType("System", "java.lang", "java.base");

    final Collection<? extends SootClassSource> classSources =
        inputLocation.getClassSources(view).collect(Collectors.toList());
    inputLocation.getClassSources(view);
    assertTrue(
        classSources.size()
            > 20000); // "a lot" not precise as this amount can differ depending on the included
    // runtime
    // library
    assertTrue(classSources.stream().anyMatch(cs -> cs.getClassType().equals(sig1)));
    assertTrue(view.getClass(sig1).isPresent());
    assertTrue(classSources.stream().anyMatch(cs -> cs.getClassType().equals(sig2)));
    assertTrue(view.getClass(sig2).isPresent());
  }

  @Test
  public void discoverModules() {
    JrtFileSystemAnalysisInputLocation inputLocation = new JrtFileSystemAnalysisInputLocation();
    Collection<ModuleSignature> modules = inputLocation.discoverModules();
    assertTrue(modules.size() > 65);
    System.out.println(modules);
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("java.base")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("java.se")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("jdk.javadoc")));
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("jdk.charsets")));
  }
}
