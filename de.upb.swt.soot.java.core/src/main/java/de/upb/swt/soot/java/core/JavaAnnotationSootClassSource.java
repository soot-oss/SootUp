package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public abstract class JavaAnnotationSootClassSource extends JavaSootClassSource {

  public JavaAnnotationSootClassSource(
      @Nonnull AnalysisInputLocation<JavaSootClass> srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  @Override
  @Nonnull
  public JavaAnnotationSootClass buildClass(@Nonnull SourceType sourceType) {
    return new JavaAnnotationSootClass(this, sourceType);
  }
}
