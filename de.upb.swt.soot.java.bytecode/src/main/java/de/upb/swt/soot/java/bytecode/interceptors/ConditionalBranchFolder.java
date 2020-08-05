package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Statically evaluates the conditional expression of Jimple if statements. If the condition is
 * identically true or false, the Folder replaces the conditional branch statement with an
 * unconditional goto statement
 *
 * @author Marcus Nachtigall
 */
public class ConditionalBranchFolder implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    Body.BodyBuilder builder = Body.builder(originalBody);
    Set<Stmt> nodes = originalBody.getStmtGraph().nodes();
    final ImmutableStmtGraph stmtGraph = originalBody.getStmtGraph();

    for (Stmt stmt : nodes) {
      if (stmt instanceof JIfStmt) {
        JIfStmt ifStmt = (JIfStmt) stmt;
        // check for constant-valued conditions
        Value condition = ifStmt.getCondition();
        if (Evaluator.isValueConstantValue(condition)) {
          condition = Evaluator.getConstantValueOf(condition);

          if (((IntConstant) condition).getValue() == 1) {
            // if condition is always true: redirect all predecessors to the successor of the
            // if-statement
            final List<Stmt> successors = stmtGraph.successors(ifStmt);
            Stmt nextStmt = successors.get(0);
            builder.removeFlow(ifStmt, nextStmt);

            // FIXME: [ms] remove unbranched part of if-block, too i.e. iterate/remove flow until a
            // Stmt has another predecessor

            // link previous stmt with branch target of if-Stmt
            Stmt branchTarget = successors.get(1);
            final List<Stmt> predecessors = stmtGraph.predecessors(ifStmt);
            for (Stmt predecessor : predecessors) {
              builder.addFlow(predecessor, branchTarget);
              builder.removeFlow(predecessor, ifStmt);
            }
          }
        }
      }
    }

    return builder.build();
  }
}
