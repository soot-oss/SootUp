package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.NumericConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JCmpExpr;
import sootup.core.jimple.common.expr.JCmpgExpr;
import sootup.core.jimple.common.expr.JCmplExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JMulExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.expr.JOrExpr;
import sootup.core.jimple.common.expr.JRemExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.expr.JSubExpr;
import sootup.core.jimple.common.expr.JUshrExpr;
import sootup.core.jimple.common.expr.JXorExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.views.View;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable (definition)
 * that are independent of each other. This is necessary as the AsmMethodSource maps usages of the
 * same Local index to the same Local object, which can lead to wrong Jimple e.g. when a primitive
 * type gets merged with a reference-type and augmenting the type of the Local is less precise.
 *
 * <p>For example the code:
 *
 * <pre>
 *    l0 := @this Test
 *    l1 = 0
 *    l2 = 1
 *    l1 = l1 + 1
 *    l2 = l2 + 1
 *    return
 * </pre>
 *
 * <p>to:
 *
 * <pre>
 *    l0 := @this Test
 *    l1#0 = 0
 *    l2#0 = 1
 *    l1#1 = l1#0 + 1
 *    l2#1 = l2#0 + 1
 *    return
 * </pre>
 */
public class LocalSplitter implements BodyInterceptor {

  /**
   * Which Stmts can enter an exception handler. A Stmt that throws does not complete, so the
   * handler sees the value a local had before that Stmt; a Stmt that cannot enter the handler must
   * not carry its value there, or definitions whose values never reach the same read are merged
   * into one local, e.g. an int and a String kept in the same register.
   *
   * <p>Each mode lets more Stmts enter handlers than the one before it, so the walk from each
   * definition follows more exceptional edges and visits more Stmts. VIRTUAL_MACHINE_ERRORS also
   * looks up the superclasses of each handler type. Choose the narrowest mode that is right for the
   * platform the code runs on.
   */
  public enum ExceptionalFlow {
    /**
     * Only Stmts that can throw. Right where no asynchronous exceptions exist, as on ART: its
     * verifier merges only throwing instructions into handlers, and it does not support
     * Thread.stop.
     */
    SYNCHRONOUS,
    /**
     * In addition, every Stmt into handlers that can catch a VirtualMachineError, which the JVM may
     * throw at any point (JLS 11.1.3): handlers of Throwable, Error, VirtualMachineError,
     * catch-alls, and subclasses of VirtualMachineError.
     */
    VIRTUAL_MACHINE_ERRORS,
    /**
     * Every Stmt into every handler, e.g. for exceptions a debugger injects into a thread, or
     * Thread.stop(Throwable) on Java 7 and older.
     */
    ANY
  }

  private static final String THROWABLE = "java.lang.Throwable";
  private static final String ERROR = "java.lang.Error";
  private static final String VIRTUAL_MACHINE_ERROR = "java.lang.VirtualMachineError";

  /** The superclasses of VirtualMachineError. */
  private static final Set<String> ABOVE_VIRTUAL_MACHINE_ERROR =
      Set.of("java.lang.Object", THROWABLE, ERROR);

  /** Classes on Throwable's other branch, beside VirtualMachineError. */
  private static final Set<String> BESIDE_VIRTUAL_MACHINE_ERROR =
      Set.of("java.lang.Exception", "java.lang.RuntimeException");

  @NonNull private final ExceptionalFlow exceptionalFlow;

  /**
   * Splits locals following the Java semantics: see {@link ExceptionalFlow#VIRTUAL_MACHINE_ERRORS}.
   */
  public LocalSplitter() {
    this(ExceptionalFlow.VIRTUAL_MACHINE_ERRORS);
  }

  public LocalSplitter(@NonNull ExceptionalFlow exceptionalFlow) {
    this.exceptionalFlow = exceptionalFlow;
  }

  /**
   * Contains disjoint sets of nodes which are implemented as trees. Every set is represented by a
   * tree in the forest. Each set is identified by the root node of its tree, also known as its
   * representative.
   *
   * <p><a href="https://en.wikipedia.org/wiki/Disjoint-set_data_structure">Disjoint-set data
   * structure</a>
   */
  static class DisjointSetForest<T> {
    /** Every node points to its parent in its tree. Roots of trees point to themselves. */
    @NonNull private final Map<T, T> parent = new HashMap<>();

    /** Stores the size of a tree under the key. Only updated for roots of trees. */
    @NonNull private final Map<T, Integer> sizes = new HashMap<>();

    /**
     * Creates a new set that only contains the {@code node}. Does nothing when the forest already
     * contains the {@code node}.
     */
    void add(@NonNull T node) {
      if (parent.containsKey(node)) {
        return;
      }

      parent.put(node, node);
      sizes.put(node, 1);
    }

    /** Finds the representative of the set that contains the {@code node}. */
    @NonNull T find(T node) {
      T parentNode = parent.get(node);
      if (parentNode == null) {
        throw new IllegalArgumentException("The DisjointSetForest does not contain the node.");
      }

      T itNode = node;
      while (parentNode != itNode) {
        // Path Halving to get amortized constant operations
        T grandparent = parent.get(parentNode);
        parent.put(itNode, grandparent);

        itNode = grandparent;
        parentNode = parent.get(grandparent);
      }
      return itNode;
    }

    /**
     * Combines the sets of {@code first} and {@code second}. Returns the representative of the
     * combined set.
     */
    void union(@NonNull T first, @NonNull T second) {
      first = find(first);
      second = find(second);

      if (first == second) {
        return;
      }

      final Integer firstSize = sizes.get(first);
      final Integer secondSize = sizes.get(second);

      T smaller, larger;
      if (firstSize > secondSize) {
        larger = first;
        smaller = second;
      } else {
        larger = second;
        smaller = first;
      }

      // adding the smaller subtree to the larger tree keeps the tree flatter
      parent.put(smaller, larger);
      sizes.put(larger, firstSize + secondSize);
      sizes.remove(smaller);
    }

    boolean contains(@NonNull T node) {
      return parent.containsKey(node);
    }

    int getSetCount() {
      // `sizes` only contains values for the root of the trees,
      // so its size matches the total number of sets
      return sizes.size();
    }
  }

  static class PartialStmt {
    @NonNull final Stmt backingStmt;

    /**
     * Whether the partial statement refers to only the definitions of the inner statement or only
     * to the uses.
     */
    final boolean isDef;

    PartialStmt(@NonNull Stmt backingStmt, boolean isDef) {
      this.backingStmt = backingStmt;
      this.isDef = isDef;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      PartialStmt that = (PartialStmt) o;

      if (isDef != that.isDef) {
        return false;
      }
      return backingStmt.equals(that.backingStmt);
    }

    @Override
    public int hashCode() {
      int result = backingStmt.hashCode();
      result = 31 * result + (isDef ? 1 : 0);
      return result;
    }
  }

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();

    // Cache the stmts to not have to retrieve them for every local
    List<Stmt> stmts = graph.getStmts();
    // Maps every local to its assignment stmts.
    // Contains indices to the above list to reduce bookkeeping when modifying stmts.
    Map<Local, List<Integer>> assignmentsByLocal = groupAssignmentsByLocal(stmts);

    Set<Local> newLocals = new HashSet<>();

    // whether a handler's exception type can catch a VirtualMachineError, per type
    Map<ClassType, Boolean> catchesVirtualMachineErrors = new HashMap<>();

    final Set<Local> locals = builder.getLocals();
    for (Local local : locals) {
      // Use a disjoint set while walking the statement graph to union all uses of the local that
      // can be reached from each definition. This will automatically union definitions that have
      // overlapping uses and therefore can't be split.
      // It uses a `PartialStmt` instead of `Stmt` because a statement might contain both a
      // definition and use of a local, and they need to be processed separately.
      DisjointSetForest<PartialStmt> disjointSet = new DisjointSetForest<>();

      List<AbstractDefinitionStmt> assignments =
          assignmentsByLocal.getOrDefault(local, Collections.emptyList()).stream()
              .map(i -> (AbstractDefinitionStmt) stmts.get(i))
              .toList();

      if (assignments.size() <= 1) {
        // There is only a single assignment to the local, so no splitting is necessary
        newLocals.add(local);
        continue;
      }

      // Walk the statement graph starting from every definition and union all uses until a
      // different definition is encountered.
      for (AbstractDefinitionStmt assignment : assignments) {
        PartialStmt defStmt = new PartialStmt(assignment, true);
        disjointSet.add(defStmt);
        walk(
            graph,
            local,
            assignment,
            exceptionalFlow,
            view,
            catchesVirtualMachineErrors,
            useStmt -> {
              disjointSet.add(useStmt);
              disjointSet.union(defStmt, useStmt);
            });
      }

      // A use that no definition reaches under the chosen ExceptionalFlow can only be reached
      // through exceptional edges it rules out, e.g. a handler whose range cannot throw: dead code
      // for that model, but still in the graph. It is attached the most conservative way, which can
      // only merge more, so that renaming finds it.
      if (exceptionalFlow != ExceptionalFlow.ANY) {
        Set<Stmt> unreachedUses = new HashSet<>();
        for (Stmt stmt : stmts) {
          if (stmt.getUses().stream().anyMatch(l -> l == local)
              && !disjointSet.contains(new PartialStmt(stmt, false))) {
            unreachedUses.add(stmt);
          }
        }
        if (!unreachedUses.isEmpty()) {
          for (AbstractDefinitionStmt assignment : assignments) {
            PartialStmt defStmt = new PartialStmt(assignment, true);
            walk(
                graph,
                local,
                assignment,
                ExceptionalFlow.ANY,
                view,
                catchesVirtualMachineErrors,
                useStmt -> {
                  if (unreachedUses.contains(useStmt.backingStmt)) {
                    disjointSet.add(useStmt);
                    disjointSet.union(defStmt, useStmt);
                  }
                });
          }
        }
      }

      if (disjointSet.getSetCount() <= 1) {
        // There is only a single that local that can't be split
        newLocals.add(local);
        continue;
      }

      // Split locals, according to the disjoint sets found above.
      Map<PartialStmt, Local> representativeToNewLocal = new HashMap<>();
      final int[] nextId = {0}; // Java quirk; just an `int` doesn't work

      Function<PartialStmt, Local> getNewLocal =
          partialStmt ->
              representativeToNewLocal.computeIfAbsent(
                  disjointSet.find(partialStmt),
                  s -> {
                    Local newLocal;
                    do {
                      newLocal = local.withName(local.getName() + "#" + (nextId[0]++));
                    } while (locals.contains(newLocal));
                    return newLocal;
                  });

      for (int i = 0; i < stmts.size(); i++) {
        Stmt stmt = stmts.get(i);

        Optional<LValue> stmtDef = stmt.getDef();
        boolean localIsDef = stmtDef.isPresent() && stmtDef.get() == local;
        boolean localIsUse = stmt.getUses().stream().anyMatch(l -> l == local);

        Stmt oldStmt = stmt;

        if (localIsDef) {
          Local newDefLocal = getNewLocal.apply(new PartialStmt(oldStmt, true));
          if (local != newDefLocal) {
            newLocals.add(newDefLocal);
            stmt = ((AbstractDefinitionStmt) stmt).withNewDef(newDefLocal);
          }
        }

        if (localIsUse) {
          Local newUseLocal = getNewLocal.apply(new PartialStmt(oldStmt, false));
          if (local != newUseLocal) {
            newLocals.add(newUseLocal);
            stmt = stmt.withNewUse(local, newUseLocal);
          }
        }

        if (oldStmt == stmt) {
          continue;
        }

        graph.replaceNode(oldStmt, stmt);
        stmts.set(i, stmt);
      }
    }

    builder.setLocals(newLocals);
  }

  /**
   * Walks from a definition of the local to the uses its value reaches, until the local is defined
   * again, and reports each use.
   */
  private void walk(
      MutableControlFlowGraph graph,
      Local local,
      Stmt definition,
      ExceptionalFlow flow,
      View view,
      Map<ClassType, Boolean> catchesVirtualMachineErrors,
      Consumer<PartialStmt> onUse) {
    // The value only exists once the definition completed, so its walk starts on the normal
    // successors: when the definition throws, its handler still sees the previous value.
    Deque<Stmt> stack = new ArrayDeque<>(graph.successors(definition));
    Set<Stmt> visited = new HashSet<>();

    while (!stack.isEmpty()) {
      Stmt stmt = stack.pop();
      if (!visited.add(stmt)) {
        continue;
      }

      if (stmt.getUses().stream().anyMatch(l -> l == local)) {
        onUse.accept(new PartialStmt(stmt, false));
      }

      // A new assignment to the local ends the walk on its normal successors, but not on its
      // exceptional ones: a Stmt that throws has not assigned anything yet.
      Optional<LValue> defOpt = stmt.getDef();
      if (defOpt.isEmpty() || defOpt.get() != local) {
        stack.addAll(graph.successors(stmt));
      }
      for (Map.Entry<ClassType, Stmt> handler : graph.exceptionalSuccessors(stmt).entrySet()) {
        if (canEnter(stmt, handler.getKey(), flow, view, catchesVirtualMachineErrors)) {
          stack.add(handler.getValue());
        }
      }
    }
  }

  /** Whether control can enter the handler of the given exception type from the Stmt. */
  private static boolean canEnter(
      Stmt stmt,
      ClassType handlerType,
      ExceptionalFlow flow,
      View view,
      Map<ClassType, Boolean> catchesVirtualMachineErrors) {
    return switch (flow) {
      case ANY -> true;
      case VIRTUAL_MACHINE_ERRORS -> {
        if (catchesVirtualMachineErrors.computeIfAbsent(
            handlerType, type -> canCatchVirtualMachineError(type, view))) {
          yield true;
        }
        yield canThrow(stmt);
      }
      default -> canThrow(stmt);
    };
  }

  /**
   * Whether a handler of this type catches a VirtualMachineError: it is one of its superclasses (or
   * a catch-all, which Jimple types as java.lang.Throwable), or one of its subclasses. A type whose
   * superclasses cannot be followed in the view counts as one that might.
   */
  static boolean canCatchVirtualMachineError(ClassType type, View view) {
    String name = type.getFullyQualifiedName();
    if (name.equals(THROWABLE) || name.equals(ERROR) || name.equals(VIRTUAL_MACHINE_ERROR)) {
      return true;
    }
    for (ClassType current = type; current != null; ) {
      String currentName = current.getFullyQualifiedName();
      if (currentName.equals(VIRTUAL_MACHINE_ERROR)) {
        return true;
      }
      if (ABOVE_VIRTUAL_MACHINE_ERROR.contains(currentName)
          || BESIDE_VIRTUAL_MACHINE_ERROR.contains(currentName)) {
        // reached a class above VirtualMachineError, or beside it, without passing it: the type is
        // not a subclass of it, and the rest of the chain needs no lookup
        return false;
      }
      Optional<? extends SootClass> cls = view.getClass(current);
      if (cls.isEmpty()) {
        return true;
      }
      current = cls.get().getSuperclass().orElse(null);
    }
    return false;
  }

  /**
   * Whether the Stmt can throw (synchronously). Answers true for anything it does not recognise,
   * which only costs precision: an exceptional edge followed in vain can only merge more.
   */
  static boolean canThrow(Stmt stmt) {
    if (stmt instanceof JIdentityStmt
        || stmt instanceof JNopStmt
        || stmt instanceof JGotoStmt
        || stmt instanceof JIfStmt
        || stmt instanceof JSwitchStmt) {
      // an if compares immediates, a switch switches on one
      return false;
    }
    if (stmt instanceof JAssignStmt assign) {
      // storing into a field or an array element can throw
      return !(assign.getLeftOp() instanceof Local) || canThrow(assign.getRightOp());
    }
    return true;
  }

  private static boolean canThrow(Value value) {
    if (value instanceof Local
        || value instanceof NumericConstant
        || value instanceof BooleanConstant
        || value instanceof NullConstant) {
      // a String or class constant can throw on resolution, as dex's const-string and const-class
      return false;
    }
    if (value instanceof JCastExpr) {
      // a primitive conversion cannot throw, a reference cast can
      return !(value.getType() instanceof PrimitiveType);
    }
    if (value instanceof JDivExpr || value instanceof JRemExpr) {
      // only integer division throws. A Local's type cannot tell which one this is: before the
      // TypeAssigner it may be unknown, or the type of another value its register held. A float or
      // double constant operand can, as both operands have the same type.
      AbstractBinopExpr division = (AbstractBinopExpr) value;
      return !(isFloatingPointConstant(division.getOp1())
          || isFloatingPointConstant(division.getOp2()));
    }
    // other arithmetic, logic, shifts and comparisons of immediates cannot throw
    return !(value instanceof JAddExpr
        || value instanceof JSubExpr
        || value instanceof JMulExpr
        || value instanceof JAndExpr
        || value instanceof JOrExpr
        || value instanceof JXorExpr
        || value instanceof JShlExpr
        || value instanceof JShrExpr
        || value instanceof JUshrExpr
        || value instanceof JNegExpr
        || value instanceof JCmpExpr
        || value instanceof JCmplExpr
        || value instanceof JCmpgExpr);
  }

  private static boolean isFloatingPointConstant(Value value) {
    return value instanceof FloatConstant || value instanceof DoubleConstant;
  }

  @NonNull Map<Local, List<Integer>> groupAssignmentsByLocal(List<Stmt> statements) {
    Map<Local, List<Integer>> groupings = new HashMap<>();

    for (int i = 0; i < statements.size(); i++) {
      Stmt stmt = statements.get(i);
      if (!(stmt instanceof AbstractDefinitionStmt defStmt)) {
        continue;
      }

      LValue leftOp = defStmt.getLeftOp();
      if (!(leftOp instanceof Local)) {
        continue;
      }

      groupings.computeIfAbsent((Local) leftOp, x -> new ArrayList<>()).add(i);
    }

    return groupings;
  }
}
