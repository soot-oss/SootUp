package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * The ICFG registers the statements of the methods it is told to register eagerly when it is
 * created, and those of the other methods when they are first needed.
 */
public class LazyStmtOwnerTest {

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));

  private SootMethod method(String name) {
    return view
        .getClass(view.getIdentifierFactory().getClassType("ICFGExample"))
        .get()
        .getMethods()
        .stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .get();
  }

  private final SootMethod entry = method("entryPoint");
  private final SootMethod thirdMethod = method("thirdMethod");
  private final CallGraph cg =
      new ClassHierarchyAnalysisAlgorithm(view)
          .initialize(Collections.singletonList(entry.getSignature()));

  /** Records the methods whose statements the ICFG registers. */
  private JimpleBasedInterproceduralCFG icfg(
      List<String> registered, Predicate<SootMethod> registerEagerly) {
    return new JimpleBasedInterproceduralCFG(cg, view, false, false, registerEagerly) {
      @Override
      public void initializeStmtToOwner(SootMethod m) {
        registered.add(m.getName());
        super.initializeStmtToOwner(m);
      }
    };
  }

  @Test
  public void methodIsRegisteredWhenItsStatementsAreRequested() {
    assertTrue(cg.containsMethod(thirdMethod.getSignature()));
    List<String> registered = Collections.synchronizedList(new ArrayList<>());
    JimpleBasedInterproceduralCFG icfg = icfg(registered, m -> !m.equals(thirdMethod));

    assertTrue(registered.contains("entryPoint"));
    assertFalse(registered.contains("thirdMethod"));

    Stmt start = icfg.getStartPointsOf(thirdMethod).iterator().next();
    assertEquals(thirdMethod, icfg.getMethodOf(start));
    icfg.getStartPointsOf(thirdMethod);
    assertEquals(1, Collections.frequency(registered, "thirdMethod"));
  }

  @Test
  public void unknownStatementRegistersTheRemainingMethods() {
    List<String> registered = Collections.synchronizedList(new ArrayList<>());
    JimpleBasedInterproceduralCFG icfg = icfg(registered, m -> !m.equals(thirdMethod));

    // a statement that the client took from the body directly
    Stmt stmt = thirdMethod.getBody().getStmts().get(0);
    assertEquals(thirdMethod, icfg.getMethodOf(stmt));
    assertTrue(icfg.isReachable(stmt));
    assertEquals(1, Collections.frequency(registered, "thirdMethod"));
  }

  @Test
  public void callersAreRegisteredWithoutTheOtherMethods() {
    SootMethod secondMethod = method("secondMethod");
    List<String> registered = Collections.synchronizedList(new ArrayList<>());
    JimpleBasedInterproceduralCFG icfg =
        icfg(registered, m -> !m.equals(secondMethod) && !m.equals(thirdMethod));

    Stmt call = icfg.getCallersOf(thirdMethod).iterator().next();
    assertEquals(secondMethod, icfg.getMethodOf(call));
    assertEquals(1, Collections.frequency(registered, "secondMethod"));
    assertFalse(registered.contains("thirdMethod"));
  }

  @Test
  public void allMethodsAreRegisteredEagerlyByDefault() {
    List<String> registered = Collections.synchronizedList(new ArrayList<>());
    icfg(registered, m -> true);
    assertTrue(registered.contains("entryPoint"));
    assertTrue(registered.contains("thirdMethod"));
  }
}
