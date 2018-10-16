/*
 * @author Linghui Luo
 * @version 1.0
 */
package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
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
import de.upb.soot.namespaces.JavaSourcePathNamespace;
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
import com.ibm.wala.shrikeCT.InvalidClassFileException;
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
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.intset.FixedSizeBitVector;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Converter which converts WALA IR to jimple.
 * 
 * @author Linghui Luo created on 17.09.18
 *
 */
public class WalaIRToJimpleConverter {
  private IView view;
  private INamespace srcNamespace;

  public WalaIRToJimpleConverter(String sourceDirPath) {
    WalaJavaClassProvider classProvider = new WalaJavaClassProvider();
    srcNamespace = new JavaSourcePathNamespace(sourceDirPath, classProvider);
    view = new JavaView(null);
  }

  /**
   * Convert a wala {@link AstClass} to {@link SootClass}.
   * 
   * @param walaClass
   * @return A SootClass converted from walaClass
   */
  public SootClass convertClass(AstClass walaClass) {
    String fullyQualifiedClassName = convertClassName(walaClass.getName().toString());
    System.out.println("CLASS:" + fullyQualifiedClassName);
    ClassSignature classSignature = new DefaultSignatureFactory() {
    }.getClassSignature(fullyQualifiedClassName);
    URL url = walaClass.getSourceURL();
    Path sourcePath = Paths.get(url.getPath());
    AbstractClassSource classSource = new JavaClassSource(srcNamespace, sourcePath, classSignature);
    SootClass sootClass = new SootClass(view, classSource, converModifiers(walaClass));
    view.addSootClass(sootClass);
    sootClass.setApplicationClass();
    // convert fields
    for (IField walaField : walaClass.getAllFields()) {
      if (walaField instanceof AstField) {
        SootField sootField = convertField((AstField) walaField);
        if (sootClass.getFieldUnsafe(sootField.getName(), sootField.getType()) == null) {
          sootClass.addField(sootField);
        }
      } else {
        // TODO: sometimes also get com.ibm.wala.classLoader.FieldImpl
      }
    }
    // convert methods
    for (IMethod walaMethod : walaClass.getAllMethods()) {
      if (walaMethod instanceof AstMethod) {
        convertMethod(sootClass, (AstMethod) walaMethod);
      } else {
        // TODO: sometimes also get com.ibm.wala.classLoader.ShrikeCTMethod
      }
    }
    // add source position
    Position position = walaClass.getSourcePosition();
    sootClass.setPosition(position);
    return sootClass;
  }

  /**
   * Convert a wala {@link AstField} to {@link SootField}.
   * 
   * @param walaField
   * @return A SootField object converted from walaField.
   */
  public SootField convertField(AstField walaField) {
    Type type = convertType(walaField.getFieldTypeReference());
    walaField.isFinal();
    String name = walaField.getName().toString();
    EnumSet<Modifier> modifiers = convertModifiers(walaField);
    SootField sootField = new SootField(view, name, type, modifiers);
    return sootField;
  }

  /**
   * Convert a wala {@link AstMethod} to {@link SootMethod} and add it into the given sootClass.
   *
   * @param sootClass
   *          the SootClass which should contain the converted SootMethod
   * @param walaMethod
   *          the walMethod to be converted
   */
  public SootMethod convertMethod(SootClass sootClass, AstMethod walaMethod) {
    // create SootMethond instance
    String name = walaMethod.getName().toString();
    List<Type> paraTypes = new ArrayList<>();
    if (walaMethod.symbolTable() != null) {
      for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
        Type paraType = convertType(walaMethod.getParameterType(i));
        paraTypes.add(paraType);
      }
    } else {
      // TODO. symbol table can be null.
    }
    Type returnType = convertType(walaMethod.getReturnType());
    EnumSet<Modifier> modifier = convertModifiers(walaMethod);

    List<SootClass> thrownExceptions = new ArrayList<>();
    try {
      for (TypeReference exception : walaMethod.getDeclaredExceptions()) {
        String exceptionName = convertClassName(exception.getName().toString());
        if (!view.getSootClass(new DefaultSignatureFactory() {
        }.getClassSignature(exceptionName)).isPresent()) {
          // create exception class if it doesn't exist yet in the view.
          SootClass exceptionClass = new SootClass(view, new DefaultSignatureFactory().getClassSignature(exceptionName));
          view.addSootClass(exceptionClass);
          thrownExceptions.add(exceptionClass);
        }
      }
    } catch (UnsupportedOperationException e) {
      e.printStackTrace();
    } catch (InvalidClassFileException e) {
      e.printStackTrace();
    }
    SootMethod sootMethod = new SootMethod(view, name, paraTypes, returnType, modifier, thrownExceptions);
    sootClass.addMethod(sootMethod);
    // create and set active body of the SootMethod
    Optional<Body> body = createBody(sootMethod, walaMethod);
    if (body.isPresent()) {
      sootMethod.setPhantom(false);
      sootMethod.setActiveBody(body.get());
    } else {
      sootMethod.setPhantom(true);
    }
    // add debug info
    DebuggingInformation debugInfo = walaMethod.debugInfo();
    sootMethod.setDebugInfo(debugInfo);
    return sootMethod;
  }

  public Type convertType(TypeReference type) {
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
        // TODO: FIX THIS
        return ArrayType.getInstance(baseType, dim);
      } else if (type.isClassType()) {
        if (type.equals(TypeReference.Null)) {
          return NullType.getInstance();
        } else {
          String className = convertClassName(type.getName().toString());
          return new RefType(view, className);
        }
      }
    }
    throw new RuntimeException("Unsupported tpye: " + type);
  }

  public EnumSet<Modifier> convertModifiers(AstField field) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    // TODO
    return modifiers;
  }

  public EnumSet<Modifier> convertModifiers(AstMethod method) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    // TODO
    return modifiers;
  }

  public EnumSet<Modifier> converModifiers(AstClass klass) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    // TODO
    return modifiers;
  }

  private Optional<Body> createBody(SootMethod sootMethod, AstMethod walaMethod) {
    AbstractCFG<?, ?> cfg = walaMethod.cfg();
    if (cfg != null) {
      // convert all wala instructions to jimple statements
      SSAInstruction[] insts = (SSAInstruction[]) cfg.getInstructions();
      if (insts.length > 0) {
        Body body = new Body(sootMethod);
        // set position for body
        DebuggingInformation debugInfo = walaMethod.debugInfo();
        Position bodyPos = debugInfo.getCodeBodyPosition();
        body.setPosition(bodyPos);

        /* Look AsmMethodSource.getBody, see AsmMethodSource.emitLocals(); */

        LocalGenerator localGenerator = new LocalGenerator(body);
        if (!sootMethod.isStatic()) {
          RefType thisType = sootMethod.getDeclaringClass().getType();
          Local thisLocal = localGenerator.generateLocal(sootMethod.getDeclaringClass().getType());
          body.addLocal(thisLocal);
          body.addStmt(Jimple.newIdentityStmt(thisLocal, Jimple.newThisRef(thisType)));
        }

        for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
          TypeReference t = walaMethod.getParameterType(i);
          Type type = convertType(t);
          Local paraLocal = localGenerator.generateLocal(type);
          body.addLocal(paraLocal);
          body.addStmt(Jimple.newIdentityStmt(paraLocal, Jimple.newParameterRef(type, i)));
        }

        // TODO 2. convert traps
        // get exceptions which are not caught
        FixedSizeBitVector blocks = cfg.getExceptionalToExit();

        for (SSAInstruction inst : insts) {
          if (inst != null) {
          IStmt stmt = convertInstruction(inst);
          // set position for each statement
          Position stmtPos = debugInfo.getInstructionPosition(inst.iindex);
          stmt.setPosition(stmtPos);
            body.addStmt(stmt);
          }
          else {
            // TODO by converting foo.bar.hello.world.CopyOfLoopsAndLabels, insts contains null element.
          }
        }
        return Optional.of(body);
      }
    }
    return Optional.empty();
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
    return Jimple.newNopStmt();
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
      if (className.contains("(")) {
        sb.append(subNames[0] + "$");
        String last = subNames[subNames.length - 1];
        if (last.contains("$")) {
          String[] numberedName = last.split("\\$");
          sb.append(numberedName[numberedName.length - 1]);
        } else {
          sb.append(last);
        }
      } else {
        for (int i = 0; i < subNames.length; i++) {
          sb.append(subNames[i]);
          if (i != subNames.length - 1) {
            sb.append(".");
          }
        }
      }
    } else {
      throw new RuntimeException("Can not convert WALA class name: " + className);
    }
    return sb.toString();
  }
}
