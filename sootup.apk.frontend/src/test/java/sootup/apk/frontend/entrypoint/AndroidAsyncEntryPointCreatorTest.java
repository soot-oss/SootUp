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

import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;

/**
 * Validates step 8 of {@code ANDROID_CALL_GRAPH_PLAN.md}: async/threading entry points
 * (`Runnable`/`Callable` implementations, `AsyncTask` subclasses). Like step 3,
 * `LocationLeak1.apk`/`FlowSensitivity1.apk` bundle the `android.support` compat libraries
 * directly into their own dex, which happen to contain real implementations of all three shapes -
 * no hand-built fixture needed.
 */
public class AndroidAsyncEntryPointCreatorTest {

  @Test
  public void testCryptoHasNoAsyncEntryPoints() {
    // Crypto.apk's app classes (MainActivity, a plain utility class) implement none of these.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/Crypto.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);
    assertTrue(entryPoints.isEmpty());
  }

  @Test
  public void testFindsRunnableImplementation() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

    MethodSignature runnableRun =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.app.FragmentManagerImpl$1", "run", "void", List.of());
    assertTrue(entryPoints.contains(runnableRun));
  }

  @Test
  public void testFindsCallableImplementation() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

    MethodSignature callableCall =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v4.content.ModernAsyncTask$2",
                "call",
                "java.lang.Object",
                List.of());
    assertTrue(entryPoints.contains(callableCall));
  }

  @Test
  public void testFindsAsyncTaskDoInBackgroundViaErasedBridgeSignature() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

    MethodSignature doInBackground =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "android.support.v7.internal.widget.ActivityChooserModel$PersistHistoryAsyncTask",
                "doInBackground",
                "java.lang.Object",
                List.of("java.lang.Object[]"));
    assertTrue(entryPoints.contains(doInBackground));
  }

  @Test
  public void testFindsAllOverriddenAsyncTaskCallbacksOnOneClass() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

    String className = "android.support.v4.print.PrintHelperKitkat$2$1";
    MethodSignature onPreExecute =
        ctx.view.getIdentifierFactory().getMethodSignature(className, "onPreExecute", "void", List.of());
    MethodSignature doInBackground =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                className, "doInBackground", "java.lang.Object", List.of("java.lang.Object[]"));
    MethodSignature onPostExecute =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(className, "onPostExecute", "void", List.of("java.lang.Object"));
    MethodSignature onCancelled =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(className, "onCancelled", "void", List.of("java.lang.Object"));

    assertTrue(entryPoints.contains(onPreExecute));
    assertTrue(entryPoints.contains(doInBackground));
    assertTrue(entryPoints.contains(onPostExecute));
    assertTrue(entryPoints.contains(onCancelled));
  }

  @Test
  public void testAbstractIntermediateClassWithNoOverrideYieldsNoEntryPoint() {
    // android.support.v4.content.ModernAsyncTask$WorkerRunnable implements Callable but is an
    // abstract intermediate class that never itself overrides call() (only concrete subclasses,
    // like ModernAsyncTask$2, do) - resolveOverride correctly finds nothing to add for it.
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);

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
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    List<MethodSignature> entryPoints =
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(ctx.view, ctx.appClassNames);
    assertEquals(entryPoints.size(), new java.util.LinkedHashSet<>(entryPoints).size());
  }
}
