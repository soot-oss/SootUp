package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.types.TypeFactory;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView implements IView {

  @Nonnull private final Project project;

  @Nonnull private final Options options = new Options();

  public AbstractView(@Nonnull Project project) {
    this.project = project;
  }

  @Override
  @Nonnull
  public SignatureFactory getSignatureFactory() {
    return this.getProject().getSignatureFactory();
  }

  @Nonnull
  @Override
  public TypeFactory getTypeFactory() {
    return this.getProject().getTypeFactory();
  }

  @Override
  @Nonnull
  public ICallGraph createCallGraph() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  @Nonnull
  public ICallGraph createCallGraph(ICallGraphAlgorithm algorithm) {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  @Nonnull
  public ITypeHierarchy createTypeHierarchy() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  @Nonnull
  public Optional<Scope> getScope() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public boolean doneResolving() {
    // TODO Auto-generated methodRef stub
    return false;
  }

  @Override
  @Nonnull
  public Options getOptions() {
    return this.options;
  }

  @Nonnull
  public Project getProject() {
    return project;
  }
}
