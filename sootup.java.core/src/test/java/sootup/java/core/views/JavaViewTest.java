package sootup.java.core.views;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.model.AbstractClass;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;

/**
 * Defines tests for the {@link sootup.java.core.views.JavaView} class.
 *
 * @author Jan Martin Persch
 */
@Tag("Java8")
public class JavaViewTest {

  private List<ClassType> signatures;
  private JavaView view;

  @BeforeEach
  public void initialize() {

    // TODO fails due to dependency to asm - rewrite test to allow multimodule maven -> eagerLoader
    /*
    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile);

    this.signatures =
        Collections.unmodifiableList(
            inputLocation.getClassSources(DefaultIdentifierFactory.getInstance()).stream()
                .map(AbstractClassSource::getClassType)
                .sorted(Comparator.comparing(ClassType::toString))
                .collect(Collectors.toList()));

    Project<AnalysisInputLocation> project = new Project<>(inputLocation);

    this.view = new JavaView<>(project);

     */
  }

  private void resolveUndefinedClass() {
    ClassType signature =
        JavaIdentifierFactory.getInstance().getClassType("com.example.NonExistingClass");

    if (this.signatures.contains(signature)) {
      fail("FATAL ERROR: Non-existing class exists in signature list!");
    }

    assertFalse(this.view.getClass(signature).isPresent());
  }

  @Disabled
  public void testResolveUndefinedClassBeforeAllResolved() {
    this.resolveUndefinedClass();
  }

  @Disabled
  public void testResolveUndefinedClassAfterAllResolved() {
    this.resolveUndefinedClass();
  }

  @Disabled
  public void testResolveAll() {
    Collection<JavaSootClass> classes = this.view.getClasses();

    assertEquals(classes.size(), this.signatures.size());

    assertEquals(
        classes.stream()
            .map(AbstractClass::getType)
            .sorted(Comparator.comparing(Type::toString))
            .collect(Collectors.toList()),
        this.signatures);
  }
}
