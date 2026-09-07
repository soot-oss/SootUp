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
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidLayoutEntryPointCreator;
import sootup.apk.frontend.entrypoint.ApkTestContext;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.apk.frontend.resources.AndroidResourceTableParser;
import sootup.core.signatures.MethodSignature;

/**
 * Proves the actual point of resource-table-based precise resolution ({@code
 * AndroidResourceTableParser} + {@code AndroidLayoutEntryPointCreator}'s per-activity tracing) is a
 * real precision gain, not just a safe no-op: two real, compiled activities that both happen to
 * declare a method named {@code onSaveClicked(View)} (a common naming collision in real apps),
 * where only {@code ActivityOne}'s own real, resources.arsc-resolved layout actually wires that
 * name via {@code android:onClick} - {@code ActivityTwo}'s layout wires a different name entirely.
 *
 * <p>Under the old blanket behavior (every extracted {@code android:onClick} name checked against
 * every activity - reproduced here by feeding an empty resource-ID map, the fallback path's exact
 * shape), {@code ActivityTwo#onSaveClicked} would incorrectly become an entry point too, purely
 * because the name happens to collide - real imprecision, not merely a hypothetical one. With the
 * resource table resolved, it correctly does not.
 */
public class LayoutResourcePrecisionFixtureTest {

  private static final String ACTIVITY_ONE = "test.fixture.layoutprecision.ActivityOne";
  private static final String ACTIVITY_TWO = "test.fixture.layoutprecision.ActivityTwo";

  private static ApkTestContext ctx;
  private static AndroidManifest manifest;
  private static Map<String, Set<String>> onClickMethodNamesByFile;
  private static Map<Integer, Set<String>> layoutFileNamesByResourceId;

  @BeforeAll
  public static void buildFixture() throws Exception {
    FixtureApkBuilder builder = new FixtureApkBuilder();
    builder.layoutResource("activity_one.xml");
    builder.layoutResource("activity_two.xml");
    int activityOneLayoutId = builder.resourceIdFor("activity_one.xml");
    int activityTwoLayoutId = builder.resourceIdFor("activity_two.xml");

    String activityOneSmali =
        ".class public Ltest/fixture/layoutprecision/ActivityOne;\n"
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
            + "    const v0, 0x"
            + Integer.toHexString(activityOneLayoutId)
            + "\n"
            + "    invoke-virtual {p0, v0}, Ltest/fixture/layoutprecision/ActivityOne;->setContentView(I)V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onSaveClicked(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/layoutprecision/ActivityOne;->activityOneHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private activityOneHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    String activityTwoSmali =
        ".class public Ltest/fixture/layoutprecision/ActivityTwo;\n"
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
            + "    const v0, 0x"
            + Integer.toHexString(activityTwoLayoutId)
            + "\n"
            + "    invoke-virtual {p0, v0}, Ltest/fixture/layoutprecision/ActivityTwo;->setContentView(I)V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            // Same method name/signature as ActivityOne's real onClick target - but ActivityTwo's
            // own layout never wires this one; only onCancelClicked below.
            + ".method public onSaveClicked(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/layoutprecision/ActivityTwo;->activityTwoSaveHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method public onCancelClicked(Landroid/view/View;)V\n"
            + "    .registers 2\n"
            + "    invoke-direct {p0}, Ltest/fixture/layoutprecision/ActivityTwo;->activityTwoCancelHelper()V\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private activityTwoSaveHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n"
            + "\n"
            + ".method private activityTwoCancelHelper()V\n"
            + "    .registers 1\n"
            + "    return-void\n"
            + ".end method\n";

    Path apkPath =
        builder
            .smali(activityOneSmali)
            .smali(activityTwoSmali)
            .activity(ACTIVITY_ONE)
            .activity(ACTIVITY_TWO)
            .layoutOnClick("activity_one.xml", "onSaveClicked")
            .layoutOnClick("activity_two.xml", "onCancelClicked")
            .build();

    ctx = ApkTestContext.forApkPath(apkPath);
    manifest = AndroidManifestParser.parseFromApk(apkPath);
    onClickMethodNamesByFile = AndroidLayoutParser.parseOnClickMethodNamesByFileFromApk(apkPath);
    layoutFileNamesByResourceId =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(apkPath, "layout");
  }

  @Test
  public void testResourceTableResolvesBothLayoutsToTheirOwnFiles() {
    // Sanity check on the parser itself before trusting the entry-point-level assertions below.
    int activityOneLayoutId = (0x7f << 24) | (0x01 << 16); // first registered layoutResource() call
    int activityTwoLayoutId = (0x7f << 24) | (0x01 << 16) | 1;
    assertTrue(
        layoutFileNamesByResourceId
            .getOrDefault(activityOneLayoutId, Set.of())
            .contains("res/layout/activity_one.xml"));
    assertTrue(
        layoutFileNamesByResourceId
            .getOrDefault(activityTwoLayoutId, Set.of())
            .contains("res/layout/activity_two.xml"));
  }

  @Test
  public void testPreciseResolutionExcludesNameCollisionFromTheWrongActivity() {
    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view,
            manifest,
            ctx.appClassNames,
            onClickMethodNamesByFile,
            layoutFileNamesByResourceId);

    MethodSignature activityOneOnSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                ACTIVITY_ONE, "onSaveClicked", "void", List.of("android.view.View"));
    MethodSignature activityTwoOnCancelClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                ACTIVITY_TWO, "onCancelClicked", "void", List.of("android.view.View"));
    MethodSignature activityTwoOnSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                ACTIVITY_TWO, "onSaveClicked", "void", List.of("android.view.View"));

    assertTrue(entryPoints.contains(activityOneOnSaveClicked), "ActivityOne's real onClick target");
    assertTrue(
        entryPoints.contains(activityTwoOnCancelClicked), "ActivityTwo's real onClick target");
    assertFalse(
        entryPoints.contains(activityTwoOnSaveClicked),
        "ActivityTwo never wires onSaveClicked via its own layout - a same-named method on a "
            + "different activity must not leak in");
  }

  @Test
  public void testWithoutTheResourceTableTheCollisionWouldHaveLeakedIn() {
    // Reproduces the pre-precise-resolution behavior (empty resourceId map forces every activity
    // onto the blanket fallback) to make the precision gain above concrete, not just asserted.
    List<MethodSignature> blanketEntryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, onClickMethodNamesByFile, Map.of());

    MethodSignature activityTwoOnSaveClicked =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                ACTIVITY_TWO, "onSaveClicked", "void", List.of("android.view.View"));
    assertTrue(
        blanketEntryPoints.contains(activityTwoOnSaveClicked),
        "the blanket fallback is expected to over-approximate by including this");
  }
}
