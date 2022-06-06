package de.upb.swt.soot.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
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

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexClassProvider;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexClassSource;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexFileProvider;
import de.upb.swt.soot.java.core.JavaSootClass;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nonnull;

import de.upb.swt.soot.java.core.types.JavaClassType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

public class ApkAnalysisInputLocation implements AnalysisInputLocation<JavaSootClass> {

  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(ApkAnalysisInputLocation.class);

  private final Map<String, Integer> maxAPIs = new HashMap<String, Integer>();
  private AndroidVersionInfo androidSDKVersionInfo;
  private int androidAPIVersion = -1;
  private static final int defaultSdkVersion = 15;

  @Nonnull private final Path apkPath;
  int android_api_version = 10;

  public ApkAnalysisInputLocation(
      @Nonnull Path apkPath, Collection<MultiDexContainer.DexEntry<? extends DexFile>> dexFiles) {
    //this.dexFiles = dexFiles; // TODO: KKwip
    if (!Files.exists(apkPath)) {
      throw new ResolveException("No APK file found", apkPath);
    }
    this.apkPath = apkPath;
  }

  public ApkAnalysisInputLocation(Path apkPath) {
    this.apkPath = apkPath;
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType type, @Nonnull View<?> view) {
    DexFileProvider.DexContainer<? extends DexFile> dexContainer;
    try {
      dexContainer = new DexFileProvider().getDexFromSource(this.apkPath, type.getFullyQualifiedName());
      return Optional.ofNullable(new DexClassProvider().createClassSource(this, dexContainer.getFilePath(), type));
    } catch (IOException e){
      e.printStackTrace();
    }
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull View<?> view) {
    List<DexFileProvider.DexContainer<? extends DexFile>> dexContainers;
    Set<DexClassSource> found = new HashSet<>();
    try {
      dexContainers = new DexFileProvider().getDexFromSource(this.apkPath);
      //TODO: KKwip
      Set<String> classNames = new DexClassProvider().getClassNames(apkPath);
      for (String className : classNames) {
        DexClassSource classSource = new DexClassProvider().createClassSource(this, apkPath, new JavaClassType(className));
        found.add(classSource);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return found;
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

    File d = new File(dir);
    if (!d.exists()) {
      throw new AndroidPlatformException(
          String.format(
              "The Android platform directory you have specified (%s) does not exist. Please check.",
              dir));
    }

    File[] files = d.listFiles();
    if (files == null) {
      return -1;
    }

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
    this.maxAPIs.put(dir, maxApi);
    return maxApi;
  }

  public String getAndroidJarPath(String jars, String apk) {
    int APIVersion = getAndroidAPIVersion(jars, apk);

    String jarPath =
        jars + File.separatorChar + "android-" + APIVersion + File.separatorChar + "android.jar";

    // check that jar exists
    File f = new File(jarPath);
    if (!f.isFile()) {
      throw new AndroidPlatformException(
          String.format("error: target android.jar %s does not exist.", jarPath));
    }
    return jarPath;
  }

  private int options_android_api_version = 10;

  public int getAndroidAPIVersion() {
    return androidAPIVersion > 0
        ? androidAPIVersion
        : (options_android_api_version > 0 ? options_android_api_version : defaultSdkVersion);
  }

  private int getAndroidAPIVersion(String jars, String apk) {
    // Do we already have an API version?
    if (androidAPIVersion > 0) {
      return androidAPIVersion;
    }

    // get path to appropriate android.jar
    File jarsF = new File(jars);
    if (!jarsF.exists()) {
      throw new AndroidPlatformException(
          String.format(
              "Android platform directory '%s' does not exist!", jarsF.getAbsolutePath()));
    }
    if (apk != null && !(new File(apk)).exists()) {
      throw new RuntimeException("file '" + apk + "' does not exist!");
    }

    // Use the default if we don't have any other information
    androidAPIVersion = defaultSdkVersion;

    // Do we have an explicit API version?
    if (options_android_api_version > 0) {
      androidAPIVersion = options_android_api_version;
    } else if (apk != null) {
      if (apk.toLowerCase().endsWith(".apk")) {
        androidAPIVersion = getTargetSDKVersion(apk, jars);
      }
    }

    // If we don't have that API version installed, we take the most recent one we have
    final int maxAPI = getMaxAPIAvailable(jars);
    if (maxAPI > 0 && androidAPIVersion > maxAPI) {
      androidAPIVersion = maxAPI;
    }

    // If the platform version is missing in the middle, we take the next one
    while (androidAPIVersion < maxAPI) {
      String jarPath =
          jars
              + File.separatorChar
              + "android-"
              + androidAPIVersion
              + File.separatorChar
              + "android.jar";
      if (new File(jarPath).exists()) {
        break;
      }
      androidAPIVersion++;
    }

    return androidAPIVersion;
  }

  private int getTargetSDKVersion(String apkFile, String platformJARs) {
    // get AndroidManifest
    ZipFile archive = null;
    try {
      InputStream manifestIS = null;
      try {
        archive = new ZipFile(apkFile);
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
      androidSDKVersionInfo = AndroidVersionInfo.get(manifestIS);
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
    if (androidSDKVersionInfo.sdkTargetVersion != -1) {
      if (androidSDKVersionInfo.sdkTargetVersion > maxAPI
          && androidSDKVersionInfo.minSdkVersion != -1
          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
        logger.warn(
            "Android API version '"
                + androidSDKVersionInfo.sdkTargetVersion
                + "' not available, using minApkVersion '"
                + androidSDKVersionInfo.minSdkVersion
                + "' instead");
        APIVersion = androidSDKVersionInfo.minSdkVersion;
      } else {
        APIVersion = androidSDKVersionInfo.sdkTargetVersion;
      }
    } else if (androidSDKVersionInfo.platformBuildVersionCode != -1) {
      if (androidSDKVersionInfo.platformBuildVersionCode > maxAPI
          && androidSDKVersionInfo.minSdkVersion != -1
          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
        logger.warn(
            "Android API version '"
                + androidSDKVersionInfo.platformBuildVersionCode
                + "' not available, using minApkVersion '"
                + androidSDKVersionInfo.minSdkVersion
                + "' instead");
        APIVersion = androidSDKVersionInfo.minSdkVersion;
      } else {
        APIVersion = androidSDKVersionInfo.platformBuildVersionCode;
      }
    } else if (androidSDKVersionInfo.minSdkVersion != -1) {
      APIVersion = androidSDKVersionInfo.minSdkVersion;
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

  public AndroidVersionInfo getAndroidSDKVersionInfo() {
    return androidSDKVersionInfo;
  }

  private String options_android_jars = "";
  private String options_force_android_jar = "";
  private String options_soot_classpath = "";
  private List<String> options_process_dir;

  private String defaultAndroidClassPath() {
    // check that android.jar is not in classpath
    String androidJars = options_android_jars;
    String forceAndroidJar = options_force_android_jar;
    if ((androidJars == null || androidJars.isEmpty())
        && (forceAndroidJar == null || forceAndroidJar.isEmpty())) {
      throw new RuntimeException(
          "You are analyzing an Android application but did "
              + "not define android.jar. Options -android-jars or -force-android-jar should be used.");
    }

    // Get the platform JAR file. It either directly specified, or
    // we detect it from the target version of the APK we are
    // analyzing
    String jarPath = "";
    if (forceAndroidJar != null && !forceAndroidJar.isEmpty()) {
      jarPath = forceAndroidJar;

      if (options_android_api_version > 0) {
        androidAPIVersion = options_android_api_version;
      } else if (forceAndroidJar.contains("android-")) {
        Pattern pt =
            Pattern.compile(
                "\\b" + File.separatorChar + "android-(\\d+)" + "\\b" + File.separatorChar);
        Matcher m = pt.matcher(forceAndroidJar);
        if (m.find()) {
          androidAPIVersion = Integer.valueOf(m.group(1));
        }
      } else {
        androidAPIVersion = defaultSdkVersion;
      }
    } else if (androidJars != null && !androidJars.isEmpty()) {
      List<String> classPathEntries =
          new ArrayList<String>(Arrays.asList(options_soot_classpath.split(File.pathSeparator)));
      classPathEntries.addAll(options_process_dir);

      String targetApk = "";
      Set<String> targetDexs = new HashSet<String>();
      for (String entry : classPathEntries) {
        if (isApk(new File(entry))) {
          if (targetApk != null && !targetApk.isEmpty()) {
            throw new RuntimeException(
                "only one Android application can be analyzed when using option -android-jars.");
          }
          targetApk = entry;
        }
        if (entry.toLowerCase().endsWith(".dex")) {
          // names are case-insensitive
          targetDexs.add(entry);
        }
      }

      // We need at least one file to process
      if (targetApk == null || targetApk.isEmpty()) {
        if (targetDexs.isEmpty()) {
          throw new RuntimeException("no apk file given");
        }
        jarPath = getAndroidJarPath(androidJars, null);
      } else {
        jarPath = getAndroidJarPath(androidJars, targetApk);
      }
    }

    // We must have a platform JAR file when analyzing Android apps
    if (jarPath.isEmpty()) {
      throw new RuntimeException("android.jar not found.");
    }

    // Check the platform JAR file
    File f = new File(jarPath);
    if (!f.exists()) {
      throw new RuntimeException("file '" + jarPath + "' does not exist!");
    } else {
      logger.debug("Using '" + jarPath + "' as android.jar");
    }

    return jarPath;
  }

  public static boolean isApk(File apk) {
    // first check magic number
    // Note that there are multiple magic numbers for different versions of ZIP files, but all of
    // them
    // have "PK" at the beginning. In order to not decline possible future versions of ZIP files
    // which
    // may be supported by the JVM, we only check these two bytes.
    MagicNumberFileFilter apkFilter =
        new MagicNumberFileFilter(new byte[] {(byte) 0x50, (byte) 0x4B});
    if (!apkFilter.accept(apk)) {
      return false;
    }
    // second check if contains dex file.
    try (ZipFile zf = new ZipFile(apk)) {
      for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
        ZipEntry z = en.nextElement();
        if ("classes.dex".equals(z.getName())) {
          return true;
        }
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  public static class AndroidVersionInfo {

    public int sdkTargetVersion = -1;
    public int minSdkVersion = -1;
    public int platformBuildVersionCode = -1;

    private static AndroidVersionInfo get(InputStream manifestIS) {
      final AndroidVersionInfo versionInfo = new AndroidVersionInfo();
      final AxmlVisitor axmlVisitor =
          new AxmlVisitor() {
            private String nodeName = null;

            @Override
            public void attr(String ns, String name, int resourceId, int type, Object obj) {
              super.attr(ns, name, resourceId, type, obj);

              if (nodeName != null && name != null) {
                if (nodeName.equals("manifest")) {
                  if (name.equals("platformBuildVersionCode")) {
                    versionInfo.platformBuildVersionCode = Integer.valueOf("" + obj);
                  }
                } else if (nodeName.equals("uses-sdk")) {
                  // Obfuscated APKs often remove the attribute names and use the resourceId instead
                  // Therefore it is better to check for both variants
                  if (name.equals("targetSdkVersion")
                      || (name.isEmpty() && resourceId == 16843376)) {
                    versionInfo.sdkTargetVersion = Integer.valueOf(String.valueOf(obj));
                  } else if (name.equals("minSdkVersion")
                      || (name.isEmpty() && resourceId == 16843276)) {
                    versionInfo.minSdkVersion = Integer.valueOf(String.valueOf(obj));
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
        AxmlReader xmlReader = new AxmlReader(IOUtils.toByteArray(manifestIS));
        xmlReader.accept(axmlVisitor);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
      return versionInfo;
    }
  }
}
