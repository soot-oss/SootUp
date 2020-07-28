package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
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

    for (Stmt stmt : nodes) {
      if (stmt instanceof JIfStmt) {
        JIfStmt ifStmt = (JIfStmt) stmt;
        // check for constant-valued conditions
        Value condition = ifStmt.getCondition();
        if (Evaluator.isValueConstantValued(condition)) {
          condition = Evaluator.getConstantValueOf(condition);

          if (((IntConstant) condition).getValue() == 1) {
            // if condition always true, redirect all predecessors to the successor of the
            // if-statements
            Stmt nextStmt = ifStmt.getTarget(originalBody);
            for (Stmt predecessor : originalBody.getStmtGraph().predecessors(ifStmt)) {
              builder.addFlow(predecessor, nextStmt);
              builder.removeFlow(predecessor, ifStmt);
            }
            for (Stmt successor : originalBody.getStmtGraph().successors(ifStmt)) {
              builder.removeFlow(ifStmt, successor);
            }
          }
        }
      }
    }

    return builder.build();
  }
}
