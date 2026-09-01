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
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidLayoutEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;

/**
 * Isolates step 4 ({@code android:onClick}) against a real, hand-built, compiled APK: an {@code
 * Activity} whose {@code onCreate} does nothing interesting, plus an {@code onSaveClicked(View)}
 * method that only a layout's {@code android:onClick} attribute wires up — never called from app
 * code, so it must be unreachable without step 4 and reachable with it.
 */
public class OnClickFixtureTest {

  private static final String CLASS_NAME = "test.fixture.onclick.MainActivity";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;
  private static Set<String> onClickMethodNames;

  @BeforeAll
  public static void buildFixture() throws Exception {
    String smali =
        ".class public Ltest/fixture/onclick/MainActivity;\n"
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
            + ".end method\n"
            + "\n"
            + ".method public onSaveClicked(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/onclick/MainActivity;->onClickHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private onClickHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        new FixtureApkBuilder()
            .smali(smali)
            .activity(CLASS_NAME)
            .layoutOnClick("activity_main.xml", "onSaveClicked")
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
    onClickMethodNames = AndroidLayoutParser.parseOnClickMethodNamesFromApk(apkPath);
  }

  @Test
  public void testOnClickMethodNameIsExtractedFromRealLayoutXml() {
    assertTrue(onClickMethodNames.contains("onSaveClicked"));
  }

  @Test
  public void testOnClickEntryPointResolvesToRealMethod() {
    List<MethodSignature> onClickEntryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, onClickMethodNames);

    MethodSignature onSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(CLASS_NAME, "onSaveClicked", "void", List.of("android.view.View"));
    assertTrue(onClickEntryPoints.contains(onSaveClicked));
  }

  @Test
  public void testSyntheticManifestComponentAlsoResolvesToRealMethod() {
    // Unlike testOnClickEntryPointResolvesToRealMethod above, which resolves against the
    // fixture's own real, parsed manifest, this builds an AndroidManifest object directly (its
    // constructor is public exactly to allow this) declaring the same real, compiled class as the
    // activity - proving getOnClickEntryPoints/resolveOverride's wiring works from any
    // AndroidManifest, not just one AndroidManifestParser produced. This class used to belong to a
    // bundled library (android.support.v4.view.PagerTabStrip$2) purely because it was real,
    // already-compiled bytecode; that stood in for genuine app reachability by accident, which is
    // exactly what this fixture avoids by construction.
    AndroidManifest syntheticManifest =
        new AndroidManifest(
            "test.fixture.onclick",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, CLASS_NAME, true, true, List.of())));

    List<MethodSignature> onClickEntryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, syntheticManifest, ctx.appClassNames, Set.of("onSaveClicked"));

    MethodSignature onSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(CLASS_NAME, "onSaveClicked", "void", List.of("android.view.View"));
    assertTrue(onClickEntryPoints.contains(onSaveClicked));
  }

  @Test
  public void testHelperIsUnreachableFromLifecycleAloneButReachableWithOnClick() {
    MethodSignature onCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(CLASS_NAME, "onCreate", "void", List.of("android.os.Bundle"));
    MethodSignature onClickHelper =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(CLASS_NAME, "onClickHelper", "void", List.of());

    List<MethodSignature> lifecycleOnly =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    CallGraphAlgorithm lifecycleCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph lifecycleGraph = lifecycleCha.initialize(lifecycleOnly);
    assertFalse(lifecycleGraph.containsMethod(onClickHelper));

    List<MethodSignature> onClickEntryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, onClickMethodNames);
    List<MethodSignature> combined = new ArrayList<>(lifecycleOnly);
    combined.addAll(onClickEntryPoints);
    CallGraphAlgorithm combinedCha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph combinedGraph = combinedCha.initialize(combined);

    MethodSignature onSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(CLASS_NAME, "onSaveClicked", "void", List.of("android.view.View"));
    assertTrue(combinedGraph.callTargetsFrom(onSaveClicked).contains(onClickHelper));
    // onCreate is unrelated to onSaveClicked - just confirming both roots coexist in one graph.
    assertTrue(combinedGraph.containsMethod(onCreate));
  }
}
