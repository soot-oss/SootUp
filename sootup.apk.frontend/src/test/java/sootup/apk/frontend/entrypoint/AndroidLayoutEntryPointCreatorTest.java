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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.apk.frontend.resources.AndroidResourceTableParser;
import sootup.core.signatures.MethodSignature;

/**
 * Validates step 4 of {@code ANDROID_CALL_GRAPH_PLAN.md}. None of this module's checked-in sample
 * APKs actually use {@code android:onClick} (confirmed by {@code AndroidLayoutParserTest}), so
 * there's no manifest-declared activity in them that this creator would legitimately match against
 * real layout data. The positive "resolves to a real, compiled method" path is instead covered by
 * {@code sootup.apk.frontend.fixture.OnClickFixtureTest}, which builds a real hand-compiled fixture
 * APK (via {@code FixtureApkBuilder}) rather than borrowing a class from a bundled library the way
 * this file used to (a bundled-library class's own listener wiring is never a legitimate {@code
 * android:onClick} target for someone else's activity — see {@code
 * AndroidCallbackEntryPointCreatorTest} for why that distinction matters). These tests instead
 * cover the filtering/edge-case behavior around it: empty and non-existent method names, and
 * non-{@code Activity} components being ignored — using one of LocationLeak1.apk's real,
 * dex-declared classes purely as a stand-in for "some real class in the view," since none of these
 * cases ever reach the point of resolving a genuine override.
 */
public class AndroidLayoutEntryPointCreatorTest {

  private static final String REAL_ONCLICK_CLASS = "android.support.v4.view.PagerTabStrip$2";
  private static final String REAL_ONCLICK_METHOD = "onClick";

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
            ctx.view,
            manifest,
            ctx.appClassNames,
            Map.of("layout.xml", Set.of("thisMethodDoesNotExistAnywhere")),
            Collections.emptyMap());
    assertTrue(entryPoints.isEmpty());
  }

  @Test
  public void testEmptyOnClickNamesYieldsNoEntryPoints() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    List<MethodSignature> entryPoints =
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            ctx.view, manifest, ctx.appClassNames, Collections.emptyMap(), Collections.emptyMap());
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
            ctx.view,
            manifest,
            ctx.appClassNames,
            Map.of("layout.xml", Set.of(REAL_ONCLICK_METHOD)),
            Collections.emptyMap());
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
      Map<String, Set<String>> onClickMethodNamesByFile =
          AndroidLayoutParser.parseOnClickMethodNamesByFileFromApk(Paths.get(apk));
      Map<Integer, Set<String>> layoutFileNamesByResourceId =
          AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(Paths.get(apk), "layout");

      List<MethodSignature> entryPoints =
          AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
              ctx.view,
              manifest,
              ctx.appClassNames,
              onClickMethodNamesByFile,
              layoutFileNamesByResourceId);
      assertTrue(entryPoints.isEmpty());
    }
  }
}
