package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.*;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.Type;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class TypeResolver {

  private final Body.BodyBuilder builder;

  // A list to store all AssignStmts and IdentifyStmts in Body
  private final List<AbstractDefinitionStmt> assignments;

  public TypeResolver(Body.BodyBuilder builder) {
    this.builder = builder;
    assignments = new ArrayList<>();
    for (Stmt stmt : builder.getStmts()) {
      if (stmt instanceof AbstractDefinitionStmt) {}
    }
  }

  //
  private Type evaluateType(@Nonnull Value value) {

    Type evaluatedType = null;

    if (value instanceof IdentityRef) {
      evaluatedType = value.getType();
    } else if (value instanceof ConcreteRef) {
      if (value instanceof JFieldRef) {
        evaluatedType = value.getType();
      } else { // value instanceof JArrayRef
        // todo: understand the class Typing and create it
      }
    }

    return evaluatedType;
  }
}
