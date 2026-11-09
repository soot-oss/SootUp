package sootup.examples.typestate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import heros.InterproceduralCFG;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** Runs the typestate analysis of {@link TypestateProblem} on {@code resources/Typestate}. */
public class TypestateAnalysisTest {

  // --8<-- [start:automaton]
  /** CLOSED --open--> OPEN --write--> OPEN --close--> CLOSED */
  static Typestate fileHandleProtocol() {
    Typestate.Builder builder = Typestate.builder();
    int closed = builder.addState("CLOSED");
    int open = builder.addState("OPEN");

    builder.setInitialState(closed);
    builder.addTransition(closed, "open", open);
    builder.addTransition(open, "write", open);
    builder.addTransition(open, "close", closed);
    builder.setAccepting(closed, true);

    return builder.build();
  }

  // --8<-- [end:automaton]

  // --8<-- [start:setup]
  /** Loads the target program, builds the ICFG and solves the IDE problem for one entry method. */
  static Map<Value, TypestateFact> analyze(String entryMethodName) {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/Typestate/binary", SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    ClassType exampleType = view.getIdentifierFactory().getClassType("Example");
    SootClass exampleClass = view.getClass(exampleType).get();
    SootMethod entryMethod =
        exampleClass.getMethods().stream()
            .filter(m -> m.getName().equals(entryMethodName))
            .findFirst()
            .get();

    // the ICFG builds a class-hierarchy call graph from the given entry points
    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethod.getSignature()), false, false);

    Map<ClassType, Typestate> rules = new HashMap<>();
    rules.put(view.getIdentifierFactory().getClassType("FileHandle"), fileHandleProtocol());

    TypestateProblem problem = new TypestateProblem(icfg, entryMethod, rules);
    JimpleIDESolver<Value, TypestateFact, InterproceduralCFG<Stmt, SootMethod>> solver =
        new JimpleIDESolver<>(problem);
    solver.solve();

    List<Stmt> stmts = entryMethod.getBody().getStmts();
    return solver.resultsAt(stmts.get(stmts.size() - 1));
  }

  // --8<-- [end:setup]

  // --8<-- [start:assertions]
  /** The value the analysis computed for the local with the given name. */
  static TypestateFact factFor(Map<Value, TypestateFact> results, String localName) {
    return results.entrySet().stream()
        .filter(e -> e.getKey().toString().equals(localName))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseThrow(() -> new AssertionError(localName + " was not tracked; got " + results));
  }

  @Test
  public void correctUsageEndsInAnAcceptingState() {
    Map<Value, TypestateFact> results = analyze("correctUsage");

    // l0 is the local holding the handle; write() happens inside writeGreeting(), so this only
    // works because the analysis follows the call
    TypestateFact handle = factFor(results, "l0");
    assertEquals("CLOSED", handle.toString());
    assertTrue(handle.isAccepting(), "the handle should end in a final state");
    assertFalse(results.containsValue(TypestateFact.ERROR), "no violation expected: " + results);
  }

  @Test
  public void violationIsReported() {
    Map<Value, TypestateFact> results = analyze("protocolViolation");

    // CLOSED has no write transition, so the call folds the value to ERROR and keeps it there
    assertEquals(TypestateFact.ERROR, factFor(results, "l0"));
  }

  @Test
  public void aliasesAreTrackedByName() {
    // $stack1 and l0 are the same object, but the analysis tracks names, not objects: it never
    // learns that a call on l0 also changes the state of $stack1. Removing this limitation needs
    // a pointer analysis.
    Map<Value, TypestateFact> results = analyze("protocolViolation");
    assertEquals("CLOSED", factFor(results, "$stack1").toString());
  }
  // --8<-- [end:assertions]
}
