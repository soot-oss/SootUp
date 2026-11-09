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

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import org.jf.dexlib2.iface.DexFile;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.Util.*;
import sootup.apk.frontend.dexpler.DexClassProvider;
import sootup.apk.frontend.dexpler.DexFileProvider;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.ClassModifier;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.util.Modifiers;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;

/**
 * Analysis input location for Android APK files.
 *
 * <p>This class provides an entry point for analyzing Android APK files by extracting and
 * processing DEX (Dalvik Executable) files contained within the APK.
 */
public class ApkAnalysisInputLocation implements AnalysisInputLocation {

  Path apk_path;

  /**
   * Path to the Android platforms directory containing Android system libraries (android.jar files)
   * for different API levels. This directory is required to resolve method calls and class
   * references that are not defined in the APK itself, but are part of the Android system
   * libraries.
   *
   * <p>The Android platforms directory can be obtained from: <a
   * href="https://github.com/Sable/android-platforms">https://github.com/Sable/android-platforms</a>
   */
  private final AndroidVersionInfo androidSDKVersionInfo;

  private final List<BodyInterceptor> bodyInterceptors;

  final Map<String, EnumSet<ClassModifier>> classNamesList;

  /**
   * Creates a new ApkAnalysisInputLocation.
   *
   * @param apkPath the path to the APK file to analyze system libraries (android.jar files) for
   *     different API levels. This directory is required to resolve method calls and class
   *     references that are not defined in the APK itself, but are part of the Android system
   *     libraries. The Android platforms directory can be obtained from <a
   *     href="https://github.com/Sable/android-platforms">https://github.com/Sable/android-platforms</a>
   * @param bodyInterceptors the list of body interceptors to apply during analysis
   */
  public ApkAnalysisInputLocation(
      Path apkPath,
      AndroidVersionInfo androidSDKVersionInfo,
      List<BodyInterceptor> bodyInterceptors) {
    this.apk_path = apkPath;
    this.androidSDKVersionInfo = androidSDKVersionInfo;
    this.bodyInterceptors = bodyInterceptors;
    this.classNamesList = extractDexFilesFromPath();
  }

  private Map<String, EnumSet<ClassModifier>> extractDexFilesFromPath() {
    List<DexFileProvider.DexContainer<? extends DexFile>> dexFromSource;
    DexUtil.setAndroidVersionInfo(androidSDKVersionInfo);
    try {
      dexFromSource =
          DexFileProvider.getInstance()
              .getDexFromSource(apk_path.toFile(), androidSDKVersionInfo.getApi_version());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Map<String, EnumSet<ClassModifier>> classList = new HashMap<>();
    dexFromSource.forEach(
        dexContainer ->
            dexContainer
                .getBase()
                .getDexFile()
                .getClasses()
                .forEach(
                    dexClass ->
                        classList.put(
                            DexUtil.dottedClassName(dexClass.toString()),
                            Modifiers.getClassModifiers(dexClass.getAccessFlags()))));
    return classList;
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    return new DexClassProvider(view).createClassSource(this, apk_path, type);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return classNamesList.entrySet().stream()
        .flatMap(
            className ->
                StreamUtils.optionalToStream(
                    getClassSource(
                        view.getIdentifierFactory().getClassType(className.getKey()), view)));
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return SourceType.Application;
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }
}
