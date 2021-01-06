package de.upb.swt.soot.test.java.bytecode.inputlocation;

import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.inputlocation.JrtFileSystemAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.inputlocation.ModuleFinder;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import java.util.Collection;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar update on 16.04.2020 */
@Category(Java9Test.class)
// TODO: [ms] unignore when modules are supported
@Ignore
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

  // TODO: Test name generation for automaticModules (Java9)
  //  String jarName = "foo-1.2.3-SNAPSHOT.jar"; -> foo
  //  String jarName = "foo-bar.jar"; --> foo.bar

  @Test
  public void modularJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors()),
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/");
    Collection<String> discoveredModules = moduleFinder.discoverAllModules();
    assertTrue(discoveredModules.contains("de.upb.mod"));
  }
}
