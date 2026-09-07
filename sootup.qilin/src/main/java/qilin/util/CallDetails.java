package qilin.util;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ContextAllocNode;
import qilin.core.pag.ContextVarNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PagNode;
import qilin.util.collect.twokeymultimap.TwoKeyMultiHashMap;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

/**
 * Tracks per-callsite call relationships and contexts, mainly for the MOON debloating approach and
 * Zipper's optimized {@code PotentialContextElement}. One instance lives per {@link
 * qilin.core.PTAScene} (see {@code PTAScene#getCallDetails()}) rather than as a JVM-global
 * singleton, so independent/concurrent PTA runs never share this state.
 */
public class CallDetails {
  public static final Object STATIC_OBJ_CTX = new Object();

  private final SetMultimap<SootMethod, Pair<Object, SootMethod>> calleeToContextAndCaller =
      HashMultimap.create();
  private final Table<SootMethod, SootMethod, Set<Object>> callerCalleeToRecvObj =
      HashBasedTable.create();
  private final SetMultimap<SootMethod, SootMethod> callerToCallee = HashMultimap.create();
  private final SetMultimap<LocalVarNode, AbstractInstanceInvokeExpr> recvToInvokeExprs =
      HashMultimap.create();
  private final TwoKeyMultiHashMap<LocalVarNode, LocalVarNode, Value> argToParamToRecvValue =
      new TwoKeyMultiHashMap<>();

  private boolean enabled = false;
  private boolean initialized = false;

  public void enable() {
    enabled = true;
    initialized = true;
  }

  public void disable() {
    enabled = false;
  }

  public void addCalleeToContextAndCaller(SootMethod callee, Object ctx, SootMethod caller) {
    if (!enabled) return;
    callerToCallee.put(caller, callee);
    if (ctx instanceof AllocNode || ctx.equals(STATIC_OBJ_CTX)) {
      if (ctx instanceof ContextAllocNode contextAllocNode) {
        ctx = contextAllocNode.base();
      }
      calleeToContextAndCaller.put(callee, new Pair<>(ctx, caller));
      if (!callerCalleeToRecvObj.contains(caller, callee)) {
        callerCalleeToRecvObj.put(caller, callee, new HashSet<>());
      }
      callerCalleeToRecvObj.get(caller, callee).add(ctx);
    } else {
      throw new RuntimeException("Unknown context type");
    }
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new RuntimeException("CallDetails is not initialized");
    }
  }

  public Collection<Pair<Object, SootMethod>> usageContextAndCallerOf(SootMethod callee) {
    checkInitialized();
    return calleeToContextAndCaller.get(callee);
  }

  public void addInvokeExpr(LocalVarNode receiver, AbstractInstanceInvokeExpr invokeExpr) {
    if (!enabled) return;
    recvToInvokeExprs.put(receiver, invokeExpr);
  }

  public void addArgToParamToRecvValue(PagNode arg, PagNode param, Stmt callStmt) {
    if (!enabled) return;
    Value recvValue = getBase(callStmt);
    if (recvValue == null) {
      return;
    }
    if (arg instanceof ContextVarNode argContextVarNode) {
      arg = argContextVarNode.base();
    }
    if (param instanceof ContextVarNode paramContextVarNode) {
      param = paramContextVarNode.base();
    }
    LocalVarNode argVar = (LocalVarNode) arg;
    LocalVarNode paramVar = (LocalVarNode) param;
    if (!argToParamToRecvValue.containsKey(argVar, paramVar)
        || argToParamToRecvValue.get(argVar, paramVar).size() < 2) {
      argToParamToRecvValue.put(argVar, paramVar, recvValue);
    }
  }

  /** The receiver of an instance call statement, or {@code null} for a static call. */
  private Value getBase(Stmt callStmt) {
    AbstractInvokeExpr invokeExpr;
    if (callStmt instanceof JInvokeStmt invokeStmt) {
      invokeExpr = invokeStmt.getInvokeExpr().get();
    } else if (callStmt instanceof JAssignStmt assignStmt) {
      invokeExpr = assignStmt.getInvokeExpr().get();
    } else {
      throw new RuntimeException("Unsupported callStmt type.");
    }
    if (invokeExpr instanceof AbstractInstanceInvokeExpr instanceInvokeExpr) {
      return instanceInvokeExpr.getBase();
    }
    return null;
  }

  public Set<Value> getRecvValueOfArgAndParam(LocalVarNode arg, LocalVarNode param) {
    checkInitialized();
    return argToParamToRecvValue.get(arg, param);
  }

  public Set<SootMethod> getCalleesOf(SootMethod caller) {
    checkInitialized();
    return callerToCallee.get(caller);
  }
}
