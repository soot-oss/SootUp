package sootup.jimple.parser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class JimpleStringAnalysisInputLocation implements AnalysisInputLocation {

  @Nonnull final Path path = Paths.get("only-in-memory.jimple");
  @Nonnull final List<BodyInterceptor> bodyInterceptors;
  @Nonnull private final OverridingClassSource classSource;

  public JimpleStringAnalysisInputLocation(@Nonnull String data) {
    this(data, Collections.emptyList());
  }

  public JimpleStringAnalysisInputLocation(
      @Nonnull String data, List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;

    try {
      JimpleConverter jimpleConverter = new JimpleConverter();
      classSource = jimpleConverter.run(CharStreams.fromString(data), this, path, bodyInterceptors);

    } catch (Exception e) {
      throw new IllegalArgumentException("No valid Jimple given.", e);
    }
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return Optional.of(classSource);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return Collections.singletonList(classSource);
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return SourceType.Application;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  @Nonnull
  public ClassType getClassType() {
    return classSource.getClassType();
  }
}
