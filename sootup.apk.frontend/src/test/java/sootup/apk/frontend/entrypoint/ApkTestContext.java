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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import sootup.apk.frontend.ApkAnalysisInputLocation;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** Shared test fixture: a {@link JavaView} for a test APK plus its dex-declared class names. */
final class ApkTestContext {

  private static final String ANDROID_PLATFORMS_PATH = "src/test/resources/platforms";

  final JavaView view;
  final Set<String> appClassNames;

  private ApkTestContext(JavaView view, Set<String> appClassNames) {
    this.view = view;
    this.appClassNames = appClassNames;
  }

  static ApkTestContext forApk(String apkPathString) {
    Path apkPath = Paths.get(apkPathString);
    AndroidVersionInfo androidVersionInfo = new AndroidVersionInfo(apkPath, ANDROID_PLATFORMS_PATH);

    ApkAnalysisInputLocation apkInputLocation =
        new ApkAnalysisInputLocation(
            apkPath, androidVersionInfo, DexBodyInterceptors.Default.bodyInterceptors());
    JavaClassPathAnalysisInputLocation classPathInputLocation =
        new JavaClassPathAnalysisInputLocation(
            ANDROID_PLATFORMS_PATH
                + File.separator
                + "android-"
                + androidVersionInfo.getApi_version()
                + File.separator
                + "android.jar");

    JavaView view = new JavaView(List.of(apkInputLocation, classPathInputLocation));
    return new ApkTestContext(view, apkInputLocation.getApplicationClassNames());
  }
}
