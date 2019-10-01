package de.upb.swt.soot.core;

import de.upb.swt.soot.core.buildactor.ViewBuilder;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.util.NotYetImplementedException;
import de.upb.swt.soot.core.views.View;
import javax.annotation.Nonnull;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Project<S extends AnalysisInputLocation> {
  /** Create a project from an arbitrary list of input locations */
  public Project(@Nonnull S inputLocation) {
    this(inputLocation, DefaultIdentifierFactory.getInstance());
  }

  /** Create a project from an arbitrary list of input locations */
  public Project(@Nonnull S inputLocations, @Nonnull DefaultIdentifierFactory identifierFactory) {
    this.inputLocation = inputLocations;
    this.identifierFactory = identifierFactory;
  }

  @Nonnull private final S inputLocation;

  /** Gets the inputLocation. */
  @Nonnull
  public S getInputLocation() {
    return this.inputLocation;
  }

  @Nonnull private final IdentifierFactory identifierFactory;

  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return this.identifierFactory;
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

  @Nonnull
  public View createOnDemandView() {
    ViewBuilder<S> vb = new ViewBuilder<>(this);
    return vb.buildOnDemand();
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
