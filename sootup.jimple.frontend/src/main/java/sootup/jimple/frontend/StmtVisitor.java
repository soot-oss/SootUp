package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp\
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.*;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.java.core.language.JavaJimple;
import sootup.jimple.JimpleBaseVisitor;
import sootup.jimple.JimpleParser;

public class StmtVisitor extends JimpleBaseVisitor<Stmt> {

  @NonNull private final JimpleBodyConverterState state;
  @NonNull private final ValueVisitor valueVisitor;

  public StmtVisitor(@NonNull JimpleBodyConverterState state) {
    this.state = state;
    this.valueVisitor = new ValueVisitor(state);
  }

  @Override
  public Stmt visitStatement(JimpleParser.StatementContext ctx) {
    final JimpleParser.StmtContext stmtCtx = ctx.stmt();
    if (stmtCtx == null) {
      throw new ResolveException(
          "Couldn't parse Stmt.", state.getPath(), JimpleConverterUtil.buildPositionFromCtx(ctx));
    }
    return visitStmt(stmtCtx);
  }

  @Override
  @NonNull
  public Stmt visitStmt(JimpleParser.StmtContext ctx) {
    StmtPositionInfo pos = new SimpleStmtPositionInfo(ctx.start.getLine());

    if (ctx.BREAKPOINT() != null) {
      return Jimple.newBreakpointStmt(pos);
    } else {
      if (ctx.ENTERMONITOR() != null) {
        return Jimple.newEnterMonitorStmt(valueVisitor.visitImmediate(ctx.immediate()), pos);
      } else if (ctx.EXITMONITOR() != null) {
        return Jimple.newExitMonitorStmt(valueVisitor.visitImmediate(ctx.immediate()), pos);
      } else if (ctx.SWITCH() != null) {

        Immediate key = valueVisitor.visitImmediate(ctx.immediate());
        List<IntConstant> lookup = new ArrayList<>();
        List<String> targetLabels = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        String defaultLabel = null;

        for (JimpleParser.Case_stmtContext it : ctx.case_stmt()) {
          final JimpleParser.Case_labelContext case_labelContext = it.case_label();
          if (case_labelContext.getText() != null && case_labelContext.DEFAULT() != null) {
            if (defaultLabel == null) {
              defaultLabel = it.goto_stmt().label_name.getText();
            } else {
              throw new ResolveException(
                  "Only one default label is allowed!",
                  state.getPath(),
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }
          } else if (case_labelContext.integer_constant().getText() != null) {
            final int value = Integer.parseInt(case_labelContext.integer_constant().getText());
            min = Math.min(min, value);
            lookup.add(IntConstant.getInstance(value));
            targetLabels.add(it.goto_stmt().label_name.getText());
          } else {
            throw new ResolveException(
                "Label is invalid.",
                state.getPath(),
                JimpleConverterUtil.buildPositionFromCtx(ctx));
          }
        }
        targetLabels.add(defaultLabel);

        JSwitchStmt switchStmt;
        if (ctx.SWITCH().getText().charAt(0) == 't') {
          int high = min + lookup.size() - 1;
          switchStmt = Jimple.newTableSwitchStmt(key, min, high, pos);
        } else {
          switchStmt = Jimple.newLookupSwitchStmt(key, lookup, pos);
        }
        state.getUnresolvedBranches().put(switchStmt, targetLabels);
        return switchStmt;
      } else {
        final JimpleParser.AssignmentsContext assignments = ctx.assignments();
        if (assignments != null) {
          if (assignments.COLON_EQUALS() != null) {
            Local left = state.getLocal(assignments.local.getText());

            IdentityRef ref;
            final JimpleParser.Identity_refContext identityRefCtx = assignments.identity_ref();
            if (identityRefCtx.caught != null) {
              ref = JavaJimple.newCaughtExceptionRef();
            } else {
              final String type = assignments.identity_ref().type().getText();
              if (identityRefCtx.parameter_idx != null) {
                int idx = Integer.parseInt(identityRefCtx.parameter_idx.getText());
                ref = Jimple.newParameterRef(state.getUtil().getType(type), idx);
              } else {
                if (state.getClazz().toString().equals(type)) {
                  // reuse
                  ref = Jimple.newThisRef(state.getClazz());
                } else {
                  ref = Jimple.newThisRef(state.getUtil().getClassType(type));
                }
              }
            }
            return Jimple.newIdentityStmt(left, ref, pos);

          } else if (assignments.EQUALS() != null) {
            LValue left =
                assignments.local != null
                    ? state.getLocal(assignments.local.getText())
                    : (LValue) valueVisitor.visitReference(assignments.reference());

            final Value right = valueVisitor.visitValue(assignments.value());
            return Jimple.newAssignStmt(left, right, pos);
          } else {
            throw new ResolveException(
                "Invalid assignment.",
                state.getPath(),
                JimpleConverterUtil.buildPositionFromCtx(assignments));
          }

        } else if (ctx.IF() != null) {
          final BranchingStmt stmt =
              Jimple.newIfStmt(
                  (AbstractConditionExpr) valueVisitor.visitBool_expr(ctx.bool_expr()), pos);
          state
              .getUnresolvedBranches()
              .put(stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
          return stmt;
        } else if (ctx.goto_stmt() != null) {
          final BranchingStmt stmt = Jimple.newGotoStmt(pos);
          state
              .getUnresolvedBranches()
              .put(stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
          return stmt;
        } else if (ctx.NOP() != null) {
          return Jimple.newNopStmt(pos);
        } else if (ctx.RET() != null) {
          return Jimple.newRetStmt(valueVisitor.visitImmediate(ctx.immediate()), pos);
        } else if (ctx.RETURN() != null) {
          if (ctx.immediate() == null) {
            return Jimple.newReturnVoidStmt(pos);
          } else {
            return Jimple.newReturnStmt(valueVisitor.visitImmediate(ctx.immediate()), pos);
          }
        } else if (ctx.THROW() != null) {
          return Jimple.newThrowStmt(valueVisitor.visitImmediate(ctx.immediate()), pos);
        } else if (ctx.invoke_expr() != null) {
          return Jimple.newInvokeStmt(
              (AbstractInvokeExpr) valueVisitor.visitInvoke_expr(ctx.invoke_expr()), pos);
        }
      }
    }
    throw new ResolveException(
        "Unknown Stmt.", state.getPath(), JimpleConverterUtil.buildPositionFromCtx(ctx));
  }
}
