package sootup.analysis.interprocedural.icfg;

import java.util.*;
import java.util.stream.Collectors;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.MethodDispatchResolver;
import sootup.core.types.VoidType;
import sootup.core.util.DotExporter;
import sootup.core.views.View;

public class ICFGDotExporter {

  public static String buildICFGGraph(
      Map<MethodSignature, StmtGraph> signatureToStmtGraph, View<? extends SootClass<?>> view) {
    final StringBuilder sb = new StringBuilder();
    DotExporter.buildDiGraphObject(sb);
    Map<Integer, MethodSignature> calls;
    calls = computeCalls(signatureToStmtGraph, view);
    for (Map.Entry<MethodSignature, StmtGraph> entry : signatureToStmtGraph.entrySet()) {
      String graph = DotExporter.buildGraph(entry.getValue(), true, calls, entry.getKey());
      sb.append(graph + "\n");
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * This method finds out all the calls made in the given StmtGraphs, so it can be edged to other
   * methods.
   */
  public static Map<Integer, MethodSignature> computeCalls(
      Map<MethodSignature, StmtGraph> stmtGraphSet, View<? extends SootClass<?>> view) {
    Map<Integer, MethodSignature> calls = new HashMap<>();
    for (StmtGraph stmtGraph : stmtGraphSet.values()) {
      Collection<? extends BasicBlock<?>> blocks;
      try {
        blocks = stmtGraph.getBlocksSorted();
      } catch (Exception e) {
        blocks = stmtGraph.getBlocks();
      }
      for (BasicBlock<?> block : blocks) {
        List<Stmt> stmts = block.getStmts();
        for (Stmt stmt : stmts) {
          if (stmt.containsInvokeExpr()) {
            MethodSignature methodSignature = stmt.getInvokeExpr().getMethodSignature();
            int hashCode = stmt.hashCode();
            calls.put(hashCode, methodSignature);
            // compute all the classes that are made to the subclasses as well
            connectEdgesToSubClasses(methodSignature, view, calls);
          } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Integer currentHashCode = stmt.hashCode();
            if (jAssignStmt.getRightOp() instanceof JNewExpr) {
              // if the statement is a new expression, then there will be calls to its static
              // initializers (init and clinit), so need to compute calls to them as well
              for (MethodSignature methodSignature : stmtGraphSet.keySet()) {
                SootMethod clintMethod =
                    view.getMethod(methodSignature.getDeclClassType().getStaticInitializer())
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
      MethodSignature targetMethodSignature, View<? extends SootClass<?>> view) {
    try {
      return MethodDispatchResolver.resolveAllDispatches(view, targetMethodSignature).stream()
          .map(
              methodSignature ->
                  MethodDispatchResolver.resolveConcreteDispatch(view, methodSignature))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());
    } catch (Exception e) {
      return null;
    }
  }

  public static void connectEdgesToSubClasses(
      MethodSignature methodSignature,
      View<? extends SootClass<?>> view,
      Map<Integer, MethodSignature> calls) {
    Set<MethodSignature> methodSignatureInSubClass =
        getMethodSignatureInSubClass(methodSignature, view);
    if (methodSignatureInSubClass != null) {
      methodSignatureInSubClass.forEach(
          subclassmethodSignature -> {
            Optional<? extends SootMethod> method = view.getMethod(methodSignature);
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
}
