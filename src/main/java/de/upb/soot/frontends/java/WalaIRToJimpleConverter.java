package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.NullType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.VoidType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.java.JavaClassSource;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.loader.AstClass;
import com.ibm.wala.cast.loader.AstField;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
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
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.FixedSizeBitVector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converter which converts WALA IR to jimple.
 * 
 * @author Linghui Luo created on 17.09.18
 *
 */
public class WalaIRToJimpleConverter {
  private IView view;

  public WalaIRToJimpleConverter() {
    view = new JavaView(null);
  }

  public SootClass convertClass(AstClass walaClass) {
    INamespace srcNamespace = null;
    Path sourcePath = null;
    String fullyQualifiedClassName = null;
    ClassSignature classSignature = new DefaultSignatureFactory() {
    }.getClassSignature(fullyQualifiedClassName);
    AbstractClassSource classSource = new JavaClassSource(srcNamespace, sourcePath, classSignature);
    SootClass sootClass = new SootClass(view, classSource);
    // convert fields
    for (IField walaField : walaClass.getAllFields()) {
      SootField sootField = convertField((AstField) walaField);
      sootClass.addField(sootField);
    }
    // convert methods
    for (IMethod walaMethod : walaClass.getAllMethods()) {
      SootMethod sootMethod = convertMethod((AstMethod) walaMethod);
      sootClass.addMethod(sootMethod);
    }
    // add source position
    Position position = walaClass.getSourcePosition();
    sootClass.setPosition(position);
    return sootClass;
  }

  public SootField convertField(AstField walaField) {

    return null;
  }

  public SootMethod convertMethod(AstMethod walaMethod) {
    // create SootMethond instance
    String name = walaMethod.getName().toString();
    List<Type> paraTypes = new ArrayList<>();
    for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
      Type paraType = convertType(walaMethod.getParameterType(i));
      paraTypes.add(paraType);
    }
    Type returnType = convertType(walaMethod.getReturnType());
    int modifier = 0;
    List<SootClass> thrownExceptions = Collections.emptyList();
    // TODO check if all arguments are set up properly.
    SootMethod sootMethod = new SootMethod(view, name, paraTypes, returnType, modifier, thrownExceptions);

    // create and set active body of the SootMethod
    Body body = createBody(sootMethod, walaMethod);
    sootMethod.setActiveBody(body);

    // add debug info
    DebuggingInformation debugInfo = walaMethod.debugInfo();
    sootMethod.setDebugInfo(debugInfo);

    return sootMethod;
  }

  private Type convertType(TypeReference type) {
    if (type.isPrimitiveType()) {
      if (type.equals(TypeReference.Boolean)) {
        return BooleanType.getInstance();
      } else if (type.equals(TypeReference.Byte)) {
        return ByteType.getInstance();
      } else if (type.equals(TypeReference.Char)) {
        return CharType.getInstance();
      } else if (type.equals(TypeReference.Short)) {
        return ShortType.getInstance();
      } else if (type.equals(TypeReference.Int)) {
        return IntType.getInstance();
      } else if (type.equals(TypeReference.Long)) {
        return LongType.getInstance();
      } else if (type.equals(TypeReference.Float)) {
        return FloatType.getInstance();
      } else if (type.equals(TypeReference.Double)) {
        return DoubleType.getInstance();
      } else if (type.equals(TypeReference.Void)) {
        return VoidType.getInstance();
      }
    } else if (type.isReferenceType()) {
      if (type.isArrayType()) {
        TypeReference t = type.getArrayElementType();
        Type baseType = convertType(t);
        int dim = type.getDimensionality();
        return ArrayType.getInstance(baseType, dim);
      } else if (type.isClassType()) {
        if (type.equals(TypeReference.Null)) {
          // still valid?
          return NullType.getInstance();
        } else {
          // TODO what about special object types in wala?
          TypeName className = type.getName();
          return RefType.getInstance(className.toString());
        }
      }
    }
    throw new RuntimeException("Unsupported tpye: " + type);
  }

  private Body createBody(SootMethod sootMethod, AstMethod walaMethod) {

    Body body = new Body(sootMethod);
    // set position for body
    DebuggingInformation debugInfo = walaMethod.debugInfo();
    Position bodyPos = debugInfo.getCodeBodyPosition();
    body.setPosition(bodyPos);

    // Look AsmMethodSource.getBody
    // TODO 1. convert locals, see AsmMethodSource.emitLocals();
    for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
      TypeReference t = walaMethod.getParameterType(i);
      Type type = convertType(t);

    }
    // how to get all locals?
    // TODO 2. convert traps

    AbstractCFG<?, ?> cfg = walaMethod.cfg();

    // get exceptions which are not caught
    FixedSizeBitVector blocks = cfg.getExceptionalToExit();
    // convert all wala instructions to jimple statements
    SSAInstruction[] insts = (SSAInstruction[]) cfg.getInstructions();
    for (SSAInstruction inst : insts) {
      IStmt stmt = convertInstruction(inst);
      // set position for each statement
      Position stmtPos = debugInfo.getInstructionPosition(inst.iindex);
      stmt.setPosition(stmtPos);
      body.addStmt(stmt);
    }

    return body;
  }

  public IStmt convertInstruction(SSAInstruction walaInst) {

    // TODO what are the different types of SSAInstructions
    if (walaInst instanceof SSAConditionalBranchInstruction) {

    } else if (walaInst instanceof SSAGotoInstruction) {

    } else if (walaInst instanceof SSAReturnInstruction) {

    } else if (walaInst instanceof SSAThrowInstruction) {

    } else if (walaInst instanceof SSASwitchInstruction) {

    } else if (walaInst instanceof AstJavaInvokeInstruction) {

    } else if (walaInst instanceof SSAFieldAccessInstruction) {
      if (walaInst instanceof SSAGetInstruction) {
        // field read instruction -> assignStmt
      } else if (walaInst instanceof SSAPutInstruction) {
        // field write instruction
      } else {
        throw new RuntimeException("Unsupported instruction type: " + walaInst.getClass().toString());
      }
    } else if (walaInst instanceof SSAArrayLengthInstruction) {

    } else if (walaInst instanceof SSAArrayReferenceInstruction) {
      if (walaInst instanceof SSAArrayLoadInstruction) {

      } else if (walaInst instanceof SSAArrayStoreInstruction) {

      } else {
        throw new RuntimeException("Unsupported instruction type: " + walaInst.getClass().toString());
      }
    } else if (walaInst instanceof SSANewInstruction) {

    } else if (walaInst instanceof SSAComparisonInstruction) {

    } else if (walaInst instanceof SSAConversionInstruction) {

    } else if (walaInst instanceof SSAInstanceofInstruction) {

    } else if (walaInst instanceof SSABinaryOpInstruction) {

    }
    if (walaInst instanceof SSALoadMetadataInstruction) {

    }
    return null;
  }

  /**
   * Convert className in wala-format to soot format, e.g., wala-format: Ljava/lang/String -> soot-format: java.lang.String.
   * 
   * @param className
   *          in wala-format
   * @return className in soot.format
   */
  public String convertClassName(String className) {
    StringBuilder sb = new StringBuilder();
    if (className.startsWith("L")) {
      className = className.substring(1);
      String[] subNames = className.split("/");
      for (int i = 0; i < subNames.length; i++) {
        sb.append(subNames[i]);
        if (i != subNames.length - 1) {
          sb.append(".");
        }
      }
    } else {
      throw new RuntimeException("Can not convert WALA class name: " + className);
    }
    return sb.toString();
  }
}
