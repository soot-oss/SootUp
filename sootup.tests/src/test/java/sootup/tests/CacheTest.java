package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.cache.provider.LRUCacheProvider;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Comprises tests that test the behavior of different types of caches. It uses the MiniApp.jar for
 * testing.
 */
public class CacheTest {
  static Path pathToJar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
  static List<AnalysisInputLocation> inputLocations;

  /** Load the jar file for analysis as input location. */
  @BeforeAll
  public static void setupProject() {
    inputLocations =
        Collections.singletonList(
            PathBasedAnalysisInputLocation.create(pathToJar, SourceType.Application));
  }

  /** Test the {@link sootup.core.cache.FullCache} class */
  @Test
  public void fullCacheTest() {
    JavaView view = new JavaView(inputLocations, new FullCacheProvider());
    assertEquals(0, view.getCachedClassesCount());

    ClassType miniAppClassType = view.getIdentifierFactory().getClassType("MiniApp");
    view.getClass(miniAppClassType);
    assertEquals(1, view.getCachedClassesCount());

    ClassType utilsOperationClassType =
        view.getIdentifierFactory().getClassType("utils.Operations");
    view.getClass(utilsOperationClassType);
    assertEquals(2, view.getCachedClassesCount());

    view.getClasses().count();
    assertEquals(6, view.getCachedClassesCount());
  }

  /** Test the {@link sootup.core.cache.LRUCache} class */
  @Test
  public void lruCacheTest() {
    JavaView view = new JavaView(inputLocations, new LRUCacheProvider(1));
    assertEquals(0, view.getCachedClassesCount());

    ClassType miniAppClassType = view.getIdentifierFactory().getClassType("MiniApp");
    view.getClass(miniAppClassType);
    assertEquals(1, view.getCachedClassesCount());

    ClassType utilsOperationClassType =
        view.getIdentifierFactory().getClassType("utils.Operations");
    view.getClass(utilsOperationClassType);
    assertEquals(1, view.getCachedClassesCount());

    view.getClasses();
    assertEquals(1, view.getCachedClassesCount());

    JavaView newView = new JavaView(inputLocations, new LRUCacheProvider());
    newView.getClasses().count();
    assertEquals(6, newView.getCachedClassesCount());
  }
}
