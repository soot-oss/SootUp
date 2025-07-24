package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.spark.PAG;
import sootup.spark.NodeFactory;
import sootup.spark.PAGStmtVisitor;

public class StmtToEdgeConversionTest {

    @Test
    public void testStatementToEdgeConversions() {
        val aType = SparkTestUtil.simpleType("A");
        LValue left = new Local("a", aType);
        Value right = new JNewExpr(SparkTestUtil.simpleType("A"));
        JAssignStmt assignStmt = new JAssignStmt(left, right, StmtPositionInfo.getNoStmtPositionInfo());
        val methodPAG = new PAG();
        assignStmt.accept(PAGStmtVisitor.builder().PAG(methodPAG).build());
        val edge = methodPAG.getDelegate().edgeSet().iterator().next();
        val source = methodPAG.getDelegate().getEdgeSource(edge);
        val target = methodPAG.getDelegate().getEdgeTarget(edge);
        val expectedTarget = NodeFactory.createNode(left).get();
        val expectedSource = NodeFactory.createNode(right).get();
        assertEquals(expectedTarget, target);
        assertEquals(expectedSource, source);
    }


}
