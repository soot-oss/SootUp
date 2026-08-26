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
   */
  @NonNull
  public static List<MethodSignature> getEntryPoints(
      @NonNull View view, @NonNull AndroidManifest manifest) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    manifest
        .getApplicationClassName()
        .ifPresent(
            applicationClassName ->
                collectEntryPoints(
                    view,
                    identifierFactory,
                    applicationClassName,
                    AndroidComponentType.APPLICATION,
                    entryPoints));

    for (ManifestComponent component : manifest.getComponents()) {
      collectEntryPoints(
          view, identifierFactory, component.getClassName(), component.getType(), entryPoints);
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectEntryPoints(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull String className,
      @NonNull AndroidComponentType componentType,
      @NonNull Set<MethodSignature> out) {
    ClassType classType = identifierFactory.getClassType(className);
    for (LifecycleMethod lifecycleMethod :
        AndroidEntryPointConstants.getLifecycleMethods(componentType)) {
      resolveOverride(view, identifierFactory, classType, lifecycleMethod).ifPresent(out::add);
    }
  }

  /**
   * Walks {@code componentType}'s superclass chain (starting at itself) for the first app-defined
   * (non-library) class that overrides {@code lifecycleMethod}, matching how the Android framework
   * actually dispatches lifecycle callbacks to whichever subclass implements them (commonly a
   * shared base activity/service rather than the leaf class). Returns empty if the app never
   * overrides this callback anywhere in the hierarchy, or if the component's class isn't part of
   * the view at all.
   */
  @NonNull
  private static Optional<MethodSignature> resolveOverride(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull ClassType componentType,
      @NonNull LifecycleMethod lifecycleMethod) {
    MethodSubSignature subSignature =
        identifierFactory
            .getMethodSignature(
                componentType,
                lifecycleMethod.getName(),
                lifecycleMethod.getReturnType(),
                lifecycleMethod.getParameterTypes())
            .getSubSignature();

    ClassType current = componentType;
    while (current != null) {
      Optional<? extends SootClass> sootClass = view.getClass(current);
      if (!sootClass.isPresent() || sootClass.get().isLibraryClass()) {
        // Either unresolved, or we've walked up into the Android framework's own classes:
        // the app doesn't override this callback anywhere.
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
