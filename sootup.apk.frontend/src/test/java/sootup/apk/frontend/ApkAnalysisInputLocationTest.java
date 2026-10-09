package sootup.apk.frontend;

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
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

public class ApkAnalysisInputLocationTest {

  private static final Path APK = Paths.get("src/test/resources/FlowSensitivity1.apk");
  private static final String ANDROID_JAR = "src/test/resources/platforms/android-19/android.jar";

  /**
   * The view takes a class from the first input location that provides it. The APK location used to
   * provide a source for every requested type, so with it listed first, framework classes such as
   * Activity were shadowed by empty classes (or crashed) instead of coming from android.jar.
   */
  @Test
  public void frameworkClassIsNotShadowedByTheApk() {
    ApkAnalysisInputLocation apkLocation =
        new ApkAnalysisInputLocation(
            APK, new AndroidVersionInfo(APK, ""), DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view =
        new JavaView(
            List.of(
                apkLocation,
                new JavaClassPathAnalysisInputLocation(ANDROID_JAR, SourceType.Library)));
    ClassType activity = view.getIdentifierFactory().getClassType("android.app.Activity");

    assertFalse(apkLocation.getClassSource(activity, view).isPresent());
    JavaSootClass clazz = view.getClass(activity).get();
    assertEquals(
        SourceType.Library, clazz.getClassSource().getAnalysisInputLocation().getSourceType());
    assertTrue(clazz.getSuperclass().isPresent());
    assertFalse(clazz.getMethods().isEmpty());
  }

  /**
   * Two classes from the same APK must compare unequal: their class sources share the APK path as
   * sourcePath, and the base equals/hashCode compares only (input location, sourcePath).
   */
  @Test
  public void classSourcesOfDifferentClassesAreNotEqual() {
    ApkAnalysisInputLocation apkLocation =
        new ApkAnalysisInputLocation(
            APK, new AndroidVersionInfo(APK, ""), DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(List.of(apkLocation));

    ClassType mainActivity = view.getIdentifierFactory().getClassType("de.ecspride.MainActivity");
    ClassType buildConfig = view.getIdentifierFactory().getClassType("de.ecspride.BuildConfig");

    var sourceA = apkLocation.getClassSource(mainActivity, view).get();
    var sourceB = apkLocation.getClassSource(buildConfig, view).get();
    var sourceASecondLookup = apkLocation.getClassSource(mainActivity, view).get();

    assertFalse(sourceA.equals(sourceB));
    assertTrue(sourceA.equals(sourceASecondLookup));
    assertEquals(sourceA.hashCode(), sourceASecondLookup.hashCode());
  }

  /** Every class defined in the dex files resolves through the APK location. */
  @Test
  public void everyApkClassResolves() {
    ApkAnalysisInputLocation apkLocation =
        new ApkAnalysisInputLocation(
            APK, new AndroidVersionInfo(APK, ""), DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(List.of(apkLocation));

    assertEquals(740, apkLocation.getClassSources(view).count());
    assertTrue(
        apkLocation
            .getClassSource(
                view.getIdentifierFactory().getClassType("de.ecspride.MainActivity"), view)
            .isPresent());
  }
}
