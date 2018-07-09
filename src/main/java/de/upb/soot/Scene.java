package de.upb.soot;

import de.upb.soot.core.SootClass;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;

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

  public RefType getRefTypeUnsafe(String className) {
    // TODO Auto-generated method stub
    return null;
  }

  public RefType getOrAddRefType(RefType rt) {
    // TODO Auto-generated method stub
    return null;
  }

  public RefType getObjectType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String quotedNameOf(String s)
  { // TODO Auto-generated method stub
    return null;
  }

}
