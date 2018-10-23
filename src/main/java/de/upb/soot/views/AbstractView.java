package de.upb.soot.views;

import de.upb.soot.Options;
import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.typehierarchy.ITypeHierarchy;

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

  // TODO. change it
  public StuffAViewNeeds stuffAViewNeeds;

  protected Project project;
  protected Options options;

  protected Set<RefType> refTypes;
  protected Map<ISignature, AbstractClass> classes;
  protected Set<String> reservedNames;

  public AbstractView(Project project) {
    this.project = project;
    this.options = new Options();
    setReservedNames();
    this.refTypes = new HashSet<>();
    this.classes = new HashMap<>();
  }

  @Override
  public RefType getRefType(TypeSignature classSignature) {
    Optional<RefType> op
        = this.refTypes.stream().filter(r -> r.getTypeSignature().equals(classSignature.toString())).findFirst();
    if (!op.isPresent()) {
      RefType refType = new RefType(this, classSignature);
      this.refTypes.add(refType);
      return refType;
    }
    return op.get();
  }

  @Override
  public void addClass(AbstractClass klass) {
    this.classes.put(klass.getSignature(), klass);
  }

  @Override
  public Collection<AbstractClass> getClasses() {
    return classes.values();
  }

  @Override
  public Stream<AbstractClass> classes() {
    return this.classes.values().stream();
  }

  @Override
  public Optional<AbstractClass> getClass(ISignature signature) {
    // we can also implement this by resolving none-exist class in view on demand in future.
    return this.classes().filter(c -> c.getClassSource().getClassSignature().equals(signature)).findFirst();
  }

  @Override
  public ICallGraph createCallGraph() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ICallGraph createCallGraph(ICallGraphAlgorithm algorithm) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITypeHierarchy createTypeHierarchy() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<Scope> getScope() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean doneResolving() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String quotedNameOf(String s) {
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
  public Options getOptions() {
    return this.options;
  }
}
