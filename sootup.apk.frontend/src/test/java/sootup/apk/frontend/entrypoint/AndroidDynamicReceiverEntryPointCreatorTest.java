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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.core.signatures.MethodSignature;

/**
 * Validates the dynamic-{@code BroadcastReceiver} follow-up to step 7 of {@code
 * ANDROID_CALL_GRAPH_PLAN.md}: receiver classes with no manifest {@code <receiver>} declaration —
 * restricted to classes actually instantiated somewhere in already-reachable code (the same
 * precision tightening steps 3/8 use; see {@link InstantiatedTypeCollector}).
 *
 * <p>The positive (real, reachable, dynamically-registered receiver) case needs a hand-built
 * fixture APK — none of the checked-in sample APKs happen to bundle a library with an unregistered
 * receiver subclass the way they got "lucky" for steps 3/8 — so it lives in {@code
 * sootup.apk.frontend.fixture.DynamicReceiverFixtureTest} instead, alongside step 9's other
 * fixture-based validations.
 */
public class AndroidDynamicReceiverEntryPointCreatorTest {

  private static List<MethodSignature> getDynamicReceiverEntryPoints(
      ApkTestContext ctx, Path apkPath) {
    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);
    return AndroidDynamicReceiverEntryPointCreator.getDynamicReceiverEntryPoints(
        ctx.view, ctx.appClassNames, instantiated);
  }

  @Test
  public void testCryptoHasNoDynamicReceiverEntryPoints() {
    // Crypto.apk's app classes (MainActivity, a plain utility class) extend no such thing.
    Path apkPath = Paths.get("src/test/resources/Crypto.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    assertTrue(getDynamicReceiverEntryPoints(ctx, apkPath).isEmpty());
  }

  @Test
  public void testResultsAreDeduplicated() {
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> entryPoints = getDynamicReceiverEntryPoints(ctx, apkPath);
    assertEquals(entryPoints.size(), new LinkedHashSet<>(entryPoints).size());
  }
}
