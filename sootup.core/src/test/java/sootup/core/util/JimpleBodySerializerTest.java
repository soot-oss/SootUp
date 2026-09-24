package sootup.core.util;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2024 Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.types.PrimitiveType;

public class JimpleBodySerializerTest {

  private static final StmtPositionInfo NO_POS = StmtPositionInfo.getNoStmtPositionInfo();

  @Test
  public void testLinearBody() {
    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    JAssignStmt assign = new JAssignStmt(l1, IntConstant.getInstance(42), NO_POS);
    JReturnStmt ret = new JReturnStmt(l1, NO_POS);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(assign);
    graph.putEdge(assign, ret);

    Set<Local> locals = new LinkedHashSet<>(Collections.singletonList(l1));
    String code = JimpleBodySerializer.serialize(graph, locals);

    assertTrue(
        code.contains("Local local_l1 = new Local(\"l1\", PrimitiveType.IntType.getInstance())"));
    assertTrue(
        code.contains(
            "JAssignStmt assignStmt_0 = new JAssignStmt(local_l1, IntConstant.getInstance(42), noPos)"));
    assertTrue(code.contains("JReturnStmt returnStmt_0 = new JReturnStmt(local_l1, noPos)"));
    assertTrue(code.contains("graph.setStartingStmt(assignStmt_0)"));
    assertTrue(code.contains("graph.putEdge(assignStmt_0, returnStmt_0)"));
  }

  @Test
  public void testIfElseBody() {
    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    JAssignStmt assign = new JAssignStmt(l1, IntConstant.getInstance(0), NO_POS);
    JIfStmt ifStmt = new JIfStmt(new JEqExpr(l1, IntConstant.getInstance(0)), NO_POS);
    JReturnStmt retTrue = new JReturnStmt(IntConstant.getInstance(1), NO_POS);
    JReturnStmt retFalse = new JReturnStmt(IntConstant.getInstance(2), NO_POS);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(assign);
    graph.putEdge(assign, ifStmt);
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, retFalse);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, retTrue);

    Set<Local> locals = new LinkedHashSet<>(Collections.singletonList(l1));
    String code = JimpleBodySerializer.serialize(graph, locals);

    assertTrue(
        code.contains(
            "JIfStmt ifStmt_0 = new JIfStmt(new JEqExpr(local_l1, IntConstant.getInstance(0)), noPos)"));
    // both branch indices must appear
    assertTrue(code.contains("graph.putEdge(ifStmt_0, 0,"));
    assertTrue(code.contains("graph.putEdge(ifStmt_0, 1,"));
  }

  @Test
  public void testLoopWithGoto() {
    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    JAssignStmt init = new JAssignStmt(l1, IntConstant.getInstance(0), NO_POS);
    JAssignStmt inc = new JAssignStmt(l1, new JAddExpr(l1, IntConstant.getInstance(1)), NO_POS);
    JGotoStmt gotoStmt = new JGotoStmt(NO_POS);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(init);
    graph.putEdge(init, inc);
    graph.putEdge(inc, gotoStmt);
    graph.putEdge(gotoStmt, JGotoStmt.BRANCH_IDX, inc);

    Set<Local> locals = new LinkedHashSet<>(Collections.singletonList(l1));
    String code = JimpleBodySerializer.serialize(graph, locals);

    assertTrue(code.contains("JGotoStmt gotoStmt_0 = new JGotoStmt(noPos)"));
    assertTrue(code.contains("graph.putEdge(gotoStmt_0, 0, assignStmt_1)"));
  }

  @Test
  public void testReturnVoidAndNop() {
    JNopStmt nop = new JNopStmt(NO_POS);
    JReturnVoidStmt retVoid = new JReturnVoidStmt(NO_POS);

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(nop);
    graph.putEdge(nop, retVoid);

    String code = JimpleBodySerializer.serialize(graph, Collections.emptySet());

    assertTrue(code.contains("JNopStmt nopStmt_0 = new JNopStmt(noPos)"));
    assertTrue(code.contains("JReturnVoidStmt returnVoidStmt_0 = new JReturnVoidStmt(noPos)"));
    assertTrue(code.contains("graph.putEdge(nopStmt_0, returnVoidStmt_0)"));
  }

  @Test
  public void testTypeSerializationPrimitiveTypes() {
    assertEquals(
        "PrimitiveType.IntType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.IntType.getInstance()));
    assertEquals(
        "PrimitiveType.LongType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.LongType.getInstance()));
    assertEquals(
        "PrimitiveType.BooleanType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.BooleanType.getInstance()));
    assertEquals(
        "PrimitiveType.ByteType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.ByteType.getInstance()));
    assertEquals(
        "PrimitiveType.ShortType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.ShortType.getInstance()));
    assertEquals(
        "PrimitiveType.CharType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.CharType.getInstance()));
    assertEquals(
        "PrimitiveType.FloatType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.FloatType.getInstance()));
    assertEquals(
        "PrimitiveType.DoubleType.getInstance()",
        JimpleBodySerializer.serializeType(PrimitiveType.DoubleType.getInstance()));
  }
}
