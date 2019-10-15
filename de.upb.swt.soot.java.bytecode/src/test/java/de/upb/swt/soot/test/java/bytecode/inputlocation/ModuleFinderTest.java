package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.inputlocation.AbstractAnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.ModuleFinder;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
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
            this.getClassProvider(), jarFile, DefaultSourceTypeSpecifier.getInstance());
    Collection<String> modules = moduleFinder.discoverAllModules();
    String computedModuleName = "Soot";
    assertTrue(modules.contains(computedModuleName));
  }

  @Test
  public void discoverModule2() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(), jarFile, DefaultSourceTypeSpecifier.getInstance());
    AbstractAnalysisInputLocation inputLocation = moduleFinder.discoverModule("Soot");
    assertTrue(inputLocation instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverModule3() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(), jarFile, DefaultSourceTypeSpecifier.getInstance());
    AbstractAnalysisInputLocation inputLocation = moduleFinder.discoverModule("java.base");
    assertTrue(inputLocation instanceof JrtFileSystemAnalysisInputLocation);
  }

  @Test
  public void automaticModuleName() throws Exception {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(), jarFile, DefaultSourceTypeSpecifier.getInstance());
    String jarName = "foo-1.2.3-SNAPSHOT.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo", result);
  }

  @Test
  public void automaticModuleName2() throws Exception {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            this.getClassProvider(), jarFile, DefaultSourceTypeSpecifier.getInstance());
    String jarName = "foo-bar.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo.bar", result);
  }

  @Test
  public void modularJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(),
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/",
            DefaultSourceTypeSpecifier.getInstance());
    Collection<String> discoveredModules = moduleFinder.discoverAllModules();
    assertTrue(discoveredModules.contains("de.upb.mod"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void explodedModule() throws Exception {

    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(),
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules",
            DefaultSourceTypeSpecifier.getInstance());
    Path p =
        Paths.get("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/testMod");
    Whitebox.invokeMethod(moduleFinder, "buildModuleForExplodedModule", p);
    Field field = Whitebox.getField(moduleFinder.getClass(), "moduleInputLocation");
    Map<String, AbstractAnalysisInputLocation> values =
        (Map<String, AbstractAnalysisInputLocation>) field.get(moduleFinder);
    assertTrue(values.containsKey("fancyMod"));
  }
}
