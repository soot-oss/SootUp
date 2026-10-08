package sootup.apk.frontend.fixture;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidCallbackEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates step 3 (listener/callback interface discovery) against a real, hand-built, compiled APK:
 * an {@code Activity} whose {@code onCreate} constructs a {@code View.OnClickListener}
 * implementation but never calls its {@code onClick} directly (matching how a real registered
 * listener actually runs — the framework calls it, not app code) — {@code MyClickListener#onClick}
 * must be unreachable without step 3 and reachable with it. The construction itself (a {@code new
 * MyClickListener()} in reachable code) is what step 3's instantiated-type filter (see {@code
 * AndroidCallbackEntryPointCreatorTest}) needs to admit the candidate at all — a listener class no
 * reachable code ever constructs can never be live in real Android execution either, so it's
 * correctly excluded, not just conveniently so.
 */
public class ListenerFixtureTest {

  private static final String ACTIVITY_CLASS = "test.fixture.listener.MainActivity";
  private static final String LISTENER_CLASS = "test.fixture.listener.MyClickListener";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;
  private static Path apkPath;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String activitySmali =
        ".class public Ltest/fixture/listener/MainActivity;\n"
            + ".super Landroid/app/Activity;\n"
            + "\n"
            + ".method public constructor <init>()V\n"
            + "    .registers 1\n"
            + "    invoke-direct {p0}, Landroid/app/Activity;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onCreate(Landroid/os/Bundle;)V\n"
            + "    .registers 3\n"
            + "    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n"
            + "    new-instance v0, Ltest/fixture/listener/MyClickListener;\n"
            + "    invoke-direct {v0}, Ltest/fixture/listener/MyClickListener;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n";

    String listenerSmali =
        ".class public Ltest/fixture/listener/MyClickListener;\n"
            + ".super Ljava/lang/Object;\n"
            + ".implements Landroid/view/View$OnClickListener;\n"
            + "\n"
            + ".method public constructor <init>()V\n"
            + "    .registers 1\n"
            + "    invoke-direct {p0}, Ljava/lang/Object;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onClick(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/listener/MyClickListener;->listenerHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private listenerHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    apkPath =
        new FixtureApkBuilder()
            .smali(activitySmali)
            .smali(listenerSmali)
            .activity(ACTIVITY_CLASS)
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
  }

  private static List<MethodSignature> callbackEntryPoints() {
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);
    return AndroidCallbackEntryPointCreator.getCallbackEntryPoints(
        ctx.view, ctx.appClassNames, instantiated);
  }

  @Test
  public void testListenerEntryPointResolvesToRealMethod() {
    List<MethodSignature> callbackEntryPoints = callbackEntryPoints();

    MethodSignature onClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(LISTENER_CLASS, "onClick", "void", List.of("android.view.View"));
    assertTrue(callbackEntryPoints.contains(onClick));
  }

  @Test
  public void testHelperIsUnreachableFromLifecycleAloneButReachableWithListenerDiscovery() {
    MethodSignature listenerHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(LISTENER_CLASS, "listenerHelper", "void", List.of());

    List<MethodSignature> lifecycleOnly =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    CallGraphAlgorithm lifecycleCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleGraph = lifecycleCha.initialize(lifecycleOnly);
    assertFalse(lifecycleGraph.containsMethod(listenerHelper));

    List<MethodSignature> callbackEntryPoints = callbackEntryPoints();
    List<MethodSignature> combined = new ArrayList<>(lifecycleOnly);
    combined.addAll(callbackEntryPoints);
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph combinedGraph = combinedCha.initialize(combined);

    MethodSignature onClick =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(LISTENER_CLASS, "onClick", "void", List.of("android.view.View"));
    assertTrue(combinedGraph.callTargetsFrom(onClick).contains(listenerHelper));
  }
}
