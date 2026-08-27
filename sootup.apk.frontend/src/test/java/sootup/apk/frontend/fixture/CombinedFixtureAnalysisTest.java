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
import sootup.apk.frontend.AndroidApkAnalysis;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

/**
 * The combined fixture step 9's writeup deferred to step 10: one real, compiled, multi-component
 * APK exercising every step (1/2/3/4/5/7/8) at once, run entirely through {@link
 * AndroidApkAnalysis} — this module's public façade — rather than by calling each step's creator
 * directly the way every other fixture test does. If the façade's wiring is wrong, this is the
 * test that would catch it: a component from each step, and a helper method reachable only if
 * that step's entry points were correctly included in the combined list.
 *
 * <p>Package {@code test.fixture.combined}: {@code MainActivity} (manifest lifecycle + {@code
 * android:onClick} + explicit ICC to {@code TargetActivity}), {@code MyService}/{@code
 * MyReceiver} (manifest lifecycle), {@code TargetActivity} (ICC target), {@code MyClickListener}
 * (step 3, never manifest-declared), {@code MyTask} (step 8 {@code AsyncTask}, never
 * manifest-declared).
 */
public class CombinedFixtureAnalysisTest {

  private static final String PKG = "test.fixture.combined";

  private static AndroidApkAnalysis analysis;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String mainActivitySmali =
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
            + "    .registers 4\n"
            + "    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n"
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MainActivity;->lifecycleHelper()V\n"
            + "    new-instance v0, Landroid/content/Intent;\n"
            + "    const-class v1, L"
            + PKG.replace('.', '/')
            + "/TargetActivity;\n"
            + "    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n"
            + "    invoke-virtual {p0, v0}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onSaveClicked(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MainActivity;->onClickHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private lifecycleHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private onClickHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String targetActivitySmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/TargetActivity;\n"
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
            + "/TargetActivity;->targetHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private targetHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String serviceSmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MyService;\n"
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
            + "/MyService;->serviceHelper()V\n"
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
            + "/MyReceiver;\n"
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
            + "/MyReceiver;->receiverHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private receiverHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String listenerSmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MyClickListener;\n"
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
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MyClickListener;->listenerHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private listenerHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String taskSmali =
        ".class public L"
            + PKG.replace('.', '/')
            + "/MyTask;\n"
            + ".super Landroid/os/AsyncTask;\n"
            + "\n"
            + ".method public constructor <init>()V\n"
            + "    .registers 1\n"
            + "    invoke-direct {p0}, Landroid/os/AsyncTask;-><init>()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public doInBackground([Ljava/lang/Object;)Ljava/lang/Object;\n"
            + "    .registers 3\n"
            + "    invoke-direct {p0}, L"
            + PKG.replace('.', '/')
            + "/MyTask;->backgroundHelper()V\n"
            + "    const/4 v0, 0x0\n"
            + "    return-object v0\n"
            + ".end method\n"
            + "\n"
            + ".method private backgroundHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        new FixtureApkBuilder()
            .smali(mainActivitySmali)
            .smali(targetActivitySmali)
            .smali(serviceSmali)
            .smali(receiverSmali)
            .smali(listenerSmali)
            .smali(taskSmali)
            .activity(PKG + ".MainActivity")
            .activity(PKG + ".TargetActivity")
            .component(AndroidComponentType.SERVICE, PKG + ".MyService", List.of())
            .component(AndroidComponentType.BROADCAST_RECEIVER, PKG + ".MyReceiver", List.of())
            .layoutOnClick("activity_main.xml", "onSaveClicked")
            .build();

    analysis = AndroidApkAnalysis.create(apkPath, "src/test/resources/platforms");
  }

  @Test
  public void testEntryPointsIncludeOneFromEachStep() {
    List<MethodSignature> entryPoints = analysis.getEntryPoints();

    assertTrue(
        entryPoints.contains(sig("MainActivity", "onCreate", "void", "android.os.Bundle")),
        "step 5: activity lifecycle");
    assertTrue(
        entryPoints.contains(
            sig("MyService", "onStartCommand", "int", "android.content.Intent", "int", "int")),
        "step 5: service lifecycle");
    assertTrue(
        entryPoints.contains(
            sig(
                "MyReceiver",
                "onReceive",
                "void",
                "android.content.Context",
                "android.content.Intent")),
        "step 5: receiver lifecycle");
    assertTrue(
        entryPoints.contains(sig("MyClickListener", "onClick", "void", "android.view.View")),
        "step 3: listener discovery");
    assertTrue(
        entryPoints.contains(sig("MainActivity", "onSaveClicked", "void", "android.view.View")),
        "step 4: android:onClick");
    assertTrue(
        entryPoints.contains(sig("MyTask", "doInBackground", "java.lang.Object", "java.lang.Object[]")),
        "step 8: AsyncTask");
  }

  @Test
  public void testCallGraphWithChaConnectsEveryStepsHelper() {
    CallGraph cg = analysis.buildCallGraphWithCHA();

    assertTrue(
        cg.callTargetsFrom(sig("MainActivity", "onCreate", "void", "android.os.Bundle"))
            .contains(sig("MainActivity", "lifecycleHelper", "void")),
        "step 5 edge");
    assertTrue(
        cg.callTargetsFrom(sig("MainActivity", "onCreate", "void", "android.os.Bundle"))
            .contains(sig("TargetActivity", "onCreate", "void", "android.os.Bundle")),
        "step 7 ICC edge");
    assertTrue(
        cg.callTargetsFrom(sig("TargetActivity", "onCreate", "void", "android.os.Bundle"))
            .contains(sig("TargetActivity", "targetHelper", "void")),
        "ICC target's own subtree reachable through the ICC edge");
    assertTrue(
        cg.callTargetsFrom(
                sig("MyService", "onStartCommand", "int", "android.content.Intent", "int", "int"))
            .contains(sig("MyService", "serviceHelper", "void")),
        "step 5 edge (service)");
    assertTrue(
        cg.callTargetsFrom(
                sig(
                    "MyReceiver",
                    "onReceive",
                    "void",
                    "android.content.Context",
                    "android.content.Intent"))
            .contains(sig("MyReceiver", "receiverHelper", "void")),
        "step 5 edge (receiver)");
    assertTrue(
        cg.callTargetsFrom(sig("MyClickListener", "onClick", "void", "android.view.View"))
            .contains(sig("MyClickListener", "listenerHelper", "void")),
        "step 3 edge");
    assertTrue(
        cg.callTargetsFrom(sig("MainActivity", "onSaveClicked", "void", "android.view.View"))
            .contains(sig("MainActivity", "onClickHelper", "void")),
        "step 4 edge");
    assertTrue(
        cg.callTargetsFrom(
                sig("MyTask", "doInBackground", "java.lang.Object", "java.lang.Object[]"))
            .contains(sig("MyTask", "backgroundHelper", "void")),
        "step 8 edge");
  }

  @Test
  public void testCallGraphWithRtaAlsoWorks() {
    CallGraph cg = analysis.buildCallGraphWithRTA();
    assertTrue(
        cg.callTargetsFrom(sig("MainActivity", "onCreate", "void", "android.os.Bundle"))
            .contains(sig("TargetActivity", "onCreate", "void", "android.os.Bundle")));
  }

  private static MethodSignature sig(
      String simpleClassName, String methodName, String returnType, String... paramTypes) {
    return analysis
        .getView()
        .getIdentifierFactory()
        .getMethodSignature(PKG + "." + simpleClassName, methodName, returnType, List.of(paramTypes));
  }
}
