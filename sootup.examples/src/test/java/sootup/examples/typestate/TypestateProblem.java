package sootup.examples.typestate;

import heros.DefaultSeeds;
import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.MeetLattice;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sootup.analysis.interprocedural.ide.DefaultJimpleIDETabulationProblem;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;

/**
 * A typestate analysis expressed as an IDE problem.
 *
 * <p>The data-flow facts {@code D} are Jimple {@link Value}s — the locals that currently point to a
 * tracked object. The values {@code V} are {@link TypestateFact}s — the automaton state each of
 * those objects is in. Splitting the problem this way is what IDE buys over IFDS: IFDS alone could
 * answer "is this local a FileHandle we care about?", but not "which state is it in?".
 */
public class TypestateProblem
    extends DefaultJimpleIDETabulationProblem<
        Value, TypestateFact, InterproceduralCFG<Stmt, SootMethod>> {

  private final InterproceduralCFG<Stmt, SootMethod> icfg;
  private final SootMethod entryMethod;
  private final Map<ClassType, Typestate> rules;

  /**
   * @param rules one automaton per API class whose protocol should be checked.
   */
  public TypestateProblem(
      InterproceduralCFG<Stmt, SootMethod> icfg,
      SootMethod entryMethod,
      Map<ClassType, Typestate> rules) {
    super(icfg);
    this.icfg = icfg;
    this.entryMethod = entryMethod;
    this.rules = rules;
  }

  // --8<-- [start:zero-and-seeds]
  /**
   * The artificial "zero" fact. It is always reachable, so it is the fact that other facts are
   * generated out of — see {@link #getNormalFlow} below, where the tracked local is generated from
   * the zero fact at the {@code new} statement.
   */
  @Override
  protected Value createZeroValue() {
    return new Local("<<zero>>", NullType.getInstance());
  }

  /** Where the analysis starts: the zero fact, at the first statement of the entry method. */
  @Override
  public Map<Stmt, Set<Value>> initialSeeds() {
    return DefaultSeeds.make(
        Collections.singleton(entryMethod.getBody().getControlFlowGraph().getStartingStmt()),
        zeroValue());
  }

  // --8<-- [end:zero-and-seeds]

  // --8<-- [start:lattice]
  /** Before anything is known, every value is top. */
  @Override
  protected EdgeFunction<TypestateFact> createAllTopFunction() {
    return new AllTop<>(TypestateFact.TOP);
  }

  @Override
  protected MeetLattice<TypestateFact> createMeetLattice() {
    return new MeetLattice<TypestateFact>() {

      @Override
      public TypestateFact topElement() {
        return TypestateFact.TOP;
      }

      @Override
      public TypestateFact bottomElement() {
        return TypestateFact.BOTTOM;
      }

      @Override
      public TypestateFact meet(TypestateFact left, TypestateFact right) {
        if (left.equals(right)) {
          return left;
        }
        if (left == TypestateFact.TOP) {
          return right;
        }
        if (right == TypestateFact.TOP) {
          return left;
        }
        // a violation on one path is a violation, even if another path is fine
        if (left == TypestateFact.ERROR || right == TypestateFact.ERROR) {
          return TypestateFact.ERROR;
        }
        // two different states: the analysis cannot say which one holds here
        return TypestateFact.BOTTOM;
      }
    };
  }

  // --8<-- [end:lattice]

  // --8<-- [start:flow-factory]
  @Override
  protected FlowFunctions<Stmt, Value, SootMethod> createFlowFunctionsFactory() {
    return new FlowFunctions<Stmt, Value, SootMethod>() {

      @Override
      public FlowFunction<Value> getNormalFlowFunction(Stmt curr, Stmt succ) {
        return getNormalFlow(curr);
      }

      @Override
      public FlowFunction<Value> getCallFlowFunction(Stmt callStmt, SootMethod destinationMethod) {
        return getCallFlow(callStmt, destinationMethod);
      }

      @Override
      public FlowFunction<Value> getReturnFlowFunction(
          Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
        return getReturnFlow(callSite, calleeMethod, exitStmt);
      }

      @Override
      public FlowFunction<Value> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
        return getCallToReturnFlow(callSite);
      }
    };
  }

  // --8<-- [end:flow-factory]

  // --8<-- [start:normal-flow]
  /**
   * Inside a method body. A tracked object is born at {@code l = new C()}; an assignment between
   * locals makes the analysis follow the new name as well.
   */
  FlowFunction<Value> getNormalFlow(Stmt curr) {
    if (!(curr instanceof JAssignStmt)) {
      return Identity.v();
    }
    JAssignStmt assign = (JAssignStmt) curr;
    Value leftOp = assign.getLeftOp();
    Value rightOp = assign.getRightOp();

    if (rightOp instanceof JNewExpr && rules.containsKey(rightOp.getType())) {
      // generate the fact out of the zero fact; the edge function supplies the initial state
      return new Gen<>(leftOp, zeroValue());
    }
    if (rightOp instanceof Local) {
      return new AliasFlowFunction(leftOp, rightOp);
    }
    if (rightOp instanceof JCastExpr) {
      return new AliasFlowFunction(leftOp, ((JCastExpr) rightOp).getOp());
    }
    // anything else overwrites the left-hand side with a value we do not track
    return new AliasFlowFunction(leftOp, null);
  }

  // --8<-- [end:normal-flow]

  // --8<-- [start:call-flow]
  /**
   * Entering a callee. Arguments are renamed to the callee's parameter locals so that the tracked
   * object keeps being followed inside. Calls to the API itself are not entered at all — they are
   * summarised on the call-to-return edge instead, which is where the protocol step happens.
   */
  FlowFunction<Value> getCallFlow(Stmt callStmt, SootMethod destinationMethod) {
    if (rules.containsKey(destinationMethod.getDeclaringClassType())
        || !destinationMethod.hasBody()) {
      return KillAll.v();
    }
    AbstractInvokeExpr invokeExpr = callStmt.asInvokableStmt().getInvokeExpr().get();
    List<Immediate> args = invokeExpr.getArgs();
    List<Value> parameterLocals = parameterLocalsOf(destinationMethod);

    return source -> {
      Set<Value> result = new HashSet<>();
      for (int i = 0; i < args.size() && i < parameterLocals.size(); i++) {
        if (args.get(i).equals(source)) {
          result.add(parameterLocals.get(i));
        }
      }
      return result;
    };
  }

  // --8<-- [end:call-flow]

  // --8<-- [start:return-flow]
  /**
   * Leaving a callee. Everything local to the callee is killed; only the parameters — mapped back
   * onto the caller's arguments — and the returned value survive. Because the edge function on this
   * edge is the identity, the state the object reached inside the callee travels back with it.
   */
  FlowFunction<Value> getReturnFlow(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt) {
    AbstractInvokeExpr invokeExpr = callSite.asInvokableStmt().getInvokeExpr().get();
    List<Immediate> args = invokeExpr.getArgs();
    List<Value> parameterLocals = parameterLocalsOf(calleeMethod);
    Value returnedOp = exitStmt instanceof JReturnStmt ? ((JReturnStmt) exitStmt).getOp() : null;
    Value assignedTo =
        callSite instanceof JAssignStmt ? ((JAssignStmt) callSite).getLeftOp() : null;

    return source -> {
      Set<Value> result = new HashSet<>();
      for (int i = 0; i < args.size() && i < parameterLocals.size(); i++) {
        if (parameterLocals.get(i).equals(source)) {
          result.add(args.get(i));
        }
      }
      if (returnedOp != null && assignedTo != null && returnedOp.equals(source)) {
        result.add(assignedTo);
      }
      return result;
    };
  }

  // --8<-- [end:return-flow]

  // --8<-- [start:call-to-return-flow]
  /**
   * Flowing around a call. For an API call the receiver has to survive, because the edge function
   * on this very edge performs the protocol step. For any other call the facts that were handed to
   * the callee are killed here — they come back through the return edge, and keeping both copies
   * would hide the effect the callee had on them.
   */
  FlowFunction<Value> getCallToReturnFlow(Stmt callSite) {
    if (ruleFor(callSite) != null || icfg.getCalleesOfCallAt(callSite).isEmpty()) {
      return Identity.v();
    }
    List<Immediate> args = callSite.asInvokableStmt().getInvokeExpr().get().getArgs();
    return source -> {
      for (Immediate arg : args) {
        if (arg.equals(source)) {
          return Collections.emptySet();
        }
      }
      return Collections.singleton(source);
    };
  }

  // --8<-- [end:call-to-return-flow]

  // --8<-- [start:edge-factory]
  @Override
  protected EdgeFunctions<Stmt, Value, SootMethod, TypestateFact> createEdgeFunctionsFactory() {
    return new EdgeFunctions<Stmt, Value, SootMethod, TypestateFact>() {

      /** {@code l = new C()} starts the automaton for the new object. */
      @Override
      public EdgeFunction<TypestateFact> getNormalEdgeFunction(
          Stmt curr, Value currNode, Stmt succ, Value succNode) {
        if (curr instanceof JAssignStmt) {
          JAssignStmt assign = (JAssignStmt) curr;
          Typestate automaton = rules.get(assign.getRightOp().getType());
          if (assign.getRightOp() instanceof JNewExpr
              && automaton != null
              && currNode.equals(zeroValue())
              && succNode.equals(assign.getLeftOp())) {
            return TypestateEdgeFunction.generating(automaton);
          }
        }
        return EdgeIdentity.v();
      }

      /** {@code receiver.m()} on a tracked class is one step of the protocol. */
      @Override
      public EdgeFunction<TypestateFact> getCallToReturnEdgeFunction(
          Stmt callSite, Value callNode, Stmt returnSite, Value returnSideNode) {
        Typestate automaton = ruleFor(callSite);
        if (automaton == null) {
          return EdgeIdentity.v();
        }
        AbstractInvokeExpr invokeExpr = callSite.asInvokableStmt().getInvokeExpr().get();
        String methodName = invokeExpr.getMethodSignature().getName();
        if ("<init>".equals(methodName) || !(invokeExpr instanceof AbstractInstanceInvokeExpr)) {
          // the constructor creates the object, it is not a step of the protocol
          return EdgeIdentity.v();
        }
        Local receiver = ((AbstractInstanceInvokeExpr) invokeExpr).getBase();
        if (!receiver.equals(callNode) || !receiver.equals(returnSideNode)) {
          // some other tracked object flows past this call untouched
          return EdgeIdentity.v();
        }
        return TypestateEdgeFunction.transition(automaton, methodName);
      }

      /** Call and return edges only rename facts; the state travels along unchanged. */
      @Override
      public EdgeFunction<TypestateFact> getCallEdgeFunction(
          Stmt callStmt, Value srcNode, SootMethod destinationMethod, Value destNode) {
        return EdgeIdentity.v();
      }

      @Override
      public EdgeFunction<TypestateFact> getReturnEdgeFunction(
          Stmt callSite,
          SootMethod calleeMethod,
          Stmt exitStmt,
          Value exitNode,
          Stmt returnSite,
          Value retNode) {
        return EdgeIdentity.v();
      }
    };
  }

  // --8<-- [end:edge-factory]

  /** The automaton registered for the class whose method is invoked here, or {@code null}. */
  private Typestate ruleFor(Stmt callSite) {
    if (!callSite.isInvokableStmt() || !callSite.asInvokableStmt().getInvokeExpr().isPresent()) {
      return null;
    }
    ClassType declaringClass =
        callSite.asInvokableStmt().getInvokeExpr().get().getMethodSignature().getDeclClassType();
    return rules.get(declaringClass);
  }

  private static List<Value> parameterLocalsOf(SootMethod method) {
    List<Value> parameterLocals = new ArrayList<>();
    for (int i = 0; i < method.getParameterCount(); i++) {
      parameterLocals.add(method.getBody().getParameterLocal(i));
    }
    return parameterLocals;
  }
}
