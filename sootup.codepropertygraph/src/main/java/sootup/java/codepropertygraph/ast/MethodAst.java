package sootup.java.codepropertygraph.ast;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Modifier;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

public class MethodAst {
  private final String name;
  private final Set<Modifier> modifiers;
  private final List<Type> parameterTypes;
  private final List<Stmt> bodyStmts;
  private final Type returnType;

  public MethodAst(SootMethod method) {
    name = method.getName();
    modifiers = method.getModifiers();
    parameterTypes = method.getParameterTypes();
    bodyStmts = method.getBody().getStmts();
    returnType = method.getReturnType();
  }

  public String getName() {
    return name;
  }

  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

  public List<Stmt> getBodyStmts() {
    return bodyStmts;
  }

  public Type getReturnType() {
    return returnType;
  }
}
