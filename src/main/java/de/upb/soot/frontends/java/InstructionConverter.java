package de.upb.soot.frontends.java;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.constant.DoubleConstant;
import de.upb.soot.jimple.common.constant.FloatConstant;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.constant.NullConstant;
import de.upb.soot.jimple.common.constant.StringConstant;
import de.upb.soot.jimple.common.expr.AbstractBinopExpr;
import de.upb.soot.jimple.common.expr.AbstractConditionExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JInstanceOfExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.SignatureFactory;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.java.ssa.AstJavaNewEnclosingInstruction;
import com.ibm.wala.cast.java.ssa.EnclosingObjectReference;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.IOperator;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.shrikeBT.IShiftInstruction;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import scala.Char;

/**
 * This class converts wala instruction to jimple statement.
 * 
 * @author Linghui Luo
 *
 */
public class InstructionConverter {

  private WalaIRToJimpleConverter converter;
  private SootMethod sootMethod;
  private AstMethod walaMethod;
  private SymbolTable symbolTable;
  private LocalGenerator localGenerator;
  // <ifStmt, iindex>
  protected Map<JIfStmt, Integer> targetsOfIfStmts;
  protected Map<JGotoStmt, Integer> targetsOfGotoStmts;
  private Map<Integer, Local> locals;

  public InstructionConverter(WalaIRToJimpleConverter converter, SootMethod sootMethod, AstMethod walaMethod,
      LocalGenerator localGenerator) {
    this.converter = converter;
    this.sootMethod = sootMethod;
    this.walaMethod = walaMethod;
    this.symbolTable = walaMethod.symbolTable();
    this.localGenerator = localGenerator;
    this.targetsOfIfStmts = new HashMap<>();
    this.targetsOfGotoStmts = new HashMap<>();
    this.locals = new HashMap<>();
  }

  public Optional<IStmt> convertInstruction(SSAInstruction inst) {
    // System.out.println(sootMethod.getSignature());
    IStmt ret = Jimple.newNopStmt();
    // System.out.println(inst);
    // TODO what are the different types of SSAInstructions
    if (inst instanceof SSAConditionalBranchInstruction) {
      ret = this.convertBranchInstruction((SSAConditionalBranchInstruction) inst);
    } else if (inst instanceof SSAGotoInstruction) {
      ret = this.convertGoToInstruction((SSAGotoInstruction) inst);
    } else if (inst instanceof SSAReturnInstruction) {
      ret = this.convertReturnInstruction((SSAReturnInstruction) inst);
    } else if (inst instanceof AstJavaInvokeInstruction) {
      ret = this.convertInvokeInstruction((AstJavaInvokeInstruction) inst);
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        ret = this.convertGetInstruction((SSAGetInstruction) inst);// field read
      } else if (inst instanceof SSAPutInstruction) {
        ret = this.convertPutInstruction((SSAPutInstruction) inst);// field write
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {
      ret = convertNewInstruction((SSANewInstruction) inst);
    } else if (inst instanceof SSAConversionInstruction) {
      ret = convertConversionInstruction((SSAConversionInstruction) inst);
    } else if (inst instanceof SSAInstanceofInstruction) {
      ret = convertInstanceofInstruction((SSAInstanceofInstruction) inst);
    } else if (inst instanceof SSABinaryOpInstruction) {
      ret = this.convertBinaryOpInstruction((SSABinaryOpInstruction) inst);
    } else if (inst instanceof SSAUnaryOpInstruction) {
      ret = this.convertUnaryOpInstruction((SSAUnaryOpInstruction) inst);
    } else if (inst instanceof SSAComparisonInstruction) {
      ret = convertComparisonInstruction((SSAComparisonInstruction) inst);
      // TODO
    } else if (inst instanceof SSAThrowInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof SSASwitchInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof SSALoadMetadataInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof AssignInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof AstJavaNewEnclosingInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof EnclosingObjectReference) {
      // TODO
      ret = null;
    } else if (inst instanceof AstLexicalRead) {
      // TODO
      ret = null;
    } else if (inst instanceof SSACheckCastInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      // TODO
      ret = null;
    } else if (inst instanceof SSAArrayLengthInstruction) {
      // TODO
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        // TODO
        ret = null;
      } else if (inst instanceof SSAArrayStoreInstruction) {
        // TODO
        ret = null;
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    // if current stmt is the target of an if stmt, set it up.
    this.setTarget(ret, inst.iindex);
    return Optional.ofNullable(ret);
  }

  private IStmt convertUnaryOpInstruction(SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value op = null;
    // TODO: change type
    Type type = IntType.getInstance();
    if (symbolTable.isConstant(use)) {
      op = getConstant(use);
    } else {
      op = getLocal(type, use);
    }
    Local left = getLocal(type, def);
    JNegExpr expr = Jimple.newNegExpr(op);
    return Jimple.newAssignStmt(left, expr);
  }

  private IStmt convertPutInstruction(SSAPutInstruction inst) {
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    SignatureFactory sigfactory = converter.view.getSignatureFacotry();
    JavaClassSignature classSig = sigfactory.getClassSignature(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig = sigfactory.getFieldSignature(fieldRef.getName().toString(), classSig, fieldType.toString());
    Value fieldValue = null;
    if (inst.isStatic()) {
      fieldValue = Jimple.newStaticFieldRef(new SootField(converter.view, classSig, fieldSig,
          sigfactory.getTypeSignature(fieldType.toString()), EnumSet.of(Modifier.STATIC)));
    } else {
      int ref = inst.getRef();
      Local base = getLocal(converter.view.getRefType(classSig), ref);
      fieldValue = Jimple.newInstanceFieldRef(base,
          new SootField(converter.view, classSig, fieldSig, sigfactory.getTypeSignature(fieldType.toString())));
    }
    Value value = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      value = getConstant(val);
    } else {
      value = getLocal(fieldType, val);
    }
    return Jimple.newAssignStmt(fieldValue, value);
  }

  private IStmt convertNewInstruction(SSANewInstruction inst) {
    int result = inst.getDef();
    Type type = converter.convertType(inst.getNewSite().getDeclaredType());
    Value var = getLocal(type, result);
    Value rvalue = null;
    if (type instanceof ArrayType) {
      int use = inst.getUse(0);
      Value size = null;
      if (symbolTable.isConstant(use)) {
        size = getConstant(use);
      } else {
        // TODO: size type unsure
         size=getLocal(IntType.getInstance(), use);
      }
      rvalue = Jimple.newNewArrayExpr(type, size);
    } else {
      rvalue = Jimple.newNewExpr((RefType) type);
    }
    return Jimple.newAssignStmt(var, rvalue);
  }

  private IStmt convertComparisonInstruction(SSAComparisonInstruction inst) {
    // TODO
    return Jimple.newNopStmt();
  }

  private IStmt convertInstanceofInstruction(SSAInstanceofInstruction inst) {
    int result = inst.getDef();
    int ref = inst.getRef();
    Type checkedType = converter.convertType(inst.getCheckedType());
    // TODO. how to get type of ref?
    Local op = getLocal(UnknownType.getInstance(), ref);
    JInstanceOfExpr expr = Jimple.newInstanceOfExpr(op, checkedType);
    Value left = getLocal(BooleanType.getInstance(), result);
    return Jimple.newAssignStmt(left, expr);
  }

  private IStmt convertConversionInstruction(SSAConversionInstruction inst) {
    Type fromType = converter.convertType(inst.getFromType());
    Type toType = converter.convertType(inst.getToType());
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value lvalue = getLocal(toType, def);
    Value rvalue = null;
    if (symbolTable.isConstant(use)) {
      rvalue = getConstant(use);
    } else {
      rvalue = getLocal(fromType, use);
    }
    JCastExpr cast = Jimple.newCastExpr(rvalue, toType);
    return Jimple.newAssignStmt(lvalue, cast);
  }

  private IStmt convertInvokeInstruction(AstJavaInvokeInstruction invokeInst) {
    if (invokeInst.isSpecial()) {
      System.out.println(invokeInst);
    }
    Value invoke = null;
    CallSiteReference callee = invokeInst.getCallSite();
    MethodReference target = invokeInst.getDeclaredTarget();
    String declaringClassSignature = converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    String returnType = converter.convertType(target.getReturnType()).toString();
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Value> args = new ArrayList<>();
    for (int i = 0; i < target.getNumberOfParameters(); i++) {
      Type paraType = converter.convertType(target.getParameterType(i));// note the parameters do not include "this"
      paraTypes.add(paraType);
      parameters.add(paraType.toString());
    }
    int i = 0;
    if (!callee.isStatic()) {
      i = 1;// non-static invoke this first use is thisRef.
    }
    for (; i < invokeInst.getNumberOfUses(); i++) {
      int use = invokeInst.getUse(i);
      Value arg = null;
      if (symbolTable.isConstant(use)) {
        arg = getConstant(use);
      } else {
        if (invokeInst.getNumberOfUses() > paraTypes.size()) {
          arg = getLocal(paraTypes.get(i - 1), use);
        } else {
          arg = getLocal(paraTypes.get(i), use);
        }
      }
      assert (arg != null);
      args.add(arg);
    }

    MethodSignature methodSig = converter.view.getSignatureFacotry().getMethodSignature(target.getName().toString(),
        declaringClassSignature, returnType, parameters);

    if (!callee.isStatic()) {
      int receiver = invokeInst.getReceiver();
      Type classType = converter.convertType(target.getDeclaringClass());
      Local base = getLocal(classType, receiver);
      if (callee.isSpecial()) {
        base = getLocal(converter.convertType(walaMethod.getDeclaringClass().getReference()), receiver);
        invoke = Jimple.newSpecialInvokeExpr(converter.view, base, methodSig, args); // constructor
      } else if (callee.isVirtual()) {
        invoke = Jimple.newVirtualInvokeExpr(converter.view, base, methodSig, args);
      } else if (callee.isInterface()) {
        invoke = Jimple.newInterfaceInvokeExpr(converter.view, base, methodSig, args);
      } else {
        throw new RuntimeException("Unsupported invoke instruction: " + callee.toString());
      }
    } else {
      invoke = Jimple.newStaticInvokeExpr(converter.view, methodSig, args);
    }

    if (!invokeInst.hasDef()) {
      return Jimple.newInvokeStmt(invoke);
    } else {
      Type type = converter.convertType(invokeInst.getDeclaredResultType());
      Local v = getLocal(type, invokeInst.getDef());
      return Jimple.newAssignStmt(v, invoke);
    }
  }

  private IStmt convertBranchInstruction(SSAConditionalBranchInstruction condInst) {
    int val1 = condInst.getUse(0);
    int val2 = condInst.getUse(1);
    Value value1 = null;
    if (symbolTable.isZero(val1)) {
      value1 = IntConstant.getInstance(0);
    } else {
      value1 = getLocal(IntType.getInstance(), val1);
    }
    Value value2 = null;
    if (symbolTable.isZero(val2)) {
      value2 = IntConstant.getInstance(0);
    } else {
      value2 = getLocal(IntType.getInstance(), val1);
    }
    AbstractConditionExpr condition = null;
    IOperator op = condInst.getOperator();
    if (op.equals(Operator.EQ)) {
      condition = Jimple.newEqExpr(value1, value2);
    } else if (op.equals(Operator.NE)) {
      condition = Jimple.newNeExpr(value1, value2);
    } else if (op.equals(Operator.LT)) {
      condition = Jimple.newLtExpr(value1, value2);
    } else if (op.equals(Operator.GE)) {
      condition = Jimple.newGeExpr(value1, value2);
    } else if (op.equals(Operator.GT)) {
      condition = Jimple.newGtExpr(value1, value2);
    } else if (op.equals(Operator.LE)) {
      condition = Jimple.newLtExpr(value1, value2);
    } else {
      throw new RuntimeException("Unsupported conditional operator: " + op);
    }
    JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
    JIfStmt ifStmt = Jimple.newIfStmt(condition, target);
    this.targetsOfIfStmts.put(ifStmt, condInst.getTarget());
    return ifStmt;
    // return Jimple.newNopStmt();
  }

  private IStmt convertBinaryOpInstruction(SSABinaryOpInstruction binOpInst) {

    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    // TODO: only int type?
    Type type = IntType.getInstance();
    Value result = getLocal(type, def);
    Value op1 = null;
    if (symbolTable.isConstant(val1)) {
      op1 = getConstant(val1);
    } else {
      op1 = getLocal(type, val1);
    }
    Value op2 = null;
    if (symbolTable.isConstant(val2)) {
      op2 = getConstant(val2);
    } else {
      op2 = getLocal(type, val2);
    }
    AbstractBinopExpr binExpr = null;
    IBinaryOpInstruction.IOperator operator = binOpInst.getOperator();
    if (operator.equals(IBinaryOpInstruction.Operator.ADD)) {
      binExpr = Jimple.newAddExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.SUB)) {
      binExpr = Jimple.newSubExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.MUL)) {
      binExpr = Jimple.newMulExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.DIV)) {
      binExpr = Jimple.newDivExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.REM)) {
      binExpr = Jimple.newRemExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.AND)) {
      binExpr = Jimple.newAndExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.OR)) {
      binExpr = Jimple.newOrExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.XOR)) {
      binExpr = Jimple.newXorExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.EQ)) {
      binExpr = Jimple.newEqExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.NE)) {
      binExpr = Jimple.newNeExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.LT)) {
      binExpr = Jimple.newLtExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.GE)) {
      binExpr = Jimple.newGeExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.GT)) {
      binExpr = Jimple.newGtExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.LE)) {
      binExpr = Jimple.newLtExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.SHL)) {
      binExpr = Jimple.newShlExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.SHR)) {
      binExpr = Jimple.newShrExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.USHR)) {
      binExpr = Jimple.newUshrExpr(op1, op2);
    } else {

      throw new RuntimeException("Unsupported binary operator: " + operator.getClass());
    }
    return Jimple.newAssignStmt(result, binExpr);
  }

  private IStmt convertReturnInstruction(SSAReturnInstruction inst) {
    int result = inst.getResult();
    if (inst.returnsVoid()) {
      // this is return void stmt
      return Jimple.newReturnVoidStmt();
    } else {
      Value ret;
      if (symbolTable.isConstant(result)) {
        ret = getConstant(result);
      } else {
        // TODO. how to get the type of result?
        ret = this.getLocal(UnknownType.getInstance(), result);
      }
      return Jimple.newReturnStmt(ret);
    }
  }

  private IStmt convertGoToInstruction(SSAGotoInstruction gotoInst) {
    JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
    JGotoStmt gotoStmt = Jimple.newGotoStmt(target);
    this.targetsOfGotoStmts.put(gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  private IStmt convertGetInstruction(SSAGetInstruction inst) {
    int def = inst.getDef(0);
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    SignatureFactory sigfactory = converter.view.getSignatureFacotry();
    JavaClassSignature classSig = sigfactory.getClassSignature(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig = sigfactory.getFieldSignature(fieldRef.getName().toString(), classSig, fieldType.toString());
    Value rvalue = null;
    if (inst.isStatic()) {
      rvalue = Jimple.newStaticFieldRef(new SootField(converter.view, classSig, fieldSig,
          sigfactory.getTypeSignature(fieldType.toString()), EnumSet.of(Modifier.STATIC)));
    } else {
      int ref = inst.getRef();
      Local base = getLocal(converter.view.getRefType(classSig), ref);
      rvalue = Jimple.newInstanceFieldRef(base,
          new SootField(converter.view, classSig, fieldSig, sigfactory.getTypeSignature(fieldType.toString())));
    }
    Value var = getLocal(fieldType, def);
    return Jimple.newAssignStmt(var, rvalue);
  }

  private Constant getConstant(int valueNumber) {
    Object value = symbolTable.getConstantValue(valueNumber);
    if (value instanceof Boolean) {
      if (value.equals(true)) {
        return IntConstant.getInstance(1);
      } else {
        return IntConstant.getInstance(0);
      }
    } else if (value instanceof Byte || value instanceof Char || value instanceof Short || value instanceof Integer) {
      return IntConstant.getInstance((int) value);
    } else if (symbolTable.isLongConstant(valueNumber)) {
      return LongConstant.getInstance((long) value);
    } else if (symbolTable.isDoubleConstant(valueNumber)) {
      return DoubleConstant.getInstance((double) value);
    } else if (symbolTable.isFloatConstant(valueNumber)) {
      return FloatConstant.getInstance((float) value);
    } else if (symbolTable.isStringConstant(valueNumber)) {
      return StringConstant.getInstance((String) value);
    } else if (symbolTable.isNullConstant(valueNumber)) {
      return NullConstant.getInstance();
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    if (type.toString().equals(sootMethod.getDeclaringClassSignature().toString())) {
      if (!walaMethod.isStatic()) {
        return localGenerator.getThisLocal();
      }
    }
    if (symbolTable.isParameter(valueNumber)) {
      Local para = localGenerator.getParemeterLocal(valueNumber - 1);
      if (para != null) {
        return para;
      }
    }
    if (!locals.containsKey(valueNumber)) {
      Local local = localGenerator.generateLocal(type);
      locals.put(valueNumber, local);
    }
    Local ret = locals.get(valueNumber);

    if (!ret.getType().equals(type)) {
      // ret.setType(ret.getType().merge(type));
      // TODO. re-implement merge.
      // throw new RuntimeException("Different types for same local variable: "+ret.getType()+"<->"+type);
    }
    return ret;
  }

  /**
   * Test if the given stmt is the target stmt of {@link JIfStmt} or {@link JGotoStmt} and set it as the target if it is the
   * case.
   * 
   * @param stmt
   *          the converted jimple stmt.
   * @param iindex
   *          the instruction index of the corresponding instruction in Wala.
   */
  protected void setTarget(IStmt stmt, int iindex) {
    if (this.targetsOfIfStmts.containsValue(iindex)) {
      for (JIfStmt ifStmt : this.targetsOfIfStmts.keySet()) {
        if (this.targetsOfIfStmts.get(ifStmt).equals(iindex)) {
          ifStmt.setTarget(stmt);
        }
      }
    }
    if (this.targetsOfGotoStmts.containsValue(iindex)) {
      for (JGotoStmt gotoStmt : this.targetsOfGotoStmts.keySet()) {
        if (this.targetsOfGotoStmts.get(gotoStmt).equals(iindex)) {
          gotoStmt.setTarget(stmt);
        }
      }
    }
  }
}
