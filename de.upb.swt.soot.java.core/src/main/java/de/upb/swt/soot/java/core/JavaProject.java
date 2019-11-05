package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Project Implementation for analyzing Java.
 *
 * @author Markus Schmidt
 */
public class JavaProject extends Project {
  final boolean useJavaModules;

  public JavaProject(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier,
      boolean useJavaModules) {
    super(inputLocations, JavaIdentifierFactory.getInstance(), sourceTypeSpecifier);
    this.useJavaModules = useJavaModules;
  }

  @Nonnull
  protected JavaView chooseView() {
    if (useJavaModules) {
      // TODO: [ms] replace with the view for java modules.. problem: circular dependency on
      // soot.java.bytecode
      return new JavaView(this);
    } else {
      return new JavaView(this);
    }
  }

  @Nonnull
  @Override
  public JavaView createOnDemandView() {
    return chooseView();
  }

  @Nonnull
  @Override
  public JavaView createFullView() {
    JavaView view = chooseView();
    view.getClasses();
    return view;
  }

  /**
   * Creates a {@link JavaProject} builder.
   *
   * @return A {@link JavaProjectBuilder}.
   */
  @Nonnull
  public static JavaProjectBuilder builder() {
    return new JavaProjectBuilder();
  }

  public static class JavaProjectBuilder {
    boolean usesJavaModules = false;
    final List<AnalysisInputLocation> analysisInputLocations = new ArrayList<>();
    SourceTypeSpecifier sourceTypeSpecifier = DefaultSourceTypeSpecifier.getInstance();

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
      usesJavaModules = true;
      this.analysisInputLocations.addAll(analysisInputLocation);
      return this;
    }

    @Nonnull
    JavaProjectBuilder addModulePath(AnalysisInputLocation analysisInputLocation) {
      usesJavaModules = true;
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    public JavaProject build() {
      return new JavaProject(analysisInputLocations, sourceTypeSpecifier, usesJavaModules);
    }
  }
}
