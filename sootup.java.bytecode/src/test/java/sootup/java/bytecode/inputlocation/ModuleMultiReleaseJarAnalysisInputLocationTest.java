package sootup.java.bytecode.inputlocation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java9Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;
import sootup.java.core.views.JavaModuleView;
import sootup.java.core.views.JavaView;

@Category(Java9Test.class)
public class ModuleMultiReleaseJarAnalysisInputLocationTest extends AnalysisInputLocationTest {
  final Path mmrj = Paths.get("../shared-test-resources/multi-release-jar-modular/mrjar.jar");

  @Test
  public void modularMultiReleaseJar() {

    assertTrue(MultiReleaseJarAnalysisInputLocation.isMultiReleaseJar(mmrj));

    final ClassType utilityNoModule =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");

    final ModuleJavaClassType utilityModule =
        JavaModuleIdentifierFactory.getInstance()
            .getClassType("de.upb.swt.multirelease/de.upb.swt.multirelease.Utility");

    final ClassType classType2 =
        getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

    ModuleInfoAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation8 =
        new ModuleMultiReleaseJarAnalysisInputLocation(
            mmrj, SourceType.Application, new JavaLanguage(8));

    final JavaView view_8 =
        new JavaModuleView(
            Collections.emptyList(),
            Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation8));

    ModuleMultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation9 =
        new ModuleMultiReleaseJarAnalysisInputLocation(
            mmrj, SourceType.Application, new JavaLanguage(9));
    final JavaModuleView view_9 =
        new JavaModuleView(
            Collections.emptyList(),
            Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation9));
    ModuleSignature moduleSignature =
        JavaModuleIdentifierFactory.getModuleSignature("de.upb.swt.multirelease");

    Assert.assertEquals(Collections.singleton(moduleSignature), view_9.getNamedModules());

    Assert.assertTrue(view_9.getModuleInfo(moduleSignature).isPresent());

    Assert.assertEquals(1, view_9.getModuleClasses(moduleSignature).size());

    Assert.assertEquals(
        "de.upb.swt.multirelease.Utility",
        view_9.getModuleClasses(moduleSignature).stream()
            .findAny()
            .get()
            .getType()
            .getFullyQualifiedName());

    // for java 9
    Assert.assertEquals(
        "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityModule).get().getClassSource().getSourcePath().toString());
    // different class will be returned if no module is specified
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_9.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    Assert.assertTrue(
        view_9
            .getClass(utilityModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 9"));

    // for java 8
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Utility.class",
        view_8.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
    assertFalse(view_8.getClass(utilityModule).isPresent());
    Assert.assertEquals(
        "/de/upb/swt/multirelease/Main.class",
        view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
    // assert that method is correctly resolved to base
    Assert.assertTrue(
        view_8
            .getClass(utilityNoModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 8"));

    Assert.assertTrue(
        view_8
            .getClass(utilityNoModule)
            .get()
            .getMethod(
                getIdentifierFactory()
                    .getMethodSubSignature(
                        "printVersion",
                        getIdentifierFactory().getType("void"),
                        Collections.emptyList()))
            .get()
            .getBody()
            .toString()
            .contains("java 8"));

    // TODO: test what happens if we put a *non* ModuleClassType into a
    // ModuleMultiReleaseAnalysisInoutLocation

  }
}
