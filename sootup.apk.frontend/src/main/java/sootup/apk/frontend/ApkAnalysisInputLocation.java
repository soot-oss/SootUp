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
import sootup.core.model.SootClass;
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
   * Package prefixes for third-party libraries commonly statically linked into an app's own dex
   * (support-v4/v7, AndroidX, Play Services, Kotlin's runtime): {@link #getClassSources(View)}
   * reports {@link SourceType#Library} for classes under these prefixes even though they're
   * declared in the same dex as the app's own classes, so the rest of the pipeline (call-graph
   * construction's library-boundary checks, entry-point resolution) can tell them apart from
   * genuine app code the same reliable way it already does for platform (android.jar) classes.
   * Without this, a class like {@code android.support.v7.internal.widget.ActivityChooserView} is
   * indistinguishable from app code: its lifecycle-callback overrides get mistaken for app-authored
   * entry points, and once reached, call-graph construction walks its full body - and everything
   * transitively reachable through it - as if it were the app's own logic. FlowDroid draws the same
   * line via its {@code SystemClassHandler} package classification.
   */
  private static final List<String> BUNDLED_LIBRARY_PACKAGE_PREFIXES =
      Collections.unmodifiableList(
          Arrays.asList(
              "android.support.",
              "androidx.",
              "com.google.android.material.",
              "com.google.android.gms.",
              "kotlin.",
              "kotlinx."));

  /** Whether {@code fullyQualifiedName} belongs to a bundled/statically-linked library. */
  public static boolean isBundledLibraryClass(@NonNull String fullyQualifiedName) {
    for (String prefix : BUNDLED_LIBRARY_PACKAGE_PREFIXES) {
      if (fullyQualifiedName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A view of this location reported as the {@link AnalysisInputLocation} for bundled-library
   * classes: identical to the outer instance in every respect except {@link #getSourceType()},
   * which is what {@code JavaView} actually consults (via {@code
   * SootClassSource#getAnalysisInputLocation()}) to decide a class's {@link SourceType}.
   */
  private final AnalysisInputLocation bundledLibraryInputLocation =
      new AnalysisInputLocation() {
        @NonNull
        @Override
        public Optional<? extends SootClassSource> getClassSource(
            @NonNull ClassType type, @NonNull View view) {
          return ApkAnalysisInputLocation.this.getClassSource(type, view);
        }

        @NonNull
        @Override
        public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
          return ApkAnalysisInputLocation.this.getClassSources(view);
        }

        @NonNull
        @Override
        public SourceType getSourceType() {
          return SourceType.Library;
        }

        @NonNull
        @Override
        public List<BodyInterceptor> getBodyInterceptors() {
          return bodyInterceptors;
        }
      };

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

  /**
   * The fully qualified names of the classes actually declared in this APK's dex files - both the
   * app's own classes and any bundled library (see {@link #isBundledLibraryClass}) statically
   * linked into the same dex.
   *
   * <p>This is the reliable way to tell classes belonging to this program (app code and whatever's
   * bundled with it) apart from platform (android.jar) or other classpath classes in a {@link
   * View}. It does <em>not</em> distinguish the app's own classes from a bundled library within the
   * program - for that, check {@link SootClass#isLibraryClass()}, which now reflects both
   * boundaries correctly: {@link #getClassSource(ClassType, View)} reports {@link
   * SourceType#Library} for bundled-library classes the same way android.jar's {@code
   * AnalysisInputLocation} does for platform classes.
   */
  @NonNull
  public Set<String> getApplicationClassNames() {
    return Collections.unmodifiableSet(classNamesList.keySet());
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    AnalysisInputLocation reportedLocation =
        isBundledLibraryClass(type.getFullyQualifiedName()) ? bundledLibraryInputLocation : this;
    return new DexClassProvider(view).createClassSource(reportedLocation, apk_path, type);
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
