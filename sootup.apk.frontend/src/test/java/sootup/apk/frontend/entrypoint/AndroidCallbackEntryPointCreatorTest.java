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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Validates step 3 of {@code ANDROID_CALL_GRAPH_PLAN.md}: discovering callback/listener
 * implementations that the manifest-driven lifecycle entry points (steps 1/2/5) never reach —
 * restricted to classes actually instantiated somewhere in that reachable code (the precision
 * tightening layered on afterward; see {@link InstantiatedTypeCollector}).
 */
public class AndroidCallbackEntryPointCreatorTest {

  private static List<MethodSignature> getCallbackEntryPoints(ApkTestContext ctx, Path apkPath) {
    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);
    return AndroidCallbackEntryPointCreator.getCallbackEntryPoints(
        ctx.view, ctx.appClassNames, instantiated);
  }

  @Test
  public void testCryptoHasNoCallbackEntryPoints() {
    // Crypto.apk's only app classes are MainActivity (which doesn't itself override any listener
    // interface method beyond what Activity already implements intrinsically) and a plain utility
    // class - no listener implementations to find.
    Path apkPath = Paths.get("src/test/resources/Crypto.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    assertTrue(getCallbackEntryPoints(ctx, apkPath).isEmpty());
  }

  @Test
  public void testCallbackEntryPointsAreUnreachableFromLifecycleEntryPointsAlone() {
    // The concrete value of step 3: any callback method it finds is not found by CHA rooted only
    // at the step 5 lifecycle entry points, so without step 3 it'd be (wrongly) treated as dead -
    // and, symmetrically, everything step 3 finds must actually be instantiated in that same
    // lifecycle-only reachable code, or it wouldn't have passed the instantiated-type filter.
    Path apkPath = Paths.get("src/test/resources/LocationLeak1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);

    List<MethodSignature> lifecycleEntryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    List<MethodSignature> callbackEntryPoints = getCallbackEntryPoints(ctx, apkPath);

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleOnlyGraph = cha.initialize(lifecycleEntryPoints);
    for (MethodSignature callback : callbackEntryPoints) {
      assertFalse(
          lifecycleOnlyGraph.containsMethod(callback),
          callback + " should not be reachable without callback entry points");
    }

    // Once combined, the call graph actually contains every callback entry point as a root.
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    List<MethodSignature> combined = new ArrayList<>(lifecycleEntryPoints);
    combined.addAll(callbackEntryPoints);
    CallGraph combinedGraph = combinedCha.initialize(combined);
    for (MethodSignature callback : callbackEntryPoints) {
      assertTrue(combinedGraph.containsMethod(callback));
    }
  }

  @Test
  public void testResultsAreDeduplicated() {
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> callbackEntryPoints = getCallbackEntryPoints(ctx, apkPath);
    assertEquals(callbackEntryPoints.size(), new LinkedHashSet<>(callbackEntryPoints).size());
  }

  @Test
  public void testInstantiatedListenerIsFound() {
    // android.support.v7.internal.widget.ActivityChooserView$Callbacks#onClick and
    // android.support.v7.internal.view.menu.MenuDialogHelper#onClick are both genuinely
    // instantiated somewhere in FlowSensitivity1.apk's reachable code (verified via a diagnostic
    // dump of InstantiatedTypeCollector's output for this apk), so unlike the bundled-but-dead
    // classes this filter is meant to exclude, these must still be found.
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> callbackEntryPoints = getCallbackEntryPoints(ctx, apkPath);

    MethodSignature activityChooserViewCallbacksOnClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.widget.ActivityChooserView$Callbacks",
                "onClick",
                "void",
                List.of("android.view.View"));
    MethodSignature menuDialogHelperOnClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.view.menu.MenuDialogHelper",
                "onClick",
                "void",
                List.of("android.content.DialogInterface", "int"));

    assertTrue(callbackEntryPoints.contains(activityChooserViewCallbacksOnClick));
    assertTrue(callbackEntryPoints.contains(menuDialogHelperOnClick));
  }

  @Test
  public void testNeverInstantiatedCandidateIsExcluded() {
    // android.support.v4.content.ModernAsyncTask$WorkerRunnable exists in FlowSensitivity1.apk's
    // dex and (per AndroidAsyncEntryPointCreatorTest) genuinely has no override of its own to
    // begin with - but even a class that DID override a listener method should never appear here
    // unless something in reachable code actually constructs it. This is the same shape of gap
    // that a real Soot-vs-SootUp comparison caught in production: a bundled-but-never-used class
    // (there, most of android.support.v4.app.BackStackRecord/FragmentManagerImpl on a real
    // DroidBench sample) must not be treated as reachable just because it's linked into the dex.
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);

    assertFalse(
        instantiated.contains("android.support.v4.content.ModernAsyncTask$WorkerRunnable"),
        "sanity check: this class must not be reachable-instantiated for this test to be meaningful");

    List<MethodSignature> callbackEntryPoints = getCallbackEntryPoints(ctx, apkPath);
    boolean anyFromThatClass =
        callbackEntryPoints.stream()
            .anyMatch(
                sig ->
                    sig.getDeclClassType()
                        .getFullyQualifiedName()
                        .equals("android.support.v4.content.ModernAsyncTask$WorkerRunnable"));
    assertFalse(anyFromThatClass);
  }
}
