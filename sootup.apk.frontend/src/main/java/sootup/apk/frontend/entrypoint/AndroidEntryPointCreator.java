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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points from an {@link AndroidManifest}: the framework-invoked lifecycle
 * callbacks that are actually overridden somewhere in each declared component's class hierarchy.
 *
 * <p>Rather than synthesizing a single "dummy main" method with a Jimple body that calls every
 * lifecycle method, this returns a flat list of {@link MethodSignature}s. {@code
 * sootup.callgraph.CallGraphAlgorithm#initialize(List)} already treats every element of its
 * entry-point list as an independent root, which is call-graph-equivalent to a synthetic method
 * that unconditionally calls all of them — without needing to hand-build a {@code Body}/{@code
 * Stmt}/{@code Local} graph or an {@code AnalysisInputLocation} to host it. See {@code
 * ANDROID_CALL_GRAPH_PLAN.md} for the fuller rationale and for where a real dummy-main would become
 * necessary (e.g. control-flow-sensitive analyses built on top of the call graph).
 */
public final class AndroidEntryPointCreator {

  private AndroidEntryPointCreator() {}

  /**
   * Returns the deduplicated entry points for every component declared in {@code manifest},
   * resolved against {@code view}'s classes and type hierarchy.
   *
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}), used
   *     to tell the app's own classes apart from platform (android.jar) classes when walking up a
   *     component's superclass chain.
   */
  @NonNull
  public static List<MethodSignature> getEntryPoints(
      @NonNull View view, @NonNull AndroidManifest manifest, @NonNull Set<String> appClassNames) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    manifest
        .getApplicationClassName()
        .ifPresent(
            applicationClassName ->
                collectEntryPoints(
                    view,
                    identifierFactory,
                    appClassNames,
                    applicationClassName,
                    AndroidComponentType.APPLICATION,
                    entryPoints));

    for (ManifestComponent component : manifest.getComponents()) {
      collectEntryPoints(
          view,
          identifierFactory,
          appClassNames,
          component.getClassName(),
          component.getType(),
          entryPoints);
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectEntryPoints(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull Set<String> appClassNames,
      @NonNull String className,
      @NonNull AndroidComponentType componentType,
      @NonNull Set<MethodSignature> out) {
    ClassType classType = identifierFactory.getClassType(className);

    // The OS instantiates every manifest component itself (reflectively, via the required
    // public no-arg constructor) before calling any lifecycle method on it - there is no app
    // call site for this, the same reason lifecycle methods themselves need to be entry points.
    // Unlike lifecycle methods, this isn't resolveOverride'd up the hierarchy: Android always
    // instantiates the exact declared component class, and javac always emits a default no-arg
    // constructor on it even if the source never wrote one, so a direct lookup on classType
    // itself is both correct and sufficient.
    MethodSubSignature constructorSubSignature =
        identifierFactory
            .getMethodSignature(classType, "<init>", "void", Collections.emptyList())
            .getSubSignature();
    view.getClass(classType)
        .flatMap(sootClass -> sootClass.getMethod(constructorSubSignature))
        .ifPresent(constructor -> out.add(constructor.getSignature()));

    for (LifecycleMethod lifecycleMethod :
        AndroidEntryPointConstants.getLifecycleMethods(componentType)) {
      resolveOverride(view, identifierFactory, appClassNames, classType, lifecycleMethod)
          .ifPresent(out::add);
    }
  }

  /**
   * Walks {@code startClass}'s superclass chain (starting at itself) for the first class in {@code
   * appClassNames} that overrides {@code targetMethod} and isn't itself a library class, matching
   * how the Android framework actually dispatches a callback to whichever subclass implements it
   * (commonly a shared base activity/service rather than the leaf class). Returns empty if no such
   * class overrides this callback anywhere in the hierarchy, or if {@code startClass} isn't part of
   * the view at all.
   *
   * <p>Checks both membership in {@code appClassNames} <em>and</em> {@link
   * SootClass#isLibraryClass()} at every step, including {@code startClass} itself: {@code
   * appClassNames} excludes platform (android.jar) classes and anything else off the program
   * entirely, while {@code isLibraryClass()} excludes bundled/statically-linked libraries
   * (support-v4/v7, AndroidX, Play Services, Kotlin's runtime) that are declared in the same dex as
   * the app's own classes and so would otherwise pass the {@code appClassNames} check
   * indistinguishably from real app code. {@link sootup.apk.frontend.ApkAnalysisInputLocation} is
   * what makes {@code isLibraryClass()} a reliable signal for both boundaries: it reports {@link
   * sootup.core.model.SourceType#Library} for bundled-library classes the same way android.jar's
   * {@code AnalysisInputLocation} does for platform classes, so a framework base class's default
   * implementation (e.g. {@code Activity#onCreate}) or a support-library base class's own dispatch
   * logic (e.g. {@code FragmentActivity#onActivityResult}, which just forwards to child fragments)
   * is never mistaken for an app-authored override — at any point in the walk, {@code startClass}
   * included.
   *
   * <p>Public because callback discovery ({@code AndroidCallbackEntryPointCreator}), layout {@code
   * android:onClick} resolution ({@code AndroidLayoutEntryPointCreator}) and ICC resolution ({@code
   * sootup.apk.frontend.icc.AndroidIccResolver}) all need the exact same "find the app override of
   * this callback" logic, just starting from a different class and callback.
   */
  @NonNull
  public static Optional<MethodSignature> resolveOverride(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull Set<String> appClassNames,
      @NonNull ClassType startClass,
      @NonNull LifecycleMethod targetMethod) {
    MethodSubSignature subSignature =
        identifierFactory
            .getMethodSignature(
                startClass,
                targetMethod.getName(),
                targetMethod.getReturnType(),
                targetMethod.getParameterTypes())
            .getSubSignature();

    ClassType current = startClass;
    while (current != null) {
      if (!appClassNames.contains(current.getFullyQualifiedName())) {
        // We've walked up into a platform/library class (or off the app's own classes
        // entirely): the app doesn't override this callback anywhere.
        return Optional.empty();
      }
      Optional<? extends SootClass> sootClass = view.getClass(current);
      if (!sootClass.isPresent() || sootClass.get().isLibraryClass()) {
        // Either not in the view at all, or a bundled library class (see
        // ApkAnalysisInputLocation#isBundledLibraryClass): its overrides are pre-written
        // library dispatch plumbing, not anything the app author wrote.
        return Optional.empty();
      }
      Optional<? extends SootMethod> method = sootClass.get().getMethod(subSignature);
      if (method.isPresent()) {
        return Optional.of(method.get().getSignature());
      }
      current = sootClass.get().getSuperclass().orElse(null);
    }
    return Optional.empty();
  }
}
