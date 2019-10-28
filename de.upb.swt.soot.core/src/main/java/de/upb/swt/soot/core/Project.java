package de.upb.swt.soot.core;

import de.upb.swt.soot.core.buildactor.ViewBuilder;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import de.upb.swt.soot.core.util.NotYetImplementedException;
import de.upb.swt.soot.core.views.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Project<S extends AnalysisInputLocation> {

  @Nonnull private final IdentifierFactory identifierFactory;
  @Nonnull private final List<S> inputLocations;
  @Nonnull private final SourceTypeSpecifier sourceTypeSpecifier;

  /** Create a project from an input location and an identifier factory */
  public Project(@Nonnull S inputLocation, @Nonnull IdentifierFactory identifierFactory) {
    this(
        Collections.singleton(inputLocation),
        identifierFactory,
        DefaultSourceTypeSpecifier.getInstance());
  }

  /** Create a project from an arbitrary input location */
  public Project(
      @Nonnull S inputLocation,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this(Collections.singleton(inputLocation), identifierFactory, sourceTypeSpecifier);
  }

  /** Create a project from an arbitrary list of input locations and an identifier factory */
  public Project(@Nonnull Set<S> inputLocation, @Nonnull IdentifierFactory identifierFactory) {
    this(inputLocation, identifierFactory, DefaultSourceTypeSpecifier.getInstance());
  }

  /** Create a project from an arbitrary list of input locations */
  public Project(
      @Nonnull Set<S> inputLocations,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {

    List<S> unmodifiableInputLocations =
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
  public List<S> getInputLocations() {
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
  public View createFullView() {
    //    ViewBuilder vb = new ViewBuilder(this);
    //    return vb.buildComplete();

    throw new NotYetImplementedException();
  }

  /**
   * Creates an on-demand View that uses the default {@link
   * de.upb.swt.soot.core.inputlocation.ClassLoadingOptions} of each frontend.
   */
  @Nonnull
  public View createOnDemandView() {
    return createOnDemandView(null);
  }

  /** Creates an on-demand View with custom {@link ClassLoadingOptions}. */
  @Nonnull
  public View createOnDemandView(@Nullable ClassLoadingOptions classLoadingOptions) {
    ViewBuilder<S> vb = new ViewBuilder<>(this);
    return vb.buildOnDemand(classLoadingOptions);
  }

  /**
   * Returns a partial view on the code based on the provided scope and all input locations in the
   * project. This methodRef starts the reification process.
   *
   * @param s A scope of interest for the view
   * @return A scoped view of the provided code
   */
  @Nonnull
  public View createView(Scope s) {
    throw new NotYetImplementedException(); // TODO
  }
}
