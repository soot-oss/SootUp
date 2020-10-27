package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.util.StringTools;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.jimple.JimpleBaseVisitor;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.*;

public class JimpleConverter {

  final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private JimpleConverterUtil util;
  private Path path;

  public OverridingClassSource run(
      CharStream charStream, AnalysisInputLocation inputlocation, Path sourcePath) {
    path = sourcePath.toAbsolutePath();
    util = new JimpleConverterUtil(path.toString());

    if (charStream.size() == 0) {
      throw new ResolveException("Empty File to parse.", path, null);
    }

    JimpleLexer lexer = new JimpleLexer(charStream);
    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    parser.removeErrorListeners();
    parser.addErrorListener(
        new BaseErrorListener() {
          @Override
          public void syntaxError(
              Recognizer<?, ?> recognizer,
              Object offendingSymbol,
              int line,
              int charPositionInLine,
              String msg,
              RecognitionException e) {
            throw new ResolveException(
                "Jimple Syntaxerror: " + msg, path, new Position(line, charPositionInLine, -1, -1));
          }
        });

    ClassVisitor classVisitor = new ClassVisitor();
    classVisitor.visit(parser.file());

    return new OverridingClassSource(
        inputlocation,
        sourcePath,
        classVisitor.clazz,
        classVisitor.superclass,
        classVisitor.interfaces,
        classVisitor.outerclass,
        classVisitor.fields,
        classVisitor.methods,
        classVisitor.position,
        classVisitor.modifiers);
  }

  private class ClassVisitor extends JimpleBaseVisitor<Boolean> {

    private ClassType clazz = null;
    Set<SootField> fields = new HashSet<>();
    Set<SootMethod> methods = new HashSet<>();
    ClassType superclass = null;
    Set<ClassType> interfaces = null;
    ClassType outerclass = null; // currently not determined in Java etc -> heuristic will be used
    Position position = NoPositionInformation.getInstance();
    EnumSet<Modifier> modifiers = null;

    @Override
    @Nonnull
    public Boolean visitFile(@Nonnull JimpleParser.FileContext ctx) {

      position = buildPositionFromCtx(ctx);

      // imports
      ctx.importItem().stream()
          .filter(item -> item.location != null)
          .forEach(importCtx -> util.addImport(importCtx));

      // class_name
      if (ctx.classname != null) {

        // "$" in classname is a heuristic for an inner/outer class
        final String classname = StringTools.getUnEscapedStringOf(ctx.classname.getText());
        final int dollarPostition = classname.indexOf('$');
        if (dollarPostition > -1) {
          outerclass = util.getClassType(classname.substring(0, dollarPostition));
        }
        clazz = util.getClassType(classname);

      } else {
        throw new ResolveException(
            "Classname is not well formed.", path, buildPositionFromCtx(ctx));
      }

      modifiers = getModifiers(ctx.modifier());
      // file_type
      if (ctx.file_type() != null) {
        if (ctx.file_type().getText().equals("interface")) {
          modifiers.add(Modifier.INTERFACE);
        }
        if (ctx.file_type().getText().equals("annotation")) {
          modifiers.add(Modifier.ANNOTATION);
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
          methods.add(new MethodVisitor().visitMember(ctx.member(i)));
        } else {
          final JimpleParser.FieldContext fieldCtx = ctx.member(i).field();
          EnumSet<Modifier> modifier = getModifiers(fieldCtx.modifier());
          final Position pos = buildPositionFromCtx(ctx);
          fields.add(
              new SootField(
                  identifierFactory.getFieldSignature(
                      fieldCtx.identifier().getText(), clazz, fieldCtx.type().getText()),
                  modifier,
                  pos));
        }
      }

      return true;
    }

    private EnumSet<Modifier> getModifiers(List<JimpleParser.ModifierContext> modifier) {
      Set<Modifier> modifierSet =
          modifier.stream()
              .map(modifierContext -> Modifier.valueOf(modifierContext.getText().toUpperCase()))
              .collect(Collectors.toSet());
      return modifierSet.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(modifierSet);
    }

    private class MethodVisitor extends JimpleBaseVisitor<SootMethod> {

      private final HashMap<Stmt, List<String>> unresolvedBranches = new HashMap<>();
      private final HashMap<String, Stmt> labeledStmts = new HashMap<>();
      private HashMap<String, Local> locals = new HashMap<>();

      public Local getLocal(@Nonnull String name) {
        return locals.computeIfAbsent(
            name, (ignored) -> new Local(name, UnknownType.getInstance()));
      }

      @Override
      @Nonnull
      public SootMethod visitMethod(@Nonnull JimpleParser.MethodContext ctx) {
        Body.BodyBuilder builder = Body.builder();

        EnumSet<Modifier> modifier =
            ctx.modifier() == null ? EnumSet.noneOf(Modifier.class) : getModifiers(ctx.modifier());

        final Type type = util.getType(ctx.type().getText());
        if (type == null) {
          throw new ResolveException("Returntype not found.", path, buildPositionFromCtx(ctx));
        }

        final String methodname = ctx.method_name().getText();
        if (methodname == null) {
          throw new ResolveException(" Methodname not found.", path, buildPositionFromCtx(ctx));
        }

        List<Type> params = util.getTypeList(ctx.type_list());

        MethodSignature methodSignature =
            identifierFactory.getMethodSignature(
                StringTools.getUnEscapedStringOf(methodname), clazz, type, params);
        builder.setMethodSignature(methodSignature);

        List<ClassType> exceptions =
            ctx.throws_clause() == null
                ? Collections.emptyList()
                : util.getClassTypeList(ctx.throws_clause().type_list());

        if (ctx.method_body() == null) {
          throw new ResolveException("404 Body not found.", path, buildPositionFromCtx(ctx));
        } else if (ctx.method_body().SEMICOLON() == null) {

          // declare locals
          locals = new HashMap<>();
          if (ctx.method_body().declaration() != null) {
            for (JimpleParser.DeclarationContext it : ctx.method_body().declaration()) {
              final String typeStr = it.type().getText();
              Type localtype =
                  typeStr.equals("unknown") ? UnknownType.getInstance() : util.getType(typeStr);

              // validate nonvoid
              if (localtype == VoidType.getInstance()) {
                throw new ResolveException(
                    "Void is not an allowed Type for a Local.", path, buildPositionFromCtx(ctx));
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
                          buildPositionFromCtx(ctx));
                    }
                  }
                }
              }
            }
          }
          builder.setLocals(new HashSet<>(locals.values()));

          // statements
          StmtVisitor stmtVisitor = new StmtVisitor(builder);
          if (ctx.method_body().statement() != null) {
            ctx.method_body().statement().forEach(stmtVisitor::visitStatement);
          }

          // catch_clause
          List<Trap> traps = new ArrayList<>();
          final List<JimpleParser.Trap_clauseContext> trap_clauseContexts =
              ctx.method_body().trap_clause();
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
          builder.setTraps(traps);
        } else {
          // no body is given: no brackets, but a semicolon -> abstract
        }

        Position classPosition = buildPositionFromCtx(ctx);
        builder.setPosition(classPosition);

        // associate labeled Stmts with Branching Stmts
        for (Map.Entry<Stmt, List<String>> item : unresolvedBranches.entrySet()) {
          final List<String> targetLabels = item.getValue();
          for (String targetLabel : targetLabels) {
            final Stmt target = labeledStmts.get(targetLabel);
            if (target == null) {
              throw new ResolveException(
                  "Don't jump into the Space! The target Stmt is not found i.e. no label for: "
                      + item.getKey()
                      + " to "
                      + targetLabel,
                  path,
                  buildPositionFromCtx(ctx));

            } else {
              builder.addFlow(item.getKey(), target);
            }
          }
        }

        OverridingMethodSource oms = new OverridingMethodSource(methodSignature, builder.build());
        Position methodPosition = buildPositionFromCtx(ctx);
        return new SootMethod(oms, methodSignature, modifier, exceptions, methodPosition);
      }

      private class StmtVisitor extends JimpleBaseVisitor<Stmt> {
        @Nonnull private final Body.BodyBuilder builder;
        @Nullable private Stmt lastStmt = null;
        final ValueVisitor valueVisitor = new ValueVisitor();

        private StmtVisitor(@Nonnull Body.BodyBuilder builder) {
          this.builder = builder;
        }

        @Override
        public Stmt visitStatement(JimpleParser.StatementContext ctx) {
          final JimpleParser.StmtContext stmtCtx = ctx.stmt();
          if (stmtCtx == null) {
            throw new ResolveException("Couldn't parse Stmt.", path, buildPositionFromCtx(ctx));
          }
          Stmt stmt = visitStmt(stmtCtx);
          if (ctx.label_name != null) {
            labeledStmts.put(ctx.label_name.getText(), stmt);
          }

          if (lastStmt == null) {
            builder.setStartingStmt(stmt);
          } else {
            if (lastStmt.fallsThrough()) {
              builder.addFlow(lastStmt, stmt);
            }
          }

          lastStmt = stmt;
          return stmt;
        }

        @Override
        @Nonnull
        public Stmt visitStmt(JimpleParser.StmtContext ctx) {
          StmtPositionInfo pos = new StmtPositionInfo(ctx.start.getLine());

          if (ctx.BREAKPOINT() != null) {
            return Jimple.newBreakpointStmt(pos);
          } else {
            if (ctx.ENTERMONITOR() != null) {
              return Jimple.newEnterMonitorStmt(
                  (Immediate) valueVisitor.visitImmediate(ctx.immediate()), pos);
            } else if (ctx.EXITMONITOR() != null) {
              return Jimple.newExitMonitorStmt(
                  (Immediate) valueVisitor.visitImmediate(ctx.immediate()), pos);
            } else if (ctx.SWITCH() != null) {

              Immediate key = (Immediate) valueVisitor.visitImmediate(ctx.immediate());
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
                        "Only one default label is allowed!", path, buildPositionFromCtx(ctx));
                  }
                } else if (case_labelContext.integer_constant().getText() != null) {
                  final int value =
                      Integer.parseInt(case_labelContext.integer_constant().getText());
                  min = Math.min(min, value);
                  lookup.add(IntConstant.getInstance(value));
                  targetLabels.add(it.goto_stmt().label_name.getText());
                } else {
                  throw new ResolveException("Label is invalid.", path, buildPositionFromCtx(ctx));
                }
              }
              targetLabels.add(defaultLabel);

              JSwitchStmt switchStmt;
              if (ctx.SWITCH().getText().charAt(0) == 't') {
                int high = min + lookup.size();
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
                      "Invalid assignment.", path, buildPositionFromCtx(ctx));
                }

              } else if (ctx.IF() != null) {
                final Stmt stmt =
                    Jimple.newIfStmt(valueVisitor.visitBool_expr(ctx.bool_expr()), pos);
                unresolvedBranches.put(
                    stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
                return stmt;
              } else if (ctx.goto_stmt() != null) {
                final Stmt stmt = Jimple.newGotoStmt(pos);
                unresolvedBranches.put(
                    stmt, Collections.singletonList(ctx.goto_stmt().label_name.getText()));
                return stmt;
              } else if (ctx.NOP() != null) {
                return Jimple.newNopStmt(pos);
              } else if (ctx.RET() != null) {
                return Jimple.newRetStmt(
                    (Immediate) valueVisitor.visitImmediate(ctx.immediate()), pos);
              } else if (ctx.RETURN() != null) {
                if (ctx.immediate() == null) {
                  return Jimple.newReturnVoidStmt(pos);
                } else {
                  return Jimple.newReturnStmt(
                      (Immediate) valueVisitor.visitImmediate(ctx.immediate()), pos);
                }
              } else if (ctx.THROW() != null) {
                return Jimple.newThrowStmt(
                    (Immediate) valueVisitor.visitImmediate(ctx.immediate()), pos);
              } else if (ctx.invoke_expr() != null) {
                return Jimple.newInvokeStmt(
                    (AbstractInvokeExpr) valueVisitor.visitInvoke_expr(ctx.invoke_expr()), pos);
              }
            }
          }
          throw new ResolveException("Unknown Stmt.", path, buildPositionFromCtx(ctx));
        }
      }

      private class ValueVisitor extends JimpleBaseVisitor<Value> {

        @Override
        public Value visitValue(JimpleParser.ValueContext ctx) {
          if (ctx.NEW() != null && ctx.base_type != null) {
            final Type type = util.getType(ctx.base_type.getText());
            if (!(type instanceof ReferenceType)) {
              throw new ResolveException(
                  type + " is not a ReferenceType.", path, buildPositionFromCtx(ctx));
            }
            return Jimple.newNewExpr((ReferenceType) type);
          } else if (ctx.NEWARRAY() != null) {
            final Type type = util.getType(ctx.array_type.getText());
            if (type instanceof VoidType || type instanceof NullType) {
              throw new ResolveException(
                  type + " can not be an ArrayType.", path, buildPositionFromCtx(ctx));
            }

            Immediate dim = (Immediate) visitImmediate(ctx.array_descriptor().immediate());
            return JavaJimple.getInstance().newNewArrayExpr(type, dim);
          } else if (ctx.NEWMULTIARRAY() != null && ctx.immediate() != null) {
            final Type type = util.getType(ctx.multiarray_type.getText());
            if (!(type instanceof ReferenceType || type instanceof PrimitiveType)) {
              throw new ResolveException(
                  " Only base types are allowed", path, buildPositionFromCtx(ctx));
            }

            List<Immediate> sizes =
                ctx.immediate().stream()
                    .map(imm -> (Immediate) visitImmediate(imm))
                    .collect(Collectors.toList());
            if (sizes.size() < 1) {
              throw new ResolveException(
                  "The Size list must have at least one Element.", path, buildPositionFromCtx(ctx));
            }
            ArrayType arrtype = identifierFactory.getArrayType(type, sizes.size());
            return Jimple.newNewMultiArrayExpr(arrtype, sizes);
          } else if (ctx.nonvoid_cast != null && ctx.op != null) {
            final Type type = util.getType(ctx.nonvoid_cast.getText());
            Immediate val = (Immediate) visitImmediate(ctx.op);
            return Jimple.newCastExpr(val, type);
          } else if (ctx.INSTANCEOF() != null && ctx.op != null) {
            final Type type = util.getType(ctx.nonvoid_type.getText());
            Immediate val = (Immediate) visitImmediate(ctx.op);
            return Jimple.newInstanceOfExpr(val, type);
          }
          return super.visitValue(ctx);
        }

        @Override
        public Value visitImmediate(JimpleParser.ImmediateContext ctx) {
          if (ctx.identifier() != null) {
            return getLocal(ctx.identifier().getText());
          }
          return visitConstant(ctx.constant());
        }

        @Override
        public Value visitReference(JimpleParser.ReferenceContext ctx) {

          if (ctx.array_descriptor() != null) {
            // array
            Immediate idx = (Immediate) visitImmediate(ctx.array_descriptor().immediate());
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
                    "Unknown Nonstatic Invoke.", path, buildPositionFromCtx(ctx));
            }

          } else if (ctx.staticinvoke != null) {
            MethodSignature methodSig = util.getMethodSignature(ctx.method_signature(), ctx);
            return Jimple.newStaticInvokeExpr(methodSig, arglist);
          } else if (ctx.dynamicinvoke != null) {

            List<Type> bootstrapMethodRefParams = util.getTypeList(ctx.type_list());
            MethodSignature bootstrapMethodRef =
                identifierFactory.getMethodSignature(
                    ctx.unnamed_method_name.getText(),
                    identifierFactory.getClassType(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME),
                    util.getType(ctx.name.getText()),
                    bootstrapMethodRefParams);

            MethodSignature methodRef = util.getMethodSignature(ctx.bsm, ctx);

            List<Immediate> bootstrapArgs = getArgList(ctx.staticargs);

            return Jimple.newDynamicInvokeExpr(
                methodRef, bootstrapArgs, bootstrapMethodRef, arglist);
          }
          throw new ResolveException(
              "Malformed Invoke Expression.", path, buildPositionFromCtx(ctx));
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
            return DoubleConstant.getInstance(Double.parseDouble(floatStr));
          } else if (ctx.CLASS() != null) {
            final String text = ctx.STRING_CONSTANT().getText();
            return JavaJimple.getInstance().newClassConstant(text.substring(1, text.length() - 1));
          } else if (ctx.STRING_CONSTANT() != null) {
            final String text = StringTools.getUnEscapedStringOf(ctx.STRING_CONSTANT().getText());
            return JavaJimple.getInstance().newStringConstant(text.substring(1, text.length() - 1));
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
                    identifierFactory.getType(ctx.method_subsignature().method_name().getText()));
          }
          throw new ResolveException("Unknown Constant.", path, buildPositionFromCtx(ctx));
        }

        @Override
        public AbstractBinopExpr visitBinop_expr(JimpleParser.Binop_exprContext ctx) {

          Value left = visitImmediate(ctx.left);
          Value right = visitImmediate(ctx.right);

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
              "Unknown BinOp: " + binopctx.getText(), path, buildPositionFromCtx(ctx));
        }

        @Override
        public Expr visitUnop_expr(JimpleParser.Unop_exprContext ctx) {
          Immediate value = (Immediate) visitImmediate(ctx.immediate());
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
            arglist.add((Immediate) visitImmediate(immediate));
          }
          return arglist;
        }
      }
    }
  }

  @Nonnull
  private Position buildPositionFromCtx(@Nonnull ParserRuleContext ctx) {
    return new Position(
        ctx.start.getLine(),
        ctx.start.getCharPositionInLine(),
        ctx.stop.getLine(),
        ctx.stop.getCharPositionInLine());
  }
}
