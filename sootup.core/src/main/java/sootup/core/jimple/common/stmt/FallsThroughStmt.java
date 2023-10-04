package sootup.core.jimple.common.stmt;



/** as an equivalent to BranchingStmt */
public interface FallsThroughStmt extends Stmt {

  // has to return true in subclasses!
  // hint: this class can't be abstract and method final because of JIfStmt which would need
  // FallsThrough and BranchingStmt as parent.
  default boolean fallsThrough() {
    return true;
  }
}
