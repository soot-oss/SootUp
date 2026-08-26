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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.core.signatures.MethodSignature;

/**
 * Validates step 4 of {@code ANDROID_CALL_GRAPH_PLAN.md}. None of this module's checked-in sample
 * APKs actually use {@code android:onClick} (confirmed by {@code AndroidLayoutParserTest}), so
 * there's no manifest-declared activity in them that this creator would legitimately match against
 * real layout data. Instead, these tests build a small {@link AndroidManifest} directly (its
 * constructor is public exactly to allow this) declaring one of LocationLeak1.apk's real,
 * dex-declared classes as if it were the manifest's activity, and check the wiring against a
 * method that genuinely exists there — {@code
 * android.support.v4.view.PagerTabStrip$2#onClick(android.view.View):void}, a real bundled
 * listener implementation (see {@code AndroidCallbackEntryPointCreatorTest}) — rather than
 * fabricating bytecode.
 */
public class AndroidLayoutEntryPointCreatorTest {

  private static final String REAL_ONCLICK_CLASS = "android.support.v4.view.PagerTabStrip$2";
  private static final String REAL_ONCLICK_METHOD = "onClick";

  @Test
  public void testFindsRealMethodViaSyntheticManifestComponent() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        new AndroidManifest(
            "de.ecspride",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY,
                    REAL_ONCLICK_CLASS,
                    true,
                    true,
                    Collections.emptyList())));

    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, Set.of(REAL_ONCLICK_METHOD));

    MethodSignature expected =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                REAL_ONCLICK_CLASS, REAL_ONCLICK_METHOD, "void", List.of("android.view.View"));
    assertEquals(List.of(expected), entryPoints);
  }

  @Test
  public void testNonExistentMethodNameYieldsNoEntryPoints() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        new AndroidManifest(
            "de.ecspride",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY,
                    REAL_ONCLICK_CLASS,
                    true,
                    true,
                    Collections.emptyList())));

    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, Set.of("thisMethodDoesNotExistAnywhere"));
    assertTrue(entryPoints.isEmpty());
  }

  @Test
  public void testEmptyOnClickNamesYieldsNoEntryPoints() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, Collections.emptySet());
    assertTrue(entryPoints.isEmpty());
  }

  @Test
  public void testNonActivityComponentsAreIgnored() {
    // android:onClick is resolved against the Activity hosting the layout; a Service/Receiver/
    // Provider component matching the same class name must never be considered.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        new AndroidManifest(
            "de.ecspride",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.SERVICE,
                    REAL_ONCLICK_CLASS,
                    true,
                    true,
                    Collections.emptyList())));

    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, Set.of(REAL_ONCLICK_METHOD));
    assertTrue(entryPoints.isEmpty());
  }

  @Test
  public void testRealManifestsWithNoOnClickUsageYieldNoEntryPoints() {
    // End-to-end with the real parsed manifest and real extracted (empty) onClick names.
    for (String apk :
        List.of(
            "src/test/resources/Crypto.apk",
            "src/test/resources/LocationLeak1.apk",
            "src/test/resources/FlowSensitivity1.apk")) {
      ApkTestContext ctx = ApkTestContext.forApk(apk);
      AndroidManifest manifest = AndroidManifestParser.parseFromApk(Paths.get(apk));
      Set<String> onClickMethodNames =
          AndroidLayoutParser.parseOnClickMethodNamesFromApk(Paths.get(apk));

      List<MethodSignature> entryPoints =
          AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
              ctx.view, manifest, ctx.appClassNames, onClickMethodNames);
      assertTrue(entryPoints.isEmpty());
    }
  }
}
