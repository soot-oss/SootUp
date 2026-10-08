package sootup.examples.taintAnalysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.types.NullType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * A minimal IFDS based taint analysis. The code regions enclosed by {@code --8<--} markers are
 * included into docs/taint-analysis-example.md, so the tutorial always shows tested code.
 */
public class TaintAnalysisTest {

  // --8<-- [start:basic-taint]
  public static class BasicTaint {
    static String c;

    public void entryPoint() {
      String a = "SECRET";
      String b = a;
      c = b;
      SinkClass sc = new SinkClass();
      sc.sink(c);
    }
  }

  // --8<-- [end:basic-taint]

  // --8<-- [start:basic-taint-sanitized]
  public static class BasicTaintSanitized {
    public void entryPoint() {
      String a = "SECRET";
      String b = a;
      b = "..."; // sanitization: b is overwritten with a harmless value
      SinkClass sc = new SinkClass();
      sc.sink(b);
    }
  }

  // --8<-- [end:basic-taint-sanitized]

  // --8<-- [start:function-propagates-taint]
  public static class FunctionPropagatesTaint {
    private String id(String s) {
      return s;
    }

    public void entryPoint() {
      String a = "SECRET";
      String b = id(a);
      SinkClass sc = new SinkClass();
      sc.sink(b);
    }
  }

  // --8<-- [end:function-propagates-taint]

  // --8<-- [start:function-returns-taint]
  public static class FunctionReturnsTaint {
    private String source() {
      return "SECRET";
    }

    private void sink(String s) {}

    public void entryPoint() {
      String a = source();
      String b = a;
      sink(b);
    }
  }

  // --8<-- [end:function-returns-taint]

  // --8<-- [start:sink-class]
  public static class SinkClass {
    public void sink(String s) {
      // any tainted value reaching this method is a leak
    }
  }

  // --8<-- [end:sink-class]

  // --8<-- [start:problem-class]
  static class TaintAnalysisProblem
      extends DefaultJimpleIFDSTabulationProblem<Value, InterproceduralCFG<Stmt, SootMethod>> {

    private final SootMethod entryMethod;

    public TaintAnalysisProblem(InterproceduralCFG<Stmt, SootMethod> icfg, SootMethod entryMethod) {
      super(icfg);
      this.entryMethod = entryMethod;
    }

    // --8<-- [end:problem-class]

    // --8<-- [start:initial-seeds]
    @Override
    public Map<Stmt, Set<Value>> initialSeeds() {
      Stmt firstStmt = entryMethod.getBody().getControlFlowGraph().getStartingStmt();
      return DefaultSeeds.make(Collections.singleton(firstStmt), zeroValue());
    }

    // --8<-- [end:initial-seeds]

    // --8<-- [start:zero-value]
    @Override
    protected Value createZeroValue() {
      return new Local("<<zero>>", NullType.getInstance());
    }

    // --8<-- [end:zero-value]

    // --8<-- [start:flow-functions-factory]
    @Override
    protected FlowFunctions<Stmt, Value, SootMethod> createFlowFunctionsFactory() {
      return new FlowFunctions<Stmt, Value, SootMethod>() {

        @Override
        public FlowFunction<Value> getNormalFlowFunction(Stmt curr, Stmt succ) {
          return getNormalFlow(curr);
        }

        @Override
        public FlowFunction<Value> getCallFlowFunction(Stmt callStmt, SootMethod callee) {
          return getCallFlow(callStmt, callee);
        }

        @Override
        public FlowFunction<Value> getReturnFlowFunction(
            Stmt callSite, SootMethod callee, Stmt exitStmt, Stmt returnSite) {
          return getReturnFlow(callSite, exitStmt);
        }

        @Override
        public FlowFunction<Value> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
          return getCallToReturnFlow(callSite);
        }
      };
    }

    // --8<-- [end:flow-functions-factory]

    // --8<-- [start:is-source]
    static boolean isSource(Value value) {
      return value instanceof StringConstant
          && ((StringConstant) value).getValue().equals("SECRET");
    }

    // --8<-- [end:is-source]

    // --8<-- [start:normal-flow]
    FlowFunction<Value> getNormalFlow(Stmt curr) {
      if (!(curr instanceof JAssignStmt)) {
        return Identity.v();
      }
      JAssignStmt assign = (JAssignStmt) curr;
      Value leftOp = assign.getLeftOp();
      Value rightOp = assign.getRightOp();

      // x = "SECRET": x becomes tainted
      if (isSource(rightOp)) {
        return new Gen<>(leftOp, zeroValue());
      }

      return source -> {
        // x = ...: the old value of x is overwritten, so its taint is killed
        if (source.equivTo(leftOp)) {
          return Collections.emptySet();
        }
        Set<Value> out = new HashSet<>();
        out.add(source);
        // x = y: if y is tainted, x becomes tainted as well
        if (source.equivTo(rightOp)) {
          out.add(leftOp);
        }
        return out;
      };
    }

    // --8<-- [end:normal-flow]

    // --8<-- [start:call-flow]
    FlowFunction<Value> getCallFlow(Stmt callStmt, SootMethod callee) {
      if (!callee.hasBody()) {
        return source -> Collections.emptySet();
      }
      List<Immediate> args = callStmt.asInvokableStmt().getInvokeExpr().get().getArgs();

      return source -> {
        Set<Value> out = new HashSet<>();
        // tainted static fields are visible inside the callee as well
        if (source instanceof JStaticFieldRef) {
          out.add(source);
        }
        // a tainted argument taints the corresponding parameter of the callee
        for (int i = 0; i < args.size(); i++) {
          if (args.get(i).equivTo(source)) {
            out.add(callee.getBody().getParameterLocal(i));
          }
        }
        return out;
      };
    }

    // --8<-- [end:call-flow]

    // --8<-- [start:return-flow]
    FlowFunction<Value> getReturnFlow(Stmt callSite, Stmt exitStmt) {
      // the variable receiving the return value at the call site, e.g. b in "b = id(a)"
      Value receiver =
          callSite instanceof AbstractDefinitionStmt
              ? ((AbstractDefinitionStmt) callSite).getLeftOp()
              : null;
      Value returnOp = exitStmt instanceof JReturnStmt ? ((JReturnStmt) exitStmt).getOp() : null;

      // return "SECRET": the receiver becomes tainted
      if (receiver != null && isSource(returnOp)) {
        return new Gen<>(receiver, zeroValue());
      }

      return source -> {
        Set<Value> out = new HashSet<>();
        // tainted static fields stay tainted after the call
        if (source instanceof JStaticFieldRef) {
          out.add(source);
        }
        // a tainted return value taints the receiver
        if (receiver != null && source.equivTo(returnOp)) {
          out.add(receiver);
        }
        return out;
      };
    }

    // --8<-- [end:return-flow]

    // --8<-- [start:call-to-return-flow]
    FlowFunction<Value> getCallToReturnFlow(Stmt callSite) {
      if (!(callSite instanceof AbstractDefinitionStmt)) {
        return Identity.v();
      }
      // b = foo(..): the old value of b is overwritten by the return value
      Value receiver = ((AbstractDefinitionStmt) callSite).getLeftOp();
      return source ->
          source.equivTo(receiver) ? Collections.emptySet() : Collections.singleton(source);
    }
    // --8<-- [end:call-to-return-flow]
  }

  // --8<-- [start:create-view]
  static JavaView createView() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "target/test-classes", SourceType.Application, Collections.emptyList());
    return new JavaView(inputLocation);
  }

  // --8<-- [end:create-view]

  // --8<-- [start:find-entry-method]
  static SootMethod findEntryMethod(JavaView view, Class<?> targetClass) {
    JavaClassType classType = view.getIdentifierFactory().getClassType(targetClass.getName());
    JavaSootClass sootClass = view.getClass(classType).get();
    return sootClass.getMethodsByName("entryPoint").iterator().next();
  }

  // --8<-- [end:find-entry-method]

  // --8<-- [start:run-analysis]
  static JimpleIFDSSolver<Value, InterproceduralCFG<Stmt, SootMethod>> runAnalysis(
      JavaView view, SootMethod entryMethod) {
    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethod.getSignature()), false, false);
    TaintAnalysisProblem problem = new TaintAnalysisProblem(icfg, entryMethod);
    JimpleIFDSSolver<Value, InterproceduralCFG<Stmt, SootMethod>> solver =
        new JimpleIFDSSolver<>(problem);
    solver.solve(entryMethod.getDeclaringClassType().getClassName());
    return solver;
  }

  // --8<-- [end:run-analysis]

  // --8<-- [start:leaks-to-sink]
  static boolean leaksToSink(Class<?> targetClass) {
    JavaView view = createView();
    SootMethod entryMethod = findEntryMethod(view, targetClass);
    JimpleIFDSSolver<Value, InterproceduralCFG<Stmt, SootMethod>> solver =
        runAnalysis(view, entryMethod);

    for (Stmt stmt : entryMethod.getBody().getStmts()) {
      if (!stmt.isInvokableStmt() || !stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
        continue;
      }
      AbstractInvokeExpr invokeExpr = stmt.asInvokableStmt().getInvokeExpr().get();
      if (!invokeExpr.getMethodSignature().getName().equals("sink")) {
        continue;
      }
      // the facts that hold right before the call to sink(..)
      Set<Value> taintedValues = solver.ifdsResultsAt(stmt);
      for (Immediate arg : invokeExpr.getArgs()) {
        if (taintedValues.stream().anyMatch(arg::equivTo)) {
          return true;
        }
      }
    }
    return false;
  }

  // --8<-- [end:leaks-to-sink]

  // --8<-- [start:tests]
  @Test
  public void basicTaintLeaks() {
    assertTrue(leaksToSink(BasicTaint.class));
  }

  @Test
  public void sanitizedTaintDoesNotLeak() {
    assertFalse(leaksToSink(BasicTaintSanitized.class));
  }

  @Test
  public void taintPropagatesThroughFunction() {
    assertTrue(leaksToSink(FunctionPropagatesTaint.class));
  }

  @Test
  public void taintReturnedFromFunctionLeaks() {
    assertTrue(leaksToSink(FunctionReturnsTaint.class));
  }
  // --8<-- [end:tests]
}
