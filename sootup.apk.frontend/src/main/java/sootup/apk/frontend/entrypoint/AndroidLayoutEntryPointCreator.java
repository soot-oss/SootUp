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
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Derives call-graph entry points from {@code android:onClick} method names extracted by {@link
 * AndroidLayoutParser} (step 4): for every method name found in any layout, checks whether each
 * manifest-declared {@code Activity} (or an app-defined class in its superclass chain) defines the
 * matching {@code void <name>(android.view.View)} callback the framework would invoke via
 * reflection once such a layout is inflated.
 *
 * <p>Scoped to activities only, since {@code android:onClick} is resolved against the Context that
 * inflates the layout, which for a manifest-declared component is always an Activity (a Service,
 * BroadcastReceiver or ContentProvider never inflates a view hierarchy).
 *
 * <p>Every extracted method name is checked against every declared activity, not matched to the one
 * specific activity that actually uses that layout — see {@link AndroidLayoutParser}'s class doc
 * for why (no {@code resources.arsc} parsing yet). This over-approximates the same way step 3 does,
 * and for the same reason: soundness over precision, consistent with the rest of this module's
 * CHA/RTA-based call graph.
 */
public final class AndroidLayoutEntryPointCreator {

  private static final String ON_CLICK_RETURN_TYPE = "void";
  private static final List<String> ON_CLICK_PARAMETER_TYPES =
      Collections.singletonList("android.view.View");

  private AndroidLayoutEntryPointCreator() {}

  /**
   * @param onClickMethodNames method names extracted from the app's layouts, e.g. via {@link
   *     AndroidLayoutParser#parseOnClickMethodNamesFromApk}.
   * @param appClassNames the fully qualified names of classes actually declared in the APK's dex
   *     (see {@link sootup.apk.frontend.ApkAnalysisInputLocation#getApplicationClassNames()}).
   */
  @NonNull
  public static List<MethodSignature> getOnClickEntryPoints(
      @NonNull View view,
      @NonNull AndroidManifest manifest,
      @NonNull Set<String> appClassNames,
      @NonNull Set<String> onClickMethodNames) {
    if (onClickMethodNames.isEmpty()) {
      return Collections.emptyList();
    }

    Set<MethodSignature> entryPoints = new LinkedHashSet<>();
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    for (ManifestComponent activity : manifest.getComponents(AndroidComponentType.ACTIVITY)) {
      ClassType activityType = identifierFactory.getClassType(activity.getClassName());
      for (String onClickMethodName : onClickMethodNames) {
        LifecycleMethod onClickMethod =
            new LifecycleMethod(onClickMethodName, ON_CLICK_RETURN_TYPE, ON_CLICK_PARAMETER_TYPES);
        AndroidEntryPointCreator.resolveOverride(
                view, identifierFactory, appClassNames, activityType, onClickMethod)
            .ifPresent(entryPoints::add);
      }
    }

    return new ArrayList<>(entryPoints);
  }
}
