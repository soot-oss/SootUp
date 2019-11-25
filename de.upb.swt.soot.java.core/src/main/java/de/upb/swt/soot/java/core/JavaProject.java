package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.SourceTypeSpecifier;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Project Implementation for analyzing Java.
 *
 * @author Markus Schmidt
 * @author Linghui Luo
 */
public class JavaProject extends Project {

  private JavaViewBuilder viewBuilder;

  public JavaProject(
      JavaLanguage language,
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(language, inputLocations, JavaIdentifierFactory.getInstance(), sourceTypeSpecifier);
    this.viewBuilder = new JavaViewBuilder(this);
  }

  @Nonnull
  @Override
  public JavaView createOnDemandView() {
    return viewBuilder.createOnDemandView();
  }

  @Nonnull
  @Override
  public View createOnDemandView(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    return viewBuilder.createOnDemandView(classLoadingOptionsSpecifier);
  }

  @Nonnull
  @Override
  public JavaView createFullView() {
    return viewBuilder.createFullView();
  }

  @Nonnull
  @Override
  public JavaView createView(Scope s) {
    return viewBuilder.createView(s);
  }

  /**
   * Creates a {@link JavaProject} builder.
   *
   * @return A {@link JavaProjectBuilder}.
   */
  @Nonnull
  public static JavaProjectBuilder builder(JavaLanguage language) {
    return new JavaProjectBuilder(language);
  }

  public static class JavaProjectBuilder {
    private final List<AnalysisInputLocation> analysisInputLocations = new ArrayList<>();
    private SourceTypeSpecifier sourceTypeSpecifier = DefaultSourceTypeSpecifier.getInstance();
    private final JavaLanguage language;

    public JavaProjectBuilder(JavaLanguage language) {
      this.language = language;
    }

    @Nonnull
    public JavaProjectBuilder setSourceTypeSpecifier(SourceTypeSpecifier sourceTypeSpecifier) {
      this.sourceTypeSpecifier = sourceTypeSpecifier;
      return this;
    }

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
      this.analysisInputLocations.addAll(analysisInputLocation);
      return this;
    }

    @Nonnull
    JavaProjectBuilder addModulePath(AnalysisInputLocation analysisInputLocation) {
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    public JavaProject build() {
      return new JavaProject(language, analysisInputLocations, sourceTypeSpecifier);
    }
  }
}
