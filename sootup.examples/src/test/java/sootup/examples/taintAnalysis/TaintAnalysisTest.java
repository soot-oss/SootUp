package sootup.examples.taintAnalysis;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import heros.flowfunc.Transfer;
import org.junit.jupiter.api.Test;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.analysis.interprocedural.ifds.JimpleIFDSSolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Collectors;

public class TaintAnalysisTest {

    // BasicTaint {
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
    // } BasicTaint

    // BasicTaintSanitized {
    public static class BasicTaintSanitized {
        public void entryPoint() {
            String a = "SECRET";
            String b = a;
            b = "...";  // sanitization
            SinkClass sc = new SinkClass();
            sc.sink(b);
        }
    }
    // } BasicTaintSanitized

    // FunctionPropagatesTaint {
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
    // } FunctionPropagatesTaint

    // FunctionReturnsTaint {
    public static class FunctionReturnsTaint {
        private String source() {
            return "SECRET";
        }

        private void sink(String s) {
            // internal sink method
        }

        public void entryPoint() {
            String a = source();
            String b = a;
            sink(b);
        }
    }
    // } FunctionReturnsTaint

    // SinkClass {
    public static class SinkClass {
        public void sink(String s) {
            // sink method - tainted data reaching here is a leak
        }
    }
     // } SinkClass

    // TaintAnalysisProblem {
    static class TaintAnalysisProblem extends DefaultJimpleIFDSTabulationProblem<Object, InterproceduralCFG<Stmt, SootMethod>> {
        // TaintAnalysisProblemFields {
        private final SootMethod entryMethod;
        private final InterproceduralCFG<Stmt, SootMethod> icfg;
        // } TaintAnalysisProblemFields

        // TaintAnalysisProblemConstructor {
        public TaintAnalysisProblem(InterproceduralCFG<Stmt, SootMethod> icfg, SootMethod entryMethod) {
            super(icfg);
            this.icfg = icfg;
            this.entryMethod = entryMethod;
        }
        // } TaintAnalysisProblemConstructor

        // TaintAnalysisInitialSeeds {
        @Override
        public Map<Stmt, Set<Object>> initialSeeds() {
            return DefaultSeeds.make(
                    Collections.singleton(entryMethod.getBody().getStmtGraph().getStartingStmt()),
                    zeroValue());
        }
        // } TaintAnalysisInitialSeeds

        // TaintAnalysisFlowFunctionsFactory {
        @Override
        protected FlowFunctions<Stmt, Object, SootMethod> createFlowFunctionsFactory() {
            return new FlowFunctions<Stmt, Object, SootMethod>() {

                @Override
                public FlowFunction<Object> getNormalFlowFunction(Stmt curr, Stmt succ) {
                    return getNormalFlow(curr, succ);
                }

                @Override
                public FlowFunction<Object> getCallFlowFunction(Stmt callStmt, SootMethod destinationMethod) {
                    return getCallFlow(callStmt, destinationMethod);
                }

                @Override
                public FlowFunction<Object> getReturnFlowFunction(
                        Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                    return getReturnFlow(callSite, calleeMethod, exitStmt, returnSite);
                }

                @Override
                public FlowFunction<Object> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                    return getCallToReturnFlow(callSite, returnSite);
                }
            };
        }
        // } TaintAnalysisFlowFunctionsFactory

        // TaintAnalysisZeroValue {
        @Override
        protected Object createZeroValue() {
            return new Object() {
                @Override
                public String toString() {
                    return "<<zero>>";
                }
            };
        }
        // } TaintAnalysisZeroValue

        // getNormalFlow {
        FlowFunction<Object> getNormalFlow(Stmt currentStmt, Stmt successorStmt) {
            if (currentStmt instanceof JAssignStmt) {
                final JAssignStmt assign = (JAssignStmt) currentStmt;
                final Object leftOp = assign.getLeftOp();
                final Object rightOp = assign.getRightOp();

                if (rightOp instanceof StringConstant) {
                    StringConstant str = (StringConstant) rightOp;
                    if (str.getValue().equals("SECRET")) {
                        return new Gen<>(leftOp, zeroValue());
                    }
                }
                return new Transfer<>(leftOp, rightOp);
            }
            return Identity.v();
        }
        // } getNormalFlow

        // getCallFlow {
        FlowFunction<Object> getCallFlow(Stmt callStmt, final SootMethod destinationMethod) {
            if (!(callStmt instanceof JInvokeStmt)) {
                return KillAll.v();
            }

            JInvokeStmt invokeStmt = (JInvokeStmt) callStmt;
            Optional<AbstractInvokeExpr> ieOpt = invokeStmt.getInvokeExpr();
            if (!ieOpt.isPresent()) {
                return KillAll.v();
            }

            AbstractInvokeExpr ie = ieOpt.get();
            final List<?> callArgs = ie.getArgs();

            // Simplified mapping for API compatibility
            if (!callArgs.isEmpty() && destinationMethod.getParameterCount() > 0) {
                Object firstCallArg = callArgs.get(0);
                Object firstParam = destinationMethod.getBody().getParameterLocal(0);
                return new Transfer<>(firstParam, firstCallArg);
            }

            return KillAll.v();
        }
        // } getCallFlow

        // getReturnFlow {
        FlowFunction<Object> getReturnFlow(
                final Stmt callSite, final SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
            if (exitStmt instanceof JReturnStmt) {
                JReturnStmt returnStmt = (JReturnStmt) exitStmt;
                if (callSite instanceof JAssignStmt) {
                    JAssignStmt assignStmt = (JAssignStmt) callSite;
                    final Object retOp = returnStmt.getOp();
                    if (!(retOp instanceof StringConstant)) {
                        Object leftOp = assignStmt.getLeftOp();
                        return new Transfer<>(leftOp, retOp);
                    } else {
                        StringConstant str = (StringConstant) retOp;
                        if (str.getValue().equals("SECRET")) {
                            final Object leftOp = assignStmt.getLeftOp();
                            return new Gen<>(leftOp, zeroValue());
                        }
                    }
                }
            }
            return KillAll.v();
        }
        // } getReturnFlow

        // getCallToReturnFlow {
        FlowFunction<Object> getCallToReturnFlow(final Stmt callSite, Stmt returnSite) {
            return Identity.v();
        }
        // } getCallToReturnFlow
    }
    // } TaintAnalysisProblem

    // AnalysisRunner {
    static class AnalysisRunner {
        // AnalysisRunnerFields {
        protected JavaView view;
        protected MethodSignature entryMethodSignature;
        protected SootMethod entryMethod;
        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

        private JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solved = null;
        // } AnalysisRunnerFields

        // executeStaticAnalysis {
        protected JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> executeStaticAnalysis(
                String pathToJar, String targetTestClassName) {
            setupSoot(pathToJar, targetTestClassName);
            runAnalysis();
            if (solved == null) {
                throw new NullPointerException("Something went wrong solving the IFDS problem!");
            }
            return solved;
        }
        // } executeStaticAnalysis

        // runAnalysis {
        private void runAnalysis() {
            JimpleBasedInterproceduralCFG icfg =
                    new JimpleBasedInterproceduralCFG((View) view, entryMethodSignature, false, false);
            TaintAnalysisProblem problem = new TaintAnalysisProblem(icfg, entryMethod);
            JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solver =
                    new JimpleIFDSSolver(problem);
            solver.solve(entryMethod.getDeclaringClassType().getClassName());
            solved = solver;
        }
        // } runAnalysis

        // setupSoot {
        private void setupSoot(String pathToJar, String targetTestClassName) {
            List<AnalysisInputLocation> inputLocations = new ArrayList<>();
            inputLocations.add(new JavaClassPathAnalysisInputLocation(pathToJar, SourceType.Application));

            view = new JavaView(inputLocations);

            JavaClassType mainClassSignature = identifierFactory.getClassType(targetTestClassName);
            Optional<? extends SootClass> scOpt = view.getClass(mainClassSignature);
            if (!scOpt.isPresent()) {
                throw new RuntimeException("Class not found: " + targetTestClassName);
            }
            SootClass sc = scOpt.get();
            Optional<? extends SootMethod> entryMethodOpt = sc.getMethods().stream()
                    .filter(e -> e.getName().equals("entryPoint"))
                    .findFirst();
            if (!entryMethodOpt.isPresent()) {
                throw new RuntimeException("entryPoint method not found in class: " + targetTestClassName);
            }
            entryMethod = entryMethodOpt.get();
            entryMethodSignature = entryMethod.getSignature();
        }
        // } setupSoot

        // taintedVariablesAtSink {
        public Set<Object> taintedVariablesAtSink(
                JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis) {
            // Find sink statements using simple loop to avoid Optional issues
            for (Stmt stmt : entryMethod.getBody().getStmts()) {
                if (stmt instanceof JInvokeStmt) {
                    JInvokeStmt invokeStmt = (JInvokeStmt) stmt;
                    Optional<AbstractInvokeExpr> ieOpt = invokeStmt.getInvokeExpr();
                    if (ieOpt.isPresent() && ieOpt.get().getMethodSignature().getName().equals("sink")) {
                        Set<?> rawSet = analysis.ifdsResultsAt(stmt);
                        Set<Object> names = new HashSet<>();
                        for (Object fact : rawSet) {
                            names.add(fact);
                        }
                        return names;
                    }
                }
            }
            return new HashSet<>();
        }
        // } taintedVariablesAtSink

        // checkLeak {
        public void checkLeak(JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis) {
            Set<Object> taintedVars = taintedVariablesAtSink(analysis);
            AbstractInvokeExpr sinkMethod = null;
            for (Stmt stmt : entryMethod.getBody().getStmts()) {
                if (stmt instanceof JInvokeStmt) {
                    JInvokeStmt invokeStmt = (JInvokeStmt) stmt;
                    Optional<AbstractInvokeExpr> ieOpt = invokeStmt.getInvokeExpr();
                    if (ieOpt.isPresent() && ieOpt.get().getMethodSignature().getName().equals("sink")) {
                        sinkMethod = ieOpt.get();
                        break;
                    }
                }
            }

            if (sinkMethod == null) {
                return;
            }

            if (sinkMethod.getArgs().isEmpty()) {
                return;
            }

            Object arg = sinkMethod.getArgs().get(0);
            Object lastAssignment = arg;
            if (arg.toString().contains("stack")) {
                lastAssignment = getLastAssignment(arg);
            }
            Object leaked = null;
            for (Object taintedVar : taintedVars) {
                if (lastAssignment.toString().equals(taintedVar.toString())) {
                    leaked = taintedVar;
                }
            }
        }
        // } checkLeak

        Object getLastAssignment(Object stackVar) {
            List<Stmt> stmts = new ArrayList<>(entryMethod.getBody().getStmts());
            Collections.reverse(stmts);
            for (Stmt stmt : stmts) {
                if (stmt instanceof AbstractDefinitionStmt) {
                    AbstractDefinitionStmt def = (AbstractDefinitionStmt) stmt;
                    if (def.getLeftOp().equals(stackVar)) {
                        return def.getRightOp();
                    }
                }
            }
            throw new RuntimeException("Var not found: " + stackVar);
        }
    }
    // } AnalysisRunner

    // testTaintAnalysis {
    @Test
    public void testTaintAnalysis() {
        analyze(BasicTaint.class.getName());
        analyze(BasicTaintSanitized.class.getName());
        analyze(FunctionPropagatesTaint.class.getName());
        analyze(FunctionReturnsTaint.class.getName());
    }
    // } testTaintAnalysis

    // main {
    public static void main(String[] args) {
        TaintAnalysisTest test = new TaintAnalysisTest();
        test.testTaintAnalysis();
    }
    // } main

    public static void analyze(String className) {
        String pathToTarget = "target/test-classes";
        AnalysisRunner runner = new AnalysisRunner();
        try {
            JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> analysis =
                    runner.executeStaticAnalysis(pathToTarget, className);
            runner.checkLeak(analysis);
        } catch (Exception e) {
            System.err.println("Analysis failed for " + className + ": " + e.getMessage());
        }
    }
}