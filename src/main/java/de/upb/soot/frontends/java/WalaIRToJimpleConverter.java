package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.DefaultSignatureFactory;

import com.ibm.wala.cast.loader.AstClass;
import com.ibm.wala.cast.loader.AstField;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WalaIRToJimpleConverter {

  public WalaIRToJimpleConverter() {

  }

  public SootClass convertClass(AstClass walaClass) {
    INamespace srcNamespace = null;
    Path sourcePath = null;
    String fullyQualifiedClassName = null;
    ClassSignature classSignature = new DefaultSignatureFactory() {
    }.getClassSignature(fullyQualifiedClassName);
    ClassSource classSource = new ClassSource(srcNamespace, sourcePath, classSignature);
    SootClass sootClass = new SootClass(classSource);
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

    return sootClass;
  }

  public SootField convertField(AstField walaField) {

    return null;
  }

  public SootMethod convertMethod(AstMethod walaMethod) {
    // create SootMethond instance
    String name=walaMethod.getName().toString();
    List<Type> paraTypes = new ArrayList<>();
    for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {

      Type paraType = convertType(walaMethod.getParameterType(i));
      paraTypes.add(paraType);
    }
    Type returnType = convertType(walaMethod.getReturnType());

    int modifier = 0;

    List<SootClass> thrownExceptions = Collections.emptyList();

    SootMethod sootMethod = new SootMethod(name, paraTypes, returnType, modifier, thrownExceptions);

    // create and set active body of the SootMethod
    Body body = createBody(sootMethod, walaMethod);
    sootMethod.setActiveBody(body);

    return sootMethod;
  }


  private Type convertType(TypeReference returnType) {
    // TODO Auto-generated method stub
    return null;
  }

  private Body createBody(SootMethod sootMethod, AstMethod walaMethod) {
    Body body = new Body(sootMethod);

    // convert all wala instructions to jimple statements
    SSAInstruction[] insts = (SSAInstruction[]) walaMethod.cfg().getInstructions();
    for (SSAInstruction inst : insts) {
      Stmt stmt = convertInstruction(inst);
      body.addStmt(stmt);
    }

    return body;
  }

  public Stmt convertInstruction(SSAInstruction walaInst) {
    // TODO Auto-generated method stub
    return null;
  }

}
