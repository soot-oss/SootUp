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
import de.upb.soot.util.Numberer;
import de.upb.soot.util.StringNumberer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The Class View.
 * 
 * @author Linghui Luo created on 31.07.2018
 */
public class View extends AbstractView {
  /**
   * a static map to store the RefType of each class according to its name. RefType of each class should just have one
   * instance.
   */
  private static Map<String, RefType> nameToClass = new HashMap<String, RefType>();

  /**
   * Instantiates a new view.
   */
  public View(Project project) {
    super(project);
    RefType.setView(this);// set the view for RefType
  }

  /**
   * Gets the RefType instance for given class name.
   *
   * @param className
   *          the class name
   * @return the RefType instance with the given class name if exists, otherwise return null.
   */
  public RefType getRefType(String className) {
    return nameToClass.get(className);
  }

  /**
   * Adds the RefType instance created for the given class name to {@link View#nameToClass}.
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
    // TODO Auto-generated method stub
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
  public StringNumberer getSubSigNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addRefType(RefType refType) {
    // TODO Auto-generated method stub

  }

  @Override
  public List<SootField> getClassNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String quotedNameOf(String name) {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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

}
