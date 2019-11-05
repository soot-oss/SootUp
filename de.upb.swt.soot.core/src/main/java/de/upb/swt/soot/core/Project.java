/*
 * @author Linghui Luo
 */
package de.upb.swt.soot.core;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.views.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public abstract class Project {

  @Nonnull private final IdentifierFactory identifierFactory;
  @Nonnull private final List<AnalysisInputLocation> inputLocations;
  @Nonnull private final SourceTypeSpecifier sourceTypeSpecifier;
  @Nonnull private final Language language;
  /**
   * Create a project from an arbitrary input location.
   *
   * @param language the language
   * @param inputLocation the input location
   * @param sourceTypeSpecifier the source type specifier
   */
  public Project(
      @Nonnull Language language,
      @Nonnull AnalysisInputLocation inputLocation,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this(
        language,
        Collections.singletonList(inputLocation),
        language.getIdentifierFactory(),
        sourceTypeSpecifier);
  }

  /**
   * Create a project from an arbitrary list of input locations.
   *
   * @param language the language
   * @param inputLocations the input locations
   * @param identifierFactory the identifier factory
   * @param sourceTypeSpecifier the source type specifier
   */
  public Project(
      @Nonnull Language language,
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this.language = language;
    List<AnalysisInputLocation> unmodifiableInputLocations =
        Collections.unmodifiableList(new ArrayList<>(inputLocations));

    if (unmodifiableInputLocations.isEmpty()) {
      throw new IllegalArgumentException("The inputLocations collection must not be empty.");
    }

    this.sourceTypeSpecifier = sourceTypeSpecifier;
    this.inputLocations = unmodifiableInputLocations;
    this.identifierFactory = identifierFactory;
  }

  /** Gets the inputLocations. */
  @Nonnull
  public List<AnalysisInputLocation> getInputLocations() {
    return this.inputLocations;
  }

  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return this.identifierFactory;
  }

  @Nonnull
  public SourceTypeSpecifier getSourceTypeSpecifier() {
    return sourceTypeSpecifier;
  }

  @Nonnull
  public Language getLanguage() {
    return language;
  }

  /**
   * Create a complete view from everything in all provided input locations. This methodRef starts
   * the reification process.
   *
   * @return A complete view on the provided code
   */
  @Nonnull
  public abstract View createFullView();

  /**
   * Creates an on-demand View that uses the default {@link
   * de.upb.swt.soot.core.inputlocation.ClassLoadingOptions} of each frontend.
   */
  @Nonnull
  public abstract View createOnDemandView();

  /** Creates an on-demand View with custom {@link ClassLoadingOptions}. */
  @Nonnull
  public abstract View createOnDemandView(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier);

  /**
   * Returns a partial view on the code based on the provided scope and all input locations in the
   * project. This methodRef starts the reification process.
   *
   * @param s A scope of interest for the view
   * @return A scoped view of the provided code
   */
  @Nonnull
  public abstract View createView(Scope s);
}
