package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
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
    final Body.BodyBuilder builder = Body.builder(originalBody);
    final StmtGraph builderStmtGraph = builder.getStmtGraph();
    final ImmutableStmtGraph stmtGraph = originalBody.getStmtGraph();

    for (Stmt stmt : originalBody.getStmtGraph().nodes()) {
      if (stmt instanceof JIfStmt) {
        JIfStmt ifStmt = (JIfStmt) stmt;
        // check for constant-valued conditions
        Value condition = ifStmt.getCondition();
        if (Evaluator.isValueConstantValue(condition)) {
          condition = Evaluator.getConstantValueOf(condition);

          if (((IntConstant) condition).getValue() == 1) {
            // the evaluated if condition is always true: redirect all predecessors to the successor
            // of this
            // if-statement and prune the "true"-block stmt tree until another branch flows to a
            // Stmt
            Deque<Stmt> stack = new ArrayDeque<>();
            stack.addFirst(ifStmt);

            // link previous stmt with branch target of if-Stmt
            Stmt branchTarget = stmtGraph.successors(ifStmt).get(1);
            final List<Stmt> predecessors = stmtGraph.predecessors(ifStmt);
            for (Stmt predecessor : predecessors) {
              builder.removeFlow(predecessor, ifStmt);
              builder.addFlow(predecessor, branchTarget);
            }

            while (!stack.isEmpty()) {
              Stmt itStmt = stack.pollFirst();
              if (builderStmtGraph.containsNode(itStmt)
                  && builderStmtGraph.predecessors(itStmt).size() < 1) {
                final List<Stmt> itSuccessors = stmtGraph.successors(itStmt);
                for (final Stmt succ : itSuccessors) {
                  builder.removeFlow(itStmt, succ);
                  stack.add(succ);
                }
              }
            }
          }
        }
      }
    }

    return builder.build();
  }
}
