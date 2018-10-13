package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.Scope;
import de.upb.soot.callgraph.ICallGraph;
import de.upb.soot.callgraph.ICallGraphAlgorithm;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.typehierarchy.ITypeHierarchy;
import de.upb.soot.util.ArrayNumberer;
import de.upb.soot.util.Numberer;
import de.upb.soot.util.StringNumberer;

import java.util.HashMap;
import java.util.List;
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
  /**
   * a map to store the RefType of each class according to its name. RefType of each class should just have one instance.
   */
  protected Map<String, RefType> nameToClass;
  protected StringNumberer subSigNumberer;
  protected ArrayNumberer<SootMethod> methodNumber;
  protected Set<String> reservedNames;


  public AbstractView(Project project) {
    this.project = project;
    setReservedNames();
    this.nameToClass = new HashMap<>();
    this.subSigNumberer = new StringNumberer();
    this.methodNumber = new ArrayNumberer<>();
  }

  @Override
  public StringNumberer getSubSigNumberer() {
    return subSigNumberer;
  }

  @Override
  public void addRefType(RefType refType) {
    if (!nameToClass.containsKey(refType.getClassName())) {
      nameToClass.put(refType.getClassName(), refType);
    }
  }

  /**
   * Gets the RefType instance for given class name.
   *
   * @param className
   *          the class name
   * @return the RefType instance with the given class name if exists, otherwise return null.
   */
  @Override
  public RefType getRefType(String className) {
    return nameToClass.get(className);
  }

  /**
   * Adds the RefType instance created for the given class name to {@link JavaView#nameToClass}.
   *
   * @param className
   *          the class name
   * @param ref
   *          the RefType instance with the given class name
   */
  public void addRefType(String className, RefType ref) {
    if (!nameToClass.containsKey(className)) {
      nameToClass.put(className, ref);
    }
  }

  @Override
  public List<SootClass> getClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<SootClass> classes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<SootClass> getSootClass(ClassSignature signature) {
    return null;
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
  public List<SootField> getClassNumberer() {
    // TODO Auto-generated method stub
    return null;
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

  @Override
  public Numberer<SootField> getFieldNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean allowsPhantomRefs() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Numberer<SootMethod> getMethodNumberer() {
    return this.methodNumber;
  }

  @Override
  public List<Local> getLocalNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Numberer<Type> getTypeNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RefType getObjectType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootClass getSootClass(String className) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Set a list of reserved names. The list can be different in different programming languages.
   */
  protected abstract void setReservedNames();

}
