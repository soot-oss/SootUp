package sootup.jimple.parser;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.*;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.jimple.JimpleBaseVisitor;
import sootup.jimple.JimpleParser;

public class JimpleConverter {

  public OverridingClassSource run(
      @Nonnull CharStream charStream,
      @Nonnull AnalysisInputLocation<?> inputlocation,
      @Nonnull Path sourcePath) {
    return run(charStream, inputlocation, sourcePath, Collections.emptyList());
  }

  public OverridingClassSource run(
      @Nonnull CharStream charStream,
      @Nonnull AnalysisInputLocation<?> inputlocation,
      @Nonnull Path sourcePath,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {

    final JimpleParser jimpleParser =
        JimpleConverterUtil.createJimpleParser(charStream, sourcePath);
    jimpleParser.setErrorHandler(new BailErrorStrategy());

    return run(jimpleParser, inputlocation, sourcePath, bodyInterceptors);
  }

  public OverridingClassSource run(
      @Nonnull JimpleParser parser,
      @Nonnull AnalysisInputLocation<?> inputlocation,
      @Nonnull Path sourcePath) {
    return run(parser, inputlocation, sourcePath, Collections.emptyList());
  }

  public OverridingClassSource run(
      @Nonnull JimpleParser parser,
      @Nonnull AnalysisInputLocation<?> inputlocation,
      @Nonnull Path sourcePath,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {

    // FIXME: [ms] apply bodyInterceptors or better: move that logic into View itself!

    ClassVisitor classVisitor;
    try {
      classVisitor = new ClassVisitor(sourcePath);
      classVisitor.visit(parser.file());
    } catch (ParseCancellationException ex) {
      throw new ResolveException("Syntax Error", sourcePath, ex);
    }

    return new OverridingClassSource(
        classVisitor.methods,
        classVisitor.fields,
        classVisitor.modifiers,
        classVisitor.interfaces,
        classVisitor.superclass,
        classVisitor.outerclass,
        classVisitor.position,
        sourcePath,
        classVisitor.clazz,
        inputlocation);
  }

  private static class ClassVisitor extends JimpleBaseVisitor<Boolean> {

    @Nonnull
    private final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    @Nonnull private final JimpleConverterUtil util;
    @Nonnull private final Path path;

    public ClassVisitor(@Nonnull Path path) {
      this.path = path;
      util = new JimpleConverterUtil(path);
    }

    private ClassType clazz = null;
    Set<SootField> fields = new HashSet<>();
    Set<SootMethod> methods = new HashSet<>();
    ClassType superclass = null;
    Set<ClassType> interfaces = null;
    ClassType outerclass = null; // currently not determined in Java etc -> heuristic will be used
    Position position = NoPositionInformation.getInstance();
    EnumSet<ClassModifier> modifiers = null;

    @Override
    @Nonnull
    public Boolean visitFile(@Nonnull JimpleParser.FileContext ctx) {

      position = JimpleConverterUtil.buildPositionFromCtx(ctx);

      // imports
      ctx.importItem().stream().filter(item -> item.location != null).forEach(util::addImport);

      // class_name
      if (ctx.classname != null) {

        // "$" in classname is a heuristic for an inner/outer class
        final String classname = ctx.classname.getText();
        final int dollarPostition = classname.indexOf('$');
        if (dollarPostition > -1) {
          outerclass = util.getClassType(classname.substring(0, dollarPostition));
        }
        clazz = util.getClassType(classname);

      } else {
        throw new ResolveException(
            "Classname is not well formed.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
      }

      modifiers = getClassModifiers(ctx.class_modifier());
      // file_type
      if (ctx.file_type() != null) {
        if (ctx.file_type().getText().equals("interface")) {
          modifiers.add(ClassModifier.INTERFACE);
        }
        if (ctx.file_type().getText().equals("annotation")) {
          modifiers.add(ClassModifier.ANNOTATION);
        }
      }

      // extends_clause
      if (ctx.extends_clause() != null) {
        superclass = util.getClassType(ctx.extends_clause().classname.getText());
      } else {
        superclass = null;
      }

      // implements_clause
      if (ctx.implements_clause() != null) {
        interfaces = util.getClassTypeSet(ctx.implements_clause().type_list());
      } else {
        interfaces = Collections.emptySet();
      }

      // member
      for (int i = 0; i < ctx.member().size(); i++) {
        if (ctx.member(i).method() != null) {
          final SootMethod m = new MethodVisitor().visitMember(ctx.member(i));
          if (methods.stream()
              .anyMatch(
                  meth -> {
                    final MethodSignature signature = m.getSignature();
                    return meth.getSignature().equals(signature);
                  })) {
            throw new ResolveException(
                "Method with the same Signature does already exist.", path, m.getPosition());
          }
          methods.add(m);

        } else {
          final JimpleParser.FieldContext fieldCtx = ctx.member(i).field();
          EnumSet<FieldModifier> modifier = getFieldModifiers(fieldCtx.field_modifier());
          final Position pos = JimpleConverterUtil.buildPositionFromCtx(fieldCtx);
          final String fieldName = Jimple.unescape(fieldCtx.identifier().getText());
          final SootField f =
              new SootField(
                  identifierFactory.getFieldSignature(fieldName, clazz, fieldCtx.type().getText()),
                  modifier,
                  pos);
          if (fields.stream().anyMatch(e -> e.getName().equals(fieldName))) {
            throw new ResolveException("Field with the same name does already exist.", path, pos);
          } else {
            fields.add(f);
          }
        }
      }

      return true;
    }

    private EnumSet<ClassModifier> getClassModifiers(
        List<JimpleParser.Class_modifierContext> modifier) {
      return modifier.stream()
          .map(modContext -> ClassModifier.valueOf(modContext.getText().toUpperCase()))
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(ClassModifier.class)));
    }

    private EnumSet<MethodModifier> getMethodModifiers(
        List<JimpleParser.Method_modifierContext> modifier) {
      return modifier.stream()
          .map(modContext -> MethodModifier.valueOf(modContext.getText().toUpperCase()))
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(MethodModifier.class)));
    }

    private EnumSet<FieldModifier> getFieldModifiers(
        List<JimpleParser.Field_modifierContext> modifier) {
      return modifier.stream()
          .map(modContext -> FieldModifier.valueOf(modContext.getText().toUpperCase()))
          .collect(Collectors.toCollection(() -> EnumSet.noneOf(FieldModifier.class)));
    }

    private class MethodVisitor extends JimpleBaseVisitor<SootMethod> {

      private final HashMap<BranchingStmt, List<String>> unresolvedBranches = new HashMap<>();
      private final HashMap<String, Stmt> labeledStmts = new HashMap<>();
      private HashMap<String, Local> locals = new HashMap<>();

      public Local getLocal(@Nonnull String name) {
        return locals.computeIfAbsent(
            name, (ignored) -> new Local(name, UnknownType.getInstance()));
      }

      @Override
      @Nonnull
      public SootMethod visitMethod(@Nonnull JimpleParser.MethodContext ctx) {

        EnumSet<MethodModifier> modifier =
            ctx.method_modifier() == null
                ? EnumSet.noneOf(MethodModifier.class)
                : getMethodModifiers(ctx.method_modifier());

        final JimpleParser.Method_subsignatureContext method_subsignatureContext =
            ctx.method_subsignature();
        if (method_subsignatureContext == null) {
          throw new ResolveException(
              "Methodsubsignature not found.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        final Type type = util.getType(method_subsignatureContext.type().getText());
        if (type == null) {
          throw new ResolveException(
              "Returntype not found.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        final String methodname = method_subsignatureContext.method_name().getText();
        if (methodname == null) {
          throw new ResolveException(
              "Methodname not found.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        List<Type> params = util.getTypeList(method_subsignatureContext.type_list());

        MethodSignature methodSignature =
            identifierFactory.getMethodSignature(clazz, Jimple.unescape(methodname), type, params);

        List<ClassType> exceptions =
            ctx.throws_clause() == null
                ? Collections.emptyList()
                : util.getClassTypeList(ctx.throws_clause().type_list());

        List<Trap> traps = new ArrayList<>();
        List<Stmt> stmtList = new ArrayList<>();
        Map<BranchingStmt, List<Stmt>> branchingMap = new HashMap<>();

        if (ctx.method_body() == null) {
          throw new ResolveException(
              "404 Body not found.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        } else if (ctx.method_body().SEMICOLON() == null) {

          // declare locals
          locals = new HashMap<>();
          final JimpleParser.Method_body_contentsContext method_body_contentsContext =
              ctx.method_body().method_body_contents();
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
                    path,
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
                          path,
                          JimpleConverterUtil.buildPositionFromCtx(ctx));
                    }
                  }
                }
              }
            }
          }

          // statements
          StmtVisitor stmtVisitor = new StmtVisitor();
          final JimpleParser.StatementsContext statements =
              method_body_contentsContext.statements();
          if (statements != null && statements.statement() != null) {
            statements
                .statement()
                .forEach(stmtCtx -> stmtList.add(stmtVisitor.visitStatement(stmtCtx)));
          }

          // catch_clause
          final List<JimpleParser.Trap_clauseContext> trap_clauseContexts =
              method_body_contentsContext.trap_clauses().trap_clause();
          if (trap_clauseContexts != null) {
            for (JimpleParser.Trap_clauseContext it : trap_clauseContexts) {
              ClassType exceptionType = util.getClassType(it.exceptiontype.getText());
              String beginLabel = it.from.getText();
              String toLabel = it.to.getText();
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

        Position classPosition = JimpleConverterUtil.buildPositionFromCtx(ctx);

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
                  path,
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }
            targets.add(target);
          }
          branchingMap.put(item.getKey(), targets);
        }

        Position methodPosition = JimpleConverterUtil.buildPositionFromCtx(ctx);
        final Body build;
        try {

          MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
          graph.initializeWith(stmtList, branchingMap, traps);
          Body.BodyBuilder builder = Body.builder(graph);

          builder.setModifiers(modifier);
          builder.setMethodSignature(methodSignature);
          builder.setLocals(new HashSet<>(locals.values()));
          builder.setPosition(classPosition);

          build = builder.build();
        } catch (Exception e) {
          throw new ResolveException(methodname + " " + e.getMessage(), path, methodPosition, e);
        }

        OverridingBodySource oms = new OverridingBodySource(methodSignature, build);
        return new SootMethod(oms, methodSignature, modifier, exceptions, methodPosition);
      }

      private class StmtVisitor extends JimpleBaseVisitor<Stmt> {
        final ValueVisitor valueVisitor = new ValueVisitor();

        private StmtVisitor() {}

        @Override
        public Stmt visitStatement(JimpleParser.StatementContext ctx) {
          final JimpleParser.StmtContext stmtCtx = ctx.stmt();
          if (stmtCtx == null) {
            throw new ResolveException(
                "Couldn't parse Stmt.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
          }
          Stmt stmt = visitStmt(stmtCtx);
          if (ctx.label_name != null) {
            final String labelname = ctx.label_name.getText();
            labeledStmts.put(labelname, stmt);
          }
          return stmt;
        }

        @Override
        @Nonnull
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
                        path,
                        JimpleConverterUtil.buildPositionFromCtx(ctx));
                  }
                } else if (case_labelContext.integer_constant().getText() != null) {
                  final int value =
                      Integer.parseInt(case_labelContext.integer_constant().getText());
                  min = Math.min(min, value);
                  lookup.add(IntConstant.getInstance(value));
                  targetLabels.add(it.goto_stmt().label_name.getText());
                } else {
                  throw new ResolveException(
                      "Label is invalid.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
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
              unresolvedBranches.put(switchStmt, targetLabels);
              return switchStmt;
            } else {
              final JimpleParser.AssignmentsContext assignments = ctx.assignments();
              if (assignments != null) {
                if (assignments.COLON_EQUALS() != null) {
                  Local left = getLocal(assignments.local.getText());

                  IdentityRef ref;
                  final JimpleParser.Identity_refContext identityRefCtx =
                      assignments.identity_ref();
                  if (identityRefCtx.caught != null) {
                    ref = JavaJimple.getInstance().newCaughtExceptionRef();
                  } else {
                    final String type = assignments.identity_ref().type().getText();
                    if (identityRefCtx.parameter_idx != null) {
                      int idx = Integer.parseInt(identityRefCtx.parameter_idx.getText());
                      ref = Jimple.newParameterRef(util.getType(type), idx);
                    } else {
                      if (clazz.toString().equals(type)) {
                        // reuse
                        ref = Jimple.newThisRef(clazz);
                      } else {
                        ref = Jimple.newThisRef(util.getClassType(type));
                      }
                    }
                  }
                  return Jimple.newIdentityStmt(left, ref, pos);

                } else if (assignments.EQUALS() != null) {
                  Value left =
                      assignments.local != null
                          ? getLocal(assignments.local.getText())
                          : valueVisitor.visitReference(assignments.reference());

                  final Value right = valueVisitor.visitValue(assignments.value());
                  return Jimple.newAssignStmt(left, right, pos);
                } else {
                  throw new ResolveException(
                      "Invalid assignment.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
                }

              } else if (ctx.IF() != null) {
                final BranchingStmt stmt =
                    Jimple.newIfStmt(
                        (AbstractConditionExpr) valueVisitor.visitBool_expr(ctx.bool_expr()), pos);
                unresolvedBranches.put(
                    stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
                return stmt;
              } else if (ctx.goto_stmt() != null) {
                final BranchingStmt stmt = Jimple.newGotoStmt(pos);
                unresolvedBranches.put(
                    stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
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
              "Unknown Stmt.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }
      }

      private class ValueVisitor extends JimpleBaseVisitor<Value> {

        @Override
        public Value visitValue(JimpleParser.ValueContext ctx) {
          if (ctx.NEW() != null && ctx.base_type != null) {
            final Type type = util.getType(ctx.base_type.getText());
            if (!(type instanceof ReferenceType)) {
              throw new ResolveException(
                  type + " is not a ReferenceType.",
                  path,
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }
            return Jimple.newNewExpr((ClassType) type);
          } else if (ctx.NEWARRAY() != null) {
            final Type type = util.getType(ctx.array_type.getText());
            if (type instanceof VoidType || type instanceof NullType) {
              throw new ResolveException(
                  type + " can not be an ArrayType.",
                  path,
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }

            Immediate dim = visitImmediate(ctx.array_descriptor().immediate());
            return JavaJimple.getInstance().newNewArrayExpr(type, dim);
          } else if (ctx.NEWMULTIARRAY() != null && ctx.immediate() != null) {
            final Type type = util.getType(ctx.multiarray_type.getText());
            if (!(type instanceof ReferenceType || type instanceof PrimitiveType)) {
              throw new ResolveException(
                  " Only base types are allowed",
                  path,
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }

            List<Immediate> sizes =
                ctx.immediate().stream().map(this::visitImmediate).collect(Collectors.toList());
            if (sizes.size() < 1) {
              throw new ResolveException(
                  "The Size list must have at least one Element.",
                  path,
                  JimpleConverterUtil.buildPositionFromCtx(ctx));
            }
            ArrayType arrtype = identifierFactory.getArrayType(type, sizes.size());
            return Jimple.newNewMultiArrayExpr(arrtype, sizes);
          } else if (ctx.nonvoid_cast != null && ctx.op != null) {
            final Type type = util.getType(ctx.nonvoid_cast.getText());
            Immediate val = visitImmediate(ctx.op);
            return Jimple.newCastExpr(val, type);
          } else if (ctx.INSTANCEOF() != null && ctx.op != null) {
            final Type type = util.getType(ctx.nonvoid_type.getText());
            Immediate val = visitImmediate(ctx.op);
            return Jimple.newInstanceOfExpr(val, type);
          }
          return super.visitValue(ctx);
        }

        @Override
        public Immediate visitImmediate(JimpleParser.ImmediateContext ctx) {
          if (ctx.identifier() != null) {
            return getLocal(ctx.identifier().getText());
          }
          return visitConstant(ctx.constant());
        }

        @Override
        public Value visitReference(JimpleParser.ReferenceContext ctx) {

          if (ctx.array_descriptor() != null) {
            // array
            Immediate idx = visitImmediate(ctx.array_descriptor().immediate());
            Local type = getLocal(ctx.identifier().getText());
            return JavaJimple.getInstance().newArrayRef(type, idx);
          } else if (ctx.DOT() != null) {
            // instance field
            String base = ctx.identifier().getText();
            FieldSignature fs = util.getFieldSignature(ctx.field_signature());
            return Jimple.newInstanceFieldRef(getLocal(base), fs);

          } else {
            // static field
            FieldSignature fs = util.getFieldSignature(ctx.field_signature());
            return Jimple.newStaticFieldRef(fs);
          }
        }

        @Override
        public Expr visitInvoke_expr(JimpleParser.Invoke_exprContext ctx) {

          List<Immediate> arglist = getArgList(ctx.arg_list(0));

          if (ctx.nonstaticinvoke != null) {
            Local base = getLocal(ctx.local_name.getText());
            MethodSignature methodSig = util.getMethodSignature(ctx.method_signature(), ctx);

            switch (ctx.nonstaticinvoke.getText().charAt(0)) {
              case 'i':
                return Jimple.newInterfaceInvokeExpr(base, methodSig, arglist);
              case 'v':
                return Jimple.newVirtualInvokeExpr(base, methodSig, arglist);
              case 's':
                return Jimple.newSpecialInvokeExpr(base, methodSig, arglist);
              default:
                throw new ResolveException(
                    "Unknown Nonstatic Invoke.",
                    path,
                    JimpleConverterUtil.buildPositionFromCtx(ctx));
            }

          } else if (ctx.staticinvoke != null) {
            MethodSignature methodSig = util.getMethodSignature(ctx.method_signature(), ctx);
            return Jimple.newStaticInvokeExpr(methodSig, arglist);
          } else if (ctx.dynamicinvoke != null) {

            List<Type> bootstrapMethodRefParams = util.getTypeList(ctx.type_list());
            MethodSignature bootstrapMethodRef =
                identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                        JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME),
                    ctx.STRING_CONSTANT().getText().replace("\"", ""),
                    util.getType(ctx.name.getText()),
                    bootstrapMethodRefParams);

            MethodSignature methodRef = util.getMethodSignature(ctx.bsm, ctx);

            List<Immediate> bootstrapArgs = getArgList(ctx.staticargs);

            return Jimple.newDynamicInvokeExpr(
                methodRef, bootstrapArgs, bootstrapMethodRef, getArgList(ctx.dyn_args));
          }
          throw new ResolveException(
              "Malformed Invoke Expression.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        @Override
        public Constant visitConstant(JimpleParser.ConstantContext ctx) {

          if (ctx.integer_constant() != null) {
            String intConst = ctx.integer_constant().getText();
            int lastCharPos = intConst.length() - 1;
            if (intConst.charAt(lastCharPos) == 'L' || intConst.charAt(lastCharPos) == 'l') {
              intConst = intConst.substring(0, lastCharPos);
              return LongConstant.getInstance(Long.parseLong(intConst));
            }
            return IntConstant.getInstance(Integer.parseInt(intConst));
          } else if (ctx.FLOAT_CONSTANT() != null) {
            String floatStr = ctx.FLOAT_CONSTANT().getText();
            int lastCharPos = floatStr.length() - 1;
            if (floatStr.charAt(lastCharPos) == 'F' || floatStr.charAt(lastCharPos) == 'f') {
              floatStr = floatStr.substring(0, lastCharPos);
              return FloatConstant.getInstance(Float.parseFloat(floatStr));
            }

            if (floatStr.charAt(0) == '#') {
              switch (floatStr.substring(1)) {
                case "Infinity":
                  return DoubleConstant.getInstance(Double.POSITIVE_INFINITY);
                case "-Infinity":
                  return DoubleConstant.getInstance(Double.NEGATIVE_INFINITY);
                case "NaN":
                  return DoubleConstant.getInstance(Double.NaN);
              }
            }

            return DoubleConstant.getInstance(Double.parseDouble(floatStr));
          } else if (ctx.CLASS() != null) {
            final String text = Jimple.unescape(ctx.STRING_CONSTANT().getText());
            return JavaJimple.getInstance().newClassConstant(text);
          } else if (ctx.STRING_CONSTANT() != null) {
            final String text = Jimple.unescape(ctx.STRING_CONSTANT().getText());
            return JavaJimple.getInstance().newStringConstant(text);
          } else if (ctx.BOOL_CONSTANT() != null) {
            final char firstChar = ctx.BOOL_CONSTANT().getText().charAt(0);
            return BooleanConstant.getInstance(firstChar == 't' || firstChar == 'T');
          } else if (ctx.NULL() != null) {
            return NullConstant.getInstance();
          } else if (ctx.methodhandle != null && ctx.method_signature() != null) {
            final MethodSignature methodSignature =
                util.getMethodSignature(ctx.method_signature(), ctx);
            // TODO: [ms] support handles with JFieldRef too
            // FIXME: [ms] update/specify tag when its printed
            // return JavaJimple.getInstance().newMethodHandle( , 0);
            return JavaJimple.getInstance().newMethodHandle(methodSignature, 0);
          } else if (ctx.methodtype != null && ctx.method_subsignature() != null) {
            final JimpleParser.Type_listContext typelist = ctx.method_subsignature().type_list();
            final List<Type> typeList = util.getTypeList(typelist);
            return JavaJimple.getInstance()
                .newMethodType(
                    typeList,
                    identifierFactory.getType(ctx.method_subsignature().type().getText()));
          }
          throw new ResolveException(
              "Unknown Constant.", path, JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        @Override
        public AbstractBinopExpr visitBinop_expr(JimpleParser.Binop_exprContext ctx) {

          Immediate left = visitImmediate(ctx.left);
          Immediate right = visitImmediate(ctx.right);

          JimpleParser.BinopContext binopctx = ctx.binop();

          if (binopctx.AND() != null) {
            return new JAndExpr(left, right);
          } else if (binopctx.OR() != null) {
            return new JOrExpr(left, right);
          } else if (binopctx.CMP() != null) {
            return new JCmpExpr(left, right);
          } else if (binopctx.CMPG() != null) {
            return new JCmpgExpr(left, right);
          } else if (binopctx.CMPL() != null) {
            return new JCmplExpr(left, right);
          } else if (binopctx.CMPEQ() != null) {
            return new JEqExpr(left, right);
          } else if (binopctx.CMPNE() != null) {
            return new JNeExpr(left, right);
          } else if (binopctx.CMPGT() != null) {
            return new JGtExpr(left, right);
          } else if (binopctx.CMPGE() != null) {
            return new JGeExpr(left, right);
          } else if (binopctx.CMPLT() != null) {
            return new JLtExpr(left, right);
          } else if (binopctx.CMPLE() != null) {
            return new JLeExpr(left, right);
          } else if (binopctx.SHL() != null) {
            return new JShlExpr(left, right);
          } else if (binopctx.SHR() != null) {
            return new JShrExpr(left, right);
          } else if (binopctx.USHR() != null) {
            return new JUshrExpr(left, right);
          } else if (binopctx.PLUS() != null) {
            return new JAddExpr(left, right);
          } else if (binopctx.MINUS() != null) {
            return new JSubExpr(left, right);
          } else if (binopctx.MULT() != null) {
            return new JMulExpr(left, right);
          } else if (binopctx.DIV() != null) {
            return new JDivExpr(left, right);
          } else if (binopctx.XOR() != null) {
            return new JXorExpr(left, right);
          } else if (binopctx.MOD() != null) {
            return new JRemExpr(left, right);
          }
          throw new ResolveException(
              "Unknown BinOp: " + binopctx.getText(),
              path,
              JimpleConverterUtil.buildPositionFromCtx(ctx));
        }

        @Override
        public Expr visitUnop_expr(JimpleParser.Unop_exprContext ctx) {
          Immediate value = visitImmediate(ctx.immediate());
          if (ctx.unop().NEG() != null) {
            return Jimple.newNegExpr(value);
          } else {
            return Jimple.newLengthExpr(value);
          }
        }

        @Nonnull
        private List<Immediate> getArgList(JimpleParser.Arg_listContext ctx) {
          if (ctx == null || ctx.immediate() == null) {
            return Collections.emptyList();
          }
          final List<JimpleParser.ImmediateContext> immediates = ctx.immediate();
          List<Immediate> arglist = new ArrayList<>(immediates.size());
          for (JimpleParser.ImmediateContext immediate : immediates) {
            arglist.add(visitImmediate(immediate));
          }
          return arglist;
        }
      }
    }
  }
}
