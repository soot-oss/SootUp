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

package qilin.core;

import qilin.core.context.Context;
import qilin.core.pag.*;
import qilin.core.solver.Propagator;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.heapabst.HeapAbstractor;
import qilin.parm.select.CtxSelector;
import sootup.core.model.SootMethod;

/*
 * This represents a parameterized PTA which could be concreted to many pointer analyses.
 * */
public abstract class CorePTA extends PTA {
  /*
   * The following three parameterized functions must be initialized before doing the pointer analysis.
   * */
  protected CtxConstructor ctxCons;
  protected CtxSelector ctxSel;
  protected HeapAbstractor heapAbst;

  public CorePTA(PTAScene scene) {
    super(scene);
  }

  public CtxSelector ctxSelector() {
    return ctxSel;
  }

  public void setContextSelector(CtxSelector ctxSelector) {
    this.ctxSel = ctxSelector;
  }

  public CtxConstructor ctxConstructor() {
    return ctxCons;
  }

  public HeapAbstractor heapAbstractor() {
    return this.heapAbst;
  }

  public abstract Propagator getPropagator();

  @Override
  public Context createCalleeCtx(
      ContextMethod caller, AllocNode receiverNode, CallSite callSite, SootMethod target) {
    return ctxCons.constructCtx(caller, (ContextAllocNode) receiverNode, callSite, target);
  }

  public Context emptyContext() {
    return CtxConstructor.emptyContext;
  }

  @Override
  public Node parameterize(Node n, Context context) {
    if (context == null) {
      throw new RuntimeException("null context!!!");
    }
    if (n instanceof LocalVarNode) {
      LocalVarNode lvn = (LocalVarNode) n;
      return parameterize(lvn, context);
    }
    if (n instanceof FieldRefNode) {
      FieldRefNode frn = (FieldRefNode) n;
      return parameterize(frn, context);
    }
    if (n instanceof AllocNode) {
      AllocNode an = (AllocNode) n;
      return parameterize(an, context);
    }
    if (n instanceof FieldValNode) {
      FieldValNode fvn = (FieldValNode) n;
      return parameterize(fvn, context);
    }
    if (n instanceof GlobalVarNode) {
      GlobalVarNode gvn = (GlobalVarNode) n;
      return pag.makeContextVarNode(gvn, emptyContext());
    }
    throw new RuntimeException("cannot parameterize this node: " + n);
  }

  public ContextField parameterize(FieldValNode fvn, Context context) {
    Context ctx = ctxSel.select(fvn, context);
    return pag.makeContextField(ctx, fvn);
  }

  protected ContextVarNode parameterize(LocalVarNode vn, Context context) {
    Context ctx = ctxSel.select(vn, context);
    return pag.makeContextVarNode(vn, ctx);
  }

  protected FieldRefNode parameterize(FieldRefNode frn, Context context) {
    return pag.makeFieldRefNode((VarNode) parameterize(frn.getBase(), context), frn.getField());
  }

  protected ContextAllocNode parameterize(AllocNode node, Context context) {
    Context ctx = ctxSel.select(node, context);
    return pag.makeContextAllocNode(node, ctx);
  }

  /** Finds or creates the ContextMethod for method and context. */
  @Override
  public ContextMethod parameterize(SootMethod method, Context context) {
    Context ctx = ctxSel.select(method, context);
    return pag.makeContextMethod(ctx, method);
  }

  public AllocNode getRootNode() {
    return rootNode;
  }
}
