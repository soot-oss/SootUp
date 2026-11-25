package sootup.java.bytecode.frontend.inputlocation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.ScopedAnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class ScopedAnalysisInputLocationTest {

  private static final String CLASS_PATH = "src/test/resources/interceptors";
  private static final Predicate<ClassType> IS_SPLITTER_TARGET =
      type -> type.getClassName().startsWith("LocalSplitter");

  @Test
  public void onlyMatchingClassesAreVisible() {
    JavaView view =
        new JavaView(
            ScopedAnalysisInputLocation.of(
                new JavaClassPathAnalysisInputLocation(CLASS_PATH), IS_SPLITTER_TARGET));

    assertTrue(
        view.getClass(view.getIdentifierFactory().getClassType("LocalSplitterTarget")).isPresent());
    assertFalse(
        view.getClass(view.getIdentifierFactory().getClassType("CopyPropagator")).isPresent());

    Set<String> names =
        view.getClasses().map(c -> c.getType().getClassName()).collect(Collectors.toSet());
    assertFalse(names.isEmpty());
    assertTrue(names.stream().allMatch(n -> n.startsWith("LocalSplitter")), names.toString());
  }

  @Test
  public void complementaryScopesAssignSourceTypes() {
    JavaView view =
        new JavaView(
            Arrays.asList(
                ScopedAnalysisInputLocation.of(
                    new JavaClassPathAnalysisInputLocation(CLASS_PATH, SourceType.Application),
                    IS_SPLITTER_TARGET),
                ScopedAnalysisInputLocation.of(
                    new JavaClassPathAnalysisInputLocation(CLASS_PATH, SourceType.Library),
                    IS_SPLITTER_TARGET.negate())));

    JavaSootClass splitterTarget =
        view.getClass(view.getIdentifierFactory().getClassType("LocalSplitterTarget")).get();
    JavaSootClass copyPropagator =
        view.getClass(view.getIdentifierFactory().getClassType("CopyPropagator")).get();
    assertTrue(splitterTarget.isApplicationClass());
    assertTrue(copyPropagator.isLibraryClass());

    // every class is provided exactly once
    long all = view.getClasses().count();
    long distinct = view.getClasses().map(JavaSootClass::getType).distinct().count();
    assertEquals(distinct, all);
  }
}
