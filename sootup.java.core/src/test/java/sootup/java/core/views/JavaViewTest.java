package sootup.java.core.views;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaSootClass;

/**
 * Defines tests for the {@link sootup.java.core.views.JavaView} class.
 *
 * @author Jan Martin Persch
 */
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
                .map(SootClassSource::getClassType)
                .sorted(Comparator.comparing(ClassType::toString))
                .collect(Collectors.toList()));

    Project<AnalysisInputLocation> project = new Project<>(inputLocation);

    this.view = new JavaView<>(project);

     */
  }

  private void resolveUndefinedClass() {
    ClassType signature = view.getIdentifierFactory().getClassType("com.example.NonExistingClass");

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
    Stream<JavaSootClass> classes = this.view.getClasses();

    assertEquals(classes.count(), this.signatures.size());

    assertEquals(
        classes
            .map(JavaSootClass::getType)
            .sorted(Comparator.comparing(Type::toString))
            .collect(Collectors.toList()),
        this.signatures);
  }

  /** Counts how often the input location is asked to resolve a class source. */
  private static class CountingInputLocation extends EagerInputLocation {
    int lookups = 0;

    @Override
    public @NonNull Optional<SootClassSource> getClassSource(
        @NonNull ClassType type, sootup.core.views.View view) {
      lookups++;
      return super.getClassSource(type, view);
    }
  }

  /**
   * A type that no input location can provide must only be looked up once - {@link
   * JavaView#getClass} memoizes the absence. Without that, every repeated lookup re-probes every
   * input location, which dominates call graph construction against an incomplete classpath. This
   * counts the lookups rather than timing them, so it is not sensitive to machine speed.
   */
  @Test
  public void testAbsentClassIsResolvedOnlyOnce() {
    CountingInputLocation inputLocation = new CountingInputLocation();
    JavaView view = new JavaView(inputLocation);
    ClassType absent = view.getIdentifierFactory().getClassType("com.example.NonExistingClass");

    for (int i = 0; i < 20; i++) {
      assertFalse(view.getClass(absent).isPresent());
    }

    assertEquals(1, inputLocation.lookups, "absent type must only be resolved once");
  }

  @Test
  public void testViewEagerLoading() {
    AnalysisInputLocation inputLocation = new EagerInputLocation();
    JavaView view = new JavaView(inputLocation);
    assertFalse(view.isFullyResolved);

    JavaEagerView viewEagerLoad = new JavaEagerView(inputLocation);
    assertTrue(viewEagerLoad.isFullyResolved);

    JavaEagerView viewEagerLoad1 = new JavaEagerView(Collections.singletonList(inputLocation));
    assertTrue(viewEagerLoad1.isFullyResolved);

    JavaEagerView viewEagerLoad2 =
        new JavaEagerView(Collections.singletonList(inputLocation), new FullCacheProvider());
    assertTrue(viewEagerLoad2.isFullyResolved);
  }
}
