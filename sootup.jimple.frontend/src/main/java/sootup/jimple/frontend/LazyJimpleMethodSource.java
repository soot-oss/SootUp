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

import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.jimple.JimpleParser;

/**
 * A BodySource implementation that lazily resolves method bodies from Jimple AST. Similar to
 * AsmMethodSource, this class stores the parsing context and only constructs the Body when
 * resolveBody() is called.
 */
public class LazyJimpleMethodSource implements BodySource {

  @NonNull private final MethodSignature methodSignature;
  private final JimpleParser.@NonNull MethodContext methodContext;
  @NonNull private final Path sourcePath;
  private final @NonNull List<BodyInterceptor> bodyInterceptors;
  @NonNull private final View view;
  @NonNull private final JimpleConverterUtil util;
  @NonNull private final ClassType declaringClass;
  private final @NonNull EnumSet<MethodModifier> methodModifiers;
  private final @NonNull List<ClassType> exceptions;
  @NonNull private final Position methodPosition;
  private Body _bodySource;
  private final IdentifierFactory identifierFactory;

  public LazyJimpleMethodSource(
      @NonNull MethodSignature methodSignature,
      JimpleParser.@NonNull MethodContext methodContext,
      @NonNull Path sourcePath,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull View view,
      @NonNull JimpleConverterUtil util,
      @NonNull ClassType declaringClass,
      @NonNull EnumSet<MethodModifier> methodModifiers,
      @NonNull List<ClassType> exceptions,
      @NonNull Position methodPosition) {
    this.methodSignature = methodSignature;
    this.methodContext = methodContext;
    this.sourcePath = sourcePath;
    this.bodyInterceptors = bodyInterceptors;
    this.view = view;
    this.util = util;
    this.declaringClass = declaringClass;
    this.methodModifiers = methodModifiers;
    this.exceptions = exceptions;
    this.methodPosition = methodPosition;
    this.identifierFactory = view.getIdentifierFactory();
    this._bodySource = null;
  }

  @Override
  @NonNull
  public MethodSignature getSignature() {
    return methodSignature;
  }

  @Override
  @NonNull
  public Body resolveBody(@NonNull Iterable<MethodModifier> modifierIt) {
    // Create a helper class to parse the method body
    if (_bodySource == null) {
      MethodBodyParser parser = new MethodBodyParser();
      _bodySource = parser.parseMethodBody();
    }

    return _bodySource;
  }

  @Override
  public Object resolveAnnotationsDefaultValue() {
    return null; // Jimple doesn't store annotation default values
  }

  /**
   * Helper class to parse method body from the stored context. This is essentially the logic
   * extracted from JimpleConverter.MethodVisitor.visitMethod
   */
  private class MethodBodyParser {
    private final HashMap<BranchingStmt, List<String>> unresolvedBranches = new HashMap<>();
    private final HashMap<String, Stmt> labeledStmts = new HashMap<>();
    private HashMap<String, Local> locals = new HashMap<>();

    @NonNull
    public Body parseMethodBody() {
      List<Trap> traps = new ArrayList<>();
      List<List<Stmt>> blocks = new ArrayList<>();
      Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();

      if (methodContext.method_body() == null) {
        throw new ResolveException(
            "404 Body not found.",
            sourcePath,
            JimpleConverterUtil.buildPositionFromCtx(methodContext));
      }

      if (methodContext.method_body().SEMICOLON() != null) {
        throw new ResolveException(
            "Method has no body (abstract/native).",
            sourcePath,
            JimpleConverterUtil.buildPositionFromCtx(methodContext));
      }

      // declare locals
      locals = new HashMap<>();
      final JimpleParser.Method_body_contentsContext method_body_contentsContext =
          methodContext.method_body().method_body_contents();
      if (method_body_contentsContext.declarations() != null) {
        for (JimpleParser.DeclarationContext it :
            method_body_contentsContext.declarations().declaration()) {
          final String typeStr = it.type().getText();
          Type localtype =
              typeStr.equals("unknown") ? UnknownType.getInstance() : util.getType(typeStr);

          // validate nonvoid
          if (localtype == VoidType.getInstance()) {
            throw new ResolveException(
                "Void is not an allowed Type for a Local.",
                sourcePath,
                JimpleConverterUtil.buildPositionFromCtx(methodContext));
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
                      sourcePath,
                      JimpleConverterUtil.buildPositionFromCtx(methodContext));
                }
              }
            }
          }
        }
      }

      // statements
      JimpleBodyConverterState state =
          new JimpleBodyConverterState(
              sourcePath, util, declaringClass, identifierFactory, unresolvedBranches, locals);
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
          ClassType exceptionType = util.getClassType(it.exceptiontype.getText());
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

      Position classPosition = JimpleConverterUtil.buildPositionFromCtx(methodContext);

      // associate labeled Stmts with Branching Stmts
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
                sourcePath,
                JimpleConverterUtil.buildPositionFromCtx(methodContext));
          }
          targets.add(target);
        }
        successorMap.put(item.getKey(), targets);
      }

      final Body build;
      try {
        MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
        graph.initializeWith(blocks, successorMap, traps);
        Body.BodyBuilder builder = Body.builder(graph);

        builder.setModifiers(methodModifiers);
        builder.setMethodSignature(methodSignature);
        builder.setLocals(new HashSet<>(locals.values()));
        builder.setPosition(classPosition);

        build = builder.build();
      } catch (Exception e) {
        throw new ResolveException(
            methodSignature.getName() + " " + e.getMessage(), sourcePath, methodPosition, e);
      }

      // Apply body interceptors
      Body.BodyBuilder bodyBuilder = Body.builder(build, methodModifiers);
      for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
        try {
          bodyInterceptor.interceptBody(bodyBuilder, view);
          bodyBuilder.getControlFlowGraph().validateStmtConnectionsInGraph();
        } catch (Exception e) {
          throw new IllegalStateException(
              "Failed to apply " + bodyInterceptor + " to " + methodSignature, e);
        }
      }

      return bodyBuilder.build();
    }
  }
}
