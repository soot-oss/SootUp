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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidAsyncEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates step 8 ({@code AsyncTask}) against a real, hand-built, compiled APK: an {@code
 * AsyncTask} subclass whose {@code doInBackground}/{@code onPostExecute} are never called from app
 * code (no {@code .execute(...)} call site anywhere - matching how the framework actually invokes
 * them, reflectively, off a background thread), so they must be unreachable without step 8 and
 * reachable with it. Uses the type-erased {@code doInBackground([Ljava/lang/Object;)} signature
 * directly, the same signature a real generic {@code AsyncTask<Params, Progress, Result>}
 * subclass's compiler-generated bridge method would carry.
 */
public class AsyncTaskFixtureTest {

  private static final String ACTIVITY_CLASS = "test.fixture.async.MainActivity";
  private static final String TASK_CLASS = "test.fixture.async.MyTask";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String activitySmali =
        ".class public Ltest/fixture/async/MainActivity;\n"
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
            + "    return-void\n"
            + ".end method\n";

    String taskSmali =
        ".class public Ltest/fixture/async/MyTask;\n"
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
            + "    invoke-direct {p0}, Ltest/fixture/async/MyTask;->backgroundHelper()V\n"
            + "    const/4 v0, 0x0\n"
            + "    return-object v0\n"
            + ".end method\n"
            + "\n"
            + ".method public onPostExecute(Ljava/lang/Object;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/async/MyTask;->postHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private backgroundHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private postHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        new FixtureApkBuilder()
            .smali(activitySmali)
            .smali(taskSmali)
            .activity(ACTIVITY_CLASS)
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
  }

  @Test
  public void testAsyncTaskEntryPointsResolveToRealMethods() {
    List<MethodSignature> asyncEntryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

    MethodSignature doInBackground =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                TASK_CLASS, "doInBackground", "java.lang.Object", List.of("java.lang.Object[]"));
    MethodSignature onPostExecute =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TASK_CLASS, "onPostExecute", "void", List.of("java.lang.Object"));

    assertTrue(asyncEntryPoints.contains(doInBackground));
    assertTrue(asyncEntryPoints.contains(onPostExecute));
  }

  @Test
  public void testHelpersAreUnreachableFromLifecycleAloneButReachableWithAsyncDiscovery() {
    MethodSignature backgroundHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TASK_CLASS, "backgroundHelper", "void", List.of());
    MethodSignature postHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TASK_CLASS, "postHelper", "void", List.of());

    List<MethodSignature> lifecycleOnly =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    CallGraphAlgorithm lifecycleCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleGraph = lifecycleCha.initialize(lifecycleOnly);
    assertFalse(lifecycleGraph.containsMethod(backgroundHelper));
    assertFalse(lifecycleGraph.containsMethod(postHelper));

    List<MethodSignature> asyncEntryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);
    List<MethodSignature> combined = new ArrayList<>(lifecycleOnly);
    combined.addAll(asyncEntryPoints);
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph combinedGraph = combinedCha.initialize(combined);

    MethodSignature doInBackground =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                TASK_CLASS, "doInBackground", "java.lang.Object", List.of("java.lang.Object[]"));
    MethodSignature onPostExecute =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TASK_CLASS, "onPostExecute", "void", List.of("java.lang.Object"));
    assertTrue(combinedGraph.callTargetsFrom(doInBackground).contains(backgroundHelper));
    assertTrue(combinedGraph.callTargetsFrom(onPostExecute).contains(postHelper));
  }
}
