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
import de.upb.soot.jimple.common.constant.StringConstant;
import de.upb.soot.jimple.common.expr.AbstractBinopExpr;
import de.upb.soot.jimple.common.expr.AbstractConditionExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.SignatureFactory;

import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.IOperator;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
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
    IStmt ret = Jimple.newNopStmt();
    System.out.println(inst);
    // TODO what are the different types of SSAInstructions
    if (inst instanceof SSAConditionalBranchInstruction) {
      ret = this.convertBranchInstruction((SSAConditionalBranchInstruction) inst);
    } else if (inst instanceof SSAGotoInstruction) {
      ret = this.convertGoToInstruction((SSAGotoInstruction) inst);
    } else if (inst instanceof SSAReturnInstruction) {
      ret = this.convertReturnInstruction((SSAReturnInstruction) inst);
    } else if (inst instanceof SSAThrowInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof SSASwitchInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof AstJavaInvokeInstruction) {
      ret = this.convertInvokeInstruction((AstJavaInvokeInstruction) inst);
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        ret = this.convertGetInstruction((SSAGetInstruction) inst);
      } else if (inst instanceof SSAPutInstruction) {
        // field write instruction
        // TODO
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSAArrayLengthInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        // TODO
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      } else if (inst instanceof SSAArrayStoreInstruction) {
        // TODO
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof SSAComparisonInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof SSAConversionInstruction) {
      ret = convertConversionInstruction((SSAConversionInstruction) inst);
    } else if (inst instanceof SSAInstanceofInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    } else if (inst instanceof SSABinaryOpInstruction) {
      SSABinaryOpInstruction binOpInst = (SSABinaryOpInstruction) inst;
      ret = this.convertBinaryOpInstruction(binOpInst);
    } else if (inst instanceof SSALoadMetadataInstruction) {
      // TODO
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    // if current stmt is the target of an if stmt, set it up.
    this.setTarget(ret, inst.iindex);
    return Optional.ofNullable(ret);
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
    MethodReference target = invokeInst.getDeclaredTarget();
    String declaringClassSignature = converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    String returnType = converter.convertType(target.getReturnType()).toString();
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Value> args = new ArrayList<>();

    for (int i = 0; i < target.getNumberOfParameters(); i++) {
      Type paraType = converter.convertType(target.getParameterType(i));
      paraTypes.add(paraType);
      parameters.add(paraType.toString());
    }
    int i = 0;
    if (!invokeInst.isStatic()) {
      i = 1;// virtual invoke this first use is thisRef.
    }
    for (; i < invokeInst.getNumberOfUses(); i++) {
      Local arg = getLocal(paraTypes.get(i - 1), invokeInst.getUse(i));
      args.add(arg);
    }

    MethodSignature methodSig = converter.view.getSignatureFacotry().getMethodSignature(target.getName().toString(),
        declaringClassSignature, returnType, parameters);
    if (invokeInst.isSpecial()) {
      if (!sootMethod.isStatic()) {
        // constructor
        Local base = localGenerator.getThisLocal();
        JSpecialInvokeExpr expr = Jimple.newSpecialInvokeExpr(converter.view, base, methodSig, args);
        return Jimple.newInvokeStmt(expr);
      } else {
        JStaticInvokeExpr expr = Jimple.newStaticInvokeExpr(converter.view, methodSig, args);
        return Jimple.newInvokeStmt(expr);
      }
    } else {
      if (!sootMethod.isStatic()) {
        int receiver = invokeInst.getReceiver();
        Type classType = converter.convertType(target.getDeclaringClass());
        Local base = getLocal(classType, receiver);
        JVirtualInvokeExpr expr = Jimple.newVirtualInvokeExpr(converter.view, base, methodSig, args);
        return Jimple.newInvokeStmt(expr);
      } else {
        JStaticInvokeExpr expr = Jimple.newStaticInvokeExpr(converter.view, methodSig, args);
        return Jimple.newInvokeStmt(expr);
      }
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
    Value op1 = getLocal(type, val1);
    Value op2 = getLocal(type, val2);
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
    } else {
      throw new RuntimeException("Unsupported binary operator: " + operator);
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
        ret = this.locals.get(result);
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
    } else if (value instanceof Long) {
      return LongConstant.getInstance((long) value);
    } else if (value instanceof Double) {
      return DoubleConstant.getInstance((double) value);
    } else if (value instanceof Float) {
      return FloatConstant.getInstance((float) value);
    } else if (value instanceof String) {
      return StringConstant.getInstance((String) value);
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    if (symbolTable.isParameter(valueNumber)) {
      if (walaMethod.isStatic()) {
        return localGenerator.getParemeterLocal(valueNumber);
      } else {
        return localGenerator.getParemeterLocal(valueNumber - 1);
      }
    }
    if (!walaMethod.isStatic() && valueNumber == 0) {
      return localGenerator.getThisLocal();
    }
    if (!locals.containsKey(valueNumber)) {
      Local local = localGenerator.generateLocal(type);
      locals.put(valueNumber, local);
    }
    return locals.get(valueNumber);
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
