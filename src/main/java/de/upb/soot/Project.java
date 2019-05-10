package de.upb.soot;

import de.upb.soot.buildactor.ViewBuilder;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.TypeFactory;
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
public class Project<N extends INamespace> {
  /** Create a project from an arbitrary list of namespaces */
  public Project(@Nonnull N namespace) {
    this(namespace, DefaultSignatureFactory.getInstance(), DefaultTypeFactory.getInstance());
  }

  /** Create a project from an arbitrary list of namespaces */
  public Project(
      @Nonnull N namespaces,
      @Nonnull SignatureFactory signatureFactory,
      @Nonnull TypeFactory typeFactory) {
    this.namespace = namespaces;
    this.signatureFactory = signatureFactory;
    this.typeFactory = typeFactory;
  }

  @Nonnull private final N namespace;

  /** Gets the namespace. */
  @Nonnull
  public N getNamespace() {
    return this.namespace;
  }

  @Nonnull private final SignatureFactory signatureFactory;

  @Nonnull private final TypeFactory typeFactory;

  @Nonnull
  public SignatureFactory getSignatureFactory() {
    return this.signatureFactory;
  }

  public TypeFactory getTypeFactory() {
    return typeFactory;
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
