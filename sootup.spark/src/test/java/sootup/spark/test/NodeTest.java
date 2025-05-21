package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.spark.NodeFactory;
import sootup.spark.node.Node;

import java.util.Optional;

public class NodeTest {

    private ClassType simpleType(String name){
        return new ClassType() {
            @Override
            public String getFullyQualifiedName() {
                return name;
            }

            @Override
            public String getClassName() {
                return name;
            }

            @Override
            public PackageName getPackageName() {
                return new PackageName("");
            }
        };
    }

    @Test
    public void testValueToNodeConversions(){
        Value local = new Local("a", simpleType("A"));
        Value newExpr = new JNewExpr(simpleType("A"));
        Optional<Node> optVarNode = NodeFactory.createNode(local);
        Optional<Node> optAllocNode = NodeFactory.createNode(newExpr);
        assertTrue(optVarNode.isPresent());
        assertTrue(optAllocNode.isPresent());
    }

    public void testStatementToEdgeConversions(){
        LValue left = new Local("a", simpleType("A"));
        Value right = new JNewExpr(simpleType("A"));
        JAssignStmt assignStmt = new JAssignStmt(left, right, StmtPositionInfo.getNoStmtPositionInfo());
    }

}
