package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import org.jspecify.annotations.NonNull;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/**
 * Gives every use of a constant its own local, like Soot's SharedInitializationLocalSplitter.
 *
 * <p>Dalvik reuses a register holding a constant for uses of different types, e.g. {@code v0 = 0}
 * passed as an int and as a boolean. {@code LocalSplitter} cannot separate those uses since they
 * share one definition, so no valid typing exists. Uses that share a non-constant definition stay
 * together, because only the constant can be duplicated.
 *
 * <p>Expects {@code LocalSplitter} to have run.
 */
public class DexSharedInitializationLocalSplitter implements BodyInterceptor {

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    Set<Local> locals = new LinkedHashSet<>(builder.getLocals());
    Set<String> takenNames = new HashSet<>();
    locals.forEach(local -> takenNames.add(local.getName()));

    for (Local local : candidates(graph.getStmts())) {
      split(graph, local, locals, takenNames);
    }
    builder.setLocals(locals);
  }

  /** Locals with a constant definition that are used more than once. */
  private static List<Local> candidates(List<Stmt> stmts) {
    Set<Local> constantDefined = new LinkedHashSet<>();
    Map<Local, Integer> useCount = new HashMap<>();
    for (Stmt stmt : stmts) {
      if (stmt instanceof JAssignStmt assign
          && assign.getLeftOp() instanceof Local local
          && assign.getRightOp() instanceof Constant) {
        constantDefined.add(local);
      }
      stmt.getUses().stream()
          .filter(Local.class::isInstance)
          .forEach(use -> useCount.merge((Local) use, 1, Integer::sum));
    }
    List<Local> candidates = new ArrayList<>();
    for (Local local : constantDefined) {
      if (useCount.getOrDefault(local, 0) > 1) {
        candidates.add(local);
      }
    }
    return candidates;
  }

  private static void split(
      MutableControlFlowGraph graph, Local local, Set<Local> locals, Set<String> takenNames) {
    splitRepeatedArguments(graph, local, locals, takenNames);
    Map<Stmt, Set<AbstractDefinitionStmt>> defsOfUse = reachingDefinitions(graph, local);

    // one cluster per use, merged when uses share a non-constant definition
    List<Cluster> clusters = new ArrayList<>();
    for (Map.Entry<Stmt, Set<AbstractDefinitionStmt>> entry : defsOfUse.entrySet()) {
      Cluster cluster = new Cluster();
      cluster.uses.add(entry.getKey());
      for (AbstractDefinitionStmt def : entry.getValue()) {
        (isConstant(def) ? cluster.constantDefs : cluster.otherDefs).add(def);
      }
      boolean merged = true;
      while (merged) {
        merged = false;
        for (Iterator<Cluster> it = clusters.iterator(); it.hasNext(); ) {
          Cluster existing = it.next();
          if (!Collections.disjoint(existing.otherDefs, cluster.otherDefs)) {
            cluster.uses.addAll(existing.uses);
            cluster.constantDefs.addAll(existing.constantDefs);
            cluster.otherDefs.addAll(existing.otherDefs);
            it.remove();
            merged = true;
          }
        }
      }
      clusters.add(cluster);
    }
    if (clusters.size() <= 1) {
      return;
    }

    Map<Stmt, Stmt> current = new IdentityHashMap<>();
    Set<Stmt> reusedConstantDefs = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Cluster cluster : clusters) {
      if (cluster.constantDefs.isEmpty()) {
        continue;
      }
      Local newLocal = local.withName(freshName(local.getName(), takenNames));
      locals.add(newLocal);

      for (AbstractDefinitionStmt def : cluster.constantDefs) {
        if (reusedConstantDefs.add(def)) {
          // the first cluster takes over the original definition, which no other use needs
          replace(graph, current, def, ((JAssignStmt) current(current, def)).withNewDef(newLocal));
        } else {
          JAssignStmt copy =
              Jimple.newAssignStmt(
                  newLocal, ((JAssignStmt) def).getRightOp(), def.getPositionInfo());
          graph.insertAfter(current(current, def), copy);
        }
      }
      for (AbstractDefinitionStmt def : cluster.otherDefs) {
        AbstractDefinitionStmt stmt = (AbstractDefinitionStmt) current(current, def);
        replace(graph, current, def, stmt.withNewDef(newLocal));
      }
      for (Stmt use : cluster.uses) {
        replace(graph, current, use, current(current, use).withNewUse(local, newLocal));
      }
    }
  }

  /**
   * {@code foo(int,boolean)($u0, $u0)} needs a different type per argument, so each repeated
   * argument gets its own local when only constants reach the call.
   */
  private static void splitRepeatedArguments(
      MutableControlFlowGraph graph, Local local, Set<Local> locals, Set<String> takenNames) {

    for (Map.Entry<Stmt, Set<AbstractDefinitionStmt>> entry :
        reachingDefinitions(graph, local).entrySet()) {
      Stmt stmt = entry.getKey();
      Set<AbstractDefinitionStmt> defs = entry.getValue();
      if (defs.isEmpty()
          || !defs.stream().allMatch(DexSharedInitializationLocalSplitter::isConstant)
          || !stmt.isInvokableStmt()
          || stmt.asInvokableStmt().getInvokeExpr().isEmpty()) {
        continue;
      }
      AbstractInvokeExpr invoke = stmt.asInvokableStmt().getInvokeExpr().get();
      List<Immediate> args = new ArrayList<>(invoke.getArgs());
      boolean seen = false;
      for (int i = 0; i < args.size(); i++) {
        if (args.get(i) != local) {
          continue;
        }
        if (!seen) {
          seen = true;
          continue;
        }
        Local copyLocal = local.withName(freshName(local.getName(), takenNames));
        locals.add(copyLocal);
        for (AbstractDefinitionStmt def : defs) {
          graph.insertAfter(
              def, Jimple.newAssignStmt(copyLocal, def.getRightOp(), def.getPositionInfo()));
        }
        args.set(i, copyLocal);
      }
      if (args.equals(invoke.getArgs())) {
        continue;
      }
      AbstractInvokeExpr newInvoke;
      if (invoke instanceof JStaticInvokeExpr staticInvoke) {
        newInvoke = staticInvoke.withArgs(args);
      } else if (invoke instanceof AbstractInstanceInvokeExpr instanceInvoke) {
        newInvoke = instanceInvoke.withArgs(args);
      } else {
        continue;
      }
      if (stmt instanceof JInvokeStmt invokeStmt) {
        graph.replaceNode(stmt, invokeStmt.withInvokeExpr(newInvoke));
      } else if (stmt instanceof JAssignStmt assign) {
        graph.replaceNode(stmt, assign.withRValue(newInvoke));
      }
    }
  }

  /** For every use of the local, the definitions that reach it. */
  private static Map<Stmt, Set<AbstractDefinitionStmt>> reachingDefinitions(
      MutableControlFlowGraph graph, Local local) {
    Map<Stmt, Set<AbstractDefinitionStmt>> defsOfUse = new LinkedHashMap<>();
    for (Stmt stmt : graph.getStmts()) {
      if (!(stmt instanceof AbstractDefinitionStmt def) || def.getLeftOp() != local) {
        continue;
      }
      Deque<Stmt> worklist = new ArrayDeque<>(graph.getAllSuccessors(def));
      Set<Stmt> visited = Collections.newSetFromMap(new IdentityHashMap<>());
      while (!worklist.isEmpty()) {
        Stmt next = worklist.pop();
        if (!visited.add(next)) {
          continue;
        }
        if (next.getUses().stream().anyMatch(use -> use == local)) {
          defsOfUse.computeIfAbsent(next, k -> new LinkedHashSet<>()).add(def);
        }
        if (next.getDef().filter(d -> d == local).isEmpty()) {
          worklist.addAll(graph.getAllSuccessors(next));
        }
      }
    }
    return defsOfUse;
  }

  private static boolean isConstant(AbstractDefinitionStmt def) {
    return def instanceof JAssignStmt && def.getRightOp() instanceof Constant;
  }

  private static Stmt current(Map<Stmt, Stmt> current, Stmt original) {
    return current.getOrDefault(original, original);
  }

  private static void replace(
      MutableControlFlowGraph graph, Map<Stmt, Stmt> current, Stmt original, Stmt replacement) {
    graph.replaceNode(current(current, original), replacement);
    current.put(original, replacement);
  }

  private static String freshName(String name, Set<String> takenNames) {
    for (int i = 1; ; i++) {
      String candidate = name + "_" + i;
      if (takenNames.add(candidate)) {
        return candidate;
      }
    }
  }

  private static final class Cluster {
    final Set<Stmt> uses = new LinkedHashSet<>();
    final Set<AbstractDefinitionStmt> constantDefs = new LinkedHashSet<>();
    final Set<AbstractDefinitionStmt> otherDefs = new LinkedHashSet<>();
  }
}
