package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.IdentifierFactory;
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
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(inputLocations, identifierFactory, sourceTypeSpecifier);
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
  public static JavaProject.Builder builder() {
    return new Builder();
  }

  public static class Builder {
    final List<AnalysisInputLocation> analysisInputLocations = new ArrayList();
    IdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
    SourceTypeSpecifier sourceTypeSpecifier = DefaultSourceTypeSpecifier.getInstance();

    @Nonnull
    public Builder setSourceTypeSpecifier(IdentifierFactory identifierFactory) {
      this.identifierFactory = identifierFactory;
      return this;
    }
    /*  TODO: [ms] should not be necessary anymore when project<Language> is implemented
    Builder setIdentifierFactory(IdentifierFactory identifierFactory){
      this.identifierFactory = identifierFactory;
      return this;
    }
    */

    // TODO: [ms] create AnalysisInputLocations here? determining the needed one automatically? then
    // we need a possibility to "debug"/see which files where found for the consumer.
    @Nonnull
    public Builder addClassPath(Collection<AnalysisInputLocation> analysisInputLocations) {
      this.analysisInputLocations.addAll(analysisInputLocations);
      return this;
    }

    @Nonnull
    public Builder addClassPath(AnalysisInputLocation analysisInputLocation) {
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    Builder addModulePath(Collection<AnalysisInputLocation> analysisInputLocation) {
      // TODO: [ms] java9
      this.analysisInputLocations.addAll(analysisInputLocation);
      return this;
    }

    @Nonnull
    Builder addModulePath(AnalysisInputLocation analysisInputLocation) {
      // TODO: [ms] java9
      this.analysisInputLocations.add(analysisInputLocation);
      return this;
    }

    @Nonnull
    public JavaProject make() {
      return new JavaProject(analysisInputLocations, identifierFactory, sourceTypeSpecifier);
    }
  }
}
