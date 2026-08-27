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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates steps 1/2/5 (manifest parsing, lifecycle tables, entry-point derivation) against a real,
 * hand-built, compiled multi-component APK: an {@code Activity}, a {@code Service} and a {@code
 * BroadcastReceiver}, each declared only in the manifest — no listeners, no ICC, no {@code
 * AsyncTask} — and each lifecycle callback calling its own private helper method, so reachability
 * of that helper is a direct proxy for "was this lifecycle method correctly wired up as an entry
 * point."
 */
public class ManifestLifecycleFixtureTest {

  private static final String PKG = "test.fixture.lifecycle";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String activitySmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MainActivity;\n"
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
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MainActivity;->activityHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private activityHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String serviceSmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MainService;\n"
            + ".super Landroid/app/Service;\n"
            + "\n"
            + ".method public constructor <init>()V\n"
            + "    .registers 1\n"
            + "    invoke-direct {p0}, Landroid/app/Service;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onBind(Landroid/content/Intent;)Landroid/os/IBinder;\n"
            + "    .registers 3\n"
            + "    const/4 v0, 0x0\n"
            + "    return-object v0\n"
            + ".end method\n"
            + "\n"
            + ".method public onStartCommand(Landroid/content/Intent;II)I\n"
            + "    .registers 5\n"
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MainService;->serviceHelper()V\n"
            + "    const/4 v0, 0x0\n"
            + "    return v0\n"
            + ".end method\n"
            + "\n"
            + ".method private serviceHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String receiverSmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MainReceiver;\n"
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
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MainReceiver;->receiverHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private receiverHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        new FixtureApkBuilder()
            .smali(activitySmali)
            .smali(serviceSmali)
            .smali(receiverSmali)
            .activity(PKG + ".MainActivity")
            .component(
                sootup.apk.frontend.manifest.AndroidComponentType.SERVICE,
                PKG + ".MainService",
                List.of())
            .component(
                sootup.apk.frontend.manifest.AndroidComponentType.BROADCAST_RECEIVER,
                PKG + ".MainReceiver",
                List.of())
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
  }

  @Test
  public void testActivityLifecycleHelperIsReachable() {
    assertEntryPointReaches(
        PKG + ".MainActivity",
        "onCreate",
        "void",
        List.of("android.os.Bundle"),
        PKG + ".MainActivity",
        "activityHelper");
  }

  @Test
  public void testServiceLifecycleHelperIsReachable() {
    assertEntryPointReaches(
        PKG + ".MainService",
        "onStartCommand",
        "int",
        List.of("android.content.Intent", "int", "int"),
        PKG + ".MainService",
        "serviceHelper");
  }

  @Test
  public void testReceiverLifecycleHelperIsReachable() {
    assertEntryPointReaches(
        PKG + ".MainReceiver",
        "onReceive",
        "void",
        List.of("android.content.Context", "android.content.Intent"),
        PKG + ".MainReceiver",
        "receiverHelper");
  }

  private static void assertEntryPointReaches(
      String entryClass,
      String entryMethod,
      String entryReturnType,
      List<String> entryParams,
      String helperClass,
      String helperMethod) {
    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);

    MethodSignature entrySig =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(entryClass, entryMethod, entryReturnType, entryParams);
    assertTrue(entryPoints.contains(entrySig));

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph cg = cha.initialize(entryPoints);

    MethodSignature helperSig =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(helperClass, helperMethod, "void", List.of());
    assertTrue(cg.callTargetsFrom(entrySig).contains(helperSig));
  }
}
