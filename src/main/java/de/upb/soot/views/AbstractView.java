package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView implements IView {

  @Nonnull protected final Project project;
  protected final Options options;

//  protected final Set<ReferenceTypeSignature> refTypes;
  protected final Map<ISignature, AbstractClass> classes;

  public AbstractView(@Nonnull Project project) {
    this.project = project;
    this.options = new Options();
//    this.refTypes = new HashSet<>();
    this.classes = new HashMap<>();
  }

  @Override
  @Nonnull
  public SignatureFactory getSignatureFactory() {
    return this.project.getSignatureFactory();
  }

//  @Override
//  public @Nonnull JavaClassSignature getRefType(@Nonnull TypeSignature classSignature) {
//    Optional<ReferenceTypeSignature> op = this.refTypes.stream().filter(r -> r.getTypeSignature().equals(classSignature)).findFirst();
//    if (!op.isPresent()) {
//      ReferenceTypeSignature refType = DefaultSignatureFactory.getInstance().getClassSignature(classSignature);
//      this.refTypes.add(refType);
//      return refType;
//    }
//    return op.get();
//  }

  @Override
  public void addClass(@Nonnull AbstractClass klass) {
    this.classes.put(klass.getSignature(), klass);
  }

  @Override
  @Nonnull
  public Collection<AbstractClass> getClasses() {
    return classes.values();
  }

  @Override
  @Nonnull
  public Stream<AbstractClass> classes() {
    return this.classes.values().stream();
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
}
