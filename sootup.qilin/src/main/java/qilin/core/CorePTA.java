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

import java.util.Objects;
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
public abstract class CorePTA extends PTA implements Parameterizer {
  private CtxConstructor ctxCons;
  private CtxSelector ctxSel;
  private HeapAbstractor heapAbst;
  private boolean componentsInitialized = false;

  public CorePTA(PTAScene scene) {
    super(scene);
  }

  /**
   * Wires up the three context-sensitivity policies this analysis needs, atomically. Must be called
   * exactly once, from the concrete subclass's constructor, right after {@code super(scene)}
   * returns. Grouping all three into a single call -- instead of three independent {@code
   * protected} field assignments a subclass constructor could partially forget -- turns a missing
   * policy into an immediate, descriptive failure here instead of a {@code NullPointerException}
   * surfacing later, deep inside the solver.
   */
  protected final void initComponents(
      CtxConstructor ctxCons, CtxSelector ctxSel, HeapAbstractor heapAbst) {
    if (componentsInitialized) {
      throw new IllegalStateException("initComponents() has already been called for " + this);
    }
    this.ctxCons = Objects.requireNonNull(ctxCons, "ctxCons");
    this.ctxSel = Objects.requireNonNull(ctxSel, "ctxSel");
    this.heapAbst = Objects.requireNonNull(heapAbst, "heapAbst");
    this.componentsInitialized = true;
  }

  private void checkComponentsInitialized() {
    if (!componentsInitialized) {
      throw new IllegalStateException(
          "context-sensitivity components not initialized: the "
              + getClass().getSimpleName()
              + " constructor must call initComponents(...) before this analysis can be used");
    }
  }

  public CtxSelector ctxSelector() {
    checkComponentsInitialized();
    return ctxSel;
  }

  public void setContextSelector(CtxSelector ctxSelector) {
    checkComponentsInitialized();
    this.ctxSel = Objects.requireNonNull(ctxSelector, "ctxSelector");
  }

  public CtxConstructor ctxConstructor() {
    checkComponentsInitialized();
    return ctxCons;
  }

  public HeapAbstractor heapAbstractor() {
    checkComponentsInitialized();
    return heapAbst;
  }

  public abstract Propagator getPropagator();

  @Override
  public Context createCalleeCtx(
      ContextMethod caller, AllocNode receiverNode, CallSite callSite, SootMethod target) {
    return ctxConstructor().constructCtx(caller, (ContextAllocNode) receiverNode, callSite, target);
  }

  public Context emptyContext() {
    return CtxConstructor.emptyContext;
  }

  /**
   * Dispatches to the matching {@code parameterize} overload via double dispatch on {@code n}'s
   * runtime type (see {@link PagNode#parameterize(Parameterizer, Context)}), instead of an {@code
   * instanceof} cascade that a new {@link PagNode} subtype could silently fall through.
   */
  @Override
  public PagNode parameterize(PagNode n, Context context) {
    if (context == null) {
      throw new RuntimeException("null context!!!");
    }
    return n.parameterize(this, context);
  }

  @Override
  public ContextField parameterize(FieldValNode fvn, Context context) {
    Context ctx = ctxSelector().select(fvn, context);
    return pag.makeContextField(ctx, fvn);
  }

  @Override
  public ContextVarNode parameterize(LocalVarNode vn, Context context) {
    Context ctx = ctxSelector().select(vn, context);
    return pag.makeContextVarNode(vn, ctx);
  }

  @Override
  public FieldRefNode parameterize(FieldRefNode frn, Context context) {
    return pag.makeFieldRefNode((VarNode) parameterize(frn.getBase(), context), frn.getField());
  }

  @Override
  public ContextAllocNode parameterize(AllocNode node, Context context) {
    Context ctx = ctxSelector().select(node, context);
    return pag.makeContextAllocNode(node, ctx);
  }

  /**
   * Global variables are never context-sensitive, so they always parameterize to the empty context.
   */
  @Override
  public ContextVarNode parameterize(GlobalVarNode gvn, Context context) {
    return pag.makeContextVarNode(gvn, emptyContext());
  }

  /** Finds or creates the ContextMethod for method and context. */
  @Override
  public ContextMethod parameterize(SootMethod method, Context context) {
    Context ctx = ctxSelector().select(method, context);
    return pag.makeContextMethod(ctx, method);
  }

  public AllocNode getRootNode() {
    return rootNode;
  }
}
