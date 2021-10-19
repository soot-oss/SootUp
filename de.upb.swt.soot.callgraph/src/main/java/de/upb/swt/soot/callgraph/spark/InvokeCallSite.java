package de.upb.swt.soot.callgraph.spark;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;

public class InvokeCallSite extends AbstractCallSite {
  public static final int MUST_BE_NULL = 0;
  public static final int MUST_NOT_BE_NULL = 1;
  public static final int MAY_BE_NULL = -1;

  private final AbstractInstanceInvokeExpr iie;
  private final Local argArray;
  private final Local base;
  private final int nullnessCode;
  private final ArrayTypes reachingTypes;

  public InvokeCallSite(
      Stmt stmt, SootMethod container, AbstractInstanceInvokeExpr iie, Local base) {
    this(stmt, container, iie, base, (Local) null, 0);
  }

  public InvokeCallSite(
      Stmt stmt,
      SootMethod container,
      AbstractInstanceInvokeExpr iie,
      Local base,
      Local argArray,
      int nullnessCode) {
    super(stmt, container);
    this.iie = iie;
    this.base = base;
    this.argArray = argArray;
    this.nullnessCode = nullnessCode;
    this.reachingTypes = null;
  }

  public InvokeCallSite(
      Stmt stmt,
      SootMethod container,
      AbstractInstanceInvokeExpr iie,
      Local base,
      ArrayTypes reachingArgTypes,
      int nullnessCode) {
    super(stmt, container);
    this.iie = iie;
    this.base = base;
    this.argArray = null;
    this.nullnessCode = nullnessCode;
    this.reachingTypes = reachingArgTypes;
  }

  /** @deprecated use {@link #getStmt()} */
  @Deprecated
  public Stmt stmt() {
    return stmt;
  }

  /** @deprecated use {@link #getContainer()} */
  @Deprecated
  public SootMethod container() {
    return container;
  }

  public InstanceInvokeExpr iie() {
    return iie;
  }

  public Local base() {
    return base;
  }

  public Local argArray() {
    return argArray;
  }

  public int nullnessCode() {
    return nullnessCode;
  }

  public ArrayTypes reachingTypes() {
    return reachingTypes;
  }

  @Override
  public String toString() {
    return stmt.toString();
  }
}
