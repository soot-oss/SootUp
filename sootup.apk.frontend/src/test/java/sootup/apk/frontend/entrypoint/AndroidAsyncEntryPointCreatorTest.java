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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * Validates step 8 of {@code ANDROID_CALL_GRAPH_PLAN.md}: async/threading entry points
 * (`Runnable`/`Callable` implementations, `AsyncTask` subclasses) — restricted to classes actually
 * instantiated somewhere in already-reachable code (the precision tightening layered on afterward;
 * see {@link InstantiatedTypeCollector}).
 */
public class AndroidAsyncEntryPointCreatorTest {

  private static List<MethodSignature> getAsyncEntryPoints(ApkTestContext ctx, Path apkPath) {
    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);
    Set<String> instantiated = ctx.instantiatedClassNamesFromCoreEntryPoints(manifest, apkPath);
    return AndroidAsyncEntryPointCreator.getAsyncEntryPoints(
        ctx.view, ctx.appClassNames, instantiated);
  }

  @Test
  public void testCryptoHasNoAsyncEntryPoints() {
    // Crypto.apk's app classes (MainActivity, a plain utility class) implement none of these.
    Path apkPath = Paths.get("src/test/resources/Crypto.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    assertTrue(getAsyncEntryPoints(ctx, apkPath).isEmpty());
  }

  @Test
  public void testAbstractIntermediateClassWithNoOverrideYieldsNoEntryPoint() {
    // android.support.v4.content.ModernAsyncTask$WorkerRunnable implements Callable but is an
    // abstract intermediate class that never itself overrides call() (only concrete subclasses do)
    // - resolveOverride correctly finds nothing to add for it, independent of the instantiated-type
    // filter (see testNeverInstantiatedCandidateIsExcluded in AndroidCallbackEntryPointCreatorTest
    // for the "never instantiated at all" case this filter itself is responsible for).
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> entryPoints = getAsyncEntryPoints(ctx, apkPath);

    boolean anyFromWorkerRunnable =
        entryPoints.stream()
            .anyMatch(
                sig ->
                    sig.getDeclClassType()
                        .getFullyQualifiedName()
                        .equals("android.support.v4.content.ModernAsyncTask$WorkerRunnable"));
    assertFalse(anyFromWorkerRunnable);
  }

  @Test
  public void testResultsAreDeduplicated() {
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> entryPoints = getAsyncEntryPoints(ctx, apkPath);
    assertEquals(entryPoints.size(), new LinkedHashSet<>(entryPoints).size());
  }

  @Test
  public void testInstantiatedAsyncTypesAreFound() {
    // All three are genuinely instantiated somewhere in FlowSensitivity1.apk's reachable code
    // (verified via a diagnostic dump of InstantiatedTypeCollector's output for this apk) and
    // cover all three shapes step 8 looks for: a Callable, a Runnable, and an AsyncTask subclass.
    Path apkPath = Paths.get("src/test/resources/FlowSensitivity1.apk");
    ApkTestContext ctx = ApkTestContext.forApkPath(apkPath);
    List<MethodSignature> entryPoints = getAsyncEntryPoints(ctx, apkPath);

    MethodSignature modernAsyncTaskCall =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.content.ModernAsyncTask$2",
                "call",
                "java.lang.Object",
                List.of());
    MethodSignature fragmentManagerImplRun =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.app.FragmentManagerImpl$1", "run", "void", List.of());
    MethodSignature persistHistoryAsyncTaskDoInBackground =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.widget.ActivityChooserModel$PersistHistoryAsyncTask",
                "doInBackground",
                "java.lang.Object",
                List.of("java.lang.Object[]"));

    assertTrue(entryPoints.contains(modernAsyncTaskCall));
    assertTrue(entryPoints.contains(fragmentManagerImplRun));
    assertTrue(entryPoints.contains(persistHistoryAsyncTaskDoInBackground));
  }
}
