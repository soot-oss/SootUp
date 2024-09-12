package sootup.apk.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jf.dexlib2.iface.DexFile;
import sootup.apk.parser.Util.*;
import sootup.apk.parser.dexpler.DexClassProvider;
import sootup.apk.parser.dexpler.DexFileProvider;
import sootup.apk.parser.main.AndroidVersionInfo;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.Modifiers;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;

public class ApkAnalysisInputLocation implements AnalysisInputLocation {

  Path apk_path;

  String android_jar_path;

  private final AndroidVersionInfo androidSDKVersionInfo;

  private final List<BodyInterceptor> bodyInterceptors;

  final Map<String, EnumSet<ClassModifier>> classNamesList;

  public ApkAnalysisInputLocation(
      Path apkPath, String android_jar_path, List<BodyInterceptor> bodyInterceptors) {
    this.apk_path = apkPath;
    androidSDKVersionInfo = new AndroidVersionInfo(apkPath, android_jar_path);
    this.android_jar_path = android_jar_path;
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
}
