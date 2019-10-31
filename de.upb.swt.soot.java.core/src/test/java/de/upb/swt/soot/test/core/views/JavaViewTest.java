package de.upb.swt.soot.test.core.views;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.views.JavaView;
import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Defines tests for the {@link JavaView} class.
 *
 * @author Jan Martin Persch
 */
@Category(Java8Test.class)
public class JavaViewTest {

  private List<ClassType> signatures;
  private JavaView view;

  public static final String jarFile =
      "../shared-test-resources/java9-target/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";

  @Before
  public void initialize() {

    assertTrue(new File(jarFile).exists());

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

  @Ignore
  public void testResolveIteratively() {
    this.signatures.forEach(
        it -> {
          AbstractClass<? extends AbstractClassSource> clazz = this.view.getClass(it).orElse(null);
          assertNotNull("Class for signature \"" + it + "\" not found.", clazz);
          assertEquals(it, clazz.getType());
        });
  }

  private void resolveUndefinedClass() {
    ClassType signature =
        JavaIdentifierFactory.getInstance().getClassType("com.example.NonExistingClass");

    if (this.signatures.contains(signature)) {
      Assert.fail("FATAL ERROR: Non-existing class exists in signature list!");
    }

    assertFalse(this.view.getClass(signature).isPresent());
  }

  @Ignore
  public void testResolveUndefinedClassBeforeAllResolved() {
    this.resolveUndefinedClass();
  }

  @Ignore
  public void testResolveUndefinedClassAfterAllResolved() {
    this.resolveUndefinedClass();
  }

  @Ignore
  public void testResolveAll() {
    Collection<AbstractClass<? extends AbstractClassSource>> classes = this.view.getClasses();

    assertEquals(classes.size(), this.signatures.size());

    assertEquals(
        classes.stream()
            .map(AbstractClass::getType)
            .sorted(Comparator.comparing(Type::toString))
            .collect(Collectors.toList()),
        this.signatures);
  }
}
