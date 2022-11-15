package sootup.java.bytecode.inputlocation;

import static org.junit.Assert.*;

import categories.Java9Test;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.signatures.ModuleSignature;

/** @author Kaustubh Kelkar */
@Category(Java9Test.class)
public class ModuleFinderTest extends AnalysisInputLocationTest {

  @Test
  public void discoverModuleByName() {
    ModuleFinder moduleFinder = new ModuleFinder(war.toString());
    AnalysisInputLocation<JavaSootClass> inputLocation =
        moduleFinder.getModule(JavaModuleIdentifierFactory.getModuleSignature("dummyWarApp"));
    assertTrue(inputLocation instanceof PathBasedAnalysisInputLocation);
  }

  @Test
  public void discoverModuleInAllModules() {
    ModuleFinder moduleFinder = new ModuleFinder(war.toString());
    Collection<ModuleSignature> modules = moduleFinder.getAllModules();
    assertTrue(modules.contains(JavaModuleIdentifierFactory.getModuleSignature("dummyWarApp")));
  }

  @Test
  public void testModuleJar() {
    ModuleFinder moduleFinder =
        new ModuleFinder("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/");
    Collection<ModuleSignature> discoveredModules = moduleFinder.getAllModules();
    assertTrue(
        discoveredModules.contains(JavaModuleIdentifierFactory.getModuleSignature("de.upb.mod")));
  }

  @Test
  public void testModuleExploded() {
    ModuleFinder moduleFinder =
        new ModuleFinder("../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/");
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
            "../shared-test-resources/java9-target/de/upb/soot/namespaces/modules/automaticModuleWithManifest");

    assertNotNull(
        moduleFinder.getModule(
            JavaModuleIdentifierFactory.getModuleSignature(
                "automaticmoduleWithNamingViaManifestModuleName")));

    assertNull(
        moduleFinder.getModule(
            JavaModuleIdentifierFactory.getModuleSignature("AutomaticmoduleWithManifest")));
  }
}
