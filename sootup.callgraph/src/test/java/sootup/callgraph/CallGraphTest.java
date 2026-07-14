package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public abstract class CallGraphTest {

  protected AbstractCallGraphAlgorithm algorithm;
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  protected JavaView view;

  protected abstract AbstractCallGraphAlgorithm createAlgorithm(JavaView view);

  protected JavaView createViewForClassPath(String classPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library, Collections.emptyList()));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            classPath, SourceType.Application, Collections.emptyList()));

    return new JavaView(inputLocations);
  }

  CallGraph loadCallGraph(String testDirectory, String className) {
    String classPath = "src/test/resources/callgraph/" + testDirectory + "/binary";

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    view = createViewForClassPath(classPath);
    identifierFactory = view.getIdentifierFactory();
    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, identifierFactory.getMainSubSignature());

    SootClass sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(m, mainMethodSignature + " not found in classloader");

    algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        cg.containsMethod(mainMethodSignature), mainMethodSignature + " is not found in CallGraph");
    return cg;
  }

  protected InvokableStmt getInvokableStmt(
      MethodSignature sourceMethod, MethodSignature staticTargetMethod) {
    return getInvokableStmt(sourceMethod, staticTargetMethod, 0);
  }

  protected InvokableStmt getInvokableStmt(
      MethodSignature sourceMethod, MethodSignature staticTargetMethod, int index) {
    int currentIndex = 0;
    SootMethod method = view.getMethod(sourceMethod).orElse(null);
    assertNotNull(method);
    for (Stmt invokableStmt : method.getBody().getStmts()) {
      if (invokableStmt instanceof InvokableStmt
          && ((InvokableStmt) invokableStmt).getInvokeExpr().isPresent()) {
        AbstractInvokeExpr stmt = ((InvokableStmt) invokableStmt).getInvokeExpr().orElse(null);
        assertNotNull(stmt);
        if (stmt.getMethodSignature().equals(staticTargetMethod)) {
          if (currentIndex == index) {
            return (InvokableStmt) invokableStmt;
          }
          currentIndex++;
        }
      }
    }
    throw new RuntimeException(
        "No invokable stmt of method " + staticTargetMethod + " found for " + sourceMethod);
  }

  protected InvokableStmt getInvokableStmtNonInvokeExpr(
      MethodSignature sourceMethod, ClassType targetClass) {
    return getInvokableStmtNonInvokeExpr(sourceMethod, targetClass, false, 0);
  }

  protected InvokableStmt getInvokableStmtNonInvokeExpr(
      MethodSignature sourceMethod, ClassType targetClass, boolean leftExpr, int index) {
    int currentIndex = 0;
    SootMethod method = view.getMethod(sourceMethod).orElse(null);
    assertNotNull(method);

    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();
    for (Stmt invokableStmt : method.getBody().getStmts()) {
      // look only at assigments which do Invoke but does not contain a direct invoke expr
      // static fields and new array expressions
      if (invokableStmt instanceof JAssignStmt
          && ((InvokableStmt) invokableStmt).getInvokeExpr().isEmpty()
          && ((InvokableStmt) invokableStmt).invokesStaticInitializer()) {
        Value expr;
        // look at the left or right side of the assigment
        if (leftExpr) {
          expr = ((JAssignStmt) invokableStmt).getLeftOp();
        } else {
          expr = ((JAssignStmt) invokableStmt).getRightOp();
        }
        // extract the class type
        ClassType classType = null;
        if (expr instanceof JFieldRef) {
          classType = ((JFieldRef) expr).getFieldSignature().getDeclClassType();
        } else if (expr instanceof JNewExpr
            || expr instanceof JNewArrayExpr
            || expr instanceof JNewMultiArrayExpr) {
          instantiateVisitor.init();
          expr.accept(instantiateVisitor);
          classType = instantiateVisitor.getResult();
        }
        // probably caused by invoke caused on the other operand side
        if (classType == null) {
          continue;
        }
        if (classType.equals(targetClass)) {
          // found fitting stmt in given position
          if (currentIndex == index) {
            return (InvokableStmt) invokableStmt;
          }
          // search next fitting stmt
          currentIndex++;
        }
      }
    }
    throw new RuntimeException(
        "No invokable assignment stmt of class " + targetClass + " found for " + sourceMethod);
  }
}
