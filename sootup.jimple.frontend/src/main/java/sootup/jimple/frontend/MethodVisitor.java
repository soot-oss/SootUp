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

import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.jimple.JimpleBaseVisitor;
import sootup.jimple.JimpleParser;

public abstract class MethodVisitor extends JimpleBaseVisitor<SootMethod> {

  @NonNull protected final ClassVisitor classVisitor;
  protected final HashMap<BranchingStmt, List<String>> unresolvedBranches = new HashMap<>();
  protected final HashMap<String, Stmt> labeledStmts = new HashMap<>();
  protected final HashMap<String, Local> locals = new HashMap<>();

  public MethodVisitor(@NonNull ClassVisitor classVisitor) {
    this.classVisitor = classVisitor;
  }

  @Override
  @NonNull
  public SootMethod visitMethod(JimpleParser.@NonNull MethodContext ctx) {

    EnumSet<MethodModifier> modifier =
        ctx.method_modifier() == null
            ? EnumSet.noneOf(MethodModifier.class)
            : classVisitor.getMethodModifiers(ctx.method_modifier());

    final JimpleParser.Method_subsignatureContext method_subsignatureContext =
        ctx.method_subsignature();
    if (method_subsignatureContext == null) {
      throw new ResolveException(
          "Methodsubsignature not found.",
          classVisitor.path,
          JimpleConverterUtil.buildPositionFromCtx(ctx));
    }

    final Type type = classVisitor.util.getType(method_subsignatureContext.type().getText());
    if (type == null) {
      throw new ResolveException(
          "Returntype not found.",
          classVisitor.path,
          JimpleConverterUtil.buildPositionFromCtx(ctx));
    }

    final String methodname = method_subsignatureContext.method_name().getText();
    if (methodname == null) {
      throw new ResolveException(
          "Methodname not found.",
          classVisitor.path,
          JimpleConverterUtil.buildPositionFromCtx(ctx));
    }

    List<Type> params = classVisitor.util.getTypeList(method_subsignatureContext.type_list());

    MethodSignature methodSignature =
        classVisitor.identifierFactory.getMethodSignature(
            classVisitor.clazz, JimpleUtils.unescape(methodname), type, params);

    List<ClassType> exceptions =
        ctx.throws_clause() == null
            ? Collections.emptyList()
            : classVisitor.util.getClassTypeList(ctx.throws_clause().type_list());

    Position methodPosition = JimpleConverterUtil.buildPositionFromCtx(ctx);

    return resolveMethod(ctx, methodSignature, modifier, exceptions, methodPosition);
  }

  protected abstract SootMethod resolveMethod(
      JimpleParser.MethodContext ctx,
      MethodSignature methodSignature,
      EnumSet<MethodModifier> modifier,
      List<ClassType> exceptions,
      Position methodPosition);

  // Common utility for eager parsing used by EagerMethodVisitor
  protected void parseMethodBody(
      JimpleParser.MethodContext ctx,
      List<Trap> traps,
      List<List<Stmt>> blocks,
      Map<BranchingStmt, List<Stmt>> successorMap) {

    if (ctx.method_body() == null) {
      throw new ResolveException(
          "404 Body not found.", classVisitor.path, JimpleConverterUtil.buildPositionFromCtx(ctx));
    } else if (ctx.method_body().SEMICOLON() == null) {

      // declare locals
      locals.clear();
      final JimpleParser.Method_body_contentsContext method_body_contentsContext =
          ctx.method_body().method_body_contents();
      if (method_body_contentsContext.declarations() != null) {
        for (JimpleParser.DeclarationContext it :
            method_body_contentsContext.declarations().declaration()) {
          final String typeStr = it.type().getText();
          Type localtype =
              typeStr.equals("unknown")
                  ? UnknownType.getInstance()
                  : classVisitor.util.getType(typeStr);

          // validate nonvoid
          if (localtype == VoidType.getInstance()) {
            throw new ResolveException(
                "Void is not an allowed Type for a Local.",
                classVisitor.path,
                JimpleConverterUtil.buildPositionFromCtx(ctx));
          }

          if (it.arg_list() != null) {
            final List<JimpleParser.ImmediateContext> immediates = it.arg_list().immediate();
            if (immediates != null) {
              for (JimpleParser.ImmediateContext immediate : immediates) {
                if (immediate != null && immediate.local != null) {
                  String localname = immediate.local.getText();
                  locals.put(localname, new Local(localname, localtype));
                } else {
                  throw new ResolveException(
                      "Thats not a Local in the Local Declaration.",
                      classVisitor.path,
                      JimpleConverterUtil.buildPositionFromCtx(ctx));
                }
              }
            }
          }
        }
      }

      // statements
      JimpleBodyConverterState state =
          new JimpleBodyConverterState(
              classVisitor.path,
              classVisitor.util,
              classVisitor.clazz,
              classVisitor.identifierFactory,
              unresolvedBranches,
              locals);
      StmtVisitor stmtVisitor = new StmtVisitor(state);
      final JimpleParser.StatementsContext statements = method_body_contentsContext.statements();
      if (statements != null && statements.statement() != null) {
        List<Stmt> currentStmtList = new ArrayList<>();
        for (JimpleParser.StatementContext stmtCtx : statements.statement()) {
          Stmt newestStmt = stmtVisitor.visitStatement(stmtCtx);
          if (stmtCtx.label_name != null) {
            if (!currentStmtList.isEmpty()) {
              blocks.add(currentStmtList);
              currentStmtList = new ArrayList<>();
            }
            final String labelname = stmtCtx.label_name.getText();
            labeledStmts.put(labelname, newestStmt);
          }

          currentStmtList.add(newestStmt);

          if (newestStmt.branches()) {
            if (!currentStmtList.isEmpty()) {
              blocks.add(currentStmtList);
              currentStmtList = new ArrayList<>();
            }
          }
        }

        // check for dangling Block
        if (!currentStmtList.isEmpty()) {
          blocks.add(currentStmtList);
        }
      }

      // catch_clause
      final List<JimpleParser.Trap_clauseContext> trap_clauseContexts =
          method_body_contentsContext.trap_clauses().trap_clause();
      if (trap_clauseContexts != null) {
        for (JimpleParser.Trap_clauseContext it : trap_clauseContexts) {
          ClassType exceptionType = classVisitor.util.getClassType(it.exceptiontype.getText());
          String beginLabel = it.from.getText();
          String toLabel = it.to.getText();
          if (beginLabel.equals(toLabel)) continue;

          String handlerLabel = it.with.getText();
          traps.add(
              Jimple.newTrap(
                  exceptionType,
                  labeledStmts.get(beginLabel),
                  labeledStmts.get(toLabel),
                  labeledStmts.get(handlerLabel)));
        }
      }
    }
  }

  protected Map<BranchingStmt, List<Stmt>> resolveSuccessors(JimpleParser.MethodContext ctx) {
    Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
    for (Map.Entry<BranchingStmt, List<String>> item : unresolvedBranches.entrySet()) {
      final List<String> targetLabels = item.getValue();
      final List<Stmt> targets = new ArrayList<>(targetLabels.size());
      for (String targetLabel : targetLabels) {
        final Stmt target = labeledStmts.get(targetLabel);
        if (target == null) {
          throw new ResolveException(
              "Don't jump into the Space! The target Stmt is not found i.e. no label for: "
                  + item.getKey()
                  + " to "
                  + targetLabel,
              classVisitor.path,
              JimpleConverterUtil.buildPositionFromCtx(ctx));
        }
        targets.add(target);
      }
      successorMap.put(item.getKey(), targets);
    }
    return successorMap;
  }
}
