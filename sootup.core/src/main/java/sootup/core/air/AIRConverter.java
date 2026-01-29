package sootup.core.air;

import org.jspecify.annotations.NonNull;
import sootup.core.air.common.AIRStmt;
import sootup.core.air.AIR.*;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;

import java.util.*;

public class AIRConverter {
    public List<AIRStmt> convert(Body jimpleBody) {

        AIRTranslationVisitor translator = new AIRTranslationVisitor(jimpleBody);

        // We iterate through Jimple statements and let the visitor handle the mapping
        for (Stmt stmt : jimpleBody.getStmts()) {
            stmt.accept(translator);
        }

        return translator.getAirStmts();
    }

    /**
     * The Visitor Implementation.
     * It maps Jimple-specific objects to our AIR POJOs.
     */
    private static class AIRTranslationVisitor extends AbstractStmtVisitor {
        private final List<AIRStmt> airStmts = new ArrayList<>();
        private final Body body;

        private AIRTranslationVisitor(Body body) {
            this.body = body;
        }

        public List<AIRStmt> getAirStmts() {
            return airStmts;
        }

        @Override
        public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            String leftVar = stmt.getLeftOp().toString();
            var rightOp = stmt.getRightOp();

            if (rightOp instanceof Constant) {
                airStmts.add(new AIRSetConstantStmt(leftVar, rightOp.toString()));
            } else if (rightOp instanceof AbstractBinopExpr expr) {
                airStmts.add(new AIRApplyStmt(leftVar, expr.getSymbol(),
                        List.of(expr.getOp1().toString(), expr.getOp2().toString())));
            } else {
                // Default to a simple copy (x = y)
                airStmts.add(new AIRCopyStmt(leftVar, rightOp.toString()));
            }
        }

        @Override
        public void caseIfStmt(@NonNull JIfStmt stmt) {
            AbstractConditionExpr cond = (AbstractConditionExpr) stmt.getCondition();

            // Add Assume
            airStmts.add(new AIR.AIRAssumeStmt(
                    cond.getOp1().toString(),
                    cond.getSymbol(),
                    cond.getOp2().toString()
            ));
        }

        @Override
        public void caseGotoStmt(@NonNull JGotoStmt stmt) {
            airStmts.add(new AIR.AIRGotoStmt(stmt.getTargetStmts(body).get(0).toString()));
        }

        @Override
            public void caseReturnStmt(@NonNull JReturnStmt stmt) {
                airStmts.add(new AIRRetStmt(stmt.getOp().toString()));
            }
    }
}
