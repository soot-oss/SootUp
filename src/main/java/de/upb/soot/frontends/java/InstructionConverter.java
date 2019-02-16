package de.upb.soot.frontends.java;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstAssertInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
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
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.ClassConstant;
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
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JInstanceOfExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.expr.JNewExpr;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JCaughtExceptionRef;
import de.upb.soot.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.jimple.common.ref.JStaticFieldRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class converts wala instruction to jimple statement.
 * 
 * @author Linghui Luo
 *
 */
class InstructionConverter {

  private WalaIRToJimpleConverter converter;
  private SootMethod sootMethod;
  private AstMethod walaMethod;
  private SymbolTable symbolTable;
  private LocalGenerator localGenerator;
  // <ifStmt, iindex>
  protected Map<JIfStmt, Integer> targetsOfIfStmts;
  protected Map<JGotoStmt, Integer> targetsOfGotoStmts;
  protected Map<JLookupSwitchStmt, List<Integer>> targetsOfLookUpSwitchStmts;
  protected Map<JLookupSwitchStmt, Integer> defaultOfLookUpSwitchStmts;
  protected Map<JLookupSwitchStmt, List<IStmt>> targetStmtsOfLookUpSwitchStmts;
  private Map<Integer, Local> locals;
  private SignatureFactory sigFactory;

  public InstructionConverter(WalaIRToJimpleConverter converter, SootMethod sootMethod, AstMethod walaMethod,
      LocalGenerator localGenerator) {
    this.converter = converter;
    this.sootMethod = sootMethod;
    this.walaMethod = walaMethod;
    this.symbolTable = walaMethod.symbolTable();
    this.localGenerator = localGenerator;
    this.targetsOfIfStmts = new HashMap<>();
    this.targetsOfGotoStmts = new HashMap<>();
    this.targetsOfLookUpSwitchStmts = new HashMap<>();
    this.defaultOfLookUpSwitchStmts = new HashMap<>();
    this.locals = new HashMap<>();
    this.sigFactory = converter.view.getSignatureFactory();
  }

  public List<IStmt> convertInstruction(SSAInstruction inst) {
    List<IStmt> stmts = new ArrayList<>();
    // System.out.println(sootMethod.getSignature());
    // System.out.println(inst);
    if (inst instanceof SSAConditionalBranchInstruction) {
      stmts.add(this.convertBranchInstruction((SSAConditionalBranchInstruction) inst));
    } else if (inst instanceof SSAGotoInstruction) {
      stmts.add(this.convertGoToInstruction((SSAGotoInstruction) inst));
    } else if (inst instanceof SSAReturnInstruction) {
      stmts.add(this.convertReturnInstruction((SSAReturnInstruction) inst));
    } else if (inst instanceof AstJavaInvokeInstruction) {
      stmts.add(this.convertInvokeInstruction((AstJavaInvokeInstruction) inst));
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        stmts.add(this.convertGetInstruction((SSAGetInstruction) inst));// field read
      } else if (inst instanceof SSAPutInstruction) {
        stmts.add(this.convertPutInstruction((SSAPutInstruction) inst));// field write
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {
      stmts.add(convertNewInstruction((SSANewInstruction) inst));
    } else if (inst instanceof SSAConversionInstruction) {
      stmts.add(convertConversionInstruction((SSAConversionInstruction) inst));
    } else if (inst instanceof SSAInstanceofInstruction) {
      stmts.add(convertInstanceofInstruction((SSAInstanceofInstruction) inst));
    } else if (inst instanceof SSABinaryOpInstruction) {
      stmts.add(this.convertBinaryOpInstruction((SSABinaryOpInstruction) inst));
    } else if (inst instanceof SSAUnaryOpInstruction) {
      stmts.add(this.convertUnaryOpInstruction((SSAUnaryOpInstruction) inst));
    } else if (inst instanceof SSAThrowInstruction) {
      stmts.add(this.convertThrowInstruction((SSAThrowInstruction) inst));
    } else if (inst instanceof SSASwitchInstruction) {
      stmts.add(this.convertSwitchInstruction((SSASwitchInstruction) inst));
    } else if (inst instanceof SSALoadMetadataInstruction) {
      stmts.add(this.convertLoadMetadataInstruction((SSALoadMetadataInstruction) inst));
    } else if (inst instanceof EnclosingObjectReference) {
      stmts.add(this.convertEnclosingObjectReference((EnclosingObjectReference) inst));
    } else if (inst instanceof AstLexicalRead) {
      stmts = (this.convertAstLexicalRead((AstLexicalRead) inst));
    } else if (inst instanceof AstLexicalWrite) {
      stmts = (this.convertAstLexicalWrite((AstLexicalWrite) inst));
    } else if (inst instanceof AstAssertInstruction) {
      stmts = this.convertAssertInstruction((AstAssertInstruction) inst);
    } else if (inst instanceof SSACheckCastInstruction) {
      stmts.add(this.convertCheckCastInstruction((SSACheckCastInstruction) inst));
    } else if (inst instanceof SSAMonitorInstruction) {
      stmts.add(this.convertMonitorInstruction((SSAMonitorInstruction) inst));// for synchronized statement
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      stmts.add(this.convertGetCaughtExceptionInstruction((SSAGetCaughtExceptionInstruction) inst));
    } else if (inst instanceof SSAArrayLengthInstruction) {
      stmts.add(this.convertArrayLengthInstruction((SSAArrayLengthInstruction) inst));
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        stmts.add(this.convertArrayLoadInstruction((SSAArrayLoadInstruction) inst));
      } else if (inst instanceof SSAArrayStoreInstruction) {
        stmts.add(this.convertArrayStoreInstruction((SSAArrayStoreInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    return stmts;
  }

  private IStmt convertArrayStoreInstruction(SSAArrayStoreInstruction inst) {
    Local base = getLocal(UnknownType.INSTANCE, inst.getArrayRef());
    int i = inst.getIndex();
    Value index;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(IntType.INSTANCE, i);
    }
    JArrayRef arrayRef = Jimple.newArrayRef(base, index);
    Value rvalue;
    int value = inst.getValue();
    if (symbolTable.isConstant(value)) {
      rvalue = getConstant(value);
    } else {
      rvalue = getLocal(base.getType(), value);
    }
    return Jimple.newAssignStmt(arrayRef, rvalue);
  }

  private IStmt convertArrayLoadInstruction(SSAArrayLoadInstruction inst) {
    Local base = getLocal(UnknownType.INSTANCE, inst.getArrayRef());
    int i = inst.getIndex();
    Value index;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(IntType.INSTANCE, i);
    }
    JArrayRef arrayRef = Jimple.newArrayRef(base, index);
    Value left;
    int def = inst.getDef();
    left = getLocal(base.getType(), def);
    return Jimple.newAssignStmt(left, arrayRef);
  }

  private IStmt convertArrayLengthInstruction(SSAArrayLengthInstruction inst) {
    int result = inst.getDef();
    Local left = getLocal(IntType.INSTANCE, result);
    int arrayRef = inst.getArrayRef();
    Local arrayLocal = getLocal(UnknownType.INSTANCE, arrayRef);
    Value right = Jimple.newLengthExpr(arrayLocal);
    return Jimple.newAssignStmt(left, right);
  }

  private IStmt convertGetCaughtExceptionInstruction(SSAGetCaughtExceptionInstruction inst) {
    int exceptionValue = inst.getException();
    Local local = getLocal(RefType.getInstance("java.lang.Throwable"), exceptionValue);
    JCaughtExceptionRef caught = Jimple.newCaughtExceptionRef();
    return Jimple.newIdentityStmt(local, caught);
  }

  private IStmt convertMonitorInstruction(SSAMonitorInstruction inst) {
    Value op = getLocal(UnknownType.INSTANCE, inst.getRef());
    if (inst.isMonitorEnter()) {
      return Jimple.newEnterMonitorStmt(op);
    } else {
      return Jimple.newExitMonitorStmt(op);
    }
  }

  private List<IStmt> convertAssertInstruction(AstAssertInstruction inst) {
    List<IStmt> stmts = new ArrayList<>();
    // create a static field for checking if assertion is disabled.
    JavaClassSignature cSig = sootMethod.getDeclaringClassSignature();
    FieldSignature fieldSig = sigFactory.getFieldSignature("$assertionsDisabled", cSig, "boolean");
    SootField assertionsDisabled
        = new SootField(converter.view, cSig, sigFactory.getFieldSignature("$assertionsDisabled", cSig, "boolean"),
            sigFactory.getTypeSignature("boolean"), EnumSet.of(Modifier.FINAL, Modifier.STATIC));
    converter.addSootField(assertionsDisabled);
    Local testLocal = localGenerator.generateLocal(BooleanType.INSTANCE);
    JStaticFieldRef assertFieldRef = Jimple.newStaticFieldRef(converter.view, fieldSig);
    JAssignStmt assignStmt = Jimple.newAssignStmt(testLocal, assertFieldRef);
    stmts.add(assignStmt);

    // add ifStmt for testing assertion is disabled.
    JEqExpr condition = Jimple.newEqExpr(testLocal, IntConstant.getInstance(1));
    JNopStmt nopStmt = Jimple.newNopStmt();
    JIfStmt ifStmt = Jimple.newIfStmt(condition, nopStmt);
    stmts.add(ifStmt);

    // create ifStmt for the actual assertion.
    Local assertLocal = getLocal(BooleanType.INSTANCE, inst.getUse(0));
    JEqExpr assertionExpr = Jimple.newEqExpr(assertLocal, IntConstant.getInstance(1));
    JIfStmt assertIfStmt = Jimple.newIfStmt(assertionExpr, nopStmt);
    stmts.add(assertIfStmt);
    // create failed assertion code.

    RefType assertionErrorType = RefType.getInstance("java.lang.AssertionError");
    Local failureLocal = localGenerator.generateLocal(assertionErrorType);
    JNewExpr newExpr = Jimple.newNewExpr(assertionErrorType);
    JAssignStmt newAssignStmt = Jimple.newAssignStmt(failureLocal, newExpr);
    stmts.add(newAssignStmt);
    MethodSignature methodSig
        = sigFactory.getMethodSignature("<init>", "java.lang.AssertionError", "void", Collections.emptyList());
    JSpecialInvokeExpr invoke = Jimple.newSpecialInvokeExpr(converter.view, failureLocal, methodSig);
    JInvokeStmt invokeStmt = Jimple.newInvokeStmt(invoke);
    stmts.add(invokeStmt);
    JThrowStmt throwStmt = Jimple.newThrowStmt(failureLocal);
    stmts.add(throwStmt);

    // add nop in the end
    stmts.add(nopStmt);// TODO. This should be removed later
    return stmts;
  }

  private List<IStmt> convertAstLexicalWrite(AstLexicalWrite inst) {
    List<IStmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Value right;
      if (symbolTable.isConstant(access.valueNumber)) {
        right = getConstant(access.valueNumber);
      } else {
        right = getLocal(type, access.valueNumber);
      }
      JavaClassSignature cSig = sootMethod.getDeclaringClassSignature();
      // TODO check modifier
      Value left;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig = sigFactory.getFieldSignature("val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(converter.view, cSig, fieldSig, sigFactory.getTypeSignature(type.toString()),
            EnumSet.of(Modifier.FINAL));
        left = Jimple.newInstanceFieldRef(converter.view, localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field);// add this field to class
        // TODO in old jimple this is not supported
      } else {
        left = localGenerator.generateLocal(type);
      }
      stmts.add(Jimple.newAssignStmt(left, right));
    }
    return stmts;
  }

  private List<IStmt> convertAstLexicalRead(AstLexicalRead inst) {
    List<IStmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Local left = getLocal(type, access.valueNumber);
      JavaClassSignature cSig = sootMethod.getDeclaringClassSignature();
      // TODO check modifier
      Value rvalue;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig = sigFactory.getFieldSignature("val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(converter.view, cSig, fieldSig, sigFactory.getTypeSignature(type.toString()),
            EnumSet.of(Modifier.FINAL));
        rvalue = Jimple.newInstanceFieldRef(converter.view, localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field);// add this field to class
      } else {
        rvalue = localGenerator.generateLocal(type);
      }
      stmts.add(Jimple.newAssignStmt(left, rvalue));
    }
    return stmts;
  }

  private IStmt convertEnclosingObjectReference(EnclosingObjectReference inst) {
    Type enclosingType = converter.convertType(inst.getEnclosingType());
    Value variable = getLocal(enclosingType, inst.getDef());
    JavaClassSignature cSig = sootMethod.getDeclaringClassSignature();

    // TODO check modifier
    FieldSignature fieldSig = sigFactory.getFieldSignature("this$0", cSig, enclosingType.toString());
    SootField enclosingObject = new SootField(converter.view, cSig, fieldSig,
        sigFactory.getTypeSignature(enclosingType.toString()), EnumSet.of(Modifier.FINAL));
    JInstanceFieldRef rvalue = Jimple.newInstanceFieldRef(converter.view, localGenerator.getThisLocal(), fieldSig);
    return Jimple.newAssignStmt(variable, rvalue);
  }

  private IStmt convertCheckCastInstruction(SSACheckCastInstruction inst) {
    TypeReference[] types = inst.getDeclaredResultTypes();
    Local result = getLocal(converter.convertType(types[0]), inst.getResult());
    Value rvalue;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      rvalue = getConstant(val);
    } else {
      rvalue = getLocal(converter.convertType(types[0]), val);
    }
    // TODO declaredResultType is wrong
    JCastExpr castExpr = Jimple.newCastExpr(rvalue, converter.convertType(types[0]));
    return Jimple.newAssignStmt(result, castExpr);
  }

  private IStmt convertLoadMetadataInstruction(SSALoadMetadataInstruction inst) {
    Local lval = getLocal(converter.convertType(inst.getType()), inst.getDef());
    TypeReference token = (TypeReference) inst.getToken();
    ClassConstant c = ClassConstant.getInstance(token.getName().toString());
    return Jimple.newAssignStmt(lval, c);
  }

  private IStmt convertSwitchInstruction(SSASwitchInstruction inst) {
    int val = inst.getUse(0);
    Local local = getLocal(UnknownType.INSTANCE, val);
    int[] cases = inst.getCasesAndLabels();
    int defaultCase = inst.getDefault();
    List<IntConstant> lookupValues = new ArrayList<>();
    List<Integer> targetsList = new ArrayList<>();
    List<? extends IStmt> targets = new ArrayList<>();
    for (int i = 0; i < cases.length; i++) {
      int c = cases[i];
      if (i % 2 == 0) {
        IntConstant cValue = IntConstant.getInstance(c);
        lookupValues.add(cValue);
      } else {
        targetsList.add(c);
        targets.add(null);// add null as placeholder for targets
      }
    }
    IStmt defaultTarget = null;
    JLookupSwitchStmt stmt = Jimple.newLookupSwitchStmt(local, lookupValues, targets, defaultTarget);
    this.targetsOfLookUpSwitchStmts.put(stmt, targetsList);
    this.defaultOfLookUpSwitchStmts.put(stmt, defaultCase);
    return stmt;
  }

  private IStmt convertThrowInstruction(SSAThrowInstruction inst) {
    int exception = inst.getException();
    Local local = getLocal(UnknownType.INSTANCE, exception);
    return Jimple.newThrowStmt(local);
  }

  private IStmt convertUnaryOpInstruction(SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value op;
    // TODO: change type
    Type type = UnknownType.INSTANCE;
    if (symbolTable.isConstant(use)) {
      op = getConstant(use);
      type = op.getType();
    } else {
      op = getLocal(type, use);
    }
    Local left = getLocal(type, def);
    if (inst instanceof AssignInstruction) {
      return Jimple.newAssignStmt(left, op);
    } else {
      JNegExpr expr = Jimple.newNegExpr(op);
      return Jimple.newAssignStmt(left, expr);
    }
  }

  private IStmt convertPutInstruction(SSAPutInstruction inst) {
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    JavaClassSignature classSig = sigFactory.getClassSignature(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig = sigFactory.getFieldSignature(fieldRef.getName().toString(), classSig, fieldType.toString());
    Value fieldValue;
    if (inst.isStatic()) {
      fieldValue = Jimple.newStaticFieldRef(converter.view, fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(converter.view.getRefType(classSig), ref);
      fieldValue = Jimple.newInstanceFieldRef(converter.view, base, fieldSig);
    }
    Value value;
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
    Value rvalue;
    if (type instanceof ArrayType) {
      int use = inst.getUse(0);
      Value size;
      if (symbolTable.isConstant(use)) {
        size = getConstant(use);
      } else {
        // TODO: size type unsure
        size = getLocal(IntType.INSTANCE, use);
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
    Local op = getLocal(UnknownType.INSTANCE, ref);
    JInstanceOfExpr expr = Jimple.newInstanceOfExpr(op, checkedType);
    Value left = getLocal(BooleanType.INSTANCE, result);
    return Jimple.newAssignStmt(left, expr);
  }

  private IStmt convertConversionInstruction(SSAConversionInstruction inst) {
    Type fromType = converter.convertType(inst.getFromType());
    Type toType = converter.convertType(inst.getToType());
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value lvalue = getLocal(toType, def);
    Value rvalue;
    if (symbolTable.isConstant(use)) {
      rvalue = getConstant(use);
    } else {
      rvalue = getLocal(fromType, use);
    }
    JCastExpr cast = Jimple.newCastExpr(rvalue, toType);
    return Jimple.newAssignStmt(lvalue, cast);
  }

  private IStmt convertInvokeInstruction(AstJavaInvokeInstruction invokeInst) {
    Value invoke;
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
      Value arg;
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

    MethodSignature methodSig = converter.view.getSignatureFactory().getMethodSignature(target.getName().toString(),
        declaringClassSignature, returnType, parameters);

    if (!callee.isStatic()) {

      int receiver = invokeInst.getReceiver();
      Type classType = converter.convertType(target.getDeclaringClass());
      Local base = getLocal(classType, receiver);
      if (callee.isSpecial()) {
        Type baseType = UnknownType.INSTANCE;
        // TODO. baseType could be a problem.
        base = getLocal(baseType, receiver);
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
    Value value1;
    if (symbolTable.isZero(val1)) {
      value1 = IntConstant.getInstance(0);
    } else {
      value1 = getLocal(IntType.INSTANCE, val1);
    }
    Value value2;
    if (symbolTable.isZero(val2)) {
      value2 = IntConstant.getInstance(0);
    } else {
      value2 = getLocal(IntType.INSTANCE, val1);
    }
    AbstractConditionExpr condition;
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
  }

  private IStmt convertBinaryOpInstruction(SSABinaryOpInstruction binOpInst) {
    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    // TODO: only int type?
    Type type = IntType.INSTANCE;
    Value result = getLocal(type, def);
    Value op1;
    if (symbolTable.isConstant(val1)) {
      op1 = getConstant(val1);
      type = op1.getType();
    } else {
      op1 = getLocal(type, val1);
    }
    Value op2;
    if (symbolTable.isConstant(val2)) {
      op2 = getConstant(val2);
      type = op2.getType();
    } else {
      op2 = getLocal(type, val2);
    }
    AbstractBinopExpr binExpr;
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
        ret = this.getLocal(UnknownType.INSTANCE, result);
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
    JavaClassSignature classSig = sigFactory.getClassSignature(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig = sigFactory.getFieldSignature(fieldRef.getName().toString(), classSig, fieldType.toString());
    Value rvalue;
    if (inst.isStatic()) {
      rvalue = Jimple.newStaticFieldRef(converter.view, fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(converter.view.getRefType(classSig), ref);
      rvalue = Jimple.newInstanceFieldRef(converter.view, base, fieldSig);
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
    } else if (value instanceof Byte || value instanceof Character || value instanceof Short || value instanceof Integer) {
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
      return NullConstant.INSTANCE;
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    if (locals.containsKey(valueNumber)) {
      return locals.get(valueNumber);
    }
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
    if (this.defaultOfLookUpSwitchStmts.containsValue(iindex)) {
      for (JLookupSwitchStmt lookupSwitch : this.defaultOfLookUpSwitchStmts.keySet()) {
        if (this.defaultOfLookUpSwitchStmts.get(lookupSwitch).equals(iindex)) {
          lookupSwitch.setDefaultTarget(stmt);
        }
      }
    }
    for (JLookupSwitchStmt lookupSwith : this.targetsOfLookUpSwitchStmts.keySet()) {
      if (this.targetsOfLookUpSwitchStmts.get(lookupSwith).contains(iindex)) {
        List<IStmt> targets = lookupSwith.getTargets();
        if (targets.contains(null)) {// targets only contains placeholder
          targets = new ArrayList<>();
        }
        targets.add(stmt);
        lookupSwith.setTargets(targets);
      }
    }
  }

}
