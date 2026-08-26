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

import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Validates step 3 of {@code ANDROID_CALL_GRAPH_PLAN.md}: discovering callback/listener
 * implementations that the manifest-driven lifecycle entry points (steps 1/2/5) never reach.
 */
public class AndroidCallbackEntryPointCreatorTest {

  @Test
  public void testCryptoHasNoCallbackEntryPoints() {
    // Crypto.apk's only app classes are MainActivity (which doesn't itself override any listener
    // interface method beyond what Activity already implements intrinsically) and a plain utility
    // class - no listener implementations to find.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/Crypto.apk");
    List<MethodSignature> callbackEntryPoints =
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(ctx.view, ctx.appClassNames);
    assertTrue(callbackEntryPoints.isEmpty());
  }

  @Test
  public void testLocationLeakFindsBundledListenerImplementations() {
    // LocationLeak1.apk bundles the android.support.v4 compat library directly in its dex, which
    // contains real OnClickListener/DialogInterface listener implementations - genuine app-bundled
    // code, not platform (android.jar) code, and exactly what step 3 should surface.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    List<MethodSignature> callbackEntryPoints =
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(ctx.view, ctx.appClassNames);

    MethodSignature pagerTabStripOnClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.view.PagerTabStrip$2",
                "onClick",
                "void",
                List.of("android.view.View"));
    MethodSignature dialogFragmentOnCancel =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.app.DialogFragment",
                "onCancel",
                "void",
                List.of("android.content.DialogInterface"));

    assertTrue(callbackEntryPoints.contains(pagerTabStripOnClick));
    assertTrue(callbackEntryPoints.contains(dialogFragmentOnCancel));
  }

  @Test
  public void testCallbackEntryPointsAreUnreachableFromLifecycleEntryPointsAlone() {
    // The concrete value of step 3: these callback methods are not found by CHA rooted only at
    // the step 5 lifecycle entry points, so without step 3 they'd be (wrongly) treated as dead.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    List<MethodSignature> lifecycleEntryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    List<MethodSignature> callbackEntryPoints =
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(ctx.view, ctx.appClassNames);
    assertFalse(callbackEntryPoints.isEmpty());

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleOnlyGraph = cha.initialize(lifecycleEntryPoints);
    for (MethodSignature callback : callbackEntryPoints) {
      assertFalse(
          lifecycleOnlyGraph.containsMethod(callback),
          callback + " should not be reachable without callback entry points");
    }

    // Once combined, the call graph actually contains every callback entry point as a root.
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    List<MethodSignature> combined = new java.util.ArrayList<>(lifecycleEntryPoints);
    combined.addAll(callbackEntryPoints);
    CallGraph combinedGraph = combinedCha.initialize(combined);
    for (MethodSignature callback : callbackEntryPoints) {
      assertTrue(combinedGraph.containsMethod(callback));
    }
  }

  @Test
  public void testFlowSensitivityFindsMultipleDistinctListenerInterfaces() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> callbackEntryPoints =
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(ctx.view, ctx.appClassNames);

    // The bundled appcompat/support library implements several distinct listener interfaces
    // (OnClickListener, OnItemClickListener, OnKeyListener, TextWatcher, ...); assert we found a
    // representative one from each rather than pinning the exact (large, library-version-specific)
    // count.
    MethodSignature actionBarViewOnClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.widget.ActionBarView$2",
                "onClick",
                "void",
                List.of("android.view.View"));
    MethodSignature searchViewTextWatcher =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.widget.SearchView$12",
                "afterTextChanged",
                "void",
                List.of("android.text.Editable"));
    MethodSignature menuItemClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.view.menu.MenuItemWrapperICS$OnMenuItemClickListenerWrapper",
                "onMenuItemClick",
                "boolean",
                List.of("android.view.MenuItem"));

    assertTrue(callbackEntryPoints.contains(actionBarViewOnClick));
    assertTrue(callbackEntryPoints.contains(searchViewTextWatcher));
    assertTrue(callbackEntryPoints.contains(menuItemClick));
  }

  @Test
  public void testResultsAreDeduplicated() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> callbackEntryPoints =
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(ctx.view, ctx.appClassNames);
    assertEquals(callbackEntryPoints.size(), new java.util.LinkedHashSet<>(callbackEntryPoints).size());
  }
}
