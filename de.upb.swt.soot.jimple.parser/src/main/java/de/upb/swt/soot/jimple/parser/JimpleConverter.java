package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
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
import de.upb.swt.soot.core.signatures.PackageName;
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

class JimpleConverter {

  final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private Map<String, PackageName> imports = new HashMap<>();

  private Type getType(String typename) {
    typename = StringTools.getUnEscapedStringOf(typename);
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getType(typename)
        : identifierFactory.getType(packageName.getPackageName() + "." + typename);
  }

  private ClassType getClassType(String typename) {
    typename = StringTools.getUnEscapedStringOf(typename);
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getClassType(typename)
        : identifierFactory.getClassType(typename, packageName.getPackageName());
  }

  public OverridingClassSource run(
      CharStream charStream,
      AnalysisInputLocation inputlocation,
      Path sourcePath,
      ClassType classSignature) {
    JimpleLexer lexer = new JimpleLexer(charStream);
    TokenStream tokens = new CommonTokenStream(lexer);
    JimpleParser parser = new JimpleParser(tokens);

    if (charStream.size() == 0) {
      throw new RuntimeException("Empty File to parse.");
    }

    ClassVisitor classVisitor = new ClassVisitor();

    try {
      classVisitor.visit(parser.file());
    } catch (Exception e) {
      throw new RuntimeException(
          "The Jimple file " + sourcePath.toAbsolutePath() + " is not well formed.", e);
    }

    /*  TODO: adapt check for innerclass
    if( !classVisitor.clazz.equals(classSignature) ){
      throw new RuntimeException("Filename "+ classVisitor.clazz + " does not match the parsed Classname: "+ classSignature );
    }
    */

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
    ClassType outerclass = null; // currently not determined in Java etc -> heuristic used
    Position position = NoPositionInformation.getInstance();
    EnumSet<Modifier> modifiers = null;

    @Override
    @Nonnull
    public Boolean visitFile(@Nonnull JimpleParser.FileContext ctx) {

      // position
      position =
          new Position(
              ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.stop.getLine(), -1);

      // imports
      imports =
          ctx.importItem().stream()
              .filter(item -> item.location != null)
              .map(item -> identifierFactory.getClassType(item.location.getText()))
              .collect(
                  Collectors.toMap(
                      ClassType::getClassName,
                      ClassType::getPackageName,
                      (a, b) -> {
                        if (!a.equals(b)) {
                          throw new IllegalStateException(
                              "Multiple Imports for the same ClassName can not be resolved!");
                        }
                        return b;
                      }));

      // class_name
      if (ctx.classname != null) {

        // "$" in classname is a heuristic for an inner/outer class
        final String classname = StringTools.getUnEscapedStringOf(ctx.classname.getText());
        final int dollarPostition = classname.indexOf('$');
        if (dollarPostition > -1) {
          outerclass = getClassType(classname.substring(0, dollarPostition));
        }
        clazz = getClassType(classname);

      } else {
        throw new IllegalStateException("Class is not well formed.");
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
        superclass = getClassType(ctx.extends_clause().classname.getText());
      } else {
        superclass = null;
      }

      // implements_clause
      if (ctx.implements_clause() != null) {
        interfaces =
            getTypeList(ctx.implements_clause().type_list()).stream()
                .map(identifierFactory::getClassType)
                .collect(Collectors.toSet());
      } else {
        interfaces = Collections.emptySet();
      }

      // member
      for (int i = 0; i < ctx.member().size(); i++) {
        if (ctx.member(i).method() != null) {
          methods.add(new MethodVisitor().visitMember(ctx.member(i)));
        } else {
          final JimpleParser.FieldContext field = ctx.member(i).field();
          EnumSet<Modifier> modifier = getModifiers(field.modifier());
          fields.add(
              new SootField(
                  identifierFactory.getFieldSignature(
                      field.IDENTIFIER().getText(), clazz, field.type().getText()),
                  modifier));
        }
      }

      return true;
    }

    List<String> getTypeList(JimpleParser.Type_listContext ctx) {
      final List<JimpleParser.TypeContext> typeList = ctx.type();
      final int size = typeList.size();
      List<String> list = new ArrayList<>(size);

      for (JimpleParser.TypeContext typeContext : typeList) {
        list.add(typeContext.getText());
      }

      return list;
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

      public Local getLocal(String name) {
        final Local local = locals.get(name);
        if (local == null) {
          throw new IllegalStateException("a Stmt tried to reference an undeclared Local: " + name);
        }
        return local;
      }

      @Override
      @Nonnull
      public SootMethod visitMethod(@Nonnull JimpleParser.MethodContext ctx) {
        Body.BodyBuilder builder = Body.builder();

        EnumSet<Modifier> modifier =
            ctx.modifier() == null ? EnumSet.noneOf(Modifier.class) : getModifiers(ctx.modifier());

        final Type type = getType(ctx.type().getText());
        if (type == null) {
          throw new IllegalStateException("returntype not found");
        }

        final String methodname = ctx.method_name().getText();
        if (methodname == null) {
          throw new IllegalStateException("methodname not found");
        }

        List<Type> params =
            ctx.type_list() == null
                ? Collections.emptyList()
                : getTypeList(ctx.type_list()).stream()
                    .map(identifierFactory::getType)
                    .collect(Collectors.toList());

        MethodSignature methodSignature =
            identifierFactory.getMethodSignature(
                StringTools.getUnEscapedStringOf(methodname), clazz, type, params);
        builder.setMethodSignature(methodSignature);

        List<ClassType> exceptions =
            ctx.throws_clause() == null
                ? Collections.emptyList()
                : getTypeList(ctx.throws_clause().type_list()).stream()
                    .map(identifierFactory::getClassType)
                    .collect(Collectors.toList());

        if (ctx.method_body() == null) {
          throw new IllegalStateException("Body not found");
        } else if (ctx.method_body().SEMICOLON() == null) {

          // declare locals
          locals = new HashMap<>();
          if (ctx.method_body().declaration() != null) {
            for (JimpleParser.DeclarationContext it : ctx.method_body().declaration()) {
              final String typeStr = it.type().getText();
              Type localtype =
                  typeStr.equals("unknown") ? UnknownType.getInstance() : getType(typeStr);

              // validate nonvoid
              if (localtype == VoidType.getInstance()) {
                throw new IllegalStateException("void is not an allowed Type for a Local.");
              }

              if (it.arg_list() != null) {
                final List<JimpleParser.ImmediateContext> immediates = it.arg_list().immediate();
                if (immediates != null) {
                  for (JimpleParser.ImmediateContext immediate : immediates) {
                    if (immediate != null && immediate.local != null) {
                      String localname = immediate.local.getText();
                      locals.put(localname, new Local(localname, localtype));
                    } else {
                      throw new RuntimeException(
                          "In the Local Declaration you need to reference Locals.");
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
              ClassType exceptionType = getClassType(it.exceptiontype.getText());
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

        Position position =
            new Position(
                ctx.start.getLine(),
                ctx.start.getCharPositionInLine(),
                ctx.stop.getLine(),
                ctx.stop.getCharPositionInLine());
        builder.setPosition(position);

        // associate labeled Stmts with Branching Stmts
        for (Map.Entry<Stmt, List<String>> item : unresolvedBranches.entrySet()) {
          final List<String> targetLabels = item.getValue();
          for (String targetLabel : targetLabels) {
            final Stmt target = labeledStmts.get(targetLabel);
            if (target == null) {
              throw new IllegalStateException(
                  "don't jump into the space! target Stmt not found i.e. no label for: "
                      + item.getKey()
                      + " to "
                      + targetLabel);
            } else {
              builder.addFlow(item.getKey(), target);
            }
          }
        }

        OverridingMethodSource oms = new OverridingMethodSource(methodSignature, builder.build());

        return new SootMethod(oms, methodSignature, modifier, exceptions);
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
          Stmt stmt = visitStmt(ctx.stmt());
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
                    throw new RuntimeException("only one default label is allowed!");
                  }
                } else if (case_labelContext.integer_constant().getText() != null) {
                  final int value =
                      Integer.parseInt(case_labelContext.integer_constant().getText());
                  min = Math.min(min, value);
                  lookup.add(IntConstant.getInstance(value));
                  targetLabels.add(it.goto_stmt().label_name.getText());
                } else {
                  throw new RuntimeException("Label is invalid.");
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
                      ref = Jimple.newParameterRef(getType(type), idx);
                    } else {
                      if (clazz.toString().equals(type)) {
                        // reuse
                        ref = Jimple.newThisRef(clazz);
                      } else {
                        ref = Jimple.newThisRef(getClassType(type));
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
                  throw new RuntimeException("bad assignment");
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
          throw new RuntimeException("Unknown Stmt");
        }
      }

      private class ValueVisitor extends JimpleBaseVisitor<Value> {

        @Override
        public Value visitValue(JimpleParser.ValueContext ctx) {
          if (ctx.NEW() != null) {
            final Type type = getType(ctx.base_type.getText());
            if (!(type instanceof ReferenceType)) {
              throw new IllegalStateException(type + " is not a ReferenceType.");
            }
            return Jimple.newNewExpr((ReferenceType) type);
          } else if (ctx.NEWARRAY() != null) {
            final Type type = getType(ctx.array_type.getText());
            if (type instanceof VoidType || type instanceof NullType) {
              throw new IllegalStateException(type + " can not be an array type.");
            }

            Immediate dim = (Immediate) visitImmediate(ctx.array_descriptor().immediate());
            return JavaJimple.getInstance().newNewArrayExpr(type, dim);
          } else if (ctx.NEWMULTIARRAY() != null) {
            final Type type = getType(ctx.multiarray_type.getText());
            if (!(type instanceof ReferenceType || type instanceof PrimitiveType)) {
              throw new IllegalStateException("only base types are allowed");
            }

            List<Immediate> sizes =
                ctx.immediate().stream()
                    .map(imm -> (Immediate) visitImmediate(imm))
                    .collect(Collectors.toList());
            if (sizes.size() < 1) {
              throw new IllegalStateException("size list must have at least one element;");
            }
            ArrayType arrtype =
                JavaIdentifierFactory.getInstance().getArrayType(type, sizes.size());
            return Jimple.newNewMultiArrayExpr(arrtype, sizes);
          } else if (ctx.nonvoid_cast != null) {
            final Type type = getType(ctx.nonvoid_cast.getText());
            Immediate val = (Immediate) visitImmediate(ctx.op);
            return Jimple.newCastExpr(val, type);
          } else if (ctx.INSTANCEOF() != null) {
            final Type type = getType(ctx.nonvoid_type.getText());
            Immediate val = (Immediate) visitImmediate(ctx.op);
            return Jimple.newInstanceOfExpr(val, type);
          }
          return super.visitValue(ctx);
        }

        @Override
        public Value visitImmediate(JimpleParser.ImmediateContext ctx) {
          if (ctx.IDENTIFIER() != null) {
            return getLocal(ctx.IDENTIFIER().getText());
          }
          return visitConstant(ctx.constant());
        }

        @Override
        public Value visitReference(JimpleParser.ReferenceContext ctx) {

          if (ctx.array_descriptor() != null) {
            // array
            Immediate idx = (Immediate) visitImmediate(ctx.array_descriptor().immediate());
            Local type = getLocal(ctx.IDENTIFIER().getText());
            return JavaJimple.getInstance().newArrayRef(type, idx);
          } else if (ctx.DOT() != null) {
            // instance field
            String base = ctx.IDENTIFIER().getText();
            FieldSignature fs = getFieldSignature(ctx.field_signature());

            return Jimple.newInstanceFieldRef(getLocal(base), fs);

          } else {
            // static field
            FieldSignature fs = getFieldSignature(ctx.field_signature());
            return Jimple.newStaticFieldRef(fs);
          }
        }

        private FieldSignature getFieldSignature(JimpleParser.Field_signatureContext ctx) {
          String classname = ctx.classname.getText();
          Type type = getType(ctx.type().getText());
          String fieldname = ctx.fieldname.getText();
          return identifierFactory.getFieldSignature(fieldname, getClassType(classname), type);
        }

        @Override
        public Expr visitInvoke_expr(JimpleParser.Invoke_exprContext ctx) {

          List<Immediate> arglist = getArgList(ctx.arg_list(0));

          if (ctx.nonstaticinvoke != null) {
            Local base = getLocal(ctx.local_name.getText());
            MethodSignature methodSig = getMethodSignature(ctx.method_signature());

            switch (ctx.nonstaticinvoke.getText().charAt(0)) {
              case 'i':
                return Jimple.newInterfaceInvokeExpr(base, methodSig, arglist);
              case 'v':
                return Jimple.newVirtualInvokeExpr(base, methodSig, arglist);
              case 's':
                return Jimple.newSpecialInvokeExpr(base, methodSig, arglist);
              default:
                throw new IllegalStateException("malformed nonstatic invoke.");
            }

          } else if (ctx.staticinvoke != null) {
            MethodSignature methodSig = getMethodSignature(ctx.method_signature());
            return Jimple.newStaticInvokeExpr(methodSig, arglist);
          } else if (ctx.dynamicinvoke != null) {

            // FIXME: [ms] look in old soot how it should look like; implement MethodType.toString()
            Type type = getType(ctx.type().getText());
            List<Type> bootstrapMethodRefParams =
                ctx.type_list() != null
                    ? getTypeList(ctx.type_list()).stream()
                        .map(identifierFactory::getType)
                        .collect(Collectors.toList())
                    : Collections.emptyList();
            String unnamed_method_name = ctx.unnamed_method_name.getText();

            MethodSignature bootstrapMethodRef =
                identifierFactory.getMethodSignature(
                    unnamed_method_name,
                    identifierFactory.getClassType(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME),
                    type,
                    bootstrapMethodRefParams);

            MethodSignature methodRef = getMethodSignature(ctx.bsm);

            List<Immediate> bootstrapArgs = getArgList(ctx.staticargs);

            return Jimple.newDynamicInvokeExpr(
                bootstrapMethodRef, bootstrapArgs, methodRef, arglist);
          }
          throw new IllegalStateException("malformed Invoke Expression.");
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
            return BooleanConstant.getInstance(
                ctx.BOOL_CONSTANT().getText().charAt(0) == 't'
                    || ctx.BOOL_CONSTANT().getText().charAt(0) == 'T');
          } else if (ctx.NULL() != null) {
            return NullConstant.getInstance();
          }
          throw new IllegalStateException("Unknown Constant");
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
          } else if (binopctx.MOD() != null) {
            return new JRemExpr(left, right);
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
          }
          throw new RuntimeException("Unknown BinOp: " + binopctx.getText());
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
        private MethodSignature getMethodSignature(JimpleParser.Method_signatureContext ctx) {
          String classname = ctx.class_name.getText();
          Type type = getType(ctx.type().getText());
          String methodname = ctx.method_name().getText();
          final JimpleParser.Type_listContext parameterList = ctx.type_list();
          List<Type> params =
              parameterList != null
                  ? getTypeList(parameterList).stream()
                      .map(identifierFactory::getType)
                      .collect(Collectors.toList())
                  : Collections.emptyList();
          return identifierFactory.getMethodSignature(
              methodname, getClassType(classname), type, params);
        }

        @Nonnull
        private List<Immediate> getArgList(JimpleParser.Arg_listContext ctx) {
          if (ctx == null || ctx.immediate() == null) {
            return Collections.emptyList();
          }
          final List<JimpleParser.ImmediateContext> immediates = ctx.immediate();
          List<Immediate> arglist = new ArrayList<>();
          for (JimpleParser.ImmediateContext immediate : immediates) {
            arglist.add((Immediate) visitImmediate(immediate));
          }
          return arglist;
        }
      }
    }
  }
}
