package sootup.java.bytecode.inputlocation;

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
import sootup.java.core.interceptors.BytecodeBodyInterceptors;

// Frage: müssten nicht alle Vorkommen von DefaultRT und JrtFile durch DefaultRuntime ersetzt
// werden, wenn DefaultRT mit DefaultRuntime ersetzt wird?
public class DefaultRuntimeAnalysisInputLocation implements AnalysisInputLocation {

  private final AnalysisInputLocation instance;

  public DefaultRuntimeAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public DefaultRuntimeAnalysisInputLocation(@Nonnull SourceType srcType) {
    // doesn't match with the second DefaultRTJarAnalysisInputLocation constructor
    // shouldn't be problematic, cause of the third constructor
    this(srcType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public DefaultRuntimeAnalysisInputLocation(
      @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors) {
    String version = System.getProperty("java.version");

    if (version.startsWith("1")) {
      instance = new DefaultRTJarAnalysisInputLocation(srcType, bodyInterceptors);
    } else {
      instance = new JrtFileSystemAnalysisInputLocation(srcType, bodyInterceptors);
    }
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return instance.getClassSource(type, view);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return instance.getClassSources(view);
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return instance.getSourceType();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return instance.getBodyInterceptors();
  }
}
