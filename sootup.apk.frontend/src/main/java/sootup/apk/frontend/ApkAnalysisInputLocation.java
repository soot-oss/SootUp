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
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.dexpler.DexClassProvider;
import sootup.apk.frontend.dexpler.DexFileProvider;
import sootup.apk.frontend.dexpler.DexLibWrapper;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
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

  private final List<BodyInterceptor> bodyInterceptors;

  // owned by this location, so the dex data is released together with it
  private final DexLibWrapper dexLibWrapper;

  /**
   * Creates a new ApkAnalysisInputLocation.
   *
   * <p>Only classes defined in the APK are provided. Framework classes (android.*) need the
   * android.jar as a separate library input location.
   *
   * @param apkPath the APK, dex or odex file, or a directory of dex files
   * @param androidSDKVersionInfo API level information of the APK
   * @param bodyInterceptors the list of body interceptors to apply during analysis
   */
  public ApkAnalysisInputLocation(
      Path apkPath,
      AndroidVersionInfo androidSDKVersionInfo,
      List<BodyInterceptor> bodyInterceptors) {
    this.apk_path = apkPath;
    this.bodyInterceptors = bodyInterceptors;
    try {
      this.dexLibWrapper =
          new DexLibWrapper(
              new DexFileProvider()
                  .getDexFromSource(apkPath.toFile(), androidSDKVersionInfo.getApi_version()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    return new DexClassProvider(view, dexLibWrapper).createClassSource(this, apk_path, type);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return dexLibWrapper.getClassNames().stream()
        .flatMap(
            className ->
                StreamUtils.optionalToStream(
                    getClassSource(view.getIdentifierFactory().getClassType(className), view)));
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
