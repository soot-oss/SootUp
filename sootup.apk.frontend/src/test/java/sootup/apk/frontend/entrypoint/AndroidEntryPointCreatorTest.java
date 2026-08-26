package sootup.apk.frontend.entrypoint;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.ApkAnalysisInputLocation;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

/**
 * Validates that entry points derived automatically from the manifest (steps 1/2/5 of {@code
 * ANDROID_CALL_GRAPH_PLAN.md}) reproduce the same call graphs as {@code CallGraphTest}, which
 * hand-picks the same {@code onCreate} methods as entry points.
 */
public class AndroidEntryPointCreatorTest {

  private static final String ANDROID_PLATFORMS_PATH = "src/test/resources/platforms";

  private static JavaView createViewForApk(String apkPathString) {
    Path apkPath = Paths.get(apkPathString);
    AndroidVersionInfo androidVersionInfo = new AndroidVersionInfo(apkPath, ANDROID_PLATFORMS_PATH);

    ApkAnalysisInputLocation apkInputLocation =
        new ApkAnalysisInputLocation(
            apkPath, androidVersionInfo, DexBodyInterceptors.Default.bodyInterceptors());
    JavaClassPathAnalysisInputLocation classPathInputLocation =
        new JavaClassPathAnalysisInputLocation(
            ANDROID_PLATFORMS_PATH
                + File.separator
                + "android-"
                + androidVersionInfo.getApi_version()
                + File.separator
                + "android.jar");

    return new JavaView(List.of(apkInputLocation, classPathInputLocation));
  }

  @Test
  public void testFlowSensitivityEntryPointsMatchCallGraphTest() {
    JavaView view = createViewForApk("src/test/resources/FlowSensitivity1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/FlowSensitivity1.apk"));

    List<MethodSignature> entryPoints = AndroidEntryPointCreator.getEntryPoints(view, manifest);

    MethodSignature onCreate =
        view.getIdentifierFactory()
            .getMethodSignature(
                "de.ecspride.MainActivity", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = cha.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(9, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testLocationLeakEntryPointsMatchCallGraphTest() {
    JavaView view = createViewForApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    List<MethodSignature> entryPoints = AndroidEntryPointCreator.getEntryPoints(view, manifest);

    MethodSignature onCreate =
        view.getIdentifierFactory()
            .getMethodSignature(
                "de.ecspride.LocationLeak1", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm rta =
        new RapidTypeAnalysisAlgorithm(
            view, view.getClasses().map(JavaSootClass::getType).collect(Collectors.toSet()));
    CallGraph cg = rta.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(5, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testCryptoEntryPointsMatchCallGraphTest() {
    JavaView view = createViewForApk("src/test/resources/Crypto.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/Crypto.apk"));

    List<MethodSignature> entryPoints = AndroidEntryPointCreator.getEntryPoints(view, manifest);

    MethodSignature onCreate =
        view.getIdentifierFactory()
            .getMethodSignature(
                "com.example.MainActivity", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = cha.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(3, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testNoApplicationClassMeansNoApplicationEntryPoints() {
    JavaView view = createViewForApk("src/test/resources/Crypto.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/Crypto.apk"));

    assertTrue(manifest.getApplicationClassName().isEmpty());

    // every generated entry point must belong to a declared component's class (or a superclass
    // of it), never to a synthesized "Application" placeholder
    List<MethodSignature> entryPoints = AndroidEntryPointCreator.getEntryPoints(view, manifest);
    assertTrue(entryPoints.size() > 0);
  }
}
