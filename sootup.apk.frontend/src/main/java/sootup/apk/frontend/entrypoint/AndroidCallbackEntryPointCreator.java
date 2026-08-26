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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points by scanning every app class for ones implementing a known Android
 * UI callback/listener interface ({@link AndroidCallbackConstants}) — code the framework can invoke
 * once such a class is registered on a widget, even though no in-app call site directly calls it.
 *
 * <p>Unlike {@link AndroidEntryPointCreator} this doesn't start from the manifest: it scans the
 * app's whole class set, since a listener implementation can appear anywhere (an anonymous inner
 * class in an Activity, a top-level class, a Fragment, ...), not just on manifest-declared
 * components.
 */
public final class AndroidCallbackEntryPointCreator {

  private AndroidCallbackEntryPointCreator() {}

  /**
   * Returns the deduplicated callback entry points found among {@code appClassNames}.
   *
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   *     Scanning is restricted to these, not {@code view.getClasses()}, so that platform
   *     (android.jar) classes that themselves implement one of these listener interfaces (e.g.
   *     {@code Activity} implements {@code View.OnCreateContextMenuListener}) aren't mistaken for
   *     app-defined callback targets.
   */
  @NonNull
  public static List<MethodSignature> getCallbackEntryPoints(
      @NonNull View view, @NonNull Set<String> appClassNames) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    Map<String, List<LifecycleMethod>> listenerInterfaceMethods =
        AndroidCallbackConstants.getListenerInterfaceMethods();

    for (String className : appClassNames) {
      ClassType classType = identifierFactory.getClassType(className);
      view.getClass(classType)
          .ifPresent(
              sootClass ->
                  collectListenerOverrides(
                      view,
                      identifierFactory,
                      typeHierarchy,
                      appClassNames,
                      sootClass,
                      listenerInterfaceMethods,
                      entryPoints));
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectListenerOverrides(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull TypeHierarchy typeHierarchy,
      @NonNull Set<String> appClassNames,
      @NonNull SootClass sootClass,
      @NonNull Map<String, List<LifecycleMethod>> listenerInterfaceMethods,
      @NonNull Set<MethodSignature> out) {
    Set<String> implementedInterfaceNames =
        typeHierarchy
            .implementedInterfacesOf(sootClass.getType())
            .map(ClassType::getFullyQualifiedName)
            .collect(Collectors.toSet());

    for (Map.Entry<String, List<LifecycleMethod>> entry : listenerInterfaceMethods.entrySet()) {
      if (!implementedInterfaceNames.contains(entry.getKey())) {
        continue;
      }
      for (LifecycleMethod callbackMethod : entry.getValue()) {
        AndroidEntryPointCreator.resolveOverride(
                view, identifierFactory, appClassNames, sootClass.getType(), callbackMethod)
            .ifPresent(out::add);
      }
    }
  }
}
