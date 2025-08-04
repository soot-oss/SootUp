package sootup.java.bytecode.frontend.inputlocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.signatures.ModuleSignature;

/**
 * @author Kaustubh Kelkar
 */
public class ModuleFinderTest extends AnalysisInputLocationTest {

  @Test
  public void discoverJarModuleByName() {
    ModuleFinder moduleFinder = new ModuleFinder(jar);
    AnalysisInputLocation inputLocation =
        moduleFinder.getModule(JavaModuleIdentifierFactory.getModuleSignature("MiniApp"));
    assertTrue(inputLocation instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverJarModuleInAllModules() {
    ModuleFinder moduleFinder = new ModuleFinder(jar);
    Collection<ModuleSignature> modules = moduleFinder.getAllModules();
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("MiniApp")));
  }

  @Test
  public void discoverWarModuleByName() {
    ModuleFinder moduleFinder = new ModuleFinder(war);
    AnalysisInputLocation inputLocation =
        moduleFinder.getModule(JavaModuleIdentifierFactory.getModuleSignature("dummyWarApp"));
    assertTrue(inputLocation instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverWarModuleInAllModules() {
    ModuleFinder moduleFinder = new ModuleFinder(war);
    Collection<ModuleSignature> modules = moduleFinder.getAllModules();
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("dummyWarApp")));
  }

  @Test
  public void testModuleJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            Paths.get("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/"));
    Collection<ModuleSignature> discoveredModules = moduleFinder.getAllModules();
    assertTrue(
        discoveredModules.contains(JavaModuleIdentifierFactory.getModuleSignature("de.upb.mod")));
  }

  @Test
  public void testModuleExploded() {
    ModuleFinder moduleFinder =
        new ModuleFinder(
            Paths.get("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/"));
    Collection<ModuleSignature> discoveredModules = moduleFinder.getAllModules();
    assertTrue(
        discoveredModules.contains(JavaModuleIdentifierFactory.getModuleSignature("fancyMod")));
  }

  @Test
  public void testAutomaticModuleNaming() {
    assertEquals(
        "foo.bar", ModuleFinder.createModuleNameForAutomaticModule(Paths.get("foo-bar.jar")));
    assertEquals(
        "foo",
        ModuleFinder.createModuleNameForAutomaticModule(Paths.get("foo-1.2.3-SNAPSHOT.jar")));
  }

  @Test
  public void testAutomaticModuleNamingViaManifest() {

    ModuleFinder moduleFinder =
        new ModuleFinder(
            Paths.get(
                "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/automaticModuleWithManifest"));

    assertNotNull(
        moduleFinder.getModule(
            JavaModuleIdentifierFactory.getModuleSignature(
                "automaticmoduleWithNamingViaManifestModuleName")));

    assertNull(
        moduleFinder.getModule(
            JavaModuleIdentifierFactory.getModuleSignature("AutomaticmoduleWithManifest")));
  }
}
