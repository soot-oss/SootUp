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
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.icc.AndroidIccResolver;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.MutableCallGraph;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates step 7 (ICC/Intent resolution) against a real, hand-built, compiled multi-component APK:
 * {@code MainActivity#onCreate} builds {@code new Intent(this, TargetActivity.class)} and calls
 * {@code startActivity(Intent)} — real dalvik {@code const-class}/{@code new-instance}/ {@code
 * invoke-direct} instructions this time, not a hand-written Jimple stand-in for them, so this
 * exercises the real dex-to-Jimple translation this module implements as well as {@code
 * AndroidIccResolver}'s constant tracing.
 */
public class IccFixtureTest {

  private static final String MAIN_CLASS = "test.fixture.icc.MainActivity";
  private static final String TARGET_CLASS = "test.fixture.icc.TargetActivity";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String mainSmali =
        ".class public Ltest/fixture/icc/MainActivity;\n"
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
            + "    new-instance v0, Landroid/content/Intent;\n"
            + "    const-class v1, Ltest/fixture/icc/TargetActivity;\n"
            + "    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V\n"
            + "    invoke-virtual {p0, v0}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V\n"
            + "    return-void\n"
            + ".end method\n";

    String targetSmali =
        ".class public Ltest/fixture/icc/TargetActivity;\n"
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
            + "    invoke-direct {p0}, Ltest/fixture/icc/TargetActivity;->targetHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private targetHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        new FixtureApkBuilder()
            .smali(mainSmali)
            .smali(targetSmali)
            .activity(MAIN_CLASS)
            .activity(TARGET_CLASS)
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
  }

  @Test
  public void testIccEdgeConnectsRealCallSiteToRealTarget() {
    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);

    MethodSignature mainOnCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(MAIN_CLASS, "onCreate", "void", List.of("android.os.Bundle"));
    MethodSignature targetOnCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TARGET_CLASS, "onCreate", "void", List.of("android.os.Bundle"));
    MethodSignature targetHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(TARGET_CLASS, "targetHelper", "void", List.of());

    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(ctx.view).initialize(entryPoints);

    // before step 7: no edge, since startActivity can't be resolved by ordinary virtual dispatch
    // (android.app.Activity isn't part of this minimal fixture's classpath).
    assertFalse(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));

    AndroidIccResolver.addIccEdges(cg, ctx.view, manifest, ctx.appClassNames);

    assertTrue(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
    // and TargetActivity's own subtree (already expanded via step 5) is reachable through it.
    assertTrue(cg.callTargetsFrom(targetOnCreate).contains(targetHelper));
  }
}
