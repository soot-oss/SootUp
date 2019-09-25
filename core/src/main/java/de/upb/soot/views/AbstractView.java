package de.upb.soot.views;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.CallGraph;
import de.upb.soot.callgraph.CallGraphAlgorithm;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView<S extends AnalysisInputLocation> implements View {

  @Nonnull private final Project<S> project;

  @Nonnull private final Options options = new Options();

  // TODO: uncomment!
  /*
    private final Supplier<MutableTypeHierarchy> lazyTypeHierarchy =
        Suppliers.memoize(() -> $ViewTypeHierarchyAccessor.createViewTypeHierarchy(this));
  */
  public AbstractView(@Nonnull Project<S> project) {
    this.project = project;
  }

  @Override
  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return this.getProject().getIdentifierFactory();
  }

  @Override
  @Nonnull
  public CallGraph createCallGraph() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  @Nonnull
  public CallGraph createCallGraph(CallGraphAlgorithm algorithm) {
    // TODO Auto-generated methodRef stub
    return null;
  }

  // TODO: uncomment!
  /*
    @Override
    @Nonnull
    public TypeHierarchy typeHierarchy() {
      return lazyTypeHierarchy.get();
    }
  */
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
  public Project<S> getProject() {
    return project;
  }
}
