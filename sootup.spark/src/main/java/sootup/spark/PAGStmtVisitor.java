package sootup.spark;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;

@Slf4j
@Builder
@Getter
@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
public class PAGStmtVisitor extends AbstractStmtVisitor {

    PAG PAG;

    @Override
    public void caseAssignStmt(JAssignStmt stmt) {
        val left = stmt.getLeftOp();
        val right = stmt.getRightOp();
        val leftNode = NodeFactory.createNode(left);
        val rightNode = NodeFactory.createNode(right);
        if(leftNode.isPresent() && rightNode.isPresent()){
            val source = rightNode.get();
            val target = leftNode.get();
            PAG.addEdge(source, target);
        } else {
            log.warn("Missing nodes for left: {} <- right: {}", left, right);
        }
    }


}
