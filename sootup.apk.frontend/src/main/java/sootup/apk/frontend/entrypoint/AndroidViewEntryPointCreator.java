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
 * Derives call-graph entry points for custom {@code android.view.View} subclass callbacks ({@code
 * onDraw}, {@code onMeasure}, ...). A custom View is never manifest-declared (unlike
 * Activity/Service/BroadcastReceiver/ContentProvider — see {@link AndroidEntryPointCreator}) and
 * isn't invoked through a registration API returning a listener interface either (unlike {@link
 * AndroidCallbackEntryPointCreator}'s targets) — the framework calls back onto it once an instance
 * is attached to the view hierarchy (inflated from layout XML, or constructed and added
 * programmatically via {@code ViewGroup#addView}), which no static entry-point list captures.
 * DroidBench's AndroidSpecific/View1 relies on exactly this: {@code MyView.onDraw} leaks a device
 * id stored in a static field by a separate entry point ({@code onCreate}), and never runs at all
 * without this discovery.
 *
 * <p>Same shape as {@link AndroidFragmentEntryPointCreator}: {@code View} is a base class, not an
 * interface, so membership is checked via {@link TypeHierarchy#superClassesOf}, restricted to
 * classes actually instantiated in already-reachable code (see {@link InstantiatedTypeCollector})
 * for the same sound reason given there — a custom View can only ever run if something first
 * constructs a live instance of it (or the layout inflater does, which itself only happens for a
 * layout resource the app references from reachable code).
 *
 * <p>The method list covers the common callbacks a custom View overrides to draw itself or react to
 * layout/input/focus/attachment changes. Trying to resolve a method a given View subclass never
 * declares is a harmless no-op (see {@link AndroidEntryPointCreator#resolveOverride}), so folding
 * it into one flat list costs nothing.
 */
public final class AndroidViewEntryPointCreator {

  private AndroidViewEntryPointCreator() {}

  private static final String VIEW_BASE_CLASS = "android.view.View";

  private static final List<LifecycleMethod> VIEW_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod(
                  "onDraw", "void", Collections.singletonList("android.graphics.Canvas")),
              new LifecycleMethod("onMeasure", "void", Arrays.asList("int", "int")),
              new LifecycleMethod(
                  "onLayout", "void", Arrays.asList("boolean", "int", "int", "int", "int")),
              new LifecycleMethod(
                  "onSizeChanged", "void", Arrays.asList("int", "int", "int", "int")),
              new LifecycleMethod(
                  "onTouchEvent", "boolean", Collections.singletonList("android.view.MotionEvent")),
              new LifecycleMethod(
                  "onKeyDown", "boolean", Arrays.asList("int", "android.view.KeyEvent")),
              new LifecycleMethod(
                  "onKeyUp", "boolean", Arrays.asList("int", "android.view.KeyEvent")),
              new LifecycleMethod(
                  "onFocusChanged",
                  "void",
                  Arrays.asList("boolean", "int", "android.graphics.Rect")),
              new LifecycleMethod("onAttachedToWindow", "void", Collections.emptyList()),
              new LifecycleMethod("onDetachedFromWindow", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "onWindowFocusChanged", "void", Collections.singletonList("boolean")),
              new LifecycleMethod(
                  "dispatchDraw", "void", Collections.singletonList("android.graphics.Canvas"))));

  /**
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   * @param instantiatedClassNames the fully qualified names of classes known to be instantiated
   *     somewhere in already-reachable code (see {@link InstantiatedTypeCollector}) — a candidate
   *     class not in this set is skipped entirely, per the class doc above.
   */
  @NonNull
  public static List<MethodSignature> getViewEntryPoints(
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
                  collectViewOverrides(
                      view,
                      identifierFactory,
                      typeHierarchy,
                      appClassNames,
                      sootClass,
                      entryPoints));
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectViewOverrides(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull TypeHierarchy typeHierarchy,
      @NonNull Set<String> appClassNames,
      @NonNull SootClass sootClass,
      @NonNull Set<MethodSignature> out) {
    boolean isView =
        typeHierarchy
            .superClassesOf(sootClass.getType())
            .anyMatch(superType -> VIEW_BASE_CLASS.equals(superType.getFullyQualifiedName()));
    if (!isView) {
      return;
    }

    for (LifecycleMethod method : VIEW_METHODS) {
      AndroidEntryPointCreator.resolveOverride(
              view, identifierFactory, appClassNames, sootClass.getType(), method)
          .ifPresent(out::add);
    }
  }
}
