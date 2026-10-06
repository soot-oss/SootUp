package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

class LocalPackerTest {
  @Test
  void reusesFirstAllocatedColorForNonOverlappingLocals() {
    Local first = Jimple.newLocal("a", PrimitiveType.getInt());
    Local second = Jimple.newLocal("b", PrimitiveType.getInt());
    var position = StmtPositionInfo.getNoStmtPositionInfo();
    var init = Jimple.newAssignStmt(first, IntConstant.getInstance(1), position);
    var add =
        Jimple.newAssignStmt(
            second, Jimple.newAddExpr(first, IntConstant.getInstance(1)), position);
    var ret = Jimple.newReturnStmt(second, position);
    var graph = new MutableBlockControlFlowGraph();
    graph.putEdge(init, add);
    graph.putEdge(add, ret);
    graph.setStartingStmt(init);
    var factory = JavaIdentifierFactory.getInstance();
    var builder =
        Body.builder(graph)
            .setLocals(new LinkedHashSet<>(List.of(first, second)))
            .setMethodSignature(
                factory.getMethodSignature(
                    factory.getClassType("example.Test"),
                    "test",
                    PrimitiveType.getInt(),
                    List.of()));

    // a dies at b = a + 1, so both values fit in one local. There are no precolored parameters.
    new LocalPacker().interceptBody(builder, new JavaView(Collections.emptyList()));

    assertEquals(1, builder.getLocals().size());
    Local packed = builder.getLocals().iterator().next();
    var stmts = builder.getStmts();
    assertSame(packed, ((JAssignStmt) stmts.get(0)).getLeftOp());
    var packedAdd = (JAssignStmt) stmts.get(1);
    assertSame(packed, packedAdd.getLeftOp());
    assertSame(packed, ((JAddExpr) packedAdd.getRightOp()).getOp1());
    assertSame(packed, ((JReturnStmt) stmts.get(2)).getOp());
  }
}
