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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points for <em>dynamically</em> registered {@code BroadcastReceiver}s —
 * ones constructed and handed to {@code Context#registerReceiver(BroadcastReceiver,
 * IntentFilter)} at runtime, as opposed to declared with a manifest {@code <receiver>} element
 * (already covered by {@link AndroidEntryPointCreator}, which walks {@link
 * sootup.apk.frontend.manifest.AndroidManifest}). Fills the gap flagged in {@code
 * ANDROID_CALL_GRAPH_PLAN.md} step 7's known limitations: "Dynamically registered
 * BroadcastReceivers ... aren't modeled at all."
 *
 * <p>Same shape as {@link AndroidAsyncEntryPointCreator}'s {@code AsyncTask} handling, not a new
 * one: {@code BroadcastReceiver} is an abstract class, so membership is checked via {@link
 * TypeHierarchy#superClassesOf}, not {@code implementedInterfacesOf}. Rather than tracing the
 * specific {@code registerReceiver} call site that hands off a given instance (more precise, but
 * the same complexity-for-precision tradeoff already declined for steps 3/8's own scans — see
 * their class docs), this takes the same broader, already-established approach: any app class
 * extending {@code android.content.BroadcastReceiver} has its {@code onReceive} callback treated
 * as reachable, restricted to classes actually instantiated in already-reachable code (see {@link
 * InstantiatedTypeCollector}). That restriction is sound, not a heuristic, for the same reason it
 * is for steps 3/8: a receiver can only ever be registered if something first constructs a live
 * instance of it, so "never `new`'d in reachable code" is a valid precondition for "can never
 * actually fire".
 *
 * <p>A manifest-declared {@code <receiver>} extends {@code BroadcastReceiver} too, so this scan
 * naturally rediscovers it whenever it's also instantiated in reachable code — harmless, since
 * {@code AndroidApkAnalysis}'s combined entry-point set is deduplicated. The two sources stay
 * conceptually distinct: manifest receivers don't need an instantiation site at all (the OS
 * constructs them reflectively, which is exactly why {@link AndroidEntryPointCreator} adds their
 * constructor as its own entry point) and are found unconditionally by that scan; this scan is the
 * only source for a receiver class that has no manifest declaration whatsoever.
 */
public final class AndroidDynamicReceiverEntryPointCreator {

  private AndroidDynamicReceiverEntryPointCreator() {}

  /**
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   * @param instantiatedClassNames the fully qualified names of classes known to be instantiated
   *     somewhere in already-reachable code (see {@link InstantiatedTypeCollector}) — a candidate
   *     class not in this set is skipped entirely, per the class doc above.
   */
  @NonNull
  public static List<MethodSignature> getDynamicReceiverEntryPoints(
      @NonNull View view,
      @NonNull Set<String> appClassNames,
      @NonNull Set<String> instantiatedClassNames) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    List<LifecycleMethod> receiverMethods =
        AndroidEntryPointConstants.getLifecycleMethods(AndroidComponentType.BROADCAST_RECEIVER);
    String receiverBaseClass = AndroidComponentType.BROADCAST_RECEIVER.getFrameworkBaseClass();

    for (String className : appClassNames) {
      if (!instantiatedClassNames.contains(className)) {
        continue;
      }
      ClassType classType = identifierFactory.getClassType(className);
      view.getClass(classType)
          .ifPresent(
              sootClass ->
                  collectReceiverOverrides(
                      view,
                      identifierFactory,
                      typeHierarchy,
                      appClassNames,
                      sootClass,
                      receiverBaseClass,
                      receiverMethods,
                      entryPoints));
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectReceiverOverrides(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull TypeHierarchy typeHierarchy,
      @NonNull Set<String> appClassNames,
      @NonNull SootClass sootClass,
      @NonNull String receiverBaseClass,
      @NonNull List<LifecycleMethod> receiverMethods,
      @NonNull Set<MethodSignature> out) {
    boolean isBroadcastReceiver =
        typeHierarchy
            .superClassesOf(sootClass.getType())
            .anyMatch(superType -> superType.getFullyQualifiedName().equals(receiverBaseClass));
    if (!isBroadcastReceiver) {
      return;
    }

    for (LifecycleMethod method : receiverMethods) {
      AndroidEntryPointCreator.resolveOverride(
              view, identifierFactory, appClassNames, sootClass.getType(), method)
          .ifPresent(out::add);
    }
  }
}
