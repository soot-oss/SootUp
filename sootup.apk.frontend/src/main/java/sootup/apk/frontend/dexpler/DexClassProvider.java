package sootup.apk.frontend.dexpler;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class DexClassProvider implements ClassProvider {
  @Nonnull private final View view;

  public DexClassProvider(@Nonnull View view) {
    this.view = view;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      AnalysisInputLocation inputLocation, Path sourcePath, ClassType classSignature) {
    return Optional.of(new DexClassSource(view, inputLocation, classSignature, sourcePath));
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.DEX;
  }
}
