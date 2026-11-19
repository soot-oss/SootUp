package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.MutableJavaView;

/**
 * Checks that a class removed from a {@link MutableJavaView} is not loaded again from the input
 * location that still contains its class file.
 */
public class MutableJavaViewRemovalTest {

  private MutableJavaView view;
  private ClassType employee;

  @BeforeEach
  public void setup() {
    view =
        new MutableJavaView(
            PathBasedAnalysisInputLocation.create(
                Paths.get("src/test/resources/java-miniapps/MiniApp.jar"), SourceType.Application));
    employee = view.getIdentifierFactory().getClassType("ds.Employee");
  }

  private boolean listed(ClassType type) {
    return view.getClasses().anyMatch(c -> c.getType().equals(type));
  }

  @Test
  public void removedClassIsNotLoadedAgain() {
    assertTrue(view.getClass(employee).isPresent());

    view.removeClass(employee);

    assertFalse(view.getClass(employee).isPresent());
    assertFalse(listed(employee));
    assertFalse(view.getTypeHierarchy().contains(employee));
    // still removed after all classes were loaded
    assertFalse(view.getClass(employee).isPresent());
  }

  @Test
  public void classRemovedBeforeItWasLoadedStaysRemoved() {
    view.removeClass(employee);

    assertFalse(listed(employee));
    assertFalse(view.getClass(employee).isPresent());
  }

  @Test
  public void removedClassCanBeAddedAgain() {
    JavaSootClass original = view.getClass(employee).get();
    view.removeClass(employee);
    assertFalse(listed(employee));

    view.addClass(original);

    assertSame(original, view.getClass(employee).get());
    assertTrue(listed(employee));
    assertTrue(view.getTypeHierarchy().contains(employee));
  }
}
