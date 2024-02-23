package sootup.java.codepropertygraph.ddg;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VariableDefinition that = (VariableDefinition) o;
    return Objects.equals(value, that.value) && Objects.equals(stmt, that.stmt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, stmt);
  }
}
