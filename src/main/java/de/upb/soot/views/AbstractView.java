package de.upb.soot.views;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.typehierarchy.ITypeHierarchy;
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
  public IdentifierFactory getIdentifierFactory() {
    return this.getProject().getIdentifierFactory();
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
