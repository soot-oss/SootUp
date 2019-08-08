package de.upb.soot.callgraph;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class implements CHA (Class Hierarchy Algorithm)
 *
 * @author Markus Schmidt
 * @author Christian Brüggemann
 */
public class ClassHierarchyAlgorithm implements CallGraphAlgorithm {

  TypeHierarchy hierarchy;
  CallGraph callGraph;
  View v;

  @Override
  public CallGraph build(List<SootMethod> entryPoints, TypeHierarchy hierarchy) {

    this.hierarchy = hierarchy;
    CallGraph cg = new AdjacencyList();

    for (SootMethod method : entryPoints) {
      cg.addNode(method);
      analyzeMethod(method);
    }
    return cg;
  }

  private void analyzeMethod(SootMethod method) {
    if (!method.hasBody()) {
      return;
    }

    for (Stmt stmt : method.getBody().getStmts()) {
      if (stmt instanceof JAssignStmt) {
        Value assignedValue = ((JAssignStmt) stmt).getRightOp();
        if (assignedValue instanceof AbstractInvokeExpr) {
          handleInvokeExpression(method, (AbstractInvokeExpr) assignedValue);
        }
      } else if (stmt instanceof JInvokeStmt) {
        AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
        handleInvokeExpression(method, invokeExpr);
      }
    }
  }

  private void handleInvokeExpression(SootMethod method, AbstractInvokeExpr invokeExpr) {

    SootClass calledMethodClass =
        (SootClass) v.getClass(invokeExpr.getMethodSignature().getDeclClassType()).get();
    SootMethod calledMethod = calledMethodClass.getMethod(invokeExpr.getMethodSignature()).get();

    if (calledMethod.isStatic()) {
      if (!callGraph.hasNode(calledMethod)) {
        callGraph.addNode(calledMethod);
      }

      if (!callGraph.hasEdge(method, calledMethod)) {
        callGraph.addEdge(method, calledMethod);
        analyzeMethod(method);
      }
    } else {

      Stream<JavaClassType> subclasses =
          calledMethodClass.isInterface()
              ? hierarchy.implementersOf(calledMethodClass.getType()).stream()
              : hierarchy.subclassesOf(calledMethodClass.getType()).stream();

      // TODO: update when method / additional class for handling is added
      List<SootMethod> targets =
          new ArrayList<>(
              hierarchy.resolveAbstractDispatch(
                  subclasses.collect(Collectors.toList()), calledMethod));
      targets.add(calledMethod);

      for (SootMethod target : targets) {
        if (!callGraph.hasNode(target)) {
          callGraph.addNode(target);
        }

        if (!callGraph.hasEdge(method, target)) {
          callGraph.addEdge(method, target);
          analyzeMethod(target);
        }
      }
    }
  }
}
