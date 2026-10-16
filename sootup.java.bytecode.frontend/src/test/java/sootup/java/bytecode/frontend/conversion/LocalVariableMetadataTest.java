package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.*;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;

/** Verifies immutable debug bindings and metadata preservation during core body operations. */
class LocalVariableMetadataTest {
  private static final Local X = Jimple.newSlotLocal("x", PrimitiveType.getInt(), 1);
  private static final LocalVariableInfo VARIABLE = new LocalVariableInfo("x", 1, "I");
  private static final LocalVariableScope SCOPE = LocalVariableScope.of(List.of(VARIABLE));
  private static final StmtPositionInfo POSITION = new SimpleStmtPositionInfo(10);
  private static final StmtPositionInfo SCOPED =
      DefaultStmtPositionInfoFactory.withLocalVariables(POSITION, SCOPE);

  private static JAssignStmt assign(int value, StmtPositionInfo position) {
    return new JAssignStmt(X, IntConstant.getInstance(value), position);
  }

  private static Body.BodyBuilder builder() {
    return Body.builder()
        .setMethodSignature(
            new JavaIdentifierFactory().getMethodSignature("Example", "test", "int", List.of()))
        .setLocals(new LinkedHashSet<>(List.of(X)));
  }

  /**
   * Checks defensive copying, read-only bindings, value equality, and empty versus unavailable
   * scopes.
   */
  @Test
  void scopesAreImmutableAndBindingsHaveValueEquality() {
    List<LocalVariableInfo> variables = new ArrayList<>(List.of(VARIABLE));
    LocalVariableScope scope = LocalVariableScope.of(variables);
    variables.clear();
    assertEquals(List.of(VARIABLE), scope.getVariables());
    assertSame(VARIABLE, scope.getVariables().get(0));
    assertEquals("x", VARIABLE.name());
    assertEquals(1, VARIABLE.slotIndex());
    assertEquals("I", VARIABLE.descriptor());
    assertThrows(UnsupportedOperationException.class, () -> scope.getVariables().clear());
    assertEquals(VARIABLE, new LocalVariableInfo("x", 1, "I"));
    assertSame(LocalVariableScope.empty(), LocalVariableScope.of(List.of()));
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(POSITION));
    assertNotNull(
        LocalVariableStmtPositionInfo.getLocalVariables(
            DefaultStmtPositionInfoFactory.withLocalVariables(
                POSITION, LocalVariableScope.empty())));
  }

  /**
   * Rewrites an assignment, changes its source line, and removes its scope without losing operand
   * positions.
   */
  @Test
  void withersRetainSharedScopeAndSourceUpdatesPreserveOperandPositions() {
    Position operand = new LinePosition(12);
    FullStmtPositionInfo full =
        DefaultStmtPositionInfoFactory.create(new LinePosition(10), new Position[] {operand});
    JAssignStmt original =
        assign(1, DefaultStmtPositionInfoFactory.withLocalVariables(full, SCOPE));
    JAssignStmt rewritten = original.withRValue(IntConstant.getInstance(2));
    assertSame(original.getPositionInfo(), rewritten.getPositionInfo());
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(rewritten));
    assertSame(
        operand,
        assertInstanceOf(FullStmtPositionInfo.class, rewritten.getPositionInfo())
            .getOperandPosition(0));
    Stmt sourceChanged =
        rewritten.withPositionInfo(
            rewritten.getPositionInfo().withStmtPosition(new LinePosition(20)));
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(sourceChanged));
    assertEquals(20, sourceChanged.getPositionInfo().getStmtPosition().getFirstLine());
    Stmt withoutScope = DefaultStmtPositionInfoFactory.withLocalVariables(original, null);
    FullStmtPositionInfo source =
        assertInstanceOf(FullStmtPositionInfo.class, withoutScope.getPositionInfo());
    assertSame(full.getStmtPosition(), source.getStmtPosition());
    assertSame(operand, source.getOperandPosition(0));
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(source));
  }

  /**
   * Renames a local and copies the body, checking both operations retain the original scope object.
   */
  @Test
  void replaceLocalAndBodyCopiesPreserveScopes() {
    Body.BodyBuilder builder = builder();
    var graph = builder.getControlFlowGraph();
    JAssignStmt a = assign(1, SCOPED);
    JReturnVoidStmt end = new JReturnVoidStmt(POSITION);
    graph.putEdge(a, end);
    graph.setStartingStmt(a);
    Local y = Jimple.newSlotLocal("y", PrimitiveType.getInt(), 1);
    builder.replaceLocal(X, y);
    Body body = builder.build();
    Stmt replaced = body.getStmts().get(0);
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(replaced));
    assertSame(y, ((JAssignStmt) replaced).getLeftOp());
    assertSame(
        SCOPE,
        LocalVariableStmtPositionInfo.getLocalVariables(
            Body.builder(body, Set.of()).getStmts().get(0)));
    assertEquals(body.getLocals(), Set.of(y));
  }

  /**
   * Deletes scoped nodes, checking survivor metadata is preserved and the unscoped return stays
   * unscoped.
   */
  @Test
  void deletingNodesPreservesRemainingStatementScopes() {
    Body.BodyBuilder builder = builder();
    var graph = builder.getControlFlowGraph();
    JAssignStmt p = assign(0, POSITION), a = assign(1, SCOPED), b = assign(2, SCOPED);
    JReturnVoidStmt end = new JReturnVoidStmt(POSITION);
    graph.putEdge(p, a);
    graph.putEdge(a, b);
    graph.putEdge(b, end);
    graph.setStartingStmt(p);
    graph.removeNode(a);
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(b));
    graph.removeNode(b);
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(end));
  }

  /**
   * Replaces a statement and deletes its block; the replacement keeps its scope and the successor
   * stays unscoped.
   */
  @Test
  void replacingThenRemovingABlockDoesNotLeakItsScope() {
    Body.BodyBuilder builder = builder();
    var graph = (MutableBlockControlFlowGraph) builder.getControlFlowGraph();
    JGotoStmt before = new JGotoStmt(POSITION), jump = new JGotoStmt(SCOPED);
    JAssignStmt a = assign(1, SCOPED), b = assign(2, SCOPED);
    JReturnVoidStmt end = new JReturnVoidStmt(POSITION);
    graph.setStartingStmt(before);
    graph.putEdge(before, JGotoStmt.BRANCH_IDX, a);
    graph.putEdge(a, b);
    graph.putEdge(b, jump);
    graph.putEdge(jump, JGotoStmt.BRANCH_IDX, end);
    JAssignStmt replacement = a.withRValue(IntConstant.getInstance(3));
    graph.replaceNode(a, replacement);
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(replacement));
    graph.removeBlock(graph.getBlockOf(replacement));
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(end));
  }

  /**
   * Moves a scoped statement past an unscoped statement, checking metadata stays with each
   * statement.
   */
  @Test
  void movedStatementsPreserveScopes() {
    Body.BodyBuilder builder = builder();
    var graph = builder.getControlFlowGraph();
    JAssignStmt a = assign(1, SCOPED), b = assign(2, SCOPED), gap = assign(3, POSITION);
    JReturnVoidStmt end = new JReturnVoidStmt(POSITION);
    graph.setStartingStmt(a);
    graph.putEdge(a, b);
    graph.putEdge(b, gap);
    graph.putEdge(gap, end);
    graph.removeNode(b);
    graph.insertBefore(end, b);
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(a));
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(b));
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(gap));
    assertNull(LocalVariableStmtPositionInfo.getLocalVariables(end));
  }

  /**
   * Checks attaching equal bindings through separate scope objects preserves each object's
   * identity.
   */
  @Test
  void scopesWithIdenticalNamesAndSlotsRemainDistinct() {
    LocalVariableScope second = LocalVariableScope.of(List.of(new LocalVariableInfo("x", 1, "I")));
    JAssignStmt a = assign(1, SCOPED),
        b = assign(2, DefaultStmtPositionInfoFactory.withLocalVariables(POSITION, second));
    assertNotSame(
        LocalVariableStmtPositionInfo.getLocalVariables(a),
        LocalVariableStmtPositionInfo.getLocalVariables(b));
  }
}
