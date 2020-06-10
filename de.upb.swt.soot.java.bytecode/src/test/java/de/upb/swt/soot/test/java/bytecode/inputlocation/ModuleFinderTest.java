package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.ModuleFinder;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

/** @author Kaustubh Kelkar update on 16.04.2020 */
@Category(Java9Test.class)
public class ModuleFinderTest extends AnalysisInputLocationTest {

  @Test
  public void discoverModule() {
    ModuleFinder moduleFinder = new ModuleFinder(this.getClassProvider(), warFile);
    Collection<String> modules = moduleFinder.discoverAllModules();
    String computedModuleName = "dummyWarApp";
    assertTrue(modules.contains(computedModuleName));
  }

  @Test
  public void discoverModule2() {
    ModuleFinder moduleFinder = new ModuleFinder(this.getClassProvider(), warFile);
    AnalysisInputLocation inputLocation = moduleFinder.discoverModule("dummyWarApp");
    assertTrue(inputLocation instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverModule3() {
    ModuleFinder moduleFinder = new ModuleFinder(this.getClassProvider(), warFile);
    AnalysisInputLocation inputLocation = moduleFinder.discoverModule("java.base");
    assertTrue(inputLocation instanceof JrtFileSystemAnalysisInputLocation);
  }

  @Test
  public void automaticModuleName() throws Exception {
    ModuleFinder moduleFinder = new ModuleFinder(this.getClassProvider(), warFile);
    String jarName = "foo-1.2.3-SNAPSHOT.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo", result);
  }

  @Test
  public void automaticModuleName2() throws Exception {
    ModuleFinder moduleFinder = new ModuleFinder(this.getClassProvider(), warFile);
    String jarName = "foo-bar.jar";
    String result =
        Whitebox.invokeMethod(moduleFinder, "createModuleNameForAutomaticModule", jarName);
    Assert.assertEquals("foo.bar", result);
  }

  @Test
  public void modularJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors()),
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/");
    Collection<String> discoveredModules = moduleFinder.discoverAllModules();
    assertTrue(discoveredModules.contains("de.upb.mod"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void explodedModule() throws Exception {

    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors()),
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules");
    Path p =
        Paths.get("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/testMod");
    Whitebox.invokeMethod(moduleFinder, "buildModuleForExplodedModule", p);
    Field field = Whitebox.getField(moduleFinder.getClass(), "moduleInputLocation");
    Map<String, AnalysisInputLocation> values =
        (Map<String, AnalysisInputLocation>) field.get(moduleFinder);
    assertTrue(values.containsKey("fancyMod"));
  }
}
