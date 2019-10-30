package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Convenience Project for analyzing Java.
 *
 * @author Markus Schmidt
 */
public class JavaProject extends Project {

  public JavaProject(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(inputLocations, JavaIdentifierFactory.getInstance(), sourceTypeSpecifier);
  }

  @Nonnull
  @Override
  public View createOnDemandView() {
    // TODO: [ms] abstract implementation due to language independence; call it via
    // ViewBuilder.createOnDemandView() again?
    return new JavaView<>(this);
  }

  /**
   * Creates a {@link JavaProject} builder.
   *
   * @return A {@link JavaProject} builder.
   */
  @Nonnull
  public static JavaProjectBuilder builder() {
    return new JavaProjectBuilder();
  }

  public static class JavaProjectBuilder {
    final List<AnalysisInputLocation> analysisInputLocations = new ArrayList<>();
    SourceTypeSpecifier sourceTypeSpecifier = DefaultSourceTypeSpecifier.getInstance();

    @Nonnull
    public JavaProjectBuilder setSourceTypeSpecifier(SourceTypeSpecifier sourceTypeSpecifier) {
      this.sourceTypeSpecifier = sourceTypeSpecifier;
      return this;
    }

    // TODO: [ms] create AnalysisInputLocations here? determining the needed one automatically from
    // String? then
    // we need a possibility to "debug"/see which files where found for the consumer.
    @Nonnull
    public JavaProjectBuilder addClassPath(
        Collection<AnalysisInputLocation> analysisInputLocations) {
      this.analysisInputLocations.addAll(analysisInputLocations);
      return this;
    }

    @Nonnull
    public JavaProjectBuilder addClassPath(AnalysisInputLocation analysisInputLocation) {
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    JavaProjectBuilder addModulePath(Collection<AnalysisInputLocation> analysisInputLocation) {
      // TODO: [ms] java9
      this.analysisInputLocations.addAll(analysisInputLocation);
      return this;
    }

    @Nonnull
    JavaProjectBuilder addModulePath(AnalysisInputLocation analysisInputLocation) {
      // TODO: [ms] java9
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    public JavaProject make() {
      return new JavaProject(analysisInputLocations, sourceTypeSpecifier);
    }
  }
}
