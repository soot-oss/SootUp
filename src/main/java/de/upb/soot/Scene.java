package de.upb.soot;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.util.Chain;
import de.upb.soot.util.Numberer;
import de.upb.soot.util.StringNumberer;

import java.util.Collection;
import java.util.List;

public class Scene {
  private Scene() {

  }

  public static Scene getInstance() {
    return new Scene();
  }

  public List<Type> getTypeNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Local> getLocalNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public SootClass getSootClass(String readObject) {
    // TODO Auto-generated method stub
    return null;
  }

  public RefType getObjectType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String quotedNameOf(String s) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean doneResolving() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean allowsPhantomRefs() {
    // TODO Auto-generated method stub
    return false;
  }

  public Numberer<Object> getFieldNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public StringNumberer getSubSigNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getClassNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<Object> getApplicationClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Chain<SootClass> getContainingChain(SootClass sootClass) {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getLibraryClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getPhantomClasses() {
    // TODO Auto-generated method stub
    return null;

  }

  public void addRefType(RefType refType) {
    // TODO Auto-generated method stub
    return;

  }

  public Numberer<SootMethod> getMethodNumberer() {
    // TODO Auto-generated method stub
    return null;

  }

}