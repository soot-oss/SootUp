package sootup.core.jimple.basic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LinePosition;
import sootup.core.model.LocalVariableInfo;
import sootup.core.model.LocalVariableScope;
import sootup.core.model.Position;

/** Verifies metadata preservation and factory dispatch for built-in and custom position info. */
class StmtPositionInfoFactoryTest {
  private static final Position STATEMENT = new LinePosition(10);
  private static final Position OPERAND = new LinePosition(11);
  private static final LocalVariableScope SCOPE =
      LocalVariableScope.of(List.of(new LocalVariableInfo("x", 1, "I")));
  private static final StmtPositionInfoFactory CUSTOM_FACTORY = new CustomFactory();

  /** Exercises position, scope and operand changes for all four built-in metadata variants. */
  @TestFactory
  Stream<DynamicTest> copiesPreserveIndependentMetadataCapabilities() {
    StmtPositionInfo plain = DefaultStmtPositionInfoFactory.create(STATEMENT);
    FullStmtPositionInfo full =
        DefaultStmtPositionInfoFactory.create(STATEMENT, new Position[] {OPERAND});
    return Stream.of(
            plain,
            full,
            DefaultStmtPositionInfoFactory.create(STATEMENT, SCOPE),
            DefaultStmtPositionInfoFactory.withLocalVariables(full, SCOPE))
        .map(
            original ->
                DynamicTest.dynamicTest(
                    original.getClass().getSimpleName(),
                    () -> {
                      boolean hasOperands = original instanceof FullStmtPositionInfo;
                      LocalVariableScope originalScope =
                          LocalVariableStmtPositionInfo.getLocalVariables(original);
                      Position changedPosition = new LinePosition(20);
                      StmtPositionInfo changed = original.withStmtPosition(changedPosition);
                      assertMetadata(changed, changedPosition, hasOperands, OPERAND, originalScope);
                      assertMetadata(original, STATEMENT, hasOperands, OPERAND, originalScope);
                      assertSame(
                          DefaultStmtPositionInfoFactory.getInstance(), changed.getFactory());
                      assertMetadata(
                          DefaultStmtPositionInfoFactory.withStmtPosition(
                              original, changedPosition),
                          changedPosition,
                          hasOperands,
                          OPERAND,
                          originalScope);

                      StmtPositionInfo localVariables =
                          DefaultStmtPositionInfoFactory.withLocalVariables(changed, SCOPE);
                      assertMetadata(localVariables, changedPosition, hasOperands, OPERAND, SCOPE);
                      // Captured-empty locals retain the LVT interface until explicitly removed.
                      LocalVariableStmtPositionInfo empty =
                          ((LocalVariableStmtPositionInfo) localVariables)
                              .withLocalVariables(LocalVariableScope.empty());
                      assertMetadata(
                          empty, changedPosition, hasOperands, OPERAND, LocalVariableScope.empty());
                      StmtPositionInfo withoutLocalVariables = empty.withoutLocalVariables();
                      assertMetadata(
                          withoutLocalVariables, changedPosition, hasOperands, OPERAND, null);
                      assertSame(
                          DefaultStmtPositionInfoFactory.getInstance(),
                          withoutLocalVariables.getFactory());
                      assertSame(
                          withoutLocalVariables,
                          DefaultStmtPositionInfoFactory.withLocalVariables(
                              withoutLocalVariables, null));

                      Position attachedOperand = new LinePosition(21);
                      Position[] operandPositions = {attachedOperand};
                      StmtPositionInfo withOperands =
                          DefaultStmtPositionInfoFactory.withOperandPositions(
                              changed, operandPositions);
                      operandPositions[0] = new LinePosition(99);
                      assertMetadata(
                          withOperands, changedPosition, true, attachedOperand, originalScope);
                      StmtPositionInfo withoutOperands =
                          DefaultStmtPositionInfoFactory.withOperandPositions(withOperands, null);
                      assertMetadata(
                          withoutOperands, changedPosition, false, OPERAND, originalScope);
                      assertSame(
                          withoutOperands,
                          DefaultStmtPositionInfoFactory.withOperandPositions(
                              withoutOperands, null));
                      assertMetadata(changed, changedPosition, hasOperands, OPERAND, originalScope);

                      if (hasOperands) {
                        Position changedOperand = new LinePosition(21);
                        FullStmtPositionInfo changedOperands =
                            ((FullStmtPositionInfo) changed)
                                .withOperandPositions(new Position[] {changedOperand});
                        assertMetadata(
                            changedOperands, changedPosition, true, changedOperand, originalScope);
                        assertMetadata(changed, changedPosition, true, OPERAND, originalScope);
                      }
                    }));
  }

  /** Creation retains captured locals, including empty scopes without source coordinates. */
  @Test
  void creationSeparatesStatementCoordinatesAndCapturedLocals() {
    LocalVariableStmtPositionInfo captured =
        DefaultStmtPositionInfoFactory.create(STATEMENT, SCOPE);
    assertMetadata(captured, STATEMENT, false, OPERAND, SCOPE);
    assertMetadata(
        DefaultStmtPositionInfoFactory.create(STATEMENT, LocalVariableScope.empty()),
        STATEMENT,
        false,
        OPERAND,
        LocalVariableScope.empty());
    assertMetadata(
        DefaultStmtPositionInfoFactory.create(STATEMENT), STATEMENT, false, OPERAND, null);
    assertSame(
        StmtPositionInfo.getNoStmtPositionInfo(),
        DefaultStmtPositionInfoFactory.create(NoPositionInformation.getInstance()));
    assertMetadata(
        DefaultStmtPositionInfoFactory.create(
            NoPositionInformation.getInstance(), LocalVariableScope.empty()),
        NoPositionInformation.getInstance(),
        false,
        OPERAND,
        LocalVariableScope.empty());
  }

  /** Checks capability interfaces and metadata identity; a null scope requires no LVT interface. */
  private static void assertMetadata(
      StmtPositionInfo info,
      Position position,
      boolean hasOperands,
      Position operand,
      LocalVariableScope scope) {
    assertSame(position, info.getStmtPosition());
    assertEquals(hasOperands, info instanceof FullStmtPositionInfo);
    if (hasOperands) {
      assertSame(operand, ((FullStmtPositionInfo) info).getOperandPosition(0));
    }
    assertEquals(scope != null, info instanceof LocalVariableStmtPositionInfo);
    assertSame(scope, LocalVariableStmtPositionInfo.getLocalVariables(info));
  }

  /** Changes to caller-owned arrays must not affect operands stored in existing metadata. */
  @Test
  void operandArraysAreCopiedAtCreationAndReplacement() {
    Position[] operands = {OPERAND};
    FullStmtPositionInfo full = DefaultStmtPositionInfoFactory.create(STATEMENT, operands);
    operands[0] = new LinePosition(99);
    assertSame(OPERAND, full.getOperandPosition(0));

    Position replacement = new LinePosition(21);
    Position[] replacements = {replacement};
    FullStmtPositionInfo localVariables =
        (FullStmtPositionInfo) DefaultStmtPositionInfoFactory.withLocalVariables(full, SCOPE);
    FullStmtPositionInfo updated =
        assertInstanceOf(
            FullStmtPositionInfo.class,
            DefaultStmtPositionInfoFactory.withOperandPositions(localVariables, replacements));
    replacements[0] = new LinePosition(99);
    assertSame(replacement, updated.getOperandPosition(0));
    assertSame(OPERAND, localVariables.getOperandPosition(0));
    assertSame(SCOPE, LocalVariableStmtPositionInfo.getLocalVariables(updated));
    assertSame(replacement, updated.withStmtPosition(new LinePosition(30)).getOperandPosition(0));
  }

  /** Missing operand coordinates return the no-position sentinel, including for empty arrays. */
  @Test
  void invalidOperandIndexesAndEmptyOperandPositionsHaveNoPosition() {
    FullStmtPositionInfo full =
        DefaultStmtPositionInfoFactory.create(STATEMENT, new Position[] {OPERAND});
    assertSame(NoPositionInformation.getInstance(), full.getOperandPosition(-1));
    assertSame(NoPositionInformation.getInstance(), full.getOperandPosition(1));
    FullStmtPositionInfo empty = full.withOperandPositions(new Position[0]);
    assertSame(NoPositionInformation.getInstance(), empty.getOperandPosition(0));
  }

  /** Empty captured scopes and empty operand lists remain distinct from absent metadata. */
  @Test
  void capturedEmptyScopeIsAvailableWithoutSourceCoordinates() {
    StmtPositionInfo noPosition = StmtPositionInfo.getNoStmtPositionInfo();
    assertSame(noPosition, DefaultStmtPositionInfoFactory.getNoStmtPositionInfo());
    assertSame(
        noPosition, DefaultStmtPositionInfoFactory.create(NoPositionInformation.getInstance()));
    StmtPositionInfo captured =
        DefaultStmtPositionInfoFactory.withLocalVariables(noPosition, LocalVariableScope.empty());
    assertSame(NoPositionInformation.getInstance(), captured.getStmtPosition());
    assertSame(
        LocalVariableScope.empty(), LocalVariableStmtPositionInfo.getLocalVariables(captured));
    assertSame(noPosition, DefaultStmtPositionInfoFactory.withLocalVariables(captured, null));
    assertFalse(noPosition instanceof LocalVariableStmtPositionInfo);

    // An empty array enables operand capability; null removes it.
    StmtPositionInfo withOperands =
        DefaultStmtPositionInfoFactory.withOperandPositions(noPosition, new Position[0]);
    assertInstanceOf(FullStmtPositionInfo.class, withOperands);
    assertSame(noPosition, DefaultStmtPositionInfoFactory.withOperandPositions(withOperands, null));
    StmtPositionInfo withBoth =
        DefaultStmtPositionInfoFactory.withOperandPositions(captured, new Position[0]);
    assertInstanceOf(FullStmtPositionInfo.class, withBoth);
    StmtPositionInfo withoutOperands =
        DefaultStmtPositionInfoFactory.withOperandPositions(withBoth, null);
    assertMetadata(
        withoutOperands,
        NoPositionInformation.getInstance(),
        false,
        OPERAND,
        LocalVariableScope.empty());
  }

  /** Unrecognized subclasses must be rejected even when a mutation would otherwise do nothing. */
  @Test
  void inheritedDefaultFactoryRejectsUnknownSubclassesOnEveryMutation() {
    SimpleStmtPositionInfo unknown = new SimpleStmtPositionInfo(STATEMENT) {};
    assertRejected(unknown, () -> unknown.withStmtPosition(new LinePosition(20)));
    assertRejected(
        unknown, () -> DefaultStmtPositionInfoFactory.withLocalVariables(unknown, SCOPE));
    assertRejected(unknown, () -> DefaultStmtPositionInfoFactory.withLocalVariables(unknown, null));
    UnregisteredFullInfo full = new UnregisteredFullInfo();
    assertRejected(full, () -> full.withOperandPositions(new Position[] {OPERAND}));
    UnregisteredLocalVariablesInfo localVariables = new UnregisteredLocalVariablesInfo();
    assertRejected(localVariables, localVariables::withoutLocalVariables);
    for (StmtPositionInfo source : List.of(unknown, full, localVariables)) {
      assertRejected(
          source,
          () ->
              DefaultStmtPositionInfoFactory.withOperandPositions(
                  source, new Position[] {OPERAND}));
      assertRejected(
          source, () -> DefaultStmtPositionInfoFactory.withOperandPositions(source, null));
    }
  }

  /** Rejection identifies the unsupported type and explains how to provide its own factory. */
  private static void assertRejected(StmtPositionInfo source, Runnable mutation) {
    IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, mutation::run);
    assertTrue(failure.getMessage().contains(source.getClass().getName()));
    assertTrue(failure.getMessage().contains("getFactory()"));
  }

  /** Instance and facade copies retain custom concrete variants, extra state and their factory. */
  @Test
  void customFactoryPreservesConcreteVariantsAndStateThroughAllCopyMethods() {
    CustomFullInfo original = new CustomFullInfo(STATEMENT, List.of(OPERAND), "custom metadata");
    Position changedPosition = new LinePosition(20);
    CustomFullInfo changed =
        assertInstanceOf(CustomFullInfo.class, original.withStmtPosition(changedPosition));
    assertEquals(20, changed.taggedLine());
    assertEquals(original.tag, changed.tag);
    assertSame(CUSTOM_FACTORY, changed.getFactory());
    assertMetadata(changed, changedPosition, true, OPERAND, null);
    assertMetadata(original, STATEMENT, true, OPERAND, null);

    CustomLocalVariablesFullInfo localVariables =
        assertInstanceOf(
            CustomLocalVariablesFullInfo.class,
            DefaultStmtPositionInfoFactory.withLocalVariables(changed, SCOPE));
    Position changedOperand = new LinePosition(21);
    Position[] operands = {changedOperand};
    CustomLocalVariablesFullInfo rewritten =
        assertInstanceOf(
            CustomLocalVariablesFullInfo.class,
            DefaultStmtPositionInfoFactory.withOperandPositions(localVariables, operands));
    operands[0] = new LinePosition(99);
    assertMetadata(rewritten, changedPosition, true, changedOperand, SCOPE);
    assertEquals(original.tag, ((CustomFullInfo) rewritten).tag);
    assertSame(CUSTOM_FACTORY, rewritten.getFactory());
    assertMetadata(localVariables, changedPosition, true, OPERAND, SCOPE);

    Position finalPosition = new LinePosition(30);
    CustomLocalVariablesFullInfo repositioned =
        assertInstanceOf(
            CustomLocalVariablesFullInfo.class,
            DefaultStmtPositionInfoFactory.withStmtPosition(rewritten, finalPosition));
    CustomLocalVariablesFullInfo empty =
        assertInstanceOf(
            CustomLocalVariablesFullInfo.class,
            repositioned.withLocalVariables(LocalVariableScope.empty()));
    assertMetadata(empty, finalPosition, true, changedOperand, LocalVariableScope.empty());
    CustomFullInfo withoutLocalVariables =
        assertInstanceOf(CustomFullInfo.class, empty.withoutLocalVariables());
    assertEquals(CustomFullInfo.class, withoutLocalVariables.getClass());
    assertMetadata(withoutLocalVariables, finalPosition, true, changedOperand, null);
    assertEquals(30, withoutLocalVariables.taggedLine());
    assertEquals(original.tag, withoutLocalVariables.tag);
    assertSame(CUSTOM_FACTORY, withoutLocalVariables.getFactory());
    // Explicit use of the built-in factory must reject even a custom type with its own factory.
    assertRejected(
        original,
        () ->
            DefaultStmtPositionInfoFactory.getInstance()
                .withStmtPosition(original, changedPosition));
  }

  /** Operand attachment and removal preserve custom state and any captured local variables. */
  @Test
  void customFactoryPreservesStateWhenAttachingAndRemovingOperands() {
    CustomInfo original = new CustomInfo(STATEMENT, "custom metadata");
    CustomLocalVariablesInfo localVariables =
        assertInstanceOf(
            CustomLocalVariablesInfo.class,
            DefaultStmtPositionInfoFactory.withLocalVariables(original, SCOPE));
    for (CustomInfo source : List.of(original, localVariables)) {
      LocalVariableScope scope = LocalVariableStmtPositionInfo.getLocalVariables(source);
      StmtPositionInfo withOperands =
          DefaultStmtPositionInfoFactory.withOperandPositions(source, new Position[] {OPERAND});
      assertEquals(
          scope == null ? CustomFullInfo.class : CustomLocalVariablesFullInfo.class,
          withOperands.getClass());
      assertMetadata(withOperands, STATEMENT, true, OPERAND, scope);
      assertEquals(original.tag, ((CustomInfo) withOperands).tag);
      assertSame(CUSTOM_FACTORY, withOperands.getFactory());
      StmtPositionInfo withoutOperands =
          DefaultStmtPositionInfoFactory.withOperandPositions(withOperands, null);
      assertEquals(source.getClass(), withoutOperands.getClass());
      assertMetadata(withoutOperands, STATEMENT, false, OPERAND, scope);
      assertEquals(original.tag, ((CustomInfo) withoutOperands).tag);
      assertSame(CUSTOM_FACTORY, withoutOperands.getFactory());
      assertSame(
          withoutOperands,
          DefaultStmtPositionInfoFactory.withOperandPositions(withoutOperands, null));
      assertMetadata(source, STATEMENT, false, OPERAND, scope);
    }
  }

  /** The statement helper dispatches to custom factories and preserves identity for no change. */
  @Test
  void statementHelperSelectsCustomFactoryAndRetainsIdentityForNoChange() {
    CustomFullInfo info = new CustomFullInfo(STATEMENT, List.of(OPERAND), "custom metadata");
    JNopStmt original = new JNopStmt(info);
    assertSame(original, DefaultStmtPositionInfoFactory.withLocalVariables(original, null));
    Stmt updated = DefaultStmtPositionInfoFactory.withLocalVariables(original, SCOPE);
    CustomLocalVariablesFullInfo captured =
        assertInstanceOf(CustomLocalVariablesFullInfo.class, updated.getPositionInfo());
    assertMetadata(captured, STATEMENT, true, OPERAND, SCOPE);
    assertEquals(info.tag, ((CustomFullInfo) captured).tag);
    assertSame(CUSTOM_FACTORY, captured.getFactory());
    assertSame(info, original.getPositionInfo());
  }

  /** Custom factory failures propagate unchanged through instance and facade dispatch. */
  @Test
  void customFactoryFailuresPropagateWithoutFallback() {
    UnsupportedOperationException failure = new UnsupportedOperationException("custom operation");
    StmtPositionInfoFactory factory =
        new StmtPositionInfoFactory() {
          @Override
          public StmtPositionInfo withStmtPosition(StmtPositionInfo info, Position position) {
            throw failure;
          }

          @Override
          public StmtPositionInfo withOperandPositions(StmtPositionInfo info, Position[] operands) {
            throw failure;
          }

          @Override
          public StmtPositionInfo withLocalVariables(
              StmtPositionInfo info, LocalVariableScope scope) {
            throw failure;
          }
        };
    // Direct interface implementation ensures dispatch does not depend on SimpleStmtPositionInfo.
    FullStmtPositionInfo info =
        new FullStmtPositionInfo() {
          @Override
          public Position getStmtPosition() {
            return STATEMENT;
          }

          @Override
          public Position getOperandPosition(int index) {
            return OPERAND;
          }

          @Override
          public StmtPositionInfoFactory getFactory() {
            return factory;
          }
        };
    assertSame(
        failure,
        assertThrows(
            UnsupportedOperationException.class,
            () -> info.withStmtPosition(new LinePosition(20))));
    assertSame(
        failure,
        assertThrows(
            UnsupportedOperationException.class,
            () -> info.withOperandPositions(new Position[] {OPERAND})));
    assertSame(
        failure,
        assertThrows(
            UnsupportedOperationException.class,
            () -> DefaultStmtPositionInfoFactory.withOperandPositions(info, null)));
    assertSame(
        failure,
        assertThrows(
            UnsupportedOperationException.class,
            () -> DefaultStmtPositionInfoFactory.withLocalVariables(info, SCOPE)));
    assertSame(
        failure,
        assertThrows(
            UnsupportedOperationException.class,
            () -> DefaultStmtPositionInfoFactory.withLocalVariables(new JNopStmt(info), null)));
  }

  /** Custom operand metadata deliberately inheriting the default factory to test rejection. */
  private static final class UnregisteredFullInfo extends SimpleStmtPositionInfo
      implements FullStmtPositionInfo {
    private UnregisteredFullInfo() {
      super(STATEMENT);
    }

    @Override
    public Position getOperandPosition(int index) {
      return OPERAND;
    }
  }

  /** Custom local-variable metadata deliberately inheriting the default factory. */
  private static final class UnregisteredLocalVariablesInfo extends SimpleStmtPositionInfo
      implements LocalVariableStmtPositionInfo {
    private UnregisteredLocalVariablesInfo() {
      super(STATEMENT);
    }

    @Override
    public LocalVariableScope getLocalVariables() {
      return SCOPE;
    }
  }

  /** Custom family base carrying extra state that must survive every metadata transition. */
  private static class CustomInfo extends SimpleStmtPositionInfo {
    final String tag;

    private CustomInfo(Position position, String tag) {
      super(position);
      this.tag = tag;
    }

    @Override
    public StmtPositionInfoFactory getFactory() {
      return CUSTOM_FACTORY;
    }

    /** Checks that inherited behavior observes the copied statement coordinates. */
    int taggedLine() {
      return getStmtPosition().getFirstLine();
    }
  }

  /** Custom family variant with operand coordinates. */
  private static class CustomFullInfo extends CustomInfo implements FullStmtPositionInfo {
    private final List<Position> operands;

    private CustomFullInfo(Position position, List<Position> operands, String tag) {
      super(position, tag);
      this.operands = operands;
    }

    @Override
    public Position getOperandPosition(int index) {
      return index >= 0 && index < operands.size()
          ? operands.get(index)
          : NoPositionInformation.getInstance();
    }
  }

  /** Custom family variant with a captured local variable scope. */
  private static final class CustomLocalVariablesInfo extends CustomInfo
      implements LocalVariableStmtPositionInfo {
    private final LocalVariableScope scope;

    private CustomLocalVariablesInfo(Position position, String tag, LocalVariableScope scope) {
      super(position, tag);
      this.scope = scope;
    }

    @Override
    public LocalVariableScope getLocalVariables() {
      return scope;
    }
  }

  /** Custom family variant combining operand coordinates and captured locals. */
  private static final class CustomLocalVariablesFullInfo extends CustomFullInfo
      implements LocalVariableStmtPositionInfo {
    private final LocalVariableScope scope;

    private CustomLocalVariablesFullInfo(
        Position position, List<Position> operands, String tag, LocalVariableScope scope) {
      super(position, operands, tag);
      this.scope = scope;
    }

    @Override
    public LocalVariableScope getLocalVariables() {
      return scope;
    }
  }

  /** Owns all four custom variants and preserves their tag when copying metadata. */
  private static final class CustomFactory implements StmtPositionInfoFactory {
    @Override
    public StmtPositionInfo withStmtPosition(StmtPositionInfo info, Position position) {
      CustomInfo source = (CustomInfo) info;
      return copy(
          source,
          position,
          source instanceof CustomFullInfo full ? full.operands : null,
          LocalVariableStmtPositionInfo.getLocalVariables(source));
    }

    @Override
    public StmtPositionInfo withOperandPositions(StmtPositionInfo info, Position[] operands) {
      CustomInfo source = (CustomInfo) info;
      if (operands == null && !(source instanceof FullStmtPositionInfo)) {
        return source;
      }
      return copy(
          source,
          source.getStmtPosition(),
          operands == null ? null : List.of(operands),
          LocalVariableStmtPositionInfo.getLocalVariables(source));
    }

    @Override
    public StmtPositionInfo withLocalVariables(StmtPositionInfo info, LocalVariableScope scope) {
      CustomInfo source = (CustomInfo) info;
      if (scope == null && !(source instanceof LocalVariableStmtPositionInfo)) {
        return source;
      }
      return copy(
          source,
          source.getStmtPosition(),
          source instanceof CustomFullInfo full ? full.operands : null,
          scope);
    }

    /** Selects the custom concrete variant from the presence of operand positions and scope. */
    private CustomInfo copy(
        CustomInfo source, Position position, List<Position> operands, LocalVariableScope scope) {
      if (scope != null && operands != null) {
        return new CustomLocalVariablesFullInfo(position, operands, source.tag, scope);
      }
      if (scope != null) {
        return new CustomLocalVariablesInfo(position, source.tag, scope);
      }
      if (operands != null) {
        return new CustomFullInfo(position, operands, source.tag);
      }
      return new CustomInfo(position, source.tag);
    }
  }
}
