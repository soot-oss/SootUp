package de.upb.swt.soot.core;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import de.upb.swt.soot.core.views.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */

// TODO: <S extends SootLanguage>
public abstract class Project {

  @Nonnull private final IdentifierFactory identifierFactory;
  @Nonnull private final List<AnalysisInputLocation> inputLocations;
  @Nonnull private final SourceTypeSpecifier sourceTypeSpecifier;

  /** Create a project from an arbitrary input location */
  public Project(
      @Nonnull AnalysisInputLocation inputLocation,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this(Collections.singletonList(inputLocation), identifierFactory, sourceTypeSpecifier);
  }

  /** Create a project from an arbitrary list of input locations */
  public Project(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {

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
  /**
   * Create a complete view from everything in all provided input locations. This methodRef starts
   * the reification process.
   *
   * @return A complete view on the provided code
   */
  @Nonnull
  public abstract View createFullView();

  @Nonnull
  public abstract View createOnDemandView();

  /**
   * Returns a partial view on the code based on the provided scope and all input locations in the
   * project. This methodRef starts the reification process.
   *
   * @param s A scope of interest for the view
   * @return A scoped view of the provided code
   */
  @Nonnull
  public View createView(Scope s) {
    return null;
  }
}
