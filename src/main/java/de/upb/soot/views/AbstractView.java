package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.types.Type;
import de.upb.soot.types.TypeFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView implements IView {

  @Nonnull private final Project project;

  @Nonnull private final Options options;

  public AbstractView(@Nonnull Project project) {
    this.project = project;
    this.options = new Options();
  }

  @Override
  @Nonnull
  public SignatureFactory getSignatureFactory() {
    return this.getProject().getSignatureFactory();
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
