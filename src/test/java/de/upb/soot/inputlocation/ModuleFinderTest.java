package de.upb.soot.inputlocation;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.soot.frontends.asm.AsmJavaClassProvider;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

@Category(Java9Test.class)
public class ModuleFinderTest extends AbstractAnalysisInputLocationTest {

  @Test
  public void discoverModule() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(),
            "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    Collection<String> modules = moduleFinder.discoverAllModules();
    String computedModuleName = "Soot";
    assertTrue(modules.contains(computedModuleName));
  }

  @Test
  public void discoverModule2() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(),
            "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    AbstractAnalysisInputLocation namespace = moduleFinder.discoverModule("Soot");
    assertTrue(namespace instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverModule3() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(),
            "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    AbstractAnalysisInputLocation namespace = moduleFinder.discoverModule("java.base");
    assertTrue(namespace instanceof JrtFileSystemAnalysisInputLocation);
  }

  @Test
  public void automaticModuleName() throws Exception {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(),
            "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    String jarName = "foo-1.2.3-SNAPSHOT.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo", result);
  }

  @Test
  public void automaticModuleName2() throws Exception {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(),
            "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    String jarName = "foo-bar.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo.bar", result);
  }

  @Test
  public void modularJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(), "target/test-classes/de/upb/soot/namespaces/modules/");
    Collection<String> discoveredModules = moduleFinder.discoverAllModules();
    assertTrue(discoveredModules.contains("de.upb.mod"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void explodedModule() throws Exception {

    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(), "target/test-classes/de/upb/soot/namespaces/modules");
    Path p = Paths.get("target/test-classes/de/upb/soot/namespaces/modules/testMod");
    Whitebox.invokeMethod(moduleFinder, "buildModuleForExplodedModule", p);
    Field field = Whitebox.getField(moduleFinder.getClass(), "moduleNamespace");
    Map<String, AbstractAnalysisInputLocation> values =
        (Map<String, AbstractAnalysisInputLocation>) field.get(moduleFinder);
    assertTrue(values.containsKey("fancyMod"));
  }
}
