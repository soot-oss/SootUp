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
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.signatures.SootClassMemberSubSignature;
import sootup.core.types.*;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.jimple.JimpleBaseVisitor;
import sootup.jimple.JimpleParser;

public class ValueVisitor extends JimpleBaseVisitor<Value> {

  @NonNull private final JimpleBodyConverterState state;

  public ValueVisitor(@NonNull JimpleBodyConverterState state) {
    this.state = state;
  }

  @Override
  public Value visitValue(JimpleParser.ValueContext ctx) {
    if (ctx.NEW() != null && ctx.base_type != null) {
      final Type type = state.getUtil().getType(ctx.base_type.getText());
      if (!(type instanceof ReferenceType)) {
        throw new ResolveException(
            type + " is not a ReferenceType.",
            state.getPath(),
            JimpleConverterUtil.buildPositionFromCtx(ctx));
      }
      return Jimple.newNewExpr((ClassType) type);
    } else if (ctx.NEWARRAY() != null) {
      final Type type = state.getUtil().getType(ctx.array_type.getText());
      if (type instanceof VoidType || type instanceof NullType) {
        throw new ResolveException(
            type + " can not be an ArrayType.",
            state.getPath(),
            JimpleConverterUtil.buildPositionFromCtx(ctx));
      }

      Immediate dim = visitImmediate(ctx.array_descriptor().immediate());
      return JavaJimple.newNewArrayExpr(type, dim, JavaIdentifierFactory.getInstance());
    } else if (ctx.NEWMULTIARRAY() != null && ctx.immediate() != null) {
      final Type type = state.getUtil().getType(ctx.multiarray_type.getText());
      if (!(type instanceof ReferenceType || type instanceof PrimitiveType)) {
        throw new ResolveException(
            " Only base types are allowed",
            state.getPath(),
            JimpleConverterUtil.buildPositionFromCtx(ctx));
      }

      List<Immediate> sizes =
          ctx.immediate().stream().map(this::visitImmediate).collect(Collectors.toList());
      if (sizes.isEmpty()) {
        throw new ResolveException(
            "The Size list must have at least one Element.",
            state.getPath(),
            JimpleConverterUtil.buildPositionFromCtx(ctx));
      }
      ArrayType arrtype = state.getIdentifierFactory().getArrayType(type, sizes.size());
      return Jimple.newNewMultiArrayExpr(arrtype, sizes);
    } else if (ctx.nonvoid_cast != null && ctx.op != null) {
      final Type type = state.getUtil().getType(ctx.nonvoid_cast.getText());
      Immediate val = visitImmediate(ctx.op);
      return Jimple.newCastExpr(val, type);
    } else if (ctx.INSTANCEOF() != null && ctx.op != null) {
      final Type type = state.getUtil().getType(ctx.nonvoid_type.getText());
      Immediate val = visitImmediate(ctx.op);
      return Jimple.newInstanceOfExpr(val, type);
    }
    return super.visitValue(ctx);
  }

  @Override
  public Immediate visitImmediate(JimpleParser.ImmediateContext ctx) {
    if (ctx.identifier() != null) {
      return state.getLocal(ctx.identifier().getText());
    }
    return visitConstant(ctx.constant());
  }

  @Override
  public Value visitReference(JimpleParser.ReferenceContext ctx) {

    if (ctx.array_descriptor() != null) {
      // array
      Immediate idx = visitImmediate(ctx.array_descriptor().immediate());
      Local type = state.getLocal(ctx.identifier().getText());
      return JavaJimple.newArrayRef(type, idx);
    } else if (ctx.DOT() != null) {
      // instance field
      String base = ctx.identifier().getText();
      FieldSignature fs = state.getUtil().getFieldSignature(ctx.field_signature());
      return Jimple.newInstanceFieldRef(state.getLocal(base), fs);

    } else {
      // static field
      FieldSignature fs = state.getUtil().getFieldSignature(ctx.field_signature());
      return Jimple.newStaticFieldRef(fs);
    }
  }

  @Override
  public Expr visitInvoke_expr(JimpleParser.Invoke_exprContext ctx) {

    List<Immediate> arglist = getArgList(ctx.arg_list(0));

    if (ctx.nonstaticinvoke != null) {
      Local base = state.getLocal(ctx.local_name.getText());
      MethodSignature methodSig = state.getUtil().getMethodSignature(ctx.method_signature(), ctx);

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
              state.getPath(),
              JimpleConverterUtil.buildPositionFromCtx(ctx));
      }

    } else if (ctx.staticinvoke != null) {
      MethodSignature methodSig = state.getUtil().getMethodSignature(ctx.method_signature(), ctx);
      return Jimple.newStaticInvokeExpr(methodSig, arglist);
    } else if (ctx.dynamicinvoke != null) {

      List<Type> bootstrapMethodRefParams = state.getUtil().getTypeList(ctx.type_list());
      MethodSignature bootstrapMethodRef =
          state
              .getIdentifierFactory()
              .getMethodSignature(
                  state
                      .getIdentifierFactory()
                      .getClassType(JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME),
                  ctx.STRING_CONSTANT().getText().replace("\"", ""),
                  state.getUtil().getType(ctx.name.getText()),
                  bootstrapMethodRefParams);

      MethodSignature methodRef = state.getUtil().getMethodSignature(ctx.bsm, ctx);

      List<Immediate> bootstrapArgs = getArgList(ctx.staticargs);

      return Jimple.newDynamicInvokeExpr(
          methodRef, bootstrapArgs, bootstrapMethodRef, getArgList(ctx.dyn_args));
    }
    throw new ResolveException(
        "Malformed Invoke Expression.",
        state.getPath(),
        JimpleConverterUtil.buildPositionFromCtx(ctx));
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
      final String text = JimpleUtils.unescape(ctx.STRING_CONSTANT().getText());
      return JavaJimple.newClassConstant(text);
    } else if (ctx.STRING_CONSTANT() != null) {
      final String text = JimpleUtils.unescape(ctx.STRING_CONSTANT().getText());
      return JavaJimple.newStringConstant(text);
    } else if (ctx.BOOL_CONSTANT() != null) {
      final char firstChar = ctx.BOOL_CONSTANT().getText().charAt(0);
      return BooleanConstant.getInstance(firstChar == 't' || firstChar == 'T');
    } else if (ctx.NULL() != null) {
      return NullConstant.getInstance();
    } else if (ctx.methodhandle() != null) {
      JimpleParser.MethodhandleContext methodhandleContext = ctx.methodhandle();
      final String kindName = methodhandleContext.STRING_CONSTANT().getText();
      final SootClassMemberSignature<? extends SootClassMemberSubSignature> referenceSignature =
          (methodhandleContext.method_signature() != null)
              ? state
                  .getUtil()
                  .getMethodSignature(methodhandleContext.method_signature(), methodhandleContext)
              : state.getUtil().getFieldSignature(methodhandleContext.field_signature());
      return JavaJimple.newMethodHandle(
          referenceSignature,
          MethodHandle.Kind.getKind(kindName.substring(1, kindName.length() - 1)));
    } else if (ctx.methodtype != null && ctx.method_subsignature() != null) {
      final JimpleParser.Type_listContext typelist = ctx.method_subsignature().type_list();
      final List<Type> typeList = state.getUtil().getTypeList(typelist);
      return JavaJimple.newMethodType(
          typeList,
          state.getIdentifierFactory().getType(ctx.method_subsignature().type().getText()));
    }
    throw new ResolveException(
        "Unknown Constant.", state.getPath(), JimpleConverterUtil.buildPositionFromCtx(ctx));
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
        state.getPath(),
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

  @NonNull
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
