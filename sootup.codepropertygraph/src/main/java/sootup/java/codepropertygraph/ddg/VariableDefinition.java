package sootup.java.codepropertygraph.ddg;

import java.util.Optional;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;

public class VariableDefinition {
  private final Value value;
  private final Stmt stmt;

  public VariableDefinition(Value value, Stmt stmt) {
    this.value = value;
    this.stmt = stmt;
  }

  public Value getValue() {
    return value;
  }

  public Optional<Stmt> getStmt() {
    return Optional.ofNullable(stmt);
  }
}
