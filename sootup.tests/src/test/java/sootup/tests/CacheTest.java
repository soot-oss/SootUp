package sootup.tests;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.cache.provider.LRUCacheProvider;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

/**
 * Comprises tests that test the behavior of different types of caches. It uses the MiniApp.jar for
 * testing.
 */
@Category(Java8Test.class)
public class CacheTest {
  static Path pathToJar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
  static JavaProject p;

  /** Load the jar file for analysis as input location. */
  @BeforeClass
  public static void setupProject() {
    PathBasedAnalysisInputLocation location =
        PathBasedAnalysisInputLocation.create(pathToJar, SourceType.Application);
    p = JavaProject.builder(new JavaLanguage(8)).addInputLocation(location).build();
  }

  /** Test the {@link sootup.core.cache.FullCache} class */
  @Test
  public void fullCacheTest() {
    JavaView view = p.createView(new FullCacheProvider<>());
    assertEquals(0, view.getAmountOfStoredClasses());

    ClassType miniAppClassType = p.getIdentifierFactory().getClassType("MiniApp");
    view.getClass(miniAppClassType);
    assertEquals(1, view.getAmountOfStoredClasses());

    ClassType utilsOperationClassType = p.getIdentifierFactory().getClassType("utils.Operations");
    view.getClass(utilsOperationClassType);
    assertEquals(2, view.getAmountOfStoredClasses());

    view.getClasses();
    assertEquals(6, view.getAmountOfStoredClasses());
  }

  /** Test the {@link sootup.core.cache.LRUCache} class */
  @Test
  public void lruCacheTest() {
    JavaView view = p.createView(new LRUCacheProvider<>(1));
    assertEquals(0, view.getAmountOfStoredClasses());

    ClassType miniAppClassType = p.getIdentifierFactory().getClassType("MiniApp");
    view.getClass(miniAppClassType);
    assertEquals(1, view.getAmountOfStoredClasses());

    ClassType utilsOperationClassType = p.getIdentifierFactory().getClassType("utils.Operations");
    view.getClass(utilsOperationClassType);
    assertEquals(1, view.getAmountOfStoredClasses());

    view.getClasses();
    assertEquals(1, view.getAmountOfStoredClasses());

    JavaView newView = new JavaView(p, new LRUCacheProvider<>());
    newView.getClasses();
    assertEquals(6, newView.getAmountOfStoredClasses());
  }
}
