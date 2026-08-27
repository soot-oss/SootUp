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
 * Derives call-graph entry points for step 8's async/threading constructs (see {@link
 * AndroidAsyncConstants}): scans every app class for one implementing {@code Runnable}/{@code
 * Callable} (the same shape as step 3's listener-interface scan, just a different table) or
 * extending {@code android.os.AsyncTask} (a superclass relationship, checked via {@link
 * TypeHierarchy#superClassesOf}, not {@code implementedInterfacesOf} — {@code AsyncTask} is an
 * abstract class, not an interface).
 */
public final class AndroidAsyncEntryPointCreator {

  private AndroidAsyncEntryPointCreator() {}

  /**
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   */
  @NonNull
  public static List<MethodSignature> getAsyncEntryPoints(
      @NonNull View view, @NonNull Set<String> appClassNames) {
    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    Map<String, List<LifecycleMethod>> interfaceMethods =
        AndroidAsyncConstants.getInterfaceMethods();
    List<LifecycleMethod> asyncTaskMethods = AndroidAsyncConstants.getAsyncTaskMethods();

    for (String className : appClassNames) {
      ClassType classType = identifierFactory.getClassType(className);
      view.getClass(classType)
          .ifPresent(
              sootClass ->
                  collectAsyncOverrides(
                      view,
                      identifierFactory,
                      typeHierarchy,
                      appClassNames,
                      sootClass,
                      interfaceMethods,
                      asyncTaskMethods,
                      entryPoints));
    }

    return new ArrayList<>(entryPoints);
  }

  private static void collectAsyncOverrides(
      @NonNull View view,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull TypeHierarchy typeHierarchy,
      @NonNull Set<String> appClassNames,
      @NonNull SootClass sootClass,
      @NonNull Map<String, List<LifecycleMethod>> interfaceMethods,
      @NonNull List<LifecycleMethod> asyncTaskMethods,
      @NonNull Set<MethodSignature> out) {
    Set<String> implementedInterfaceNames =
        typeHierarchy
            .implementedInterfacesOf(sootClass.getType())
            .map(ClassType::getFullyQualifiedName)
            .collect(Collectors.toSet());
    for (Map.Entry<String, List<LifecycleMethod>> entry : interfaceMethods.entrySet()) {
      if (!implementedInterfaceNames.contains(entry.getKey())) {
        continue;
      }
      for (LifecycleMethod method : entry.getValue()) {
        AndroidEntryPointCreator.resolveOverride(
                view, identifierFactory, appClassNames, sootClass.getType(), method)
            .ifPresent(out::add);
      }
    }

    boolean isAsyncTask =
        typeHierarchy
            .superClassesOf(sootClass.getType())
            .anyMatch(
                superType ->
                    superType
                        .getFullyQualifiedName()
                        .equals(AndroidAsyncConstants.ASYNC_TASK_CLASS));
    if (isAsyncTask) {
      for (LifecycleMethod method : asyncTaskMethods) {
        AndroidEntryPointCreator.resolveOverride(
                view, identifierFactory, appClassNames, sootClass.getType(), method)
            .ifPresent(out::add);
      }
    }
  }
}
