package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public abstract class JavaSootClassSource extends SootClassSource {

  public JavaSootClassSource(
      @Nonnull AnalysisInputLocation srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  protected JavaSootClassSource(SootClassSource delegate) {
    super(delegate);
  }

  public abstract Iterable<AnnotationType> resolveAnnotations();

  public abstract Iterable<AnnotationType> resolveMethodAnnotations();

  // TODO: [ms] maybe move to a better place?
  public abstract Iterable<AnnotationType> resolveFieldAnnotations();
}
