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
import sootup.apk.frontend.entrypoint.AndroidDynamicReceiverEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates the dynamic-{@code BroadcastReceiver} follow-up to step 7 against a real, hand-built,
 * compiled APK: a {@code BroadcastReceiver} subclass that is constructed in reachable code (an
 * {@code Activity}'s {@code onCreate}, standing in for a real
 * {@code registerReceiver(new MyReceiver(), filter)} call site — the specific call site isn't
 * traced, only the construction, matching {@link AndroidDynamicReceiverEntryPointCreator}'s
 * blanket-scan-plus-instantiation-evidence design) and, critically, has <em>no</em> manifest {@code
 * <receiver>} declaration at all, so step 5 alone can never find it. {@code onReceive} is never
 * called from app code (no in-app call site — matching how the framework actually invokes it), so
 * it must be unreachable without this creator and reachable with it.
 */
public class DynamicReceiverFixtureTest {

  private static final String ACTIVITY_CLASS = "test.fixture.receiver.MainActivity";
  private static final String RECEIVER_CLASS = "test.fixture.receiver.MyReceiver";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;
  private static Path apkPath;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String activitySmali =
        ".class public Ltest/fixture/receiver/MainActivity;\n"
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
            + "    new-instance v0, Ltest/fixture/receiver/MyReceiver;\n"
            + "    invoke-direct {v0}, Ltest/fixture/receiver/MyReceiver;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n";

    String receiverSmali =
        ".class public Ltest/fixture/receiver/MyReceiver;\n"
            + ".super Landroid/content/BroadcastReceiver;\n"
            + "\n"
            + ".method public constructor <init>()V\n"
            + "    .registers 1\n"
            + "    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V\n"
            + "    .registers 3\n"
            + "    invoke-direct {p0}, Ltest/fixture/receiver/MyReceiver;->receiverHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private receiverHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    apkPath =
        new FixtureApkBuilder()
            .smali(activitySmali)
            .smali(receiverSmali)
            .activity(ACTIVITY_CLASS)
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
  }

  private static List<MethodSignature> dynamicReceiverEntryPoints() {
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);
    return AndroidDynamicReceiverEntryPointCreator.getDynamicReceiverEntryPoints(
        ctx.view, ctx.appClassNames, instantiated);
  }

  @Test
  public void testDynamicReceiverEntryPointResolvesToRealMethod() {
    MethodSignature onReceive =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                RECEIVER_CLASS,
                "onReceive",
                "void",
                List.of("android.content.Context", "android.content.Intent"));

    assertTrue(dynamicReceiverEntryPoints().contains(onReceive));
  }

  @Test
  public void testHelperIsUnreachableFromLifecycleAloneButReachableWithDynamicReceiverDiscovery() {
    MethodSignature receiverHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(RECEIVER_CLASS, "receiverHelper", "void", List.of());

    List<MethodSignature> lifecycleOnly =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    CallGraphAlgorithm lifecycleCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleGraph = lifecycleCha.initialize(lifecycleOnly);
    assertFalse(lifecycleGraph.containsMethod(receiverHelper));

    List<MethodSignature> combined = new ArrayList<>(lifecycleOnly);
    combined.addAll(dynamicReceiverEntryPoints());
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph combinedGraph = combinedCha.initialize(combined);

    MethodSignature onReceive =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                RECEIVER_CLASS,
                "onReceive",
                "void",
                List.of("android.content.Context", "android.content.Intent"));
    assertTrue(combinedGraph.callTargetsFrom(onReceive).contains(receiverHelper));
  }
}
