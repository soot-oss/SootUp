package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.interceptor.RunTimeBodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalVariableStmtPositionInfo;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.*;
import sootup.core.types.PrimitiveType;
import sootup.core.types.UnknownType;
import sootup.interceptors.*;
import sootup.interceptors.typeresolving.*;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/**
 * Exercises body transformations with distinct scopes, including bindings absent from optimized
 * locals.
 */
class LocalVariableMetadataPropagationTest {
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();
  private static final JavaView VIEW = new JavaView(Collections.emptyList());
  private static final Local X = new Local("x", PrimitiveType.getInt(), 0);
  private static final Local Y = new Local("y", PrimitiveType.getInt(), 1);

  private static StmtPositionInfo position(int line) {
    return new LocalVariableStmtPositionInfo.Full(
        new LinePosition(line),
        new Position[] {new LinePosition(line + 100)},
        LocalVariableScope.of(List.of(new LocalVariableInfo("debug" + line, 7, "I"))));
  }

  private static Body.BodyBuilder builder(Local... locals) {
    return Body.builder()
        .setMethodSignature(FACTORY.getMethodSignature("Example", "test", "int", List.of()))
        .setModifiers(EnumSet.of(MethodModifier.STATIC))
        .setLocals(new LinkedHashSet<>(Arrays.asList(locals)));
  }

  private static void linear(Body.BodyBuilder builder, Stmt... stmts) {
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    graph.setStartingStmt(stmts[0]);
    for (int i = 0; i + 1 < stmts.length; i++) {
      graph.putEdge((FallsThroughStmt) stmts[i], stmts[i + 1]);
    }
  }

  private static void assertMetadata(StmtPositionInfo expected, Stmt actual) {
    assertSame(expected, actual.getPositionInfo(), actual.toString());
    assertSame(
        LocalVariableStmtPositionInfo.getLocalVariables(expected),
        LocalVariableStmtPositionInfo.getLocalVariables(actual));
    assertSame(expected.getOperandPosition(0), actual.getPositionInfo().getOperandPosition(0));
  }

  /**
   * Replaces a switch with no cases by a goto, retaining switch metadata and the destination
   * return's own metadata.
   */
  @Test
  void emptySwitchReplacementKeepsSwitchScope() {
    Body.BodyBuilder builder = builder(X);
    StmtPositionInfo switchPosition = position(10), returnPosition = position(20);
    JSwitchStmt sw = Jimple.newLookupSwitchStmt(X, List.of(), switchPosition);
    JReturnStmt ret = new JReturnStmt(X, returnPosition);
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    graph.setStartingStmt(sw);
    graph.putEdge(sw, 0, ret);

    new EmptySwitchEliminator().interceptBody(builder, VIEW);

    assertInstanceOf(JGotoStmt.class, graph.getStartingStmt());
    assertMetadata(switchPosition, graph.getStartingStmt());
    assertMetadata(returnPosition, ret);
  }

  /**
   * Runs copy propagation and constant folding, checking definitions and rewritten uses keep their
   * own scopes and operand positions.
   */
  @TestFactory
  Stream<DynamicTest> propagationAndFoldingKeepEachUseScope() {
    return Stream.<Supplier<BodyInterceptor>>of(
            CopyPropagator::new, ConstantPropagatorAndFolder::new)
        .map(
            factory -> {
              BodyInterceptor interceptor = factory.get();
              return DynamicTest.dynamicTest(
                  interceptor.getClass().getSimpleName(),
                  () -> {
                    Body.BodyBuilder builder = builder(X);
                    StmtPositionInfo defPosition = position(10), returnPosition = position(20);
                    JAssignStmt def =
                        new JAssignStmt(
                            X,
                            Jimple.newAddExpr(
                                IntConstant.getInstance(2), IntConstant.getInstance(3)),
                            defPosition);
                    JReturnStmt ret = new JReturnStmt(X, returnPosition);
                    linear(builder, def, ret);

                    interceptor.interceptBody(builder, VIEW);

                    List<Stmt> stmts = builder.getStmts();
                    if (interceptor instanceof ConstantPropagatorAndFolder) {
                      assertEquals(
                          IntConstant.getInstance(5), ((JAssignStmt) stmts.get(0)).getRightOp());
                      assertNotSame(def, stmts.get(0));
                    }
                    assertEquals(IntConstant.getInstance(5), ((JReturnStmt) stmts.get(1)).getOp());
                    assertNotSame(ret, stmts.get(1));
                    assertMetadata(defPosition, stmts.get(0));
                    assertMetadata(returnPosition, stmts.get(1));
                  });
            });
  }

  /**
   * Inlines a definition into its use, combining the definition's line with the destination's scope
   * and operand positions.
   */
  @Test
  void aggregatorTakesDefinitionLineAndDestinationScopeAndOperandCoordinates() {
    Body.BodyBuilder builder = builder(X, Y);
    StmtPositionInfo defPosition = position(10), usePosition = position(20);
    JAssignStmt def = new JAssignStmt(X, IntConstant.getInstance(5), defPosition);
    JAssignStmt use = new JAssignStmt(Y, X, usePosition);
    JReturnStmt ret = new JReturnStmt(Y, position(30));
    linear(builder, def, use, ret);

    new Aggregator().interceptBody(builder, VIEW);

    Stmt aggregated = builder.getStmts().get(0);
    assertEquals(IntConstant.getInstance(5), ((JAssignStmt) aggregated).getRightOp());
    assertEquals(10, aggregated.getPositionInfo().getStmtPosition().getFirstLine());
    assertSame(
        LocalVariableStmtPositionInfo.getLocalVariables(usePosition),
        LocalVariableStmtPositionInfo.getLocalVariables(aggregated));
    assertSame(
        usePosition.getOperandPosition(0), aggregated.getPositionInfo().getOperandPosition(0));
  }

  /**
   * Inlines a goto/cast/return sequence, checking the cloned cast and return retain their original
   * metadata.
   */
  @Test
  void castAndReturnClonesKeepTheirOwnSourceScopes() {
    Local object = new Local("object", FACTORY.getClassType("java.lang.Object"), 0);
    Local string = new Local("string", FACTORY.getClassType("java.lang.String"), 1);
    Body.BodyBuilder builder = builder(object, string);
    StmtPositionInfo castPosition = position(30), returnPosition = position(40);
    JAssignStmt def = new JAssignStmt(object, NullConstant.getInstance(), position(10));
    JGotoStmt jump = new JGotoStmt(position(20));
    JAssignStmt cast =
        new JAssignStmt(string, Jimple.newCastExpr(object, string.getType()), castPosition);
    JReturnStmt ret = new JReturnStmt(string, returnPosition);
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    graph.setStartingStmt(def);
    graph.putEdge(def, jump);
    graph.putEdge(jump, JGotoStmt.BRANCH_IDX, cast);
    graph.putEdge(cast, ret);

    new CastAndReturnInliner().interceptBody(builder, VIEW);

    List<Stmt> stmts = builder.getStmts();
    assertEquals(3, stmts.size());
    assertFalse(graph.containsNode(jump));
    assertMetadata(castPosition, stmts.get(1));
    assertMetadata(returnPosition, stmts.get(2));
  }

  /**
   * Runs LocalSplitter, LocalPacker, and LocalNameStandardizer, checking replacement statements
   * retain their original metadata.
   */
  @TestFactory
  Stream<DynamicTest> localRewritersKeepAllStatementScopes() {
    return Stream.<Supplier<BodyInterceptor>>of(
            LocalSplitter::new, LocalPacker::new, LocalNameStandardizer::new)
        .map(
            factory -> {
              BodyInterceptor interceptor = factory.get();
              return DynamicTest.dynamicTest(
                  interceptor.getClass().getSimpleName(),
                  () -> {
                    Body.BodyBuilder builder = builder(X, Y);
                    JAssignStmt a = new JAssignStmt(X, IntConstant.getInstance(1), position(10));
                    JAssignStmt b = new JAssignStmt(Y, X, position(20));
                    JAssignStmt c = new JAssignStmt(X, IntConstant.getInstance(2), position(30));
                    JReturnStmt ret = new JReturnStmt(X, position(40));
                    List<Stmt> original = List.of(a, b, c, ret);
                    linear(builder, a, b, c, ret);

                    interceptor.interceptBody(builder, VIEW);

                    List<Stmt> changed = builder.getStmts();
                    assertEquals(original.size(), changed.size());
                    assertNotSame(a, changed.get(0), "Exercise replacement rather than a no-op");
                    for (int i = 0; i < original.size(); i++) {
                      assertMetadata(original.get(i).getPositionInfo(), changed.get(i));
                    }
                  });
            });
  }

  /**
   * Resolves an unknown local to int, checking replacement statements retain their original
   * metadata.
   */
  @Test
  void typeAssignerKeepsMetadataWhileReplacingUnknownLocals() {
    Local unknown = new Local("unknown", UnknownType.getInstance(), 0);
    Body.BodyBuilder builder = builder(unknown);
    JAssignStmt def = new JAssignStmt(unknown, IntConstant.getInstance(1000000), position(10));
    JReturnStmt ret = new JReturnStmt(unknown, position(20));
    linear(builder, def, ret);

    new TypeAssigner().interceptBody(builder, VIEW);

    List<Stmt> changed = builder.getStmts();
    assertEquals(PrimitiveType.getInt(), ((JAssignStmt) changed.get(0)).getLeftOp().getType());
    assertNotSame(def, changed.get(0));
    assertMetadata(def.getPositionInfo(), changed.get(0));
    assertMetadata(ret.getPositionInfo(), changed.get(1));
  }

  /**
   * Inserts a long-to-int cast for a call argument, checking the generated cast inherits the call's
   * metadata.
   */
  @Test
  void insertedTypeCastsInheritTheirUseScope() {
    Local value = new Local("value", PrimitiveType.getLong(), 0);
    Body.BodyBuilder builder = builder(value);
    StmtPositionInfo usePosition = position(20);
    JIdentityStmt def =
        new JIdentityStmt(value, Jimple.newParameterRef(PrimitiveType.getLong(), 0), position(10));
    JInvokeStmt use =
        Jimple.newInvokeStmt(
            Jimple.newStaticInvokeExpr(
                FACTORY.getMethodSignature("Example", "consume", "void", List.of("int")),
                List.of(value)),
            usePosition);
    JReturnVoidStmt ret = new JReturnVoidStmt(position(30));
    linear(builder, def, use, ret);
    Typing typing = new Typing(builder.getLocals());
    typing.set(value, PrimitiveType.getLong());
    CastCounter counter =
        new CastCounter(builder, new AugEvalFunction(VIEW), new BytecodeHierarchy(VIEW), typing);
    assertEquals(1, counter.getCastCount());

    counter.insertCastStmts();

    List<Stmt> changed = builder.getStmts();
    assertEquals(4, changed.size());
    assertInstanceOf(JAssignStmt.class, changed.get(1));
    assertMetadata(usePosition, changed.get(1));
    assertMetadata(usePosition, changed.get(2));
    assertMetadata(ret.getPositionInfo(), changed.get(3));
  }

  /**
   * Drops an unused invocation result, retaining the call as an invoke statement with the
   * assignment's metadata.
   */
  @Test
  void deadInvokeResultReplacementKeepsAssignmentScope() {
    Body.BodyBuilder builder = builder(X);
    StmtPositionInfo callPosition = position(10);
    JAssignStmt call =
        new JAssignStmt(
            X,
            Jimple.newStaticInvokeExpr(
                FACTORY.getMethodSignature("Example", "produce", "int", List.of()), List.of()),
            callPosition);
    JReturnVoidStmt ret = new JReturnVoidStmt(position(20));
    linear(builder, call, ret);

    new DeadAssignmentEliminator().interceptBody(builder, VIEW);

    assertInstanceOf(JInvokeStmt.class, builder.getStmts().get(0));
    assertMetadata(callPosition, builder.getStmts().get(0));
    assertMetadata(ret.getPositionInfo(), builder.getStmts().get(1));
  }

  /**
   * Removes a nop, unreachable return, and unused local, checking survivor scopes remain unchanged
   * without inheriting deleted metadata.
   */
  @Test
  void removalPassesKeepSurvivorScopesAndDoNotTransferDeletedScopes() {
    Body.BodyBuilder builder = builder(X, Y);
    JAssignStmt def = new JAssignStmt(X, IntConstant.getInstance(1), position(10));
    JNopStmt nop = new JNopStmt(position(20));
    JReturnStmt ret = new JReturnStmt(X, position(30));
    JReturnStmt unreachable = new JReturnStmt(IntConstant.getInstance(0), position(40));
    linear(builder, def, nop, ret);
    builder.getControlFlowGraph().addNode(unreachable);

    new NopEliminator().interceptBody(builder, VIEW);
    new UnreachableCodeEliminator().interceptBody(builder, VIEW);
    new UnusedLocalEliminator().interceptBody(builder, VIEW);

    assertEquals(List.of(def, ret), builder.getStmts());
    assertEquals(Set.of(X), builder.getLocals());
    assertMetadata(def.getPositionInfo(), builder.getStmts().get(0));
    assertMetadata(ret.getPositionInfo(), builder.getStmts().get(1));
  }

  /**
   * Folds always-true and always-false branches, checking the selected successor keeps its own
   * metadata.
   */
  @TestFactory
  Stream<DynamicTest> conditionalBranchRemovalKeepsChosenSuccessorScope() {
    return Stream.of(false, true)
        .map(
            taken ->
                DynamicTest.dynamicTest(
                    "taken=" + taken,
                    () -> {
                      Body.BodyBuilder builder = builder(X);
                      JAssignStmt def =
                          new JAssignStmt(X, IntConstant.getInstance(1), position(10));
                      JIfStmt branch =
                          new JIfStmt(
                              Jimple.newEqExpr(
                                  IntConstant.getInstance(0),
                                  IntConstant.getInstance(taken ? 0 : 1)),
                              position(20));
                      JReturnStmt fallThrough =
                          new JReturnStmt(IntConstant.getInstance(1), position(30));
                      JReturnStmt target =
                          new JReturnStmt(IntConstant.getInstance(2), position(40));
                      MutableControlFlowGraph graph = builder.getControlFlowGraph();
                      linear(builder, def, branch);
                      graph.putEdge(branch, JIfStmt.FALSE_BRANCH_IDX, fallThrough);
                      graph.putEdge(branch, JIfStmt.TRUE_BRANCH_IDX, target);

                      new ConditionalBranchFolder().interceptBody(builder, VIEW);

                      JReturnStmt chosen = taken ? target : fallThrough;
                      assertEquals(List.of(def, chosen), builder.getStmts());
                      assertMetadata(chosen.getPositionInfo(), builder.getStmts().get(1));
                    }));
  }

  /**
   * Runs CopyPropagator through RunTimeBodyInterceptor, checking delegation preserves the rewritten
   * return's metadata.
   */
  @Test
  void runtimeDelegatePreservesMetadata() {
    Body.BodyBuilder builder = builder(X);
    JAssignStmt def = new JAssignStmt(X, IntConstant.getInstance(1), position(10));
    JReturnStmt ret = new JReturnStmt(X, position(20));
    linear(builder, def, ret);

    new RunTimeBodyInterceptor(new CopyPropagator()).interceptBody(builder, VIEW);

    assertEquals(IntConstant.getInstance(1), ((JReturnStmt) builder.getStmts().get(1)).getOp());
    assertMetadata(ret.getPositionInfo(), builder.getStmts().get(1));
  }

  /**
   * Builds a diamond requiring one phi, checking it inherits join metadata while its statement
   * position remains unknown.
   */
  @Test
  void ssaPhiAssignmentsInheritJoinScope() {
    Body.BodyBuilder builder = builder(X, Y);
    StmtPositionInfo joinPosition = position(50);
    JIdentityStmt parameter =
        new JIdentityStmt(Y, Jimple.newParameterRef(PrimitiveType.getInt(), 0), position(10));
    JIfStmt branch = new JIfStmt(Jimple.newEqExpr(Y, IntConstant.getInstance(0)), position(20));
    JAssignStmt left = new JAssignStmt(X, IntConstant.getInstance(1), position(30));
    JGotoStmt jump = new JGotoStmt(position(35));
    JAssignStmt right = new JAssignStmt(X, IntConstant.getInstance(2), position(40));
    JReturnStmt join = new JReturnStmt(X, joinPosition);
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    linear(builder, parameter, branch);
    graph.putEdge(branch, JIfStmt.FALSE_BRANCH_IDX, left);
    graph.putEdge(branch, JIfStmt.TRUE_BRANCH_IDX, right);
    graph.putEdge(left, jump);
    graph.putEdge(jump, JGotoStmt.BRANCH_IDX, join);
    graph.putEdge(right, join);

    new StaticSingleAssignmentFormer().interceptBody(builder, VIEW);

    List<Stmt> changed = builder.getStmts();
    List<Stmt> phis = new ArrayList<>();
    for (Stmt stmt : changed) {
      if (stmt instanceof JAssignStmt && ((JAssignStmt) stmt).getRightOp() instanceof JPhiExpr) {
        phis.add(stmt);
      } else {
        assertNotNull(LocalVariableStmtPositionInfo.getLocalVariables(stmt), stmt.toString());
      }
    }
    assertEquals(1, phis.size(), "The diamond must exercise phi insertion");
    assertSame(
        LocalVariableStmtPositionInfo.getLocalVariables(joinPosition),
        LocalVariableStmtPositionInfo.getLocalVariables(phis.get(0)),
        "A phi inserted before the join must carry the join's captured debug scope");
    assertEquals(-1, phis.get(0).getPositionInfo().getStmtPosition().getFirstLine());
    assertSame(
        joinPosition.getOperandPosition(0), phis.get(0).getPositionInfo().getOperandPosition(0));
  }

  /**
   * Creates two phis for populated, empty, and unavailable join scopes, checking both share join
   * metadata and existing statements keep theirs.
   */
  @TestFactory
  Stream<DynamicTest> ssaMultiplePhiAssignmentsShareOriginalJoinScope() {
    return Stream.of("populated", "empty", "unavailable")
        .map(
            kind ->
                DynamicTest.dynamicTest(
                    "join scope=" + kind,
                    () -> {
                      StmtPositionInfo joinPosition =
                          kind.equals("populated")
                              ? position(50)
                              : kind.equals("empty")
                                  ? LocalVariableStmtPositionInfo.withStmtPositionInfo(
                                      new SimpleStmtPositionInfo(50), LocalVariableScope.empty())
                                  : new SimpleStmtPositionInfo(50);
                      Local condition = new Local("condition", PrimitiveType.getInt(), 2);
                      Local result = new Local("result", PrimitiveType.getInt(), 3);
                      Body.BodyBuilder builder = builder(X, Y, condition, result);
                      JIdentityStmt parameter =
                          new JIdentityStmt(
                              condition,
                              Jimple.newParameterRef(PrimitiveType.getInt(), 0),
                              position(10));
                      JIfStmt branch =
                          new JIfStmt(
                              Jimple.newEqExpr(condition, IntConstant.getInstance(0)),
                              position(20));
                      JAssignStmt leftX =
                          new JAssignStmt(X, IntConstant.getInstance(1), position(30));
                      JAssignStmt leftY =
                          new JAssignStmt(Y, IntConstant.getInstance(10), position(31));
                      JGotoStmt jump = new JGotoStmt(position(35));
                      JAssignStmt rightX =
                          new JAssignStmt(X, IntConstant.getInstance(2), position(40));
                      JAssignStmt rightY =
                          new JAssignStmt(Y, IntConstant.getInstance(20), position(41));
                      JAssignStmt join =
                          new JAssignStmt(result, Jimple.newAddExpr(X, Y), joinPosition);
                      JReturnStmt ret = new JReturnStmt(result, position(60));
                      List<Stmt> original =
                          List.of(parameter, branch, leftX, leftY, jump, rightX, rightY, join, ret);
                      Map<Integer, StmtPositionInfo> originalPositions = new HashMap<>();
                      for (Stmt stmt : original) {
                        originalPositions.put(
                            stmt.getPositionInfo().getStmtPosition().getFirstLine(),
                            stmt.getPositionInfo());
                      }
                      MutableControlFlowGraph graph = builder.getControlFlowGraph();
                      linear(builder, parameter, branch);
                      graph.putEdge(branch, JIfStmt.FALSE_BRANCH_IDX, leftX);
                      graph.putEdge(branch, JIfStmt.TRUE_BRANCH_IDX, rightX);
                      graph.putEdge(leftX, leftY);
                      graph.putEdge(leftY, jump);
                      graph.putEdge(jump, JGotoStmt.BRANCH_IDX, join);
                      graph.putEdge(rightX, rightY);
                      graph.putEdge(rightY, join);
                      graph.putEdge(join, ret);

                      new StaticSingleAssignmentFormer().interceptBody(builder, VIEW);

                      int phiCount = 0;
                      LocalVariableScope joinScope =
                          LocalVariableStmtPositionInfo.getLocalVariables(joinPosition);
                      for (Stmt stmt : builder.getStmts()) {
                        if (stmt instanceof JAssignStmt
                            && ((JAssignStmt) stmt).getRightOp() instanceof JPhiExpr) {
                          phiCount++;
                          assertSame(
                              joinScope, LocalVariableStmtPositionInfo.getLocalVariables(stmt));
                          assertEquals(-1, stmt.getPositionInfo().getStmtPosition().getFirstLine());
                          assertSame(
                              joinPosition.getOperandPosition(0),
                              stmt.getPositionInfo().getOperandPosition(0));
                        } else {
                          assertMetadata(
                              originalPositions.get(
                                  stmt.getPositionInfo().getStmtPosition().getFirstLine()),
                              stmt);
                        }
                      }
                      assertEquals(2, phiCount, "Both locals must require a phi at the join");
                      assertEquals(original.size() + 2, builder.getStmts().size());
                    }));
  }

  /**
   * Removes scoped and unscoped nops, checking the assignment retains its binding and the unscoped
   * return acquires none.
   */
  @Test
  void testLocalVariablesRemappedWhenNopRemoved() {
    StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
    Local x = new Local("x", PrimitiveType.getInt(), 0);
    StmtPositionInfo scoped =
        LocalVariableStmtPositionInfo.withStmtPositionInfo(
            pos, LocalVariableScope.of(List.of(new LocalVariableInfo("x", 0, "I"))));
    JNopStmt nop1 = new JNopStmt(scoped);
    JAssignStmt assign = Jimple.newAssignStmt(x, IntConstant.getInstance(42), scoped);
    JNopStmt nop2 = new JNopStmt(pos);
    JReturnVoidStmt ret = new JReturnVoidStmt(pos);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("com.example.Test", "foo", "void", Collections.emptyList()));
    builder.setLocals(new LinkedHashSet<>(Collections.singleton(x)));

    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    cfg.setStartingStmt(nop1);
    cfg.putEdge(nop1, assign);
    cfg.putEdge(assign, nop2);
    cfg.putEdge(nop2, ret);

    NopEliminator eliminator = new NopEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    assertEquals(2, builder.getStmts().size());
    assertEquals(assign, builder.getStmts().get(0));
    assertEquals(ret, builder.getStmts().get(1));
    assertEquals(
        List.of(new LocalVariableInfo("x", 0, "I")),
        LocalVariableStmtPositionInfo.getLocalVariables(builder.getStmts().get(0)).getVariables());
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(builder.getStmts().get(1)));
  }

  /**
   * Aggregates assignments with simple positions, adopting the definition's line while retaining
   * destination bindings.
   */
  @Test
  void aggregationKeepsDestinationBindingsWhenCopyingTheDefinitionLine() {
    LocalVariableScope definitionScope =
        LocalVariableScope.of(List.of(new LocalVariableInfo("before", 0, "I")));
    LocalVariableScope useScope =
        LocalVariableScope.of(List.of(new LocalVariableInfo("after", 0, "I")));
    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());
    var definition =
        Jimple.newAssignStmt(
            a,
            IntConstant.getInstance(7),
            LocalVariableStmtPositionInfo.withStmtPositionInfo(
                new SimpleStmtPositionInfo(10), definitionScope));
    var use =
        Jimple.newAssignStmt(
            b,
            a,
            LocalVariableStmtPositionInfo.withStmtPositionInfo(
                new SimpleStmtPositionInfo(20), useScope));
    var ret = Jimple.newReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Body.BodyBuilder builder = Body.builder().setLocals(new LinkedHashSet<>(List.of(a, b)));
    var graph = builder.getControlFlowGraph();
    graph.setStartingStmt(definition);
    graph.putEdge(definition, use);
    graph.putEdge(use, ret);
    new Aggregator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Stmt merged = builder.getStmts().get(0);
    assertEquals("b = 7", merged.toString());
    assertSame(useScope, LocalVariableStmtPositionInfo.getLocalVariables(merged));
    assertEquals(10, merged.getPositionInfo().getStmtPosition().getFirstLine());
  }
}
