package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.signatures.MethodSignature;

/** Method Signature with its calling CallGraphEdgeType and sourceStmt that invokes the call */
public class CalleeMethodSignature {

  private CallGraphEdgeType edgeType;
  private MethodSignature methodSignature;
  private Local baseObject = null;

  /**
   * The unit at which the call occurs; may be null for calls not occurring at a specific statement
   * (eg. calls in native code)
   */
  private Stmt sourceStmt;

  public CalleeMethodSignature(
      MethodSignature methodSignature, CallGraphEdgeType edgeType, Stmt sourceStmt) {
    this.methodSignature = methodSignature;
    this.edgeType = edgeType;
    this.sourceStmt = sourceStmt;
    if (sourceStmt instanceof JInvokeStmt) {
      JInvokeStmt invokeStmt = (JInvokeStmt) sourceStmt;
      AbstractInvokeExpr abstractInvokeExpr = invokeStmt.getInvokeExpr();
      // todo others
      if (abstractInvokeExpr instanceof AbstractInstanceInvokeExpr) {
        AbstractInstanceInvokeExpr abstractInstanceInvokeExpr =
            (AbstractInstanceInvokeExpr) abstractInvokeExpr;

        baseObject = abstractInstanceInvokeExpr.getBase();
      } else if (abstractInvokeExpr instanceof JStaticInvokeExpr) {
        JStaticInvokeExpr jStaticInvokeExpr = (JStaticInvokeExpr) abstractInvokeExpr;
        // baseObject = (Local) jStaticInvokeExpr.getArg(0);
      }
    }
  }

  public Local getBaseObject() {
    System.out.println("base object is " + baseObject);
    System.out.println(sourceStmt.getInvokeExpr());
    return baseObject;
  }

  public MethodSignature getMethodSignature() {
    return methodSignature;
  }

  public CallGraphEdgeType getEdgeType() {
    return edgeType;
  }

  public Stmt getSourceStmt() {
    return sourceStmt;
  }

  @Override
  public String toString() {
    return "CalleeMethodSignature{"
        + "edgeType="
        + edgeType
        + ", methodSignature="
        + methodSignature
        + ", sourceStmt="
        + sourceStmt
        + '}';
  }
}
