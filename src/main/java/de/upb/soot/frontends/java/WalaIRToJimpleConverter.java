/*
 * @author Linghui Luo
 * @version 1.0
 */
package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.Trap;
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
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.views.JavaView;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.loader.AstClass;
import com.ibm.wala.cast.loader.AstField;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.FixedSizeBitVector;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Converter which converts WALA IR to jimple.
 * 
 * @author Linghui Luo created on 17.09.18
 *
 */
public class WalaIRToJimpleConverter {
  protected JavaView view;
  private INamespace srcNamespace;
  private HashMap<String, Integer> clsWithInnerCls;
  private HashMap<String, String> walaToSootNameTable;
  private Set<SootField> sootFields;

  public WalaIRToJimpleConverter(String sourceDirPath) {
    srcNamespace = new JavaSourcePathNamespace(sourceDirPath);
    view = new JavaView(null);
    clsWithInnerCls = new HashMap<>();
    walaToSootNameTable = new HashMap<>();
  }

  /**
   * Convert a wala {@link AstClass} to {@link SootClass}.
   * 
   * @param walaClass
   * @return A SootClass converted from walaClass
   */
  public SootClass convertClass(AstClass walaClass) {
    AbstractClassSource classSource = createClassSource(walaClass);
    JavaClassSignature classSig = classSource.getClassSignature();
    // get super class
    IClass sc = walaClass.getSuperclass();
    JavaClassSignature superClass = null;
    if (sc != null) {
      superClass = view.getSignatureFacotry().getClassSignature(convertClassNameFromWala(sc.getName().toString()));
    }

    // get interfaces
    Set<JavaClassSignature> interfaces = new HashSet<>();
    for (IClass i : walaClass.getDirectInterfaces()) {
      JavaClassSignature inter
          = view.getSignatureFacotry().getClassSignature(convertClassNameFromWala(i.getName().toString()));
      interfaces.add(inter);
    }

    // get outer class
    JavaClassSignature outerClass = null;
    if (walaClass instanceof JavaClass) {
      JavaClass javaClass = (JavaClass) walaClass;
      IClass ec = javaClass.getEnclosingClass();
      if (ec != null) {
        outerClass = view.getSignatureFacotry().getClassSignature(convertClassNameFromWala(ec.getName().toString()));
      }
    }

    // add source position
    Position position = walaClass.getSourcePosition();

    // convert modifiers
    EnumSet<Modifier> modifiers = converModifiers(walaClass);

    // convert fields
    Set<IField> fields = HashSetFactory.make(walaClass.getDeclaredInstanceFields());
    fields.addAll(walaClass.getDeclaredStaticFields());
    sootFields = new HashSet<>();
    for (IField walaField : fields) {
      SootField sootField = convertField(classSig, (AstField) walaField);
      sootFields.add(sootField);
    }

    if (outerClass != null) {
      // create enclosing reference to outerClass
      Type type = view.getType(outerClass);
      FieldSignature signature = view.getSignatureFacotry().getFieldSignature("this$0", classSig, type.toString());
      SootField enclosingObject = new SootField(view, classSig, signature,
          view.getSignatureFacotry().getTypeSignature(type.toString()), EnumSet.of(Modifier.FINAL));
      sootFields.add(enclosingObject);
    }

    // convert methods
    Set<SootMethod> sootMethods = new HashSet<>();
    new SootClass(view, ResolvingLevel.SIGNATURES, classSource, ClassType.Application, Optional.ofNullable(superClass),
        interfaces, Optional.ofNullable(outerClass), sootFields, sootMethods, position, modifiers);

    for (IMethod walaMethod : walaClass.getDeclaredMethods()) {
      SootMethod sootMethod = convertMethod(classSig, (AstMethod) walaMethod);
      sootMethods.add(sootMethod);
    }

    SootClass ret
        = new SootClass(view, ResolvingLevel.BODIES, classSource, ClassType.Application, Optional.ofNullable(superClass),
            interfaces, Optional.ofNullable(outerClass), sootFields, sootMethods, position, modifiers);
    return ret;
  }

  /**
   * Create a {@link JavaClassSource} object for the given walaClass.
   * 
   * @param walaClass
   * @return
   */
  public AbstractClassSource createClassSource(AstClass walaClass) {
    String fullyQualifiedClassName = convertClassNameFromWala(walaClass.getName().toString());
    JavaClassSignature classSignature = new DefaultSignatureFactory() {
    }.getClassSignature(fullyQualifiedClassName);
    URL url = walaClass.getSourceURL();
    Path sourcePath = Paths.get(url.getPath());
    return new JavaClassSource(srcNamespace, sourcePath, classSignature);
  }

  /**
   * Convert a wala {@link AstField} to {@link SootField}.
   *
   * @param klass
   *          the class owns the field
   * @param walaField
   *          the wala field
   * @return A SootField object converted from walaField.
   */
  public SootField convertField(JavaClassSignature classSig, AstField walaField) {
    Type type = convertType(walaField.getFieldTypeReference());
    EnumSet<Modifier> modifiers = convertModifiers(walaField);
    FieldSignature signature
        = view.getSignatureFacotry().getFieldSignature(walaField.getName().toString(), classSig, type.toString());
    SootField sootField
        = new SootField(view, classSig, signature, view.getSignatureFacotry().getTypeSignature(type.toString()), modifiers);
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
  public SootMethod convertMethod(JavaClassSignature classSig, AstMethod walaMethod) {
    // create SootMethond instance
    List<TypeSignature> paraTypes = new ArrayList<>();
    List<String> sigs = new ArrayList<>();
    if (walaMethod.symbolTable() != null) {
      for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
        TypeReference type = walaMethod.getParameterType(i);
        if (!type.equals(walaMethod.getDeclaringClass().getReference())) {
          Type paraType = convertType(type);
          paraTypes.add(this.view.getSignatureFacotry().getTypeSignature(paraType.toString()));
          sigs.add(paraType.toString());
        }
      }
    }
    Type returnType = convertType(walaMethod.getReturnType());

    EnumSet<Modifier> modifiers = convertModifiers(walaMethod);

    List<JavaClassSignature> thrownExceptions = new ArrayList<>();
    try {
      for (TypeReference exception : walaMethod.getDeclaredExceptions()) {
        String exceptionName = convertClassNameFromWala(exception.getName().toString());
        JavaClassSignature exceptionSig = this.view.getSignatureFacotry().getClassSignature(exceptionName);
        thrownExceptions.add(exceptionSig);
      }
    } catch (UnsupportedOperationException e) {
      e.printStackTrace();
    } catch (InvalidClassFileException e) {
      e.printStackTrace();
    }
    // add debug info
    DebuggingInformation debugInfo = walaMethod.debugInfo();
    MethodSignature methodSig = this.view.getSignatureFacotry().getMethodSignature(walaMethod.getName().toString(), classSig,
        returnType.toString(), sigs);
    WalaIRMethodSource methodSource = new WalaIRMethodSource(methodSig);
    SootMethod sootMethod = new SootMethod(view, classSig, methodSource, paraTypes,
        this.view.getSignatureFacotry().getTypeSignature(returnType.toString()), modifiers, thrownExceptions, debugInfo);
    // create and set active body of the SootMethod
    if (!walaMethod.isAbstract()) {
      Optional<Body> body = createBody(sootMethod, walaMethod);
      if (body.isPresent()) {
        Body b = body.get();
        sootMethod = new SootMethod(sootMethod, b);
      }
    }
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
        TypeReference t = type.getInnermostElementType();
        Type baseType = convertType(t);
        int dim = type.getDimensionality();
        return ArrayType.getInstance(baseType, dim);
      } else if (type.isClassType()) {
        if (type.equals(TypeReference.Null)) {
          return NullType.getInstance();
        } else {
          String className = convertClassNameFromWala(type.getName().toString());
          return view.getRefType(this.view.getSignatureFacotry().getClassSignature(className));
        }
      }
    }
    throw new RuntimeException("Unsupported tpye: " + type);
  }

  /**
   * Return all modifiers for the given field.
   * 
   * @param field
   * @return
   */
  public EnumSet<Modifier> convertModifiers(AstField field) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    if (field.isFinal()) {
      modifiers.add(Modifier.FINAL);
    }
    if (field.isPrivate()) {
      modifiers.add(Modifier.PRIVATE);
    }
    if (field.isProtected()) {
      modifiers.add(Modifier.PROTECTED);
    }
    if (field.isPublic()) {
      modifiers.add(Modifier.PUBLIC);
    }
    if (field.isStatic()) {
      modifiers.add(Modifier.STATIC);
    }
    if (field.isVolatile()) {
      modifiers.add(Modifier.VOLATILE);
    }
    // TODO: TRANSIENT field
    return modifiers;
  }

  /**
   * Return all modifiers for the given method.
   * 
   * @param method
   * @return
   */
  public EnumSet<Modifier> convertModifiers(AstMethod method) {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    if (method.isPrivate()) {
      modifiers.add(Modifier.PRIVATE);
    }
    if (method.isProtected()) {
      modifiers.add(Modifier.PROTECTED);
    }
    if (method.isPublic()) {
      modifiers.add(Modifier.PUBLIC);
    }
    if (method.isStatic()) {
      modifiers.add(Modifier.STATIC);
    }
    if (method.isFinal()) {
      modifiers.add(Modifier.FINAL);
    }
    if (method.isAbstract()) {
      modifiers.add(Modifier.ABSTRACT);
    }
    if (method.isSynchronized()) {
      modifiers.add(Modifier.SYNCHRONIZED);
    }
    if (method.isNative()) {
      modifiers.add(Modifier.NATIVE);
    }
    if (method.isSynthetic()) {
      modifiers.add(Modifier.SYNTHETIC);
    }
    if (method.isBridge()) {
      // TODO: what is this?
    }
    if (method.isInit()) {
      // TODO:
    }
    if (method.isClinit()) {
      // TODO:
    }
    // TODO: strictfp and annotation
    return modifiers;
  }

  public EnumSet<Modifier> converModifiers(AstClass klass) {
    int modif = klass.getModifiers();
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    if (klass.isAbstract()) {
      modifiers.add(Modifier.ABSTRACT);
    }
    if (klass.isPrivate()) {
      modifiers.add(Modifier.PRIVATE);
    }
    if (klass.isSynthetic()) {
      modifiers.add(Modifier.SYNTHETIC);
    }
    if (klass.isPublic()) {
      modifiers.add(Modifier.PUBLIC);
    }
    if (klass.isInterface()) {
      modifiers.add(Modifier.INTERFACE);
    }

    // TODO: final, enum, annotation
    return modifiers;
  }

  private Optional<Body> createBody(SootMethod sootMethod, AstMethod walaMethod) {

    AbstractCFG<?, ?> cfg = walaMethod.cfg();
    if (cfg != null) {
      List<Trap> traps = new ArrayList<>();
      List<IStmt> stmts = new ArrayList<>();
      LocalGenerator localGenerator = new LocalGenerator();
      // convert all wala instructions to jimple statements
      SSAInstruction[] insts = (SSAInstruction[]) cfg.getInstructions();
      if (insts.length > 0) {

        // set position for body
        DebuggingInformation debugInfo = walaMethod.debugInfo();
        Position bodyPos = debugInfo.getCodeBodyPosition();

        /* Look AsmMethodSource.getBody, see AsmMethodSource.emitLocals(); */

        if (!sootMethod.isStatic()) {
          RefType thisType = view.getRefType(sootMethod.getDeclaringClassSignature());
          Local thisLocal = localGenerator.generateThisLocal(thisType);
          stmts.add(Jimple.newIdentityStmt(thisLocal, Jimple.newThisRef(thisType)));
        }

        int startPara = 0;
        if (!walaMethod.isStatic()) {
          // wala's first parameter is this reference for non-static method
          startPara = 1;
        }
        for (; startPara < walaMethod.getNumberOfParameters(); startPara++) {
          TypeReference t = walaMethod.getParameterType(startPara);
          Type type = convertType(t);
          Local paraLocal = localGenerator.generateParameterLocal(type, startPara);
          stmts.add(Jimple.newIdentityStmt(paraLocal, Jimple.newParameterRef(type, startPara)));
        }

        // TODO 2. convert traps
        // get exceptions which are not caught
        FixedSizeBitVector blocks = cfg.getExceptionalToExit();

        InstructionConverter instConverter = new InstructionConverter(this, sootMethod, walaMethod, localGenerator);
        Map<IStmt, Integer> stmt2IIndex = new HashMap<>();
        for (SSAInstruction inst : insts) {
          List<IStmt> retStmts = instConverter.convertInstruction(inst);
          if (!retStmts.isEmpty()) {
            for (IStmt stmt : retStmts) {
              // set position for each statement
              Position stmtPos = debugInfo.getInstructionPosition(inst.iindex);
              stmt.setPosition(stmtPos);
              stmts.add(stmt);
              stmt2IIndex.put(stmt, inst.iindex);
            }
          }
        }
        // set target for goto or conditional statements
        for (IStmt stmt : stmt2IIndex.keySet()) {
          instConverter.setTarget(stmt, stmt2IIndex.get(stmt));
        }
        // add return void stmt for methods with return type beiing void
        if (walaMethod.getReturnType().equals(TypeReference.Void)) {
          IStmt ret = Jimple.newReturnVoidStmt();
          instConverter.setTarget(ret, -1);
          stmts.add(ret);
        }
        Body body = new Body(sootMethod, localGenerator.getLocals(), traps, stmts, bodyPos);
        return Optional.of(body);
      }
    }
    return Optional.empty();
  }

  /**
   * Convert className in wala-format to soot-format, e.g., wala-format: Ljava/lang/String -> soot-format: java.lang.String.
   * 
   * @param className
   *          in wala-format
   * @return className in soot.format
   */
  public String convertClassNameFromWala(String className) {
    String cl = className.intern();
    if (walaToSootNameTable.containsKey(cl)) {
      return walaToSootNameTable.get(cl);
    }
    StringBuilder sb = new StringBuilder();
    if (className.startsWith("L")) {
      className = className.substring(1);
      String[] subNames = className.split("/");
      boolean isSpecial = false;
      for (int i = 0; i < subNames.length; i++) {
        String subName = subNames[i];
        if (subName.contains("(") || subName.contains("<")) {
          // handle anonymous or inner classes
          isSpecial = true;
          break;
        }
        if (i != 0) {
          sb.append(".");
        }
        sb.append(subName);
      }
      if (isSpecial) {
        String lastSubName = subNames[subNames.length - 1];
        String[] temp = lastSubName.split(">");
        if (temp.length > 0) {
          String name = temp[temp.length - 1];
          if (!name.contains("$")) {
            // This is an inner class
            String outClass = sb.toString();
            int count = 1;
            if (this.clsWithInnerCls.containsKey(outClass)) {
              count = this.clsWithInnerCls.get(outClass.toString()) + 1;
            }
            this.clsWithInnerCls.put(outClass, count);
            sb.append(count + "$");
          }
          sb.append(name);
        }
      }
    } else {
      throw new RuntimeException("Can not convert WALA class name: " + className);
    }
    String ret = sb.toString();
    walaToSootNameTable.put(cl, ret);
    return ret;
  }

  /**
   * Convert className in soot-format to wala-format, e.g.,soot-format: java.lang.String.-> wala-format: Ljava/lang/String
   * 
   * @param signature
   * @return
   */
  public String convertClassNameFromSoot(String signature) {
    StringBuilder sb = new StringBuilder();
    sb.append("L");
    String[] subNames = signature.split("\\.");
    for (int i = 0; i < subNames.length; i++) {
      sb.append(subNames[i]);
      if (i != subNames.length - 1) {
        sb.append("/");
      }
    }
    return sb.toString();
  }

  protected void addSootField(SootField field) {
    if (this.sootFields != null) {
      this.sootFields.add(field);
    }
  }
}
