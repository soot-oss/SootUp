package sootup.analysis.interprocedural.icfg;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022-2023 Palaniappan Muthuraman, Jonas Klauke
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
import java.util.stream.Collectors;
import sootup.callgraph.CallGraph;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.VoidType;
import sootup.core.util.DotExporter;
import sootup.core.views.View;

public class ICFGDotExporter {

  public static String buildICFGGraph(
      Map<MethodSignature, StmtGraph<?>> signatureToStmtGraph, View view, CallGraph callGraph) {
    final StringBuilder sb = new StringBuilder();
    DotExporter.buildDiGraphObject(sb);
    Map<Integer, MethodSignature> calls;
    calls = computeCalls(signatureToStmtGraph, view, callGraph);
    for (Map.Entry<MethodSignature, StmtGraph<?>> entry : signatureToStmtGraph.entrySet()) {
      String graph = DotExporter.buildGraph(entry.getValue(), true, calls, entry.getKey());
      sb.append(graph).append("\n");
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * This method finds out all the calls made in the given StmtGraphs, so it can be edged to other
   * methods.
   */
  public static Map<Integer, MethodSignature> computeCalls(
      Map<MethodSignature, StmtGraph<?>> stmtGraphSet, View view, CallGraph callgraph) {
    Map<Integer, MethodSignature> calls = new HashMap<>();
    for (Map.Entry<MethodSignature, StmtGraph<?>> entry : stmtGraphSet.entrySet()) {
      StmtGraph<?> stmtGraph = entry.getValue();
      MethodSignature source = entry.getKey();
      Collection<? extends BasicBlock<?>> blocks;
      try {
        blocks = stmtGraph.getBlocksSorted();
      } catch (Exception e) {
        blocks = stmtGraph.getBlocks();
      }
      for (BasicBlock<?> block : blocks) {
        List<Stmt> stmts = block.getStmts();
        for (Stmt stmt : stmts) {
          if (stmt.isInvokableStmt() && stmt.asInvokableStmt().containsInvokeExpr()) {
            MethodSignature target =
                stmt.asInvokableStmt().getInvokeExpr().get().getMethodSignature();
            int hashCode = stmt.hashCode();
            calls.put(hashCode, target);
            // compute all the classes that are made to the subclasses as well
            connectEdgesToSubClasses(source, target, view, calls, callgraph);
          } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Integer currentHashCode = stmt.hashCode();
            if (jAssignStmt.getRightOp() instanceof JNewExpr) {
              // if the statement is a new expression, then there will be calls to its static
              // initializers (init and clinit), so need to compute calls to them as well
              for (MethodSignature methodSignature : stmtGraphSet.keySet()) {
                SootMethod clintMethod =
                    view.getMethod(
                            view.getIdentifierFactory()
                                .getStaticInitializerSignature(methodSignature.getDeclClassType()))
                        .orElse(null);
                if (clintMethod != null) {
                  if (!calls.containsKey(stmt.hashCode())) {
                    calls.put(stmt.hashCode(), methodSignature);
                  } else {
                    MethodSignature secondInitMethodSignature = calls.get(currentHashCode);
                    currentHashCode =
                        stmtGraphSet.get(secondInitMethodSignature).getStartingStmt().hashCode();
                    calls.put(currentHashCode, methodSignature);
                  }
                }
              }
            }
          }
        }
      }
    }
    return calls;
  }

  public static Set<MethodSignature> getMethodSignatureInSubClass(
      MethodSignature source, MethodSignature target, CallGraph callGraph) {
    if (!callGraph.containsMethod(source) || !callGraph.containsMethod(target)) {
      return Collections.emptySet();
    }
    return callGraph.callTargetsFrom(source).stream()
        .filter(
            methodSignature ->
                !methodSignature.equals(target)
                    && methodSignature.getSubSignature().equals(target.getSubSignature()))
        .collect(Collectors.toSet());
  }

  public static void connectEdgesToSubClasses(
      MethodSignature source,
      MethodSignature target,
      View view,
      Map<Integer, MethodSignature> calls,
      CallGraph callgraph) {
    Set<MethodSignature> methodSignatureInSubClass =
        getMethodSignatureInSubClass(source, target, callgraph);
    methodSignatureInSubClass.forEach(
        subclassmethodSignature -> {
          Optional<? extends SootMethod> method = view.getMethod(target);
          MethodSignature initMethod =
              new MethodSignature(
                  subclassmethodSignature.getDeclClassType(),
                  new MethodSubSignature(
                      "<init>", Collections.emptyList(), VoidType.getInstance()));
          if (method.isPresent()
              && !subclassmethodSignature.toString().equals(initMethod.toString())) {
            if (method.get().hasBody()) {
              calls.put(
                  method.get().getBody().getStmtGraph().getStartingStmt().hashCode(),
                  subclassmethodSignature);
            }
          }
        });
  }
}
