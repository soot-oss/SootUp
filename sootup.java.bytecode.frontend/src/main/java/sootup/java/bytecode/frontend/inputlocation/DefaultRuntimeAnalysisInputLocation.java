package sootup.java.bytecode.frontend.inputlocation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;

/** AnalysisInputLocation that points to the shipped Java Runtime of the current JVM execution */
public class DefaultRuntimeAnalysisInputLocation implements AnalysisInputLocation {

  @Nonnull private final AnalysisInputLocation backingInputLocation;

  public DefaultRuntimeAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public DefaultRuntimeAnalysisInputLocation(@Nonnull SourceType srcType) {
    this(srcType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public DefaultRuntimeAnalysisInputLocation(
      @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors) {

    String version = System.getProperty("java.version");
    // are we using Java 8 or lower in the current JVM execution?
    if (version.startsWith("1")) {
      backingInputLocation = new DefaultRTJarAnalysisInputLocation(srcType, bodyInterceptors);
    } else {
      backingInputLocation = new JrtFileSystemAnalysisInputLocation(srcType, bodyInterceptors);
    }
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return backingInputLocation.getClassSource(type, view);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return backingInputLocation.getClassSources(view);
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return backingInputLocation.getSourceType();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return backingInputLocation.getBodyInterceptors();
  }
}
