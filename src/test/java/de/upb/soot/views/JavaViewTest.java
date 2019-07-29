package de.upb.soot.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Defines tests for the {@link JavaView} class.
 *
 * @author Jan Martin Persch
 */
@Category(Java8Test.class)
public class JavaViewTest {

  private List<JavaClassType> signatures;
  private JavaView<AnalysisInputLocation> view;

  @Before
  public void initialize() {
    String jarFile = "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";

    assertTrue(new File(jarFile).exists());

    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(jarFile);

    this.signatures =
        Collections.unmodifiableList(
            inputLocation.getClassSources(DefaultIdentifierFactory.getInstance()).stream()
                .map(AbstractClassSource::getClassType)
                .sorted(Comparator.comparing(JavaClassType::toString))
                .collect(Collectors.toList()));

    Project<AnalysisInputLocation> project = new Project<>(inputLocation);

    this.view = new JavaView<>(project);
  }

  @Test
  public void testResolveIteratively() {
    this.signatures.forEach(
        it -> {
          AbstractClass<? extends AbstractClassSource> clazz = this.view.getClass(it).orElse(null);
          assertNotNull("Class for signature \"" + it + "\" not found.", clazz);
          assertEquals(it, clazz.getType());
        });
  }

  private void resolveUndefinedClass() {
    JavaClassType signature =
        DefaultIdentifierFactory.getInstance().getClassType("com.example.NonExistingClass");

    if (this.signatures.contains(signature)) {
      Assert.fail("FATAL ERROR: Non-existing class exists in signature list!");
    }

    assertFalse(this.view.getClass(signature).isPresent());
  }

  @Test
  public void testResolveUndefinedClassBeforeAllResolved() {
    this.resolveUndefinedClass();
  }

  @Test
  public void testResolveUndefinedClassAfterAllResolved() {
    this.resolveUndefinedClass();
  }

  @Test
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
