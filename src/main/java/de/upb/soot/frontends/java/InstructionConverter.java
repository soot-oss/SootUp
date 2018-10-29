package de.upb.soot.frontends.java;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.AbstractBinopExpr;
import de.upb.soot.jimple.common.expr.AbstractConditionExpr;
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
import com.ibm.wala.ssa.ConstantValue;
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

  public IStmt convertInvokeInstruction(AstJavaInvokeInstruction invokeInst) {
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

  public IStmt convertBranchInstruction(SSAConditionalBranchInstruction condInst) {
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

  public IStmt convertBinaryOpInstruction(SSABinaryOpInstruction binOpInst) {
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

  public IStmt convertInstruction(SSAInstruction inst) {
    IStmt ret = Jimple.newNopStmt();

    // TODO what are the different types of SSAInstructions
    if (inst instanceof SSAConditionalBranchInstruction) {
      ret = this.convertBranchInstruction((SSAConditionalBranchInstruction) inst);
    } else if (inst instanceof SSAGotoInstruction) {
      ret = this.convertGoToInstruction((SSAGotoInstruction) inst);
    } else if (inst instanceof SSAReturnInstruction) {

    } else if (inst instanceof SSAThrowInstruction) {

    } else if (inst instanceof SSASwitchInstruction) {

    } else if (inst instanceof AstJavaInvokeInstruction) {
      ret = this.convertInvokeInstruction((AstJavaInvokeInstruction) inst);
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        ret = this.convertGetInstruction((SSAGetInstruction) inst);
      } else if (inst instanceof SSAPutInstruction) {
        // field write instruction
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSAArrayLengthInstruction) {

    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {

      } else if (inst instanceof SSAArrayStoreInstruction) {

      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {

    } else if (inst instanceof SSAComparisonInstruction) {

    } else if (inst instanceof SSAConversionInstruction) {

    } else if (inst instanceof SSAInstanceofInstruction) {

    } else if (inst instanceof SSABinaryOpInstruction) {
      SSABinaryOpInstruction binOpInst = (SSABinaryOpInstruction) inst;
      ret = this.convertBinaryOpInstruction(binOpInst);
    }

    if (inst instanceof SSALoadMetadataInstruction) {

    }
    // if current stmt is the target of an if stmt, set it up.
    if (targetsOfIfStmts.containsValue(inst.iindex)) {
      for (JIfStmt ifStmt : targetsOfIfStmts.keySet()) {
        if (targetsOfIfStmts.get(ifStmt).equals(inst.iindex)) {
          ifStmt.setTarget(ret);
        }
      }
    }
    if (targetsOfGotoStmts.containsValue(inst.iindex)) {
      for (JGotoStmt gotoStmt : targetsOfGotoStmts.keySet()) {
        if (targetsOfGotoStmts.get(gotoStmt).equals(inst.iindex)) {
          gotoStmt.setTarget(ret);
        }
      }
    }
    return ret;
  }

  public IStmt convertGoToInstruction(SSAGotoInstruction gotoInst) {
    JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
    JGotoStmt gotoStmt = Jimple.newGotoStmt(target);
    this.targetsOfGotoStmts.put(gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  public IStmt convertGetInstruction(SSAGetInstruction inst) {
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

  private void getValue(int valueNumber) {
    if (symbolTable.isConstant(valueNumber)) {
      ConstantValue constant = (ConstantValue) symbolTable.getConstantValue(valueNumber);
      System.out.println("contant: " + constant.getValue());
    }
  }
}
