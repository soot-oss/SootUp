package de.upb.soot;

import de.upb.soot.buildactor.ViewBuilder;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.IdentifierFactory;
import de.upb.soot.types.DefaultIdentifierFactory;
import de.upb.soot.util.NotYetImplementedException;
import de.upb.soot.views.IView;
import javax.annotation.Nonnull;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Project {
  /** Create a project from an arbitrary list of namespaces */
  public Project(@Nonnull INamespace namespace) {
    this(namespace, DefaultIdentifierFactory.getInstance());
  }

  /** Create a project from an arbitrary list of namespaces */
  public Project(
      @Nonnull INamespace namespaces,
      @Nonnull IdentifierFactory identifierFactory) {
    this.namespace = namespaces;
    this.identifierFactory = identifierFactory;
  }

  @Nonnull private final INamespace namespace;

  /** Gets the namespace. */
  @Nonnull
  public INamespace getNamespace() {
    return this.namespace;
  }

  @Nonnull private final IdentifierFactory identifierFactory;

  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return this.identifierFactory;
  }

  /**
   * Create a complete view from everything in all provided namespaces. This methodRef starts the
   * reification process.
   *
   * @return A complete view on the provided code
   */
  @Nonnull
  public IView createFullView() {
    //    ViewBuilder vb = new ViewBuilder(this);
    //    return vb.buildComplete();

    throw new NotYetImplementedException();
  }

  @Nonnull
  public IView createOnDemandView() {
    ViewBuilder vb = new ViewBuilder(this);
    return vb.buildOnDemand();
  }

  /**
   * Returns a partial view on the code based on the provided scope and all namespaces in the
   * project. This methodRef starts the reification process.
   *
   * @param s A scope of interest for the view
   * @return A scoped view of the provided code
   */
  @Nonnull
  public IView createView(Scope s) {
    throw new NotYetImplementedException(); // TODO
  }
}
