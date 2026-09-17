package sootup.apk.frontend.main;

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

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;

/**
 * Manages Android SDK version information for APK analysis.
 *
 * <p>This class determines the appropriate Android API version to use when analyzing an APK by:
 *
 * <ol>
 *   <li>Extracting the targeted SDK version from the APK's AndroidManifest.xml file
 *   <li>Finding the corresponding android.jar file from the Android platforms directory
 *   <li>Falling back to the latest available android.jar if the targeted version is not available
 *   <li>Using the android.jar file to resolve Android system library references during bytecode
 *       analysis
 * </ol>
 */
public class AndroidVersionInfo {

  private String androidPlatformsPath = "";
  private Path apk_path = null;

  /**
   * Creates a new AndroidVersionInfo instance.
   *
   * @param apkPath the path to the APK file to analyze
   * @param androidPlatformsPath the Android platforms directory (android-N/android.jar), used to
   *     pick an available API level. It is not added to the View: add the android.jar as a library
   *     input location to resolve framework classes.
   */
  public AndroidVersionInfo(Path apkPath, String androidPlatformsPath) {
    this.apk_path = apkPath;
    this.androidPlatformsPath = androidPlatformsPath;
  }

  public int sdkTargetVersion = -1;
  public int minSdkVersion = -1;
  public int platformBuildVersionCode = -1;
  int api_version = -1;
  int max_api = 0;

  private final int defaultSdkVersion = 15;

  private final Map<String, Integer> maxAPIs = new HashMap<>();

  private final Logger logger = LoggerFactory.getLogger(AndroidVersionInfo.class);

  public void get(InputStream manifestIS) {
    final AxmlVisitor axmlVisitor =
        new AxmlVisitor() {
          private String nodeName = null;

          @Override
          public void attr(String ns, String name, int resourceId, int type, Object obj) {
            super.attr(ns, name, resourceId, type, obj);

            if (nodeName != null && name != null) {
              if (nodeName.equals("manifest")) {
                if (name.equals("platformBuildVersionCode")) {
                  platformBuildVersionCode = parseSdkVersion(name, obj);
                }
              } else if (nodeName.equals("uses-sdk")) {
                // Obfuscated APKs often remove the attribute names and use the resourceId instead
                // Therefore it is better to check for both variants
                if (name.equals("targetSdkVersion") || (name.isEmpty() && resourceId == 16843376)) {
                  sdkTargetVersion = parseSdkVersion(name, obj);
                } else if (name.equals("minSdkVersion")
                    || (name.isEmpty() && resourceId == 16843276)) {
                  minSdkVersion = parseSdkVersion(name, obj);
                }
              }
            }
          }

          @Override
          public NodeVisitor child(String ns, String name) {
            nodeName = name;
            return this;
          }
        };
    try {
      AxmlReader xmlReader = new AxmlReader(ByteStreams.toByteArray(manifestIS));
      xmlReader.accept(axmlVisitor);
    } catch (Exception e) {
      logger.warn("Could not parse the Android manifest, the SDK version may be wrong", e);
    }
  }

  /** Codename SDK values such as "S" are not numbers; those are skipped. */
  private int parseSdkVersion(String attribute, Object value) {
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException e) {
      logger.warn("Ignoring non-numeric manifest attribute {}='{}'", attribute, value);
      return -1;
    }
  }

  public void getAndroidJarPath(String jars, Path apk) {
    int APIVersion = getAndroidAPIVersion(jars, apk);

    String jarPath =
        jars + File.separatorChar + "android-" + APIVersion + File.separatorChar + "android.jar";
    if (!jars.isEmpty() && !newFile(jarPath).isFile()) {
      logger.warn("android.jar for API level {} does not exist: {}", APIVersion, jarPath);
    }
  }

  private int getAndroidAPIVersion(String jars, Path apk) {
    if (api_version > 0) {
      return api_version;
    }

    if (!jars.isEmpty() && !newFile(jars).exists()) {
      logger.warn("Android platforms directory does not exist: {}", jars);
    }
    if (apk != null && !apk.toFile().exists()) {
      throw new RuntimeException("file '" + apk + "' does not exist!");
    }

    // Use the default if we don't have any other information
    api_version = defaultSdkVersion;

    if (apk != null
        && apk.getFileName() != null
        && apk.getFileName().toString().toLowerCase().endsWith(".apk")
        && apk.toFile().isFile()) {
      api_version = getTargetSDKVersion(apk, jars);
    }

    // If we don't have that API version installed, we take the most recent one we have
    final int maxAPI = getMaxAPIAvailable(jars);
    if (maxAPI > 0 && api_version > maxAPI) {
      api_version = maxAPI;
    }

    // If the platform version is missing in the middle, we take the next one
    while (api_version < maxAPI) {
      String jarPath =
          jars + File.separatorChar + "android-" + api_version + File.separatorChar + "android.jar";
      if (newFile(jarPath).exists()) {
        break;
      }
      api_version++;
    }

    return api_version;
  }

  private int getTargetSDKVersion(Path apkFile, String platformJARs) {
    // get AndroidManifest
    ZipFile archive = null;
    try {
      InputStream manifestIS = null;
      try {
        archive = new ZipFile(apkFile.toString());
        for (Enumeration<? extends ZipEntry> entries = archive.entries();
            entries.hasMoreElements(); ) {
          ZipEntry entry = entries.nextElement();
          // We are dealing with the Android manifest
          if ("AndroidManifest.xml".equals(entry.getName())) {
            manifestIS = archive.getInputStream(entry);
            break;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Error when looking for manifest in apk: " + e);
      }

      if (manifestIS == null) {
        logger.debug(
            "Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
        return defaultSdkVersion;
      }

      // process AndroidManifest.xml
      get(manifestIS);
    } finally {
      if (archive != null) {
        try {
          archive.close();
        } catch (IOException e) {
          throw new RuntimeException("Error when looking for manifest in apk: " + e);
        }
      }
    }

    int maxAPI = getMaxAPIAvailable(platformJARs);
    int APIVersion = -1;

    if (sdkTargetVersion != -1) {
      if (sdkTargetVersion > maxAPI && minSdkVersion != -1 && minSdkVersion <= maxAPI) {
        logger.warn(
            "Android API version '"
                + sdkTargetVersion
                + "' not available, using minApkVersion '"
                + minSdkVersion
                + "' instead");
        APIVersion = minSdkVersion;
      } else {
        APIVersion = sdkTargetVersion;
      }
    } else if (platformBuildVersionCode != -1) {
      if (platformBuildVersionCode > maxAPI && minSdkVersion != -1 && minSdkVersion <= maxAPI) {
        logger.warn(
            "Android API version '"
                + platformBuildVersionCode
                + "' not available, using minApkVersion '"
                + minSdkVersion
                + "' instead");
        APIVersion = minSdkVersion;
      } else {
        APIVersion = platformBuildVersionCode;
      }
    } else if (minSdkVersion != -1) {
      APIVersion = minSdkVersion;
    } else {
      logger.debug(
          "Could not find sdk version in Android manifest! Using default: " + defaultSdkVersion);
      APIVersion = defaultSdkVersion;
    }

    if (APIVersion <= 2) {
      APIVersion = 3;
    }

    return APIVersion;
  }

  /**
   * Returns the max Android API version number available in directory 'dir'
   *
   * @param dir
   * @return
   */
  private int getMaxAPIAvailable(String dir) {
    Integer mapi = this.maxAPIs.get(dir);
    if (mapi != null) {
      return mapi;
    }

    File[] files = newFile(dir).listFiles();
    if (files == null) {
      return -1;
    }

    max_api = getMaxApi(files);
    this.maxAPIs.put(dir, max_api);
    return max_api;
  }

  private int getMaxApi(File[] files) {
    int maxApi = -1;
    for (File f : files) {
      String name = f.getName();
      if (f.isDirectory() && name.startsWith("android-")) {
        try {
          int v = Integer.decode(name.split("android-")[1]);
          if (v > maxApi) {
            maxApi = v;
          }
        } catch (NumberFormatException ex) {
          // We simply ignore directories that do not follow the
          // Android naming structure
        }
      }
    }
    return maxApi;
  }

  public File newFile(String path) {
    return new File(path);
  }

  public int getApi_version() {
    getAndroidJarPath(androidPlatformsPath, apk_path);
    return api_version;
  }

  public int getSdkTargetVersion() {
    return sdkTargetVersion;
  }

  public int getMax_api() {
    return max_api;
  }

  /**
   * The android.jar path for this APK's resolved API level, under the platforms directory given at
   * construction. Existence is not checked here; {@link #getApi_version()} already logs a warning
   * if it is missing.
   */
  public String androidJarPath() {
    return androidPlatformsPath
        + File.separatorChar
        + "android-"
        + getApi_version()
        + File.separatorChar
        + "android.jar";
  }

  /**
   * A library input location for the android.jar matching this APK, so framework classes referenced
   * but not defined in the APK resolve. Type inference in particular needs it: without it, any
   * local whose type can only be found by walking the class hierarchy (not just read off a field,
   * method or catch signature) falls back to {@code java.lang.Object}. Add it to the {@link
   * sootup.core.views.View} next to the {@code ApkAnalysisInputLocation}.
   */
  public JavaClassPathAnalysisInputLocation androidJarInputLocation() {
    return new JavaClassPathAnalysisInputLocation(androidJarPath(), SourceType.Library);
  }
}
