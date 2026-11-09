package sootup.interceptors.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Code replacing one statement: a sequence of statements with forward jumps to {@link Label}s. A
 * label bound after the last statement continues with the replaced statement's successor. All
 * statements get the replaced statement's position and exceptional flows.
 */
public final class Fragment {

  /** A jump target, {@link #bind bound} to the next statement emitted. */
  public static final class Label {
    private Label() {}
  }

  private final View view;
  private final LocalGenerator localGenerator;
  private final StmtPositionInfo pos;
  private final List<Stmt> stmts = new ArrayList<>();
  private final List<Local> locals = new ArrayList<>();
  private final Map<Label, Integer> bound = new IdentityHashMap<>();
  private final Map<BranchingStmt, Label> jumps = new IdentityHashMap<>();

  private Fragment(View view, LocalGenerator localGenerator, StmtPositionInfo pos) {
    this.view = view;
    this.localGenerator = localGenerator;
    this.pos = pos;
  }

  /** An empty fragment for a statement at {@code pos}; locals come from {@code localGenerator}. */
  @NonNull
  public static Fragment of(
      @NonNull View view, @NonNull LocalGenerator localGenerator, @NonNull StmtPositionInfo pos) {
    return new Fragment(view, localGenerator, pos);
  }

  @NonNull
  public IdentifierFactory factory() {
    return view.getIdentifierFactory();
  }

  @NonNull
  public ClassType classType(@NonNull String fullyQualifiedName) {
    return factory().getClassType(fullyQualifiedName);
  }

  /** A fresh local; added to the body if this fragment is used. */
  @NonNull
  public Local newLocal(@NonNull Type type) {
    Local local = localGenerator.generateLocal(type);
    locals.add(local);
    return local;
  }

  public void assign(@NonNull LValue target, @NonNull Value value) {
    stmts.add(Jimple.newAssignStmt(target, value, pos));
  }

  public void invoke(@NonNull AbstractInvokeExpr call) {
    stmts.add(Jimple.newInvokeStmt(call, pos));
  }

  @NonNull
  public Label newLabel() {
    return new Label();
  }

  /** {@code label} denotes the next statement emitted (or the successor, if none follows). */
  public void bind(@NonNull Label label) {
    bound.put(label, stmts.size());
  }

  public void ifGoto(@NonNull AbstractConditionExpr condition, @NonNull Label target) {
    JIfStmt stmt = Jimple.newIfStmt(condition, pos);
    jumps.put(stmt, target);
    stmts.add(stmt);
  }

  public void jump(@NonNull Label target) {
    JGotoStmt stmt = Jimple.newGotoStmt(pos);
    jumps.put(stmt, target);
    stmts.add(stmt);
  }

  /** {@code base.name(args)}: an interface or virtual invoke, as {@code declaringType} demands. */
  @NonNull
  public AbstractInvokeExpr instanceCall(
      @NonNull Local base,
      @NonNull ClassType declaringType,
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull List<Type> parameterTypes,
      @NonNull List<Immediate> args) {
    var sig = factory().getMethodSignature(declaringType, name, returnType, parameterTypes);
    boolean isInterface = view.getClass(declaringType).map(SootClass::isInterface).orElse(false);
    return isInterface
        ? Jimple.newInterfaceInvokeExpr(base, sig, args)
        : Jimple.newVirtualInvokeExpr(base, sig, args);
  }

  @NonNull
  public JSpecialInvokeExpr specialCall(
      @NonNull Local base,
      @NonNull ClassType declaringType,
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull List<Type> parameterTypes,
      @NonNull List<Immediate> args) {
    return Jimple.newSpecialInvokeExpr(
        base, factory().getMethodSignature(declaringType, name, returnType, parameterTypes), args);
  }

  @NonNull
  public JStaticInvokeExpr staticCall(
      @NonNull ClassType declaringType,
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull List<Type> parameterTypes,
      @NonNull List<Immediate> args) {
    return Jimple.newStaticInvokeExpr(
        factory().getMethodSignature(declaringType, name, returnType, parameterTypes), args);
  }

  /**
   * Puts this fragment in place of {@code replaced}, a statement with exactly one successor, and
   * adds its locals to {@code builder}.
   */
  public void replace(Body.@NonNull BodyBuilder builder, @NonNull Stmt replaced) {
    if (stmts.isEmpty() || !(stmts.get(0) instanceof FallsThroughStmt first)) {
      throw new IllegalStateException("a fragment must start with a non-branching statement");
    }
    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    Stmt next = cfg.successors(replaced).get(0);
    var exceptional = cfg.exceptionalSuccessors(replaced);
    // keeps the predecessors and exceptional flows; next gets reconnected below
    cfg.replaceNode(replaced, first);
    cfg.removeEdge(first, next);
    stmts.subList(1, stmts.size()).forEach(stmt -> cfg.addNode(stmt, exceptional));
    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      Stmt fallThrough = i + 1 < stmts.size() ? stmts.get(i + 1) : next;
      if (stmt instanceof JIfStmt ifStmt) {
        cfg.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, fallThrough);
        cfg.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, target(ifStmt, next));
      } else if (stmt instanceof JGotoStmt gotoStmt) {
        cfg.putEdge(gotoStmt, 0, target(gotoStmt, next));
      } else {
        cfg.putEdge((FallsThroughStmt) stmt, fallThrough);
      }
    }
    locals.forEach(builder::addLocal);
  }

  private Stmt target(BranchingStmt jump, Stmt next) {
    int index = bound.get(jumps.get(jump));
    return index < stmts.size() ? stmts.get(index) : next;
  }
}
