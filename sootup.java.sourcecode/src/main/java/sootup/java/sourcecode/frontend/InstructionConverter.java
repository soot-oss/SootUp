package sootup.java.sourcecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Markus Schmidt and Christian Brüggemann
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
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.shrike.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrike.shrikeBT.IConditionalBranchInstruction.IOperator;
import com.ibm.wala.shrike.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.shrike.shrikeBT.IShiftInstruction;
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
import java.util.*;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootField;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.java.core.ConstantUtil;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * This class converts wala instruction to jimple statement.
 *
 * @author Linghui Luo
 */
public class InstructionConverter {

  private final DebuggingInformation debugInfo;
  private final WalaIRToJimpleConverter converter;
  private final MethodSignature methodSignature;
  private final AstMethod walaMethod;
  private final SymbolTable symbolTable;
  private final LocalGenerator localGenerator;

  private final Map<JGotoStmt, Integer> branchingTargetsOfGotoStmts;
  private final Map<JIfStmt, Integer> branchingTargetsOfIfStmts;
  private final Map<JSwitchStmt, List<Integer>> branchingTargetsOfLookUpSwitchStmts;

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
    this.branchingTargetsOfIfStmts = new LinkedHashMap<>();
    this.branchingTargetsOfGotoStmts = new LinkedHashMap<>();
    this.branchingTargetsOfLookUpSwitchStmts = new LinkedHashMap<>();
    this.locals = new HashMap<>();
    this.identifierFactory = converter.identifierFactory;
    debugInfo = walaMethod.debugInfo();
  }

  public List<Stmt> convertInstruction(SSAInstruction inst, HashMap<Integer, Stmt> stmt2iIndex) {
    List<Stmt> stmts = new ArrayList();

    if (inst instanceof SSAConditionalBranchInstruction) {
      stmts.addAll(convertBranchInstruction((SSAConditionalBranchInstruction) inst));
    } else if (inst instanceof SSAGotoInstruction) {
      stmts.add(convertGoToInstruction((SSAGotoInstruction) inst));
    } else if (inst instanceof SSAReturnInstruction) {
      stmts.add(convertReturnInstruction((SSAReturnInstruction) inst));
    } else if (inst instanceof AstJavaInvokeInstruction) {
      stmts.add(convertInvokeInstruction((AstJavaInvokeInstruction) inst));
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        stmts.add(convertGetInstruction((SSAGetInstruction) inst));
      } else if (inst instanceof SSAPutInstruction) {
        stmts.add(convertPutInstruction((SSAPutInstruction) inst));
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
      stmts.addAll(convertBinaryOpInstruction((SSABinaryOpInstruction) inst));
    } else if (inst instanceof SSAUnaryOpInstruction) {
      stmts.add(convertUnaryOpInstruction((SSAUnaryOpInstruction) inst));
    } else if (inst instanceof SSAThrowInstruction) {
      stmts.add(convertThrowInstruction((SSAThrowInstruction) inst));
    } else if (inst instanceof SSASwitchInstruction) {
      stmts.add(convertSwitchInstruction((SSASwitchInstruction) inst));
    } else if (inst instanceof SSALoadMetadataInstruction) {
      stmts.add(convertLoadMetadataInstruction((SSALoadMetadataInstruction) inst));
    } else if (inst instanceof EnclosingObjectReference) {
      stmts.add(convertEnclosingObjectReference((EnclosingObjectReference) inst));
    } else if (inst instanceof AstLexicalRead) {
      stmts = (convertAstLexicalRead((AstLexicalRead) inst));
    } else if (inst instanceof AstLexicalWrite) {
      stmts = (convertAstLexicalWrite((AstLexicalWrite) inst));
    } else if (inst instanceof AstAssertInstruction) {
      stmts = convertAssertInstruction((AstAssertInstruction) inst, stmt2iIndex);
    } else if (inst instanceof SSACheckCastInstruction) {
      stmts.add(convertCheckCastInstruction((SSACheckCastInstruction) inst));
    } else if (inst instanceof SSAMonitorInstruction) {
      stmts.add(
          convertMonitorInstruction((SSAMonitorInstruction) inst)); // for synchronized statement
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      stmts.add(convertGetCaughtExceptionInstruction((SSAGetCaughtExceptionInstruction) inst));
    } else if (inst instanceof SSAArrayLengthInstruction) {
      stmts.add(convertArrayLengthInstruction((SSAArrayLengthInstruction) inst));
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        stmts.add(convertArrayLoadInstruction((SSAArrayLoadInstruction) inst));
      } else if (inst instanceof SSAArrayStoreInstruction) {
        stmts.add(convertArrayStoreInstruction((SSAArrayStoreInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    return stmts;
  }

  private Stmt convertArrayStoreInstruction(SSAArrayStoreInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Immediate index = null;
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
    // TODO: written arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        arrayRef,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLoadInstruction(SSAArrayLoadInstruction inst) {
    Local base = getLocal(UnknownType.getInstance(), inst.getArrayRef());
    int i = inst.getIndex();
    Immediate index;
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
    // TODO: loaded arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    return Jimple.newAssignStmt(
        left,
        arrayRef,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertArrayLengthInstruction(SSAArrayLengthInstruction inst) {
    int result = inst.getDef();
    Local left = getLocal(PrimitiveType.getInt(), result);
    int arrayRef = inst.getArrayRef();
    Local arrayLocal = getLocal(UnknownType.getInstance(), arrayRef);
    Value right = Jimple.newLengthExpr(arrayLocal);

    Position[] operandPos = new Position[1];
    Position p1 = debugInfo.getOperandPosition(inst.iIndex(), 0);
    operandPos[0] = p1;
    // TODO: [ms] stmt position ends at variablename of the array
    return Jimple.newAssignStmt(
        left,
        right,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertGetCaughtExceptionInstruction(SSAGetCaughtExceptionInstruction inst) {
    int exceptionValue = inst.getException();

    // TODO: [ms] make exception type more specific
    JavaClassType exceptionClassType =
        JavaIdentifierFactory.getInstance().getClassType("java.lang.Throwable");

    Local local = getLocal(exceptionClassType, exceptionValue);
    JCaughtExceptionRef caught = JavaJimple.getInstance().newCaughtExceptionRef();

    Position[] operandPos = new Position[1];
    // TODO: [ms] position info of parameter, target is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    final JIdentityStmt handlerStmt =
        Jimple.newIdentityStmt(
            local,
            caught,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));

    return handlerStmt;
  }

  private Stmt convertMonitorInstruction(SSAMonitorInstruction inst) {
    Immediate op = getLocal(UnknownType.getInstance(), inst.getRef());

    Position[] operandPos = new Position[1];
    // TODO: [ms] referenced object position info is missing
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
      AstAssertInstruction inst, HashMap<Integer, Stmt> stmt2iIndex) {
    List<Stmt> stmts = new ArrayList<>();
    // create a static field for checking if assertion is disabled.
    JavaClassType cSig = (JavaClassType) methodSignature.getDeclClassType();
    FieldSignature fieldSig =
        identifierFactory.getFieldSignature("$assertionsDisabled", cSig, "boolean");
    SootField assertionsDisabled =
        new SootField(
            fieldSig,
            EnumSet.of(FieldModifier.FINAL, FieldModifier.STATIC),
            NoPositionInformation.getInstance());

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

    // [ms] handling multiple assertions in one body -> each has an own nop -> own link to target
    int stmtAfterAssertion = -42 - inst.iIndex();
    stmt2iIndex.put(stmtAfterAssertion, nopStmt);

    JIfStmt ifStmt =
        Jimple.newIfStmt(
            condition,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    branchingTargetsOfIfStmts.put(ifStmt, stmtAfterAssertion);
    stmts.add(ifStmt);

    // create ifStmt for the actual assertion.
    Local assertLocal = getLocal(PrimitiveType.getBoolean(), inst.getUse(0));
    JEqExpr assertionExpr = Jimple.newEqExpr(assertLocal, IntConstant.getInstance(1));

    JIfStmt assertIfStmt =
        Jimple.newIfStmt(
            assertionExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    stmts.add(assertIfStmt);
    branchingTargetsOfIfStmts.put(assertIfStmt, stmtAfterAssertion);
    // create failed assertion code.

    ClassType assertionErrorType =
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
    stmts.add(
        nopStmt); // TODO [LL] This should be removed later [ms] with the following statement after
    // assert
    return stmts;
  }

  private List<Stmt> convertAstLexicalWrite(AstLexicalWrite inst) {
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
        SootField field =
            new SootField(
                fieldSig, EnumSet.of(FieldModifier.FINAL), NoPositionInformation.getInstance());
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

  private List<Stmt> convertAstLexicalRead(AstLexicalRead inst) {
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
        SootField field =
            new SootField(
                fieldSig, EnumSet.of(FieldModifier.FINAL), NoPositionInformation.getInstance());
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

  private Stmt convertEnclosingObjectReference(EnclosingObjectReference inst) {
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

  private Stmt convertCheckCastInstruction(SSACheckCastInstruction inst) {
    TypeReference[] types = inst.getDeclaredResultTypes();
    Local result = getLocal(converter.convertType(types[0]), inst.getResult());
    Immediate rvalue = null;
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

  private Stmt convertLoadMetadataInstruction(SSALoadMetadataInstruction inst) {
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

  private Stmt convertSwitchInstruction(SSASwitchInstruction inst) {
    int val = inst.getUse(0);
    Local local = getLocal(UnknownType.getInstance(), val);
    int[] cases = inst.getCasesAndLabels();
    int defaultCase = inst.getDefault();
    List<IntConstant> lookupValues = new ArrayList<>();
    List<Integer> targetList = new ArrayList<>();

    for (int i = 0; i < cases.length; i++) {
      int c = cases[i];
      if (i % 2 == 0) {
        IntConstant cValue = IntConstant.getInstance(c);
        lookupValues.add(cValue);
      } else {
        targetList.add(c);
      }
    }
    targetList.add(defaultCase);

    Position[] operandPos = new Position[2];
    // TODO: [ms] how to organize the operands
    // TODO: has no operand positions yet for
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), ); // key
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), ); // default
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // lookups
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // targets

    JSwitchStmt stmt =
        Jimple.newLookupSwitchStmt(
            local,
            lookupValues,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    branchingTargetsOfLookUpSwitchStmts.put(stmt, targetList);
    return stmt;
  }

  private Stmt convertThrowInstruction(SSAThrowInstruction inst) {
    int exception = inst.getException();
    // TODO: [ms] make exception type more specific
    Local local = getLocal(UnknownType.getInstance(), exception);

    Position[] operandPos = new Position[1];
    // TODO: has no operand position yet for throwable
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    final JThrowStmt jThrowStmt =
        Jimple.newThrowStmt(
            local,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(inst.iIndex()), operandPos));

    return jThrowStmt;
  }

  private Stmt convertUnaryOpInstruction(SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Immediate op;
    Type type = UnknownType.getInstance();
    if (symbolTable.isConstant(use)) {
      op = getConstant(use);
    } else {
      op = getLocal(type, use);
    }

    type = op.getType();
    // is it just variable declaration?
    if (type == NullType.getInstance()) {
      // FIXME: [ms] determine type of def side
      // if null is assigned or if its just a local declaration we can't use the right side (i.e.
      // null) to determine the locals type
      type = UnknownType.getInstance();
    }
    Local left = getLocal(type, def);

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for right side or assigned variable
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

  private Stmt convertPutInstruction(SSAPutInstruction inst) {
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
    Immediate value;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      value = getConstant(val);
    } else {
      value = getLocal(fieldType, val);
    }

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for value, rvalue
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    return Jimple.newAssignStmt(
        fieldValue,
        value,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertNewInstruction(SSANewInstruction inst) {
    int result = inst.getDef();
    Type type = converter.convertType(inst.getNewSite().getDeclaredType());
    Value var = getLocal(type, result);
    Value rvalue;
    if (type instanceof ArrayType) {
      int use = inst.getUse(0);
      Immediate size;
      if (symbolTable.isConstant(use)) {
        size = getConstant(use);
      } else {
        // TODO: size type unsure
        size = getLocal(PrimitiveType.getInt(), use);
      }
      Type baseType =
          converter.convertType(inst.getNewSite().getDeclaredType().getArrayElementType());
      rvalue = JavaJimple.getInstance().newNewArrayExpr(baseType, size);
    } else {
      rvalue = Jimple.newNewExpr((ClassType) type);
    }

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for type, size
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        var,
        rvalue,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertComparisonInstruction(SSAComparisonInstruction inst) {
    // TODO imlement
    return Jimple.newNopStmt(
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), null));
  }

  private Stmt convertInstanceofInstruction(SSAInstanceofInstruction inst) {
    int result = inst.getDef();
    int ref = inst.getRef();
    Type checkedType = converter.convertType(inst.getCheckedType());
    // TODO. how to get type of ref?
    Local op = getLocal(UnknownType.getInstance(), ref);
    JInstanceOfExpr expr = Jimple.newInstanceOfExpr(op, checkedType);
    Value left = getLocal(PrimitiveType.getBoolean(), result);

    Position[] operandPos = new Position[2];
    // TODO: has no operand positions yet for checked and expected side
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        left,
        expr,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertConversionInstruction(SSAConversionInstruction inst) {
    Type fromType = converter.convertType(inst.getFromType());
    Type toType = converter.convertType(inst.getToType());
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value lvalue = getLocal(toType, def);
    Immediate rvalue;
    if (symbolTable.isConstant(use)) {
      rvalue = getConstant(use);
    } else {
      rvalue = getLocal(fromType, use);
    }
    JCastExpr cast = Jimple.newCastExpr(rvalue, toType);

    Position[] operandPos = new Position[2];
    // TODO: has no positions for lvalue, rvalue yet
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    return Jimple.newAssignStmt(
        lvalue,
        cast,
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
  }

  private Stmt convertInvokeInstruction(AstJavaInvokeInstruction invokeInst) {
    AbstractInvokeExpr invoke;
    CallSiteReference callee = invokeInst.getCallSite();
    MethodReference target = invokeInst.getDeclaredTarget();
    String declaringClassSignature =
        converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    String returnType = converter.convertType(target.getReturnType()).toString();
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Immediate> args = new ArrayList<>();
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
      Immediate arg;
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

    final Stmt jInvokeStmt;
    if (invokeInst.hasDef()) {
      Type type = converter.convertType(invokeInst.getDeclaredResultType());
      Local v = getLocal(type, invokeInst.getDef());
      jInvokeStmt =
          Jimple.newAssignStmt(
              v,
              invoke,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    } else {
      jInvokeStmt =
          Jimple.newInvokeStmt(
              invoke,
              WalaIRToJimpleConverter.convertPositionInfo(
                  debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos));
    }
    return jInvokeStmt;
  }

  private List<Stmt> convertBranchInstruction(SSAConditionalBranchInstruction condInst) {
    StmtPositionInfo posInfo =
        WalaIRToJimpleConverter.convertPositionInfo(
            debugInfo.getInstructionPosition(condInst.iIndex()), null);
    List<Stmt> stmts = new ArrayList<>();
    int val1 = condInst.getUse(0);
    int val2 = condInst.getUse(1);
    Immediate value1 = extractValueAndAddAssignStmt(posInfo, stmts, val1);
    Immediate value2 = extractValueAndAddAssignStmt(posInfo, stmts, val2);
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

    JIfStmt ifStmt = Jimple.newIfStmt(condition, posInfo);
    // target equals -1 refers to the end of the method
    branchingTargetsOfIfStmts.put(ifStmt, condInst.getTarget());
    stmts.add(ifStmt);
    return stmts;
  }

  private Immediate extractValueAndAddAssignStmt(
      StmtPositionInfo posInfo, List<Stmt> addTo, int val) {
    Immediate value;
    Object constant = null;
    if (symbolTable.isZero(val)) {
      value = IntConstant.getInstance(0);
    } else {
      if (symbolTable.isConstant(val)) {
        constant = symbolTable.getConstantValue(val);
      }
      value = getLocal(PrimitiveType.getInt(), val);
    }
    if (constant != null) {
      JAssignStmt assignStmt =
          Jimple.newAssignStmt(value, ConstantUtil.fromObject(constant), posInfo);
      addTo.add(assignStmt);
    }
    return value;
  }

  private Stmt convertReturnInstruction(SSAReturnInstruction inst) {
    int result = inst.getResult();
    if (inst.returnsVoid()) {
      // this is return void stmt
      return Jimple.newReturnVoidStmt(
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), null));
    } else {
      Immediate ret;
      if (symbolTable.isConstant(result)) {
        ret = getConstant(result);
      } else {
        ret = getLocal(UnknownType.getInstance(), result);
      }

      Position[] operandPos = new Position[1];
      operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
      return Jimple.newReturnStmt(
          ret,
          WalaIRToJimpleConverter.convertPositionInfo(
              debugInfo.getInstructionPosition(inst.iIndex()), operandPos));
    }
  }

  private List<Stmt> convertStringAddition(
      Immediate op1,
      Immediate op2,
      Immediate result,
      Type type,
      int iindex,
      AstMethod.DebuggingInformation debugInfo) {
    List<Stmt> ret = new ArrayList();
    Position p1 = debugInfo.getOperandPosition(iindex, 0);
    Position p2 = debugInfo.getOperandPosition(iindex, 1);
    Position stmtPosition = debugInfo.getInstructionPosition(iindex);

    JavaClassType sbType =
        (JavaClassType) identifierFactory.getClassType("java.lang.StringBuilder");
    Local strBuilderLocal = localGenerator.generateLocal(sbType);

    Stmt newStmt =
        Jimple.newAssignStmt(
            strBuilderLocal,
            Jimple.newNewExpr(sbType),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, null));
    ret.add(newStmt);

    MethodSignature initMethod =
        identifierFactory.getMethodSignature(
            "<init>",
            sbType.getFullyQualifiedName(),
            VoidType.getInstance().toString(),
            Collections.singletonList(type.toString()));
    CAstSourcePositionMap.Position[] pos1 = new CAstSourcePositionMap.Position[2];
    pos1[0] = null;
    pos1[1] = p1;

    Stmt specStmt =
        Jimple.newInvokeStmt(
            Jimple.newSpecialInvokeExpr(strBuilderLocal, initMethod, op1),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, pos1));

    ret.add(specStmt);

    MethodSignature appendMethod =
        identifierFactory.getMethodSignature(
            "append",
            sbType.getFullyQualifiedName(),
            sbType.toString(),
            Collections.singletonList(type.toString()));
    Local strBuilderLocal2 = localGenerator.generateLocal(sbType);
    CAstSourcePositionMap.Position[] pos2 = new CAstSourcePositionMap.Position[2];
    pos2[0] = null;
    pos2[1] = p2;

    Stmt virStmt =
        Jimple.newAssignStmt(
            strBuilderLocal2,
            Jimple.newVirtualInvokeExpr(strBuilderLocal, appendMethod, op2),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, pos2));

    ret.add(virStmt);

    MethodSignature toStringMethod =
        identifierFactory.getMethodSignature(
            "toString", sbType.getFullyQualifiedName(), sbType.toString(), Collections.emptyList());

    Stmt toStringStmt =
        Jimple.newAssignStmt(
            result,
            Jimple.newVirtualInvokeExpr(strBuilderLocal2, toStringMethod),
            WalaIRToJimpleConverter.convertPositionInfo(stmtPosition, null));

    ret.add(toStringStmt);
    return ret;
  }

  private List<Stmt> convertBinaryOpInstruction(SSABinaryOpInstruction binOpInst) {
    List<Stmt> ret = new ArrayList<>();
    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    Type type = UnknownType.getInstance();
    Immediate op1;
    if (symbolTable.isConstant(val1)) {
      op1 = getConstant(val1);
    } else {
      op1 = getLocal(type, val1);
    }
    type = op1.getType();
    Immediate op2;
    if (symbolTable.isConstant(val2)) {
      op2 = getConstant(val2);
    } else {
      op2 = getLocal(type, val2);
    }
    if (type.equals(UnknownType.getInstance())) type = op2.getType();
    AbstractBinopExpr binExpr;
    IBinaryOpInstruction.IOperator operator = binOpInst.getOperator();
    if (operator.equals(IBinaryOpInstruction.Operator.ADD)) {
      if (type.toString().equals("java.lang.String")) {
        // from wala java source code frontend we get also string addition(concatenation).
        Immediate result = getLocal(type, def);
        return convertStringAddition(op1, op2, result, type, binOpInst.iIndex(), debugInfo);
      }
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
    ret.add(
        Jimple.newAssignStmt(
            result,
            binExpr,
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(binOpInst.iIndex()), operandPos)));
    return ret;
  }

  private Stmt convertGoToInstruction(SSAGotoInstruction gotoInst) {
    JGotoStmt gotoStmt =
        Jimple.newGotoStmt(
            WalaIRToJimpleConverter.convertPositionInfo(
                debugInfo.getInstructionPosition(gotoInst.iIndex()), null));
    branchingTargetsOfGotoStmts.put(gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  private Stmt convertGetInstruction(SSAGetInstruction inst) {
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
        || value instanceof Character
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
    final Local cachedLocal = locals.get(valueNumber);
    if (cachedLocal != null) {
      return cachedLocal;
    }
    if (valueNumber == 1) {
      // in wala symbol numbers start at 1 ... the "this" parameter will be symbol
      // number 1 in a non-static method.
      if (!walaMethod.isStatic()) {
        Local thisLocal = localGenerator.getThisLocal();
        locals.put(valueNumber, thisLocal);
        return thisLocal;
      }
    }
    if (symbolTable.isParameter(valueNumber)) {
      Local para = localGenerator.getParameterLocal(valueNumber - 1);
      if (para != null) {
        return para;
      }
    }

    Local ret = locals.computeIfAbsent(valueNumber, key -> localGenerator.generateLocal(type));

    if (!ret.getType().equals(type)) {
      // ret.setType(ret.getType().merge(type));
      // TODO: re-implement merge. [CB] Don't forget type can also be UnknownType.
      // throw new RuntimeException("Different types for same local
      // variable: "+ret.getType()+"<->"+type);
    }
    return ret;
  }

  /**
   * This methods adds stmts with all branch stmts to builder ({@link JIfStmt}, {@link JGotoStmt},
   * {@link JSwitchStmt}) having set up their target stmts.
   *
   * @param stmt2iIndex
   * @return This methods returns a list of stmts with all branch stmts ({@link JIfStmt}, {@link
   *     JGotoStmt}, {@link JSwitchStmt}) having set up their target stmts.
   */
  protected Map<BranchingStmt, List<Stmt>> setUpTargets(HashMap<Integer, Stmt> stmt2iIndex) {
    Map<BranchingStmt, List<Stmt>> branchingMap = new HashMap<>();
    for (Map.Entry<JIfStmt, Integer> ifStmt : branchingTargetsOfIfStmts.entrySet()) {
      final JIfStmt key = ifStmt.getKey();
      final Integer value = ifStmt.getValue();
      branchingMap.put(key, Collections.singletonList(stmt2iIndex.get(value)));
    }

    for (Map.Entry<JGotoStmt, Integer> gotoStmt : branchingTargetsOfGotoStmts.entrySet()) {
      final JGotoStmt key = gotoStmt.getKey();
      final Integer value = gotoStmt.getValue();
      branchingMap.put(key, Collections.singletonList(stmt2iIndex.get(value)));
    }

    for (Map.Entry<JSwitchStmt, List<Integer>> item :
        branchingTargetsOfLookUpSwitchStmts.entrySet()) {
      final JSwitchStmt switchStmt = item.getKey();
      final List<Integer> targetIdxList = item.getValue();

      List<Stmt> targets = new ArrayList<>(targetIdxList.size());
      // assign target for every idx in targetIdxList of switchStmt
      for (Integer targetIdx : targetIdxList) {
        // search for matching index/stmt
        targets.add(stmt2iIndex.get(targetIdx));
      }
      branchingMap.put(switchStmt, targets);
    }
    return branchingMap;
  }

  /**
   * determines wheter a given wala index is a target of a Branching Instruction. e.g. used for
   * detection of implicit return statements in void methods.
   */
  public boolean hasJumpTarget(Integer i) {
    if (branchingTargetsOfIfStmts.containsValue(i)) return true;
    if (branchingTargetsOfGotoStmts.containsValue(i)) return true;
    for (List<Integer> list : branchingTargetsOfLookUpSwitchStmts.values()) {
      if (list.contains(i)) {
        return true;
      }
    }
    return false;
  }
}
