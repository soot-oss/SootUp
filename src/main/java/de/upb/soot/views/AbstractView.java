package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.typehierarchy.ITypeHierarchy;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Abstract class for view.
 * 
 * @author Linghui Luo
 *
 */
public abstract class AbstractView implements IView {

  protected final @Nonnull Project project;
  protected Options options;

  protected Set<RefType> refTypes;
  protected Map<ISignature, AbstractClass> classes;
  protected Set<String> reservedNames;

  public AbstractView(@Nonnull Project project) {
    this.project = project;
    this.options = new Options();
    setReservedNames();
    this.refTypes = new HashSet<>();
    this.classes = new HashMap<>();
  }

  @Override
  public @Nonnull SignatureFactory getSignatureFactory() {
    return this.project.getSignatureFactory();
  }

  @Override
  public @Nonnull RefType getRefType(@Nonnull TypeSignature classSignature) {
    Optional<RefType> op = this.refTypes.stream().filter(r -> r.getTypeSignature().equals(classSignature)).findFirst();
    if (!op.isPresent()) {
      RefType refType = new RefType(this, classSignature);
      this.refTypes.add(refType);
      return refType;
    }
    return op.get();
  }

  @Override
  public void addClass(@Nonnull AbstractClass klass) {
    this.classes.put(klass.getSignature(), klass);
  }

  @Override
  public @Nonnull Collection<AbstractClass> getClasses() {
    return classes.values();
  }

  @Override
  public @Nonnull Stream<AbstractClass> classes() {
    return this.classes.values().stream();
  }

  @Override
  public @Nonnull ICallGraph createCallGraph() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public @Nonnull ICallGraph createCallGraph(ICallGraphAlgorithm algorithm) {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public @Nonnull ITypeHierarchy createTypeHierarchy() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public @Nonnull Optional<Scope> getScope() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public boolean doneResolving() {
    // TODO Auto-generated methodRef stub
    return false;
  }

  @Override
  public @Nonnull String quotedNameOf(@Nonnull String s) {
    // Pre-check: Is there a chance that we need to escape something?
    // If not, skip the transformation altogether.
    boolean found = s.contains("-");
    for (String token : reservedNames) {
      if (s.contains(token)) {
        found = true;
        break;
      }
    }
    if (!found) {
      return s;
    }

    StringBuilder res = new StringBuilder(s.length());
    for (String part : s.split("\\.")) {
      if (res.length() > 0) {
        res.append('.');
      }
      if (part.startsWith("-") || reservedNames.contains(part)) {
        res.append('\'');
        res.append(part);
        res.append('\'');
      } else {
        res.append(part);
      }
    }
    return res.toString();
  }

  /**
   * Set a list of reserved names. The list can be different in different programming languages.
   */
  protected abstract void setReservedNames();

  @Override
  public @Nonnull Options getOptions() {
    return this.options;
  }
}
