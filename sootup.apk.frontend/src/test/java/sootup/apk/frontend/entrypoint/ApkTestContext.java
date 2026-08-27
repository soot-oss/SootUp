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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import sootup.apk.frontend.ApkAnalysisInputLocation;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.apk.frontend.icc.AndroidIccResolver;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.MutableCallGraph;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Shared test fixture: a {@link JavaView} for a test APK plus its dex-declared class names.
 *
 * <p>Public so {@code sootup.apk.frontend.fixture}'s hand-built fixture APKs (step 9 of {@code
 * ANDROID_CALL_GRAPH_PLAN.md}) can reuse the exact same view-construction logic as every other test
 * in this module, via {@link #forApkPath}.
 */
public final class ApkTestContext {

  private static final String ANDROID_PLATFORMS_PATH = "src/test/resources/platforms";

  public final JavaView view;
  public final Set<String> appClassNames;

  private ApkTestContext(JavaView view, Set<String> appClassNames) {
    this.view = view;
    this.appClassNames = appClassNames;
  }

  public static ApkTestContext forApk(String apkPathString) {
    return forApkPath(Paths.get(apkPathString));
  }

  public static ApkTestContext forApkPath(Path apkPath) {
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
                + "android.jar",
            SourceType.Library);

    JavaView view = new JavaView(List.of(apkInputLocation, classPathInputLocation));
    return new ApkTestContext(view, apkInputLocation.getApplicationClassNames());
  }

  /**
   * Replicates {@code AndroidApkAnalysis}'s phase-1 computation (manifest lifecycle + {@code
   * android:onClick} entry points, expanded with CHA plus ICC edges) and returns the classes found
   * instantiated in that reachable code — the set steps 3/8's tests need to pass as evidence a
   * candidate listener/task class is actually reachable, not just linked into the dex.
   */
  public Set<String> instantiatedClassNamesFromCoreEntryPoints(
      AndroidManifest manifest, Path apkPath) {
    List<MethodSignature> core = new ArrayList<>();
    core.addAll(AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames));
    core.addAll(
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            view,
            manifest,
            appClassNames,
            AndroidLayoutParser.parseOnClickMethodNamesFromApk(apkPath)));

    MutableCallGraph coreGraph =
        (MutableCallGraph) (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(core);
    AndroidIccResolver.addIccEdges(coreGraph, view, manifest, appClassNames);

    return InstantiatedTypeCollector.collectInstantiatedClassNames(view, coreGraph);
  }
}
