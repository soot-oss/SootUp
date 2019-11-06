package de.upb.swt.soot.java.sourcecode.frontend;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstAssertInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.java.ssa.EnclosingObjectReference;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JStmtBox;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.LocalGenerator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JEqExpr;
import de.upb.swt.soot.core.jimple.common.expr.JInstanceOfExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNegExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JStaticFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scala.Char;

/**
 * This class converts wala instruction to jimple statement.
 *
 * @author Linghui Luo
 */
public class InstructionConverter {

  private final WalaIRToJimpleConverter converter;
  private final MethodSignature methodSignature;
  private final AstMethod walaMethod;
  private final SymbolTable symbolTable;
  private final LocalGenerator localGenerator;
  // <ifStmt, iindex>
  private final Map<JIfStmt, Integer> targetsOfIfStmts;
  private final Map<JGotoStmt, Integer> targetsOfGotoStmts;
  private final Map<JLookupSwitchStmt, List<Integer>> targetsOfLookUpSwitchStmts;
  private final Map<JLookupSwitchStmt, Integer> defaultOfLookUpSwitchStmts;
  protected Map<JLookupSwitchStmt, List<Stmt>> targetStmtsOfLookUpSwitchStmts;
  private final Map<Integer, Local> locals;
  private final IdentifierFactory identifierFactory;

  InstructionConverter(
      WalaIRToJimpleConverter converter,
      MethodSignature methodSignature,
      AstMethod walaMethod,
      LocalGenerator localGenerator) {
    this.converter = converter;
    this.methodSignature = methodSignature;
    this.walaMethod = walaMethod;
    this.symbolTable = walaMethod.symbolTable();
    this.localGenerator = localGenerator;
    this.targetsOfIfStmts = new HashMap<>();
    this.targetsOfGotoStmts = new HashMap<>();
    this.targetsOfLookUpSwitchStmts = new HashMap<>();
    this.defaultOfLookUpSwitchStmts = new HashMap<>();
    this.locals = new HashMap<>();
    this.identifierFactory = converter.identifierFactory;
  }

  public List<Stmt> convertInstruction(DebuggingInformation debugInfo, SSAInstruction inst) {
    List<Stmt> stmts = new ArrayList<>();
    // System.out.println(sootMethod.getSignature());
    // System.out.println(inst);
    if (inst instanceof SSAConditionalBranchInstruction) {
      stmts.addAll(
          this.convertBranchInstruction(debugInfo, (SSAConditionalBranchInstruction) inst));
    } else if (inst instanceof SSAGotoInstruction) {
      stmts.add(this.convertGoToInstruction(debugInfo, (SSAGotoInstruction) inst));
    } else if (inst instanceof SSAReturnInstruction) {
      stmts.add(this.convertReturnInstruction(debugInfo, (SSAReturnInstruction) inst));
    } else if (inst instanceof AstJavaInvokeInstruction) {
      stmts.add(this.convertInvokeInstruction(debugInfo, (AstJavaInvokeInstruction) inst));
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        stmts.add(this.convertGetInstruction(debugInfo, (SSAGetInstruction) inst)); // field read
      } else if (inst instanceof SSAPutInstruction) {
        stmts.add(this.convertPutInstruction(debugInfo, (SSAPutInstruction) inst)); // field write
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {
      stmts.add(convertNewInstruction(debugInfo, (SSANewInstruction) inst));
    } else if (inst instanceof SSAConversionInstruction) {
      stmts.add(convertConversionInstruction(debugInfo, (SSAConversionInstruction) inst));
    } else if (inst instanceof SSAInstanceofInstruction) {
      stmts.add(convertInstanceofInstruction(debugInfo, (SSAInstanceofInstruction) inst));
    } else if (inst instanceof SSABinaryOpInstruction) {
      stmts.add(this.convertBinaryOpInstruction(debugInfo, (SSABinaryOpInstruction) inst));
    } else if (inst instanceof SSAUnaryOpInstruction) {
      stmts.add(this.convertUnaryOpInstruction(debugInfo, (SSAUnaryOpInstruction) inst));
    } else if (inst instanceof SSAThrowInstruction) {
      stmts.add(this.convertThrowInstruction(debugInfo, (SSAThrowInstruction) inst));
    } else if (inst instanceof SSASwitchInstruction) {
      stmts.add(this.convertSwitchInstruction(debugInfo, (SSASwitchInstruction) inst));
    } else if (inst instanceof SSALoadMetadataInstruction) {
      stmts.add(this.convertLoadMetadataInstruction(debugInfo, (SSALoadMetadataInstruction) inst));
    } else if (inst instanceof EnclosingObjectReference) {
      stmts.add(this.convertEnclosingObjectReference(debugInfo, (EnclosingObjectReference) inst));
    } else if (inst instanceof AstLexicalRead) {
      stmts = (this.convertAstLexicalRead(debugInfo, (AstLexicalRead) inst));
    } else if (inst instanceof AstLexicalWrite) {
      stmts = (this.convertAstLexicalWrite(debugInfo, (AstLexicalWrite) inst));
    } else if (inst instanceof AstAssertInstruction) {
      stmts = this.convertAssertInstruction(debugInfo, (AstAssertInstruction) inst);
    } else if (inst instanceof SSACheckCastInstruction) {
      stmts.add(this.convertCheckCastInstruction(debugInfo, (SSACheckCastInstruction) inst));
    } else if (inst instanceof SSAMonitorInstruction) {
      stmts.add(
          this.convertMonitorInstruction(
              debugInfo, (SSAMonitorInstruction) inst)); // for synchronized statement
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      stmts.add(
          this.convertGetCaughtExceptionInstruction(
              debugInfo, (SSAGetCaughtExceptionInstruction) inst));
    } else if (inst instanceof SSAArrayLengthInstruction) {
      stmts.add(this.convertArrayLengthInstruction(debugInfo, (SSAArrayLengthInstruction) inst));
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        stmts.add(this.convertArrayLoadInstruction(debugInfo, (SSAArrayLoadInstruction) inst));
      } else if (inst instanceof SSAArrayStoreInstruction) {
        stmts.add(this.convertArrayStoreInstruction(debugInfo, (SSAArrayStoreInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    return stmts;
  }

  private Stmt convertArrayStoreInstruction(
      DebuggingInformation debugInfo, SSAArrayStoreInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Value index = null;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(PrimitiveType.getInt(), i);
    }
    JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(base, index);
    Value rvalue = null;
    int value = inst.getValue();
    if (symbolTable.isConstant(value)) {
      rvalue = getConstant(value);
    } else {
      rvalue = getLocal(base.getType(), value);
    }

    Position[] operandPos = new Position[1];
    // FIXME: written arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        arrayRef,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLoadInstruction(
      DebuggingInformation debugInfo, SSAArrayLoadInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Value index;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i);
    } else {
      index = getLocal(PrimitiveType.getInt(), i);
    }
    JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(base, index);
    Value left = null;
    int def = inst.getDef();
    left = getLocal(base.getType(), def);

    Position[] operandPos = new Position[1];
    // FIXME: loaded arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        left,
        arrayRef,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLengthInstruction(
      DebuggingInformation debugInfo, SSAArrayLengthInstruction inst) {
    int result = inst.getDef();
    Local left = getLocal(PrimitiveType.getInt(), result);
    int arrayRef = inst.getArrayRef();
    Local arrayLocal = getLocal(UnknownType.getInstance(), arrayRef);
    Value right = Jimple.newLengthExpr(arrayLocal);

    Position[] operandPos = new Position[1];
    Position p1 = debugInfo.getOperandPosition(inst.iIndex(), 0);
    operandPos[0] = p1;
    // FIXME: [ms] stmt position ends at variablename of the array
    return Jimple.newAssignStmt(
        left,
        right,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertGetCaughtExceptionInstruction(
      DebuggingInformation debugInfo, SSAGetCaughtExceptionInstruction inst) {
    int exceptionValue = inst.getException();
    Local local =
        getLocal(
            JavaIdentifierFactory.getInstance().getClassType("java.lang.Throwable"),
            exceptionValue);
    JCaughtExceptionRef caught = JavaJimple.getInstance().newCaughtExceptionRef();

    Position[] operandPos = new Position[1];
    // FIXME: [ms] position info of parameter, target is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newIdentityStmt(
        local,
        caught,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertMonitorInstruction(
      DebuggingInformation debugInfo, SSAMonitorInstruction inst) {
    Value op = getLocal(UnknownType.getInstance(), inst.getRef());

    Position[] operandPos = new Position[1];
    // FIXME: [ms] referenced object position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    if (inst.isMonitorEnter()) {
      return Jimple.newEnterMonitorStmt(
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    } else {
      return Jimple.newExitMonitorStmt(
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertAssertInstruction(
      DebuggingInformation debugInfo, AstAssertInstruction inst) {
    List<Stmt> stmts = new ArrayList<>();
    // create a static field for checking if assertion is disabled.
    JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature("$assertionsDisabled", cSig, "boolean");
    SootField assertionsDisabled =
        new SootField(fieldSig, EnumSet.of(Modifier.FINAL, Modifier.STATIC));

    converter.addSootField(assertionsDisabled);
    Local testLocal = localGenerator.generateLocal(PrimitiveType.getBoolean());
    JStaticFieldRef assertFieldRef = Jimple.newStaticFieldRef(fieldSig);
    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    JAssignStmt assignStmt =
        Jimple.newAssignStmt(
            testLocal,
            assertFieldRef,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(assignStmt);

    // add ifStmt for testing assertion is disabled.
    JEqExpr condition = Jimple.newEqExpr(testLocal, IntConstant.getInstance(1));
    JNopStmt nopStmt =
        Jimple.newNopStmt(
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));

    JIfStmt ifStmt =
        Jimple.newIfStmt(
            condition,
            nopStmt,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(ifStmt);

    // create ifStmt for the actual assertion.
    Local assertLocal = getLocal(PrimitiveType.getBoolean(), inst.getUse(0));
    JEqExpr assertionExpr = Jimple.newEqExpr(assertLocal, IntConstant.getInstance(1));

    JIfStmt assertIfStmt =
        Jimple.newIfStmt(
            assertionExpr,
            nopStmt,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(assertIfStmt);
    // create failed assertion code.

    ReferenceType assertionErrorType =
        JavaIdentifierFactory.getInstance().getClassType("java.lang.AssertionError");
    Local failureLocal = localGenerator.generateLocal(assertionErrorType);
    JNewExpr newExpr = Jimple.newNewExpr(assertionErrorType);

    JAssignStmt newAssignStmt =
        Jimple.newAssignStmt(
            failureLocal,
            newExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(newAssignStmt);
    MethodSignature methodSig =
        identifierFactory.getMethodSignature(
            "<init>", "java.lang.AssertionError", "void", Collections.emptyList());
    JSpecialInvokeExpr invoke = Jimple.newSpecialInvokeExpr(failureLocal, methodSig);
    JInvokeStmt invokeStmt =
        Jimple.newInvokeStmt(
            invoke,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(invokeStmt);

    JThrowStmt throwStmt =
        Jimple.newThrowStmt(
            failureLocal,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(throwStmt);

    // add nop in the end
    stmts.add(nopStmt); // TODO. This should be removed later
    return stmts;
  }

  private List<Stmt> convertAstLexicalWrite(DebuggingInformation debugInfo, AstLexicalWrite inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Value right;
      if (symbolTable.isConstant(access.valueNumber)) {
        right = getConstant(access.valueNumber);
      } else {
        right = getLocal(type, access.valueNumber);
      }
      JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
      // TODO check modifier
      Value left;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig =
            identifierFactory.getFieldSignature(
                "val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));
        left = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field); // add this field to class
        // TODO in old jimple this is not supported
      } else {
        left = localGenerator.generateLocal(type);
      }
      // TODO: [ms] no instruction example found to add positioninfo
      stmts.add(
          Jimple.newAssignStmt(
              left,
              right,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(inst.iIndex()), null)));
    }
    return stmts;
  }

  private List<Stmt> convertAstLexicalRead(DebuggingInformation debugInfo, AstLexicalRead inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Local left = getLocal(type, access.valueNumber);
      JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
      // TODO check modifier
      Value rvalue = null;
      if (!walaMethod.isStatic()) {
        FieldSignature fieldSig =
            identifierFactory.getFieldSignature(
                "val$" + access.variableName, cSig, type.toString());
        SootField field = new SootField(fieldSig, EnumSet.of(Modifier.FINAL));
        rvalue = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);
        converter.addSootField(field); // add this field to class
      } else {
        rvalue = localGenerator.generateLocal(type);
      }

      // TODO: [ms] no instruction example found to add positioninfo
      stmts.add(
          Jimple.newAssignStmt(
              left,
              rvalue,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(inst.iIndex()), null)));
    }
    return stmts;
  }

  private Stmt convertEnclosingObjectReference(
      DebuggingInformation debugInfo, EnclosingObjectReference inst) {
    Type enclosingType = converter.convertType(inst.getEnclosingType());
    Value variable = getLocal(enclosingType, inst.getDef());
    JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();

    // TODO check modifier
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature("this$0", cSig, enclosingType.toString());

    JInstanceFieldRef rvalue = Jimple.newInstanceFieldRef(localGenerator.getThisLocal(), fieldSig);

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        variable,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertCheckCastInstruction(
      DebuggingInformation debugInfo, SSACheckCastInstruction inst) {
    TypeReference[] types = inst.getDeclaredResultTypes();
    Local result = getLocal(converter.convertType(types[0]), inst.getResult());
    Value rvalue = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      rvalue = getConstant(val);
    } else {
      rvalue = getLocal(converter.convertType(types[0]), val);
    }
    // TODO declaredResultType is wrong
    JCastExpr castExpr = Jimple.newCastExpr(rvalue, converter.convertType(types[0]));

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        result,
        castExpr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertLoadMetadataInstruction(
      DebuggingInformation debugInfo, SSALoadMetadataInstruction inst) {
    Local lval = getLocal(converter.convertType(inst.getType()), inst.getDef());
    TypeReference token = (TypeReference) inst.getToken();
    ClassConstant c = JavaJimple.getInstance().newClassConstant(token.getName().toString());

    // TODO: [ms] no instruction example found to add positioninfo
    return Jimple.newAssignStmt(
        lval,
        c,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertSwitchInstruction(DebuggingInformation debugInfo, SSASwitchInstruction inst) {
    int val = inst.getUse(0);
    Local local = getLocal(UnknownType.getInstance(), val);
    int[] cases = inst.getCasesAndLabels();
    int defaultCase = inst.getDefault();
    List<IntConstant> lookupValues = new ArrayList<>();
    List<Integer> targetsList = new ArrayList<>();
    List<? extends Stmt> targets = new ArrayList<>();
    for (int i = 0; i < cases.length; i++) {
      int c = cases[i];
      if (i % 2 == 0) {
        IntConstant cValue = IntConstant.getInstance(c);
        lookupValues.add(cValue);
      } else {
        targetsList.add(c);
        targets.add(null); // add null as placeholder for targets
      }
    }
    Stmt defaultTarget = null;

    Position[] operandPos = new Position[2];
    // TODO: [ms] how to organize the operands
    // FIXME: has no operand positions yet for
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), ); // key
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), ); // default
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // lookups
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // targets

    JLookupSwitchStmt stmt =
        Jimple.newLookupSwitchStmt(
            local,
            lookupValues,
            targets,
            defaultTarget,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    this.targetsOfLookUpSwitchStmts.put(stmt, targetsList);
    this.defaultOfLookUpSwitchStmts.put(stmt, defaultCase);
    return stmt;
  }

  private Stmt convertThrowInstruction(DebuggingInformation debugInfo, SSAThrowInstruction inst) {
    int exception = inst.getException();
    Local local = getLocal(UnknownType.getInstance(), exception);

    Position[] operandPos = new Position[1];
    // FIXME: has no operand position yet for throwable
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    return Jimple.newThrowStmt(
        local,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertUnaryOpInstruction(
      DebuggingInformation debugInfo, SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value op;
    Type type = UnknownType.getInstance();
    if (symbolTable.isConstant(use)) {
      op = getConstant(use);
    } else {
      op = getLocal(type, use);
    }
    type = op.getType();
    Local left = getLocal(type, def);

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for right side or assigned variable
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    if (inst instanceof AssignInstruction) {
      return Jimple.newAssignStmt(
          left,
          op,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    } else {
      JNegExpr expr = Jimple.newNegExpr(op);

      return Jimple.newAssignStmt(
          left,
          expr,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private Stmt convertPutInstruction(DebuggingInformation debugInfo, SSAPutInstruction inst) {
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    JavaClassType classSig =
        (JavaClassType)
            identifierFactory.getClassType(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature(
            fieldRef.getName().toString(), classSig, fieldType.toString());
    Value fieldValue;
    if (inst.isStatic()) {
      fieldValue = Jimple.newStaticFieldRef(fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(classSig, ref);
      fieldValue = Jimple.newInstanceFieldRef(base, fieldSig);
    }
    Value value = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      value = getConstant(val);
    } else {
      value = getLocal(fieldType, val);
    }

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for value, rvalue
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    return Jimple.newAssignStmt(
        fieldValue,
        value,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertNewInstruction(DebuggingInformation debugInfo, SSANewInstruction inst) {
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
        size = getLocal(PrimitiveType.getInt(), use);
      }
      rvalue = JavaJimple.getInstance().newNewArrayExpr(type, size);
    } else {
      rvalue = Jimple.newNewExpr((ReferenceType) type);
    }

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for type, size
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        var,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertComparisonInstruction(
      DebuggingInformation debugInfo, SSAComparisonInstruction inst) {
    // TODO imlement
    return Jimple.newNopStmt(
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertInstanceofInstruction(
      DebuggingInformation debugInfo, SSAInstanceofInstruction inst) {
    int result = inst.getDef();
    int ref = inst.getRef();
    Type checkedType = converter.convertType(inst.getCheckedType());
    // TODO. how to get type of ref?
    Local op = getLocal(UnknownType.getInstance(), ref);
    JInstanceOfExpr expr = Jimple.newInstanceOfExpr(op, checkedType);
    Value left = getLocal(PrimitiveType.getBoolean(), result);

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for checked and expected side
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        left,
        expr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertConversionInstruction(
      DebuggingInformation debugInfo, SSAConversionInstruction inst) {
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

    Position[] operandPos = new Position[2];
    // FIXME: has no positions for lvalue, rvalue yet
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        lvalue,
        cast,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertInvokeInstruction(
      DebuggingInformation debugInfo, AstJavaInvokeInstruction invokeInst) {
    Value invoke;
    CallSiteReference callee = invokeInst.getCallSite();
    MethodReference target = invokeInst.getDeclaredTarget();
    String declaringClassSignature =
        converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    String returnType = converter.convertType(target.getReturnType()).toString();
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Value> args = new ArrayList<>();
    for (int i = 0; i < target.getNumberOfParameters(); i++) {
      Type paraType = converter.convertType(target.getParameterType(i)); // note
      // the
      // parameters
      // do
      // not
      // include
      // "this"
      paraTypes.add(paraType);
      parameters.add(paraType.toString());
    }
    Position[] operandPos = new Position[invokeInst.getNumberOfUses()];
    for (int j = 0; j < invokeInst.getNumberOfUses(); j++) {
      operandPos[j] = debugInfo.getOperandPosition(invokeInst.iIndex(), j);
    }
    int i = 0;
    if (!callee.isStatic()) {
      i = 1; // non-static invoke this first use is thisRef.
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

    MethodSignature methodSig =
        identifierFactory.getMethodSignature(
            target.getName().toString(), declaringClassSignature, returnType, parameters);

    if (!callee.isStatic()) {
      int receiver = invokeInst.getReceiver();
      Type classType = converter.convertType(target.getDeclaringClass());
      Local base = getLocal(classType, receiver);
      if (callee.isSpecial()) {
        Type baseType = UnknownType.getInstance();
        // TODO. baseType could be a problem.
        base = getLocal(baseType, receiver);
        invoke = Jimple.newSpecialInvokeExpr(base, methodSig, args); // constructor
      } else if (callee.isVirtual()) {
        invoke = Jimple.newVirtualInvokeExpr(base, methodSig, args);
      } else if (callee.isInterface()) {
        invoke = Jimple.newInterfaceInvokeExpr(base, methodSig, args);
      } else {
        throw new RuntimeException("Unsupported invoke instruction: " + callee.toString());
      }
    } else {
      invoke = Jimple.newStaticInvokeExpr(methodSig, args);
    }

    if (invokeInst.hasDef()) {
      Type type = converter.convertType(invokeInst.getDeclaredResultType());
      Local v = getLocal(type, invokeInst.getDef());
      return Jimple.newAssignStmt(
          v,
          invoke,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    } else {
      return Jimple.newInvokeStmt(
          invoke,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertBranchInstruction(
      DebuggingInformation debugInfo, SSAConditionalBranchInstruction condInst) {
    StmtPositionInfo posInfo =
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(condInst.iIndex()), null);
    List<Stmt> stmts = new ArrayList<>();
    int val1 = condInst.getUse(0);
    int val2 = condInst.getUse(1);
    Value value1 = extractValueAndAddAssignStmt(posInfo, stmts, val1);
    Value value2 = extractValueAndAddAssignStmt(posInfo, stmts, val2);
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

    JIfStmt ifStmt = Jimple.newIfStmt(condition, target, posInfo);
    // target equals -1 refers to the end of the method
    this.targetsOfIfStmts.put(ifStmt, condInst.getTarget());
    stmts.add(ifStmt);
    return stmts;
  }

  private Value extractValueAndAddAssignStmt(StmtPositionInfo posInfo, List<Stmt> addTo, int val) {
    Value value;
    Integer constant = null;
    if (symbolTable.isZero(val)) {
      value = IntConstant.getInstance(0);
    } else {
      if (symbolTable.isConstant(val)) {
        Object c = symbolTable.getConstantValue(val);
        if (c instanceof Boolean) {
          constant = c.equals(true) ? 1 : 0;
        }
      }
      value = getLocal(PrimitiveType.getInt(), val);
    }
    if (constant != null) {
      JAssignStmt assignStmt =
          Jimple.newAssignStmt(value, IntConstant.getInstance(constant), posInfo);
      addTo.add(assignStmt);
    }
    return value;
  }

  private Stmt convertReturnInstruction(DebuggingInformation debugInfo, SSAReturnInstruction inst) {
    int result = inst.getResult();
    if (inst.returnsVoid()) {
      // this is return void stmt
      return Jimple.newReturnVoidStmt(
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), null));
    } else {
      Value ret;
      if (symbolTable.isConstant(result)) {
        ret = getConstant(result);
      } else {
        ret = this.getLocal(UnknownType.getInstance(), result);
      }

      Position[] operandPos = new Position[1];
      operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
      return Jimple.newReturnStmt(
          ret,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private Stmt convertBinaryOpInstruction(
      DebuggingInformation debugInfo, SSABinaryOpInstruction binOpInst) {
    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    Type type = UnknownType.getInstance();
    Value op1;
    if (symbolTable.isConstant(val1)) {
      op1 = getConstant(val1);
    } else {
      op1 = getLocal(type, val1);
    }
    type = op1.getType();
    Value op2 = null;
    if (symbolTable.isConstant(val2)) {
      op2 = getConstant(val2);
    } else {
      op2 = getLocal(type, val2);
    }
    if (type.equals(UnknownType.getInstance())) type = op2.getType();
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
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.NE)) {
      binExpr = Jimple.newNeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.LT)) {
      binExpr = Jimple.newLtExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.GE)) {
      binExpr = Jimple.newGeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.GT)) {
      binExpr = Jimple.newGtExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(CAstBinaryOp.LE)) {
      binExpr = Jimple.newLeExpr(op1, op2);
      type = PrimitiveType.getBoolean();
    } else if (operator.equals(IShiftInstruction.Operator.SHL)) {
      binExpr = Jimple.newShlExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.SHR)) {
      binExpr = Jimple.newShrExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.USHR)) {
      binExpr = Jimple.newUshrExpr(op1, op2);
    } else {
      throw new RuntimeException("Unsupported binary operator: " + operator.getClass());
    }
    Position[] operandPos = new Position[2];
    Position p1 = debugInfo.getOperandPosition(binOpInst.iIndex(), 0);
    operandPos[0] = p1;
    Position p2 = debugInfo.getOperandPosition(binOpInst.iIndex(), 1);
    operandPos[1] = p2;
    Value result = getLocal(type, def);
    return Jimple.newAssignStmt(
        result,
        binExpr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(binOpInst.iIndex()), operandPos));
  }

  private Stmt convertGoToInstruction(DebuggingInformation debugInfo, SSAGotoInstruction gotoInst) {
    JStmtBox target = (JStmtBox) Jimple.newStmtBox(null);
    JGotoStmt gotoStmt =
        Jimple.newGotoStmt(
            target,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(gotoInst.iIndex()), null));
    this.targetsOfGotoStmts.put(gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  private Stmt convertGetInstruction(DebuggingInformation debugInfo, SSAGetInstruction inst) {
    int def = inst.getDef(0);
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String walaClassName = fieldRef.getDeclaringClass().getName().toString();
    JavaClassType classSig =
        (JavaClassType)
            identifierFactory.getClassType(converter.convertClassNameFromWala(walaClassName));
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature(
            fieldRef.getName().toString(), classSig, fieldType.toString());
    Value rvalue = null;
    if (inst.isStatic()) {
      rvalue = Jimple.newStaticFieldRef(fieldSig);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(classSig, ref);
      rvalue = Jimple.newInstanceFieldRef(base, fieldSig);
    }

    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    Value var = getLocal(fieldType, def);
    return Jimple.newAssignStmt(
        var,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Constant getConstant(int valueNumber) {
    Object value = symbolTable.getConstantValue(valueNumber);
    if (value instanceof Boolean) {
      return BooleanConstant.getInstance((boolean) value);
    } else if (value instanceof Byte
        || value instanceof Char
        || value instanceof Short
        || value instanceof Integer) {
      return IntConstant.getInstance((int) value);
    } else if (symbolTable.isLongConstant(valueNumber)) {
      return LongConstant.getInstance((long) value);
    } else if (symbolTable.isDoubleConstant(valueNumber)) {
      return DoubleConstant.getInstance((double) value);
    } else if (symbolTable.isFloatConstant(valueNumber)) {
      return FloatConstant.getInstance((float) value);
    } else if (symbolTable.isStringConstant(valueNumber)) {
      return JavaJimple.getInstance().newStringConstant((String) value);
    } else if (symbolTable.isNullConstant(valueNumber)) {
      return NullConstant.getInstance();
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    if (locals.containsKey(valueNumber)) {
      return locals.get(valueNumber);
    }
    if (valueNumber == 1 || type.equals(methodSignature.getDeclClassType())) {
      // in wala symbol numbers start at 1 ... the "this" parameter will be symbol number 1 in a
      // non-static method.
      if (!walaMethod.isStatic()) {
        return localGenerator.getThisLocal();
      }
    }
    if (symbolTable.isParameter(valueNumber)) {
      Local para = localGenerator.getParameterLocal(valueNumber - 1);
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
      // TODO. re-implement merge. Don't forget type can also be UnknownType.
      // throw new RuntimeException("Different types for same local
      // variable: "+ret.getType()+"<->"+type);
    }
    return ret;
  }

  /**
   * Test if the given stmt is the target stmt of {@link JIfStmt} or {@link JGotoStmt} and set it as
   * the target if it is the case.
   *
   * @param stmt the converted jimple stmt.
   * @param iindex the instruction index of the corresponding instruction in Wala.
   */
  @SuppressWarnings("deprecation")
  protected void setTarget(Stmt stmt, int iindex) {
    if (this.targetsOfIfStmts.containsValue(iindex)) {
      for (JIfStmt ifStmt : this.targetsOfIfStmts.keySet()) {
        if (this.targetsOfIfStmts.get(ifStmt).equals(iindex)) {
          JIfStmt.$Accessor.setTarget(ifStmt, stmt);
        }
      }
    }

    // FIXME: [ms] targetbox of JGotoStmt is null @PositionInfoTest.java ->testSwitchInstruction()
    if (this.targetsOfGotoStmts.containsValue(iindex)) {
      for (JGotoStmt gotoStmt : this.targetsOfGotoStmts.keySet()) {
        if (this.targetsOfGotoStmts.get(gotoStmt).equals(iindex)) {
          JGotoStmt.$Accessor.setTarget(gotoStmt, stmt);
        }
      }
    }
    if (this.defaultOfLookUpSwitchStmts.containsValue(iindex)) {
      for (JLookupSwitchStmt lookupSwitch : this.defaultOfLookUpSwitchStmts.keySet()) {
        if (this.defaultOfLookUpSwitchStmts.get(lookupSwitch).equals(iindex)) {
          AbstractSwitchStmt.$Accessor.setDefaultTarget(lookupSwitch, stmt);
        }
      }
    }
    for (JLookupSwitchStmt lookupSwitch : this.targetsOfLookUpSwitchStmts.keySet()) {
      if (this.targetsOfLookUpSwitchStmts.get(lookupSwitch).contains(iindex)) {
        List<Stmt> targets = lookupSwitch.getTargets();
        if (targets.contains(null)) { // targets only contains
          // placeholder
          targets = new ArrayList<>();
        }
        targets.add(stmt);
        AbstractSwitchStmt.$Accessor.setTargets(lookupSwitch, targets);
      }
    }
  }
}
