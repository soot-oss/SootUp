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
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.jimple.JimpleBaseVisitor;
import de.upb.swt.soot.jimple.JimpleLexer;
import de.upb.swt.soot.jimple.JimpleParser;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.*;

class JimpleReader {

  final IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private Map<String, PackageName> imports = new HashMap<>();
  private final HashMap<Stmt, List<String>> unresolvedBranches = new HashMap<>();
  private final HashMap<String, Stmt> labeledStmts = new HashMap<>();
  private HashMap<String, Local> locals = new HashMap<>();

  private Type getType(String typename) {
    PackageName packageName = imports.get(typename);
    return packageName == null
        ? identifierFactory.getType(typename)
        : identifierFactory.getClassType(typename, packageName.getPackageName());
  }

  private ClassType getClassType(String typename) {
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

    ClassVisitor classVisitor = new ClassVisitor();

    try {
      classVisitor.visit(parser.file());
    } catch (Exception e) {
      throw new RuntimeException(
          "The Jimple file " + sourcePath.toAbsolutePath() + " is not well formed.", e);
    }

    /*    if( classVisitor.clazz != classSignature ){
          throw new RuntimeException("Filename does not match the parsed Classname.");
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
    private Stmt lastStmt = null;

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
        // FIXME: we dont print outerclass in the Printer
        final String classname = ctx.classname.getText();
        final int dollarPostition = classname.indexOf('$');
        if (dollarPostition > -1) {
          outerclass = getClassType(classname.substring(0, dollarPostition));
          clazz = getClassType(classname.substring(dollarPostition + 1));
        } else {
          clazz = getClassType(classname);
        }

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
            ctx.implements_clause().accept(new NameListVisitor()).stream()
                .map(identifierFactory::getClassType)
                .collect(Collectors.toSet());
      } else {
        interfaces = Collections.emptySet();
      }

      // member
      for (int i = 0; i < ctx.member().size(); i++) {
        SootClassMember scm = ctx.member(i).accept(new ClassMemberVisitor());
        if (scm instanceof SootMethod) {
          methods.add((SootMethod) scm);
          lastStmt = null;
        } else {
          fields.add((SootField) scm);
        }
      }

      return true;
    }

    private class NameListVisitor extends JimpleBaseVisitor<Collection<String>> {

      @Override
      public List<String> visitType_list(JimpleParser.Type_listContext ctx) {
        List<String> list = new ArrayList<>();
        iterate(list, ctx);
        return list;
      }

      @Override
      public List<String> visitName(JimpleParser.NameContext ctx) {
        return Collections.singletonList(ctx.getText());
      }

      @Override
      public Set<String> visitImplements_clause(JimpleParser.Implements_clauseContext ctx) {
        Set<String> interfaces = new HashSet<>();
        iterate(interfaces, ctx.type_list());
        return interfaces;
      }

      public void iterate(Collection<String> list, JimpleParser.Type_listContext ctx) {
        JimpleParser.Type_listContext name_listContextIterator = ctx;
        while (name_listContextIterator != null) {
          if (name_listContextIterator.type() == null) {
            break;
          }
          list.add(name_listContextIterator.type().getText());
          name_listContextIterator = name_listContextIterator.type_list();
        }
      }
    }

    private class ClassMemberVisitor extends JimpleBaseVisitor<SootClassMember<?>> {

      @Override
      public SootField visitField(JimpleParser.FieldContext ctx) {

        EnumSet<Modifier> modifier = getModifiers(ctx.modifier());

        return new SootField(
            identifierFactory.getFieldSignature(ctx.name().getText(), clazz, ctx.type().getText()),
            modifier);
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
                : ctx.type_list().accept(new NameListVisitor()).stream()
                    .map(identifierFactory::getType)
                    .collect(Collectors.toList());

        MethodSignature methodSignature =
            identifierFactory.getMethodSignature(methodname, clazz, type, params);
        builder.setMethodSignature(methodSignature);

        List<ClassType> exceptions =
            ctx.throws_clause() == null
                ? Collections.emptyList()
                : ctx.throws_clause().accept(new NameListVisitor()).stream()
                    .map(identifierFactory::getClassType)
                    .collect(Collectors.toList());

        if (ctx.method_body() == null) {
          throw new IllegalStateException("Body not found");
        } else if (ctx.method_body().SEMICOLON() != null) {
          // no body is given
        } else {

          // declare locals
          locals = new HashMap<>();
          if (ctx.method_body().declaration() != null) {
            for (JimpleParser.DeclarationContext it : ctx.method_body().declaration()) {
              final String typeStr = it.name().getText();
              Type localtype =
                  typeStr.equals("unknown") ? UnknownType.getInstance() : getType(typeStr);

              // validate nonvoid
              if (localtype == VoidType.getInstance()) {
                throw new IllegalStateException("void is not an allowed Type for a Local.");
              }

              // TODO: [ms] check is distinction necessary and not already handled?
              List<String> list =
                  (List<String>)
                      (it.type_list() != null
                          ? it.type_list().accept(new NameListVisitor())
                          : it.accept(new NameListVisitor()));
              list.forEach(
                  localname -> {
                    locals.put(localname, new Local(localname, localtype));
                  });
            }
          }
          builder.setLocals(new HashSet<>(locals.values()));

          // statements
          StmtVisitor stmtVisitor = new StmtVisitor(builder);
          if (ctx.method_body().statement() != null) {
            ctx.method_body().statement().forEach(statement -> statement.accept(stmtVisitor));
          }

          // catch_clause
          List<Trap> traps = new ArrayList<>();
          final List<JimpleParser.Trap_clauseContext> trap_clauseContexts =
              ctx.method_body().trap_clause();
          if (trap_clauseContexts != null) {
            for (JimpleParser.Trap_clauseContext it : trap_clauseContexts) {
              ClassType exceptionType = getClassType(it.exceptiontype.getText());
              // FIXME traps.. how do those stmtBoxes work?
              // Jimple.newTrap( exceptionType, jumpTargets.get(it.from),
              // jumpTargets.get(it.to),jumpTargets.get(it.with) );
            }
          }
          builder.setTraps(traps);
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
                  "don't jump into the space! target Stmt not found i.e. no label: "
                      + item.getKey());
            } else {
              builder.addFlow(item.getKey(), target);
            }
          }
        }

        OverridingMethodSource oms = new OverridingMethodSource(methodSignature, builder.build());

        return new SootMethod(oms, methodSignature, modifier, exceptions);
      }
    }

    private EnumSet<Modifier> getModifiers(List<JimpleParser.ModifierContext> modifier) {
      Set<Modifier> modifierSet =
          modifier.stream()
              .map(modifierContext -> Modifier.valueOf(modifierContext.getText().toUpperCase()))
              .collect(Collectors.toSet());
      return modifierSet.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(modifierSet);
    }

    private class ArgListVisitor extends JimpleBaseVisitor<List<Immediate>> {
      @Override
      public List<Immediate> visitArg_list(JimpleParser.Arg_listContext ctx) {
        List<Immediate> args = new ArrayList<>();
        JimpleParser.Arg_listContext immediateIterator = ctx;
        do {
          args.add((Immediate) immediateIterator.immediate().accept(new ValueVisitor()));
          immediateIterator = ctx.arg_list();
        } while (immediateIterator != null);

        return args;
      }
    }

    private class StmtVisitor extends JimpleBaseVisitor<Stmt> {
      @Nonnull private final Body.BodyBuilder builder;

      private StmtVisitor(Body.BodyBuilder builder) {
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
            //            System.out.print("link: "+ lastStmt + " => "+ stmt);
            builder.addFlow(lastStmt, stmt);
          }
        }
        System.out.println(stmt);
        lastStmt = stmt;
        return stmt;
      }

      @Override
      @Nonnull
      public Stmt visitStmt(JimpleParser.StmtContext ctx) {

        StmtPositionInfo pos = new StmtPositionInfo(ctx.start.getLine());

        if (ctx.BREAKPOINT() != null) {
          return Jimple.newBreakpointStmt(pos);
        } else if (ctx.ENTERMONITOR() != null) {
          return Jimple.newEnterMonitorStmt(
              (Immediate) ctx.immediate().accept(new ValueVisitor()), pos);
        } else if (ctx.EXITMONITOR() != null) {
          return Jimple.newExitMonitorStmt(
              (Immediate) ctx.immediate().accept(new ValueVisitor()), pos);
        } else if (ctx.SWITCH() != null) {

          Immediate key = (Immediate) ctx.immediate().accept(new ValueVisitor());
          List<IntConstant> lookup = new ArrayList<>();
          List<String> targetLabels = new ArrayList<>();
          int min = Integer.MAX_VALUE;
          String defaultLabel = null;

          for (JimpleParser.Case_stmtContext it : ctx.case_stmt()) {
            final JimpleParser.Case_labelContext case_labelContext = it.case_label();
            if (case_labelContext.getText() != null && case_labelContext.DEFAULT() != null) {
              defaultLabel = case_labelContext.getText();
            } else if (case_labelContext.getText() != null) {
              final int value = Integer.parseInt(case_labelContext.getText());
              min = Math.min(min, value);
              lookup.add(IntConstant.getInstance(value));
              targetLabels.add(case_labelContext.getText());
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
            if (assignments.EQUALS() == null) {
              Local left = (Local) assignments.local.accept(new ValueVisitor());
              final String type = assignments.at_identifier().type().getText();

              IdentityRef ref;
              final JimpleParser.At_identifierContext at_identifierContext =
                  assignments.at_identifier();
              if (at_identifierContext.caught != null) {
                ref = JavaJimple.getInstance().newCaughtExceptionRef();
              } else if (at_identifierContext.parameter_idx != null) {
                int idx = Integer.parseInt(at_identifierContext.parameter_idx.getText());
                ref = Jimple.newParameterRef(getType(type), idx);
              } else {
                // @this: refers always to the current class so we reuse the Type retreived from the
                // classname
                // TODO: parse it - validate later
                ref = Jimple.newThisRef(clazz);
              }
              return Jimple.newIdentityStmt(left, ref, pos);

            } else {
              Value left =
                  assignments.local != null
                      ? assignments.local.accept(new ValueVisitor())
                      : assignments.reference().accept(new ValueVisitor());

              final Value right = assignments.expression().accept(new ValueVisitor());
              System.out.println(left);
              System.out.println(right);
              return Jimple.newAssignStmt(left, right, pos);
            }

          } else if (ctx.IF() != null) {
            final Stmt stmt = Jimple.newIfStmt(ctx.bool_expr().accept(new ValueVisitor()), pos);
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
            return Jimple.newRetStmt((Immediate) ctx.immediate().accept(new ValueVisitor()), pos);
          } else if (ctx.RETURN() != null) {
            if (ctx.immediate() == null) {
              return Jimple.newReturnVoidStmt(pos);
            } else {
              return Jimple.newReturnStmt(
                  (Immediate) ctx.immediate().accept(new ValueVisitor()), pos);
            }
          } else if (ctx.THROW() != null) {
            return Jimple.newThrowStmt((Immediate) ctx.immediate().accept(new ValueVisitor()), pos);
          } else if (ctx.invoke_expr() != null) {
            return Jimple.newInvokeStmt(
                (AbstractInvokeExpr) ctx.invoke_expr().accept(new ValueVisitor()), pos);
          }
        }
        throw new RuntimeException("Unknown Stmt");
      }
    }

    private class ValueVisitor extends JimpleBaseVisitor<Value> {

      @Override
      public Value visitName(JimpleParser.NameContext ctx) {
        return getLocal(ctx.getText());
      }

      @Override
      public Value visitExpression(JimpleParser.ExpressionContext ctx) {
        // TODO: check naming: base_type <-> reference_type?
        // TODO: check wich basetype / nonvoid_type / ... are valid

        if (ctx.NEW() != null) {
          final Type type = getType(ctx.base_type.getText());
          if (!(type instanceof ReferenceType)) {
            throw new IllegalStateException("only base types are allowed");
          }
          return Jimple.newNewExpr((ReferenceType) type);
        } else if (ctx.NEWARRAY() != null) {
          final Type type = getType(ctx.nonvoid_type.getText());
          if (!(type instanceof ReferenceType)) {
            throw new IllegalStateException("only base types are allowed");
          }
          Immediate dim = (Immediate) ctx.fixed_array_descriptor().name().accept(this);
          return JavaJimple.getInstance().newNewArrayExpr(type, dim);
        } else if (ctx.NEWMULTIARRAY() != null) {
          final Type type = getType(ctx.base_type.getText());
          if (!(type instanceof ReferenceType)) {
            throw new IllegalStateException("only base types are allowed");
          }
          List<Immediate> sizes =
              ctx.immediate().stream()
                  .map(imm -> (Immediate) imm.accept(this))
                  .collect(Collectors.toList());
          if (sizes.size() < 1) {
            throw new IllegalStateException("size list must have at least one element;");
          }
          ArrayType arrtype = JavaIdentifierFactory.getInstance().getArrayType(type, sizes.size());
          return Jimple.newNewMultiArrayExpr(arrtype, sizes);
        } else if (ctx.nonvoid_cast != null) {
          final Type type = getType(ctx.nonvoid_cast.getText());
          Immediate val = (Immediate) ctx.op.accept(this);
          return Jimple.newCastExpr(val, type);
        } else if (ctx.INSTANCEOF() != null) {
          final Type type = getType(ctx.nonvoid_type.getText());
          Immediate val = (Immediate) ctx.op.accept(this);
          return Jimple.newInstanceOfExpr(val, type);
        }
        return super.visitExpression(ctx);
      }

      @Override
      public Value visitImmediate(JimpleParser.ImmediateContext ctx) {
        if (ctx.name() != null) {
          return getLocal(ctx.name().getText());
        }
        return ctx.constant().accept(this);
      }

      @Override
      public Value visitReference(JimpleParser.ReferenceContext ctx) {

        if (ctx.fixed_array_descriptor() != null) {
          // array
          Immediate idx = (Immediate) ctx.fixed_array_descriptor().name().accept(this);
          Local type = getLocal(ctx.name().getText());
          return JavaJimple.getInstance().newArrayRef(type, idx);
        } else if (ctx.DOT() != null) {
          // instance field
          String base = ctx.name().getText();
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

      private MethodSignature getMethodSignature(JimpleParser.Method_signatureContext ctx) {
        String classname = ctx.class_name.getText();
        Type type = getType(ctx.type().getText());
        String methodname = ctx.method_name().getText();
        final JimpleParser.Type_listContext parameterList = ctx.type_list();
        List<Type> params =
            parameterList != null
                ? parameterList.accept(new NameListVisitor()).stream()
                    .map(identifierFactory::getType)
                    .collect(Collectors.toList())
                : Collections.emptyList();
        return identifierFactory.getMethodSignature(
            methodname, getClassType(classname), type, params);
      }

      @Override
      public Expr visitInvoke_expr(JimpleParser.Invoke_exprContext ctx) {

        if (ctx.nonstaticinvoke != null) {
          Local base = getLocal(ctx.local_name.getText());
          MethodSignature methodSig = getMethodSignature(ctx.method_signature());
          List<Immediate> arglist =
              ctx.arg_list() != null && ctx.arg_list().size() > 0
                  ? ctx.arg_list().get(0).accept(new ArgListVisitor())
                  : Collections.emptyList();

          switch (ctx.nonstaticinvoke.getText().charAt(0)) {
            case 'i':
              return Jimple.newInterfaceInvokeExpr(base, methodSig, arglist);
            case 'v':
              return Jimple.newVirtualInvokeExpr(base, methodSig, arglist);
            case 's':
              return Jimple.newSpecialInvokeExpr(base, methodSig, arglist);
            default:
              throw new IllegalStateException("malformed nonstatic invoke");
          }

        } else if (ctx.staticinvoke != null) {
          MethodSignature methodSig = getMethodSignature(ctx.method_signature());
          List<Immediate> arglist = ctx.arg_list().get(0).accept(new ArgListVisitor());
          return Jimple.newStaticInvokeExpr(methodSig, arglist);
        } else if (ctx.dynamicinvoke != null) {

          Type type = getType(ctx.type().getText());
          List<Type> bootstrapMethodRefParams =
              ctx.type_list() != null
                  ? ctx.type_list().accept(new NameListVisitor()).stream()
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
          List<Immediate> args =
              ctx.dynargs != null
                  ? ctx.dynargs.accept(new ArgListVisitor())
                  : Collections.emptyList();
          List<Immediate> bootstrapArgs =
              ctx.staticargs != null
                  ? ctx.staticargs.accept(new ArgListVisitor())
                  : Collections.emptyList();

          return Jimple.newDynamicInvokeExpr(bootstrapMethodRef, bootstrapArgs, methodRef, args);
        }
        throw new IllegalStateException("malformed Invoke Expression.");
      }

      @Override
      public Constant visitConstant(JimpleParser.ConstantContext ctx) {

        if (ctx.INTEGER_CONSTANT() != null) {
          String intConst = ctx.INTEGER_CONSTANT().getText();
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
          final String text = ctx.CLASS().getText();
          return JavaJimple.getInstance().newClassConstant(text.substring(1, text.length() - 1));
        } else if (ctx.STRING_CONSTANT() != null) {
          final String text = ctx.STRING_CONSTANT().getText();
          return JavaJimple.getInstance().newStringConstant(text.substring(1, text.length() - 1));
        } else if (ctx.NULL() != null) {
          return NullConstant.getInstance();
        }
        throw new IllegalStateException("Unknown Constant");
      }

      @Override
      public AbstractBinopExpr visitBinop_expr(JimpleParser.Binop_exprContext ctx) {

        Value left = ctx.left.accept(this);
        Value right = ctx.right.accept(this);

        JimpleParser.BinopContext binopctx = ctx.op;

        // [ms] maybe its faster to switch( binopctx.getText().hashCode() ) ?
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
        }
        throw new RuntimeException("Unknown BinOp");
      }

      @Override
      public Expr visitUnop_expr(JimpleParser.Unop_exprContext ctx) {
        Immediate value = (Immediate) ctx.immediate().accept(this);
        if (ctx.unop().NEG() != null) {
          return Jimple.newNegExpr(value);
        } else {
          return Jimple.newLengthExpr(value);
        }
      }
    }

    public Local getLocal(String name) {
      final Local local = locals.get(name);
      if (local == null) {
        throw new IllegalStateException("a Stmt tried to reference an undeclared Local: " + name);
      }
      return local;
    }
  }
}
