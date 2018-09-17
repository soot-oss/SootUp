package de.upb.soot.namespaces;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

import categories.Java9Test;

@Category(Java9Test.class)

public class ModuleFinderTest extends AbstractNamespaceTest {

  @Test
  public void discoverModule() {
    ModuleFinder moduleFinder
        = new ModuleFinder(this.getClassProvider(), "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    Collection<String> modules = moduleFinder.discoverAllModules();
    String computedModuleName = "Soot";
    Assert.assertTrue(modules.contains(computedModuleName));
  }

  @Test
  public void automaticModuleName() throws Exception {
    ModuleFinder moduleFinder
        = new ModuleFinder(this.getClassProvider(), "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    String jarName = "foo-1.2.3-SNAPSHOT.jar";
    String result = Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo", result);

  }

  @Test
  public void automaticModuleName2() throws Exception {
    ModuleFinder moduleFinder
        = new ModuleFinder(this.getClassProvider(), "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
    String jarName = "foo-bar.jar";
    String result = Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo.bar", result);

  }
}