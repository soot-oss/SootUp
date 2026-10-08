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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points for {@code Fragment} lifecycle callbacks. A {@code Fragment} is
 * never manifest-declared (unlike Activity/Service/BroadcastReceiver/ContentProvider — see {@link
 * AndroidEntryPointCreator}) and isn't invoked through a registration API returning a listener
 * interface either (unlike {@link AndroidCallbackEntryPointCreator}'s targets) — the framework
 * calls back onto it once an instance is attached to a {@code FragmentManager} transaction
 * (programmatically, e.g. {@code FragmentTransaction#add}, or via layout inflation), which no
 * static entry-point list captures.
 *
 * <p>Same shape as {@link AndroidDynamicReceiverEntryPointCreator}'s {@code BroadcastReceiver}
 * handling: {@code Fragment} is a base class, not an interface, so membership is checked via {@link
 * TypeHierarchy#superClassesOf} against every known Fragment base class (the pre-support {@code
 * android.app.Fragment}, the support-v4 backport, and the AndroidX successor — an app may target
 * any of the three depending on its minSdkVersion/migration state), restricted to classes actually
 * instantiated in already-reachable code (see {@link InstantiatedTypeCollector}) for the same sound
 * reason given there: a Fragment can only ever run if something first constructs a live instance of
 * it.
 *
 * <p>The method list mixes generic {@code Fragment} lifecycle ({@code onAttach}/{@code
 * onCreate}/{@code onCreateView}/{@code onActivityCreated}/{@code onStart}/{@code onResume}/{@code
 * onPause}/{@code onStop}/{@code onSaveInstanceState}/{@code onDestroyView}/{@code
 * onDestroy}/{@code onDetach}) with one {@code ListFragment}-specific callback ({@code
 * onListItemClick}, dispatched by {@code ListFragment}'s own internal item-click listener). Trying
 * to resolve a method a given Fragment subclass never declares is a harmless no-op (see {@link
 * AndroidEntryPointCreator#resolveOverride}), so folding it into one flat list costs nothing.
 */
public final class AndroidFragmentEntryPointCreator {

  private AndroidFragmentEntryPointCreator() {}

  /**
   * The fully qualified names of every Android SDK base class a Fragment subclass may ultimately
   * extend, across the pre-support, support-v4, and AndroidX Fragment APIs.
   */
  private static final List<String> FRAGMENT_BASE_CLASSES =
      Collections.unmodifiableList(
          Arrays.asList(
              "android.app.Fragment",
              "android.support.v4.app.Fragment",
              "androidx.fragment.app.Fragment"));

  private static final List<LifecycleMethod> FRAGMENT_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod(
                  "onAttach", "void", Collections.singletonList("android.app.Activity")),
              new LifecycleMethod(
                  "onCreate", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod(
                  "onCreateView",
                  "android.view.View",
                  Arrays.asList(
                      "android.view.LayoutInflater",
                      "android.view.ViewGroup",
                      "android.os.Bundle")),
              new LifecycleMethod(
                  "onActivityCreated", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod("onStart", "void", Collections.emptyList()),
              new LifecycleMethod("onResume", "void", Collections.emptyList()),
              new LifecycleMethod("onPause", "void", Collections.emptyList()),
              new LifecycleMethod("onStop", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "onSaveInstanceState", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod("onDestroyView", "void", Collections.emptyList()),
              new LifecycleMethod("onDestroy", "void", Collections.emptyList()),
              new LifecycleMethod("onDetach", "void", Collections.emptyList()),
              // ListFragment-specific; harmless no-op via resolveOverride on non-ListFragment
              // Fragment subclasses (see class doc).
              new LifecycleMethod(
                  "onListItemClick",
                  "void",
                  Arrays.asList("android.widget.ListView", "android.view.View", "int", "long"))));

  /**
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   * @param instantiatedClassNames the fully qualified names of classes known to be instantiated
   *     somewhere in already-reachable code (see {@link InstantiatedTypeCollector}) — a candidate
   *     class not in this set is skipped entirely, per the class doc above.
   */
  @NonNull
  public static List<MethodSignature> getFragmentEntryPoints(
      @NonNull View view,
      @NonNull Set<String> appClassNames,
      @NonNull Set<String> instantiatedClassNames) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();

    for (String className : appClassNames) {
      if (!instantiatedClassNames.contains(className)) {
        continue;
      }
      ClassType classType = identifierFactory.getClassType(className);
      view.getClass(classType)
          .ifPresent(
              sootClass ->
                  collectFragmentOverrides(
                      view,
                      identifierFactory,
                      typeHierarchy,
                      appClassNames,
                      sootClass,
                      entryPoints));
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectFragmentOverrides(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull TypeHierarchy typeHierarchy,
      @NonNull Set<String> appClassNames,
      @NonNull SootClass sootClass,
      @NonNull Set<MethodSignature> out) {
    boolean isFragment =
        typeHierarchy
            .superClassesOf(sootClass.getType())
            .anyMatch(
                superType -> FRAGMENT_BASE_CLASSES.contains(superType.getFullyQualifiedName()));
    if (!isFragment) {
      return;
    }

    for (LifecycleMethod method : FRAGMENT_METHODS) {
      AndroidEntryPointCreator.resolveOverride(
              view, identifierFactory, appClassNames, sootClass.getType(), method)
          .ifPresent(out::add);
    }
  }
}
