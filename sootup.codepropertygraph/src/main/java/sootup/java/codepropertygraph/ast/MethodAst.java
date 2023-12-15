package sootup.java.codepropertygraph.ast;

import java.util.List;
import java.util.Set;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

public class MethodAst {
  private final String name;
  private final Set<MethodModifier> modifiers;
  private final List<Type> parameterTypes;
  private final List<Stmt> bodyStmts;
  private final Type returnType;
  private final Body body;

  public MethodAst(SootMethod method) {
    body = method.getBody();
    name = method.getName();
    modifiers = method.getModifiers();
    parameterTypes = method.getParameterTypes();
    bodyStmts = method.getBody().getStmts();
    returnType = method.getReturnType();
  }

  public Body getBody() {
    return body;
  }

  public String getName() {
    return name;
  }

  public Set<MethodModifier> getModifiers() {
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
