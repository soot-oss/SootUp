import Util.*;
import dexpler.DexClassProvider;
import dexpler.DexFileProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;

public class ApkAnalysisInputLocation<J extends SootClass> implements AnalysisInputLocation {

  Path apk_path;

  //  String android_jar_path;

  int api_version = -1;

  //  private static final int defaultSdkVersion = 15;

  //  private final Map<String, Integer> maxAPIs = new HashMap<String, Integer>();

  //  private AndroidVersionInfo androidSDKVersionInfo;

  private final List<BodyInterceptor> bodyInterceptors;

  final Map<String, EnumSet<ClassModifier>> classNamesList;

  private static final Logger logger = LoggerFactory.getLogger(ApkAnalysisInputLocation.class);

  public ApkAnalysisInputLocation(
      Path apkPath, String android_jar_path, List<BodyInterceptor> bodyInterceptors) {
    this.apk_path = apkPath;
    this.bodyInterceptors = bodyInterceptors;
    //    this.android_jar_path = getAndroidJarPath(android_jar_path, apkPath.toString());
    this.classNamesList = extractDexFilesFromPath();
    //    SourceLocator.getInstance().setClassPath(this.apk_path.toString(), this.android_jar_path);
  }

  private Map<String, EnumSet<ClassModifier>> extractDexFilesFromPath() {
    List<DexFileProvider.DexContainer<? extends DexFile>> dexFromSource;
    try {
      dexFromSource =
          DexFileProvider.getInstance()
              .getDexFromSource(new File(apk_path.toString()), api_version);
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
                            Util.dottedClassName(dexClass.toString()),
                            DexUtil.getClassModifiers(dexClass.getAccessFlags()))));
    return classList;
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return Objects.requireNonNull(getClassSourceInternal(type, new DexClassProvider(view)));
  }

  private Optional<? extends SootClassSource> getClassSourceInternal(
      ClassType type, DexClassProvider dexClassProvider) {

    return dexClassProvider.createClassSource(this, apk_path, type);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return classNamesList.entrySet().stream()
        .flatMap(
            className ->
                StreamUtils.optionalToStream(
                    getClassSource(
                        view.getIdentifierFactory().getClassType(className.getKey()), view)))
        .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public SourceType getSourceType() {
    return SourceType.Application;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  //  public String getAndroidJarPath(String jars, String apk) {
  //    int APIVersion = getAndroidAPIVersion(jars, apk);
  //
  //    String jarPath =
  //        jars + File.separatorChar + "android-" + APIVersion + File.separatorChar +
  // "android.jar";
  //
  //    // check that jar exists
  //    File f = new File(jarPath);
  //    if (!f.isFile()) {
  //      throw new RuntimeException(
  //          String.format("error: target android.jar %s does not exist.", jarPath));
  //    }
  //    return jarPath;
  //  }
  //
  //  private int getAndroidAPIVersion(String jars, String apk) {
  //    if (api_version > 0) {
  //      return api_version;
  //    }
  //
  //    // get path to appropriate android.jar
  //    File jarsF = new File(jars);
  //    if (!jarsF.exists()) {
  //      throw new RuntimeException(
  //          String.format(
  //              "Android platform directory '%s' does not exist!", jarsF.getAbsolutePath()));
  //    }
  //    if (apk != null && !(new File(apk)).exists()) {
  //      throw new RuntimeException("file '" + apk + "' does not exist!");
  //    }
  //
  //    // Use the default if we don't have any other information
  //    api_version = defaultSdkVersion;
  //
  //    if (apk != null) {
  //      if (apk.toLowerCase().endsWith(".apk")) {
  //        api_version = getTargetSDKVersion(apk, jars);
  //      }
  //    }
  //
  //    // If we don't have that API version installed, we take the most recent one we have
  //    final int maxAPI = getMaxAPIAvailable(jars);
  //    if (maxAPI > 0 && api_version > maxAPI) {
  //      api_version = maxAPI;
  //    }
  //
  //    // If the platform version is missing in the middle, we take the next one
  //    while (api_version < maxAPI) {
  //      String jarPath =
  //          jars + File.separatorChar + "android-" + api_version + File.separatorChar +
  // "android.jar";
  //      if (new File(jarPath).exists()) {
  //        break;
  //      }
  //      api_version++;
  //    }
  //
  //    return api_version;
  //  }
  //
  //  private int getTargetSDKVersion(String apkFile, String platformJARs) {
  //    // get AndroidManifest
  //    ZipFile archive = null;
  //    try {
  //      InputStream manifestIS = null;
  //      try {
  //        archive = new ZipFile(apkFile);
  //        for (Enumeration<? extends ZipEntry> entries = archive.entries();
  //            entries.hasMoreElements(); ) {
  //          ZipEntry entry = entries.nextElement();
  //          // We are dealing with the Android manifest
  //          if ("AndroidManifest.xml".equals(entry.getName())) {
  //            manifestIS = archive.getInputStream(entry);
  //            break;
  //          }
  //        }
  //      } catch (Exception e) {
  //        throw new RuntimeException("Error when looking for manifest in apk: " + e);
  //      }
  //
  //      if (manifestIS == null) {
  //        logger.debug(
  //            "Could not find sdk version in Android manifest! Using default: " +
  // defaultSdkVersion);
  //        return defaultSdkVersion;
  //      }
  //
  //      // process AndroidManifest.xml
  //      androidSDKVersionInfo = AndroidVersionInfo.get(manifestIS);
  //    } finally {
  //      if (archive != null) {
  //        try {
  //          archive.close();
  //        } catch (IOException e) {
  //          throw new RuntimeException("Error when looking for manifest in apk: " + e);
  //        }
  //      }
  //    }
  //
  //    int maxAPI = getMaxAPIAvailable(platformJARs);
  //    int APIVersion = -1;
  //
  //    if (androidSDKVersionInfo.sdkTargetVersion != -1) {
  //      if (androidSDKVersionInfo.sdkTargetVersion > maxAPI
  //          && androidSDKVersionInfo.minSdkVersion != -1
  //          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
  //        logger.warn(
  //            "Android API version '"
  //                + androidSDKVersionInfo.sdkTargetVersion
  //                + "' not available, using minApkVersion '"
  //                + androidSDKVersionInfo.minSdkVersion
  //                + "' instead");
  //        APIVersion = androidSDKVersionInfo.minSdkVersion;
  //      } else {
  //        APIVersion = androidSDKVersionInfo.sdkTargetVersion;
  //      }
  //    } else if (androidSDKVersionInfo.platformBuildVersionCode != -1) {
  //      if (androidSDKVersionInfo.platformBuildVersionCode > maxAPI
  //          && androidSDKVersionInfo.minSdkVersion != -1
  //          && androidSDKVersionInfo.minSdkVersion <= maxAPI) {
  //        logger.warn(
  //            "Android API version '"
  //                + androidSDKVersionInfo.platformBuildVersionCode
  //                + "' not available, using minApkVersion '"
  //                + androidSDKVersionInfo.minSdkVersion
  //                + "' instead");
  //        APIVersion = androidSDKVersionInfo.minSdkVersion;
  //      } else {
  //        APIVersion = androidSDKVersionInfo.platformBuildVersionCode;
  //      }
  //    } else if (androidSDKVersionInfo.minSdkVersion != -1) {
  //      APIVersion = androidSDKVersionInfo.minSdkVersion;
  //    } else {
  //      logger.debug(
  //          "Could not find sdk version in Android manifest! Using default: " +
  // defaultSdkVersion);
  //      APIVersion = defaultSdkVersion;
  //    }
  //
  //    if (APIVersion <= 2) {
  //      APIVersion = 3;
  //    }
  //
  //    return APIVersion;
  //  }
  //
  //  /**
  //   * Returns the max Android API version number available in directory 'dir'
  //   *
  //   * @param dir
  //   * @return
  //   */
  //  private int getMaxAPIAvailable(String dir) {
  //    Integer mapi = this.maxAPIs.get(dir);
  //    if (mapi != null) {
  //      return mapi;
  //    }
  //
  //    File d = new File(dir);
  //    if (!d.exists()) {
  //      throw new RuntimeException(
  //          String.format(
  //              "The Android platform directory you have specified (%s) does not exist. Please
  // check.",
  //              dir));
  //    }
  //
  //    File[] files = d.listFiles();
  //    if (files == null) {
  //      return -1;
  //    }
  //
  //    int maxApi = -1;
  //    for (File f : files) {
  //      String name = f.getName();
  //      if (f.isDirectory() && name.startsWith("android-")) {
  //        try {
  //          int v = Integer.decode(name.split("android-")[1]);
  //          if (v > maxApi) {
  //            maxApi = v;
  //          }
  //        } catch (NumberFormatException ex) {
  //          // We simply ignore directories that do not follow the
  //          // Android naming structure
  //        }
  //      }
  //    }
  //    this.maxAPIs.put(dir, maxApi);
  //    return maxApi;
  //  }
}
