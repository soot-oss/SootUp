/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util;

import java.util.Iterator;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.context.Context;
import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ConcreteField;
import qilin.core.pag.ContextAllocNode;
import qilin.core.pag.ContextField;
import qilin.core.pag.ContextVarNode;
import qilin.core.pag.FieldRefNode;
import qilin.core.pag.GlobalVarNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.MethodParameter;
import qilin.core.pag.PAG;
import qilin.core.pag.PagNode;
import qilin.core.pag.VarNode;
import qilin.util.sets.PointsToSet;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;

/** Small queries and printing helpers over PAG nodes and points-to sets. */
public final class PagQueries {
  private PagQueries() {}

  /** Unwraps a PAG node down to the sootup/qilin value it represents. */
  public static Object getIR(Object sparkNode) {
    if (sparkNode instanceof VarNode) {
      return ((VarNode) sparkNode).getVariable();
    } else if (sparkNode instanceof AllocNode) {
      return ((AllocNode) sparkNode).getNewExpr();
    } else { // sparkField?
      if (sparkNode instanceof ConcreteField) {
        return ((ConcreteField) sparkNode).getField();
      } else { // ArrayElement
        return sparkNode;
      }
    }
  }

  public static boolean mustAlias(PTA pta, VarNode v1, VarNode v2) {
    PointsToSet v1pts = pta.reachingObjects(v1).toCIPointsToSet();
    PointsToSet v2pts = pta.reachingObjects(v2).toCIPointsToSet();
    return v1pts.pointsToSetEquals(v2pts);
  }

  public static void printPts(PTA pta, PointsToSet pts) {
    final StringBuffer ret = new StringBuffer();
    for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
      AllocNode n = it.next();
      ret.append("\t").append(n).append("\n");
    }
    System.out.print(ret);
  }

  public static String getNodeLabel(PagNode node) {
    int num = node.getNumber();
    if (node instanceof LocalVarNode) return "L" + num;
    else if (node instanceof ContextVarNode) {
      return "L" + num;
    } else if (node instanceof GlobalVarNode) return "G" + num;
    else if (node instanceof ContextField) return "OF" + num;
    else if (node instanceof FieldRefNode) return "VF" + num;
    else if (node instanceof AllocNode) return "O" + num;
    else throw new RuntimeException("no such node type exists!");
  }

  /** Extends heap's context by one more element (heap itself), for object-sensitive analyses. */
  public static Context plusplusOp(AllocNode heap) {
    ContextElement[] array;
    int s;
    if (heap instanceof ContextAllocNode) {
      ContextAllocNode csHeap = (ContextAllocNode) heap;
      ContextElements ctxElems = (ContextElements) csHeap.context();
      int ms = ctxElems.size();
      ContextElement[] oldArray = ctxElems.getElements();
      array = new ContextElement[ms + 1];
      array[0] = csHeap.base();
      System.arraycopy(oldArray, 0, array, 1, ms);
      s = ms + 1;
    } else {
      array = new ContextElement[1];
      array[0] = heap;
      s = 1;
    }
    return new ContextElements(array, s);
  }

  /**
   * Given a callee-side parameter/this/return/throw node {@code pi}, returns the corresponding
   * caller-side argument node at the call site {@code invokeStmt}.
   */
  public static LocalVarNode paramToArg(
      PAG pag, InvokableStmt invokeStmt, MethodPAG srcmpag, VarNode pi) {
    MethodNodeFactory srcnf = srcmpag.nodeFactory();
    AbstractInvokeExpr ie = invokeStmt.getInvokeExpr().get();
    MethodParameter mPi = (MethodParameter) pi.getVariable();
    LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
    LocalVarNode receiver;
    if (ie instanceof AbstractInstanceInvokeExpr) {
      AbstractInstanceInvokeExpr iie = (AbstractInstanceInvokeExpr) ie;
      Local base = iie.getBase();
      receiver = pag.findLocalVarNode(srcmpag.getMethod(), base, base.getType());
    } else {
      // static call
      receiver = thisRef;
    }
    if (mPi.isThis()) {
      return receiver;
    } else if (mPi.isReturn()) {
      if (invokeStmt instanceof JAssignStmt) {
        JAssignStmt assignStmt = (JAssignStmt) invokeStmt;
        Value mR = assignStmt.getLeftOp();
        return (LocalVarNode) pag.findValNode(mR, srcmpag.getMethod());
      } else {
        return null;
      }
    } else if (mPi.isThrowRet()) {
      return srcnf.makeInvokeStmtThrowVarNode(invokeStmt, srcmpag.getMethod());
    }
    // Normal formal parameters.
    Value arg = ie.getArg(mPi.getIndex());
    if (arg == null) {
      return null;
    } else {
      return pag.findLocalVarNode(srcmpag.getMethod(), arg, arg.getType());
    }
  }
}
