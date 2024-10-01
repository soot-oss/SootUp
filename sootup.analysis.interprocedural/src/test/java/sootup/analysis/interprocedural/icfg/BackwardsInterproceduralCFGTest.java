package sootup.analysis.interprocedural.icfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
class BackwardsInterproceduralCFGTest {
  protected static JavaView view;
  protected static MethodSignature entryMethodSignature;
  protected static SootMethod entryMethod;

  @BeforeAll
  public static void setup() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation("src/test/resources/icfg/binary"));
    view = new JavaView(inputLocations);
    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature =
        identifierFactory.getClassType("ICFGExampleForInvokableStmt");
    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();
    entryMethodSignature = entryMethod.getSignature();
  }

  @Test
  void methodStartAndEndPointTest() {
    JimpleBasedInterproceduralCFG forwardICFG =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);

    BackwardsInterproceduralCFG backwardsInterproceduralCFG =
        new BackwardsInterproceduralCFG(forwardICFG);

    assertEquals(
        forwardICFG.getStartPointsOf(entryMethod),
        backwardsInterproceduralCFG.getEndPointsOf(entryMethod));
    assertEquals(
        forwardICFG.getEndPointsOf(entryMethod),
        backwardsInterproceduralCFG.getStartPointsOf(entryMethod));
  }

  @Test
  void methodToCallerStmtTest() {
    JimpleBasedInterproceduralCFG forwardICFG =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);

    BackwardsInterproceduralCFG backwardsInterproceduralCFG =
        new BackwardsInterproceduralCFG(forwardICFG);

    MethodSignature sig =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                "ICFGExampleForInvokableStmt",
                "foo",
                "void",
                Collections.singletonList("java.lang.String"));
    Optional<JavaSootMethod> methodOpt = view.getMethod(sig);
    assertTrue(methodOpt.isPresent());
    Collection<Stmt> callersOf = backwardsInterproceduralCFG.getCallersOf(methodOpt.get());
    assertEquals(3, callersOf.size());
    Set<MethodSignature> methodSignatures =
        callersOf.stream()
            .map(c -> c.asInvokableStmt().getInvokeExpr().get().getMethodSignature())
            .collect(Collectors.toSet());
    assertTrue(methodSignatures.contains(sig));
  }
}
