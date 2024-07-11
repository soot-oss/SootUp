/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.reflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.ReferenceType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;

/**
 * This reflection model handles reflection according to the dynamic traces recorded through
 * Tamiflex.
 */
public class TamiflexModel extends ReflectionModel {
  final ClassType jlobjClassType;
  protected Map<ReflectionKind, Map<Stmt, Set<String>>> reflectionMap;

  public TamiflexModel(PTAScene ptaScene) {
    super(ptaScene);
    jlobjClassType = PTAUtils.getClassType("java.lang.Object");
    this.reflectionMap = DataFactory.createMap();
    parseTamiflexLog(CoreConfig.v().getAppConfig().REFLECTION_LOG, false);
  }

  @Override
  Collection<Stmt> transformClassForName(Stmt s) {
    // <java.lang.Class: java.lang.Class forName(java.lang.String)>
    // <java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>
    Map<Stmt, Set<String>> classForNames =
        reflectionMap.getOrDefault(ReflectionKind.ClassForName, Collections.emptyMap());
    Collection<String> fornames = classForNames.get(s);
    if (fornames == null) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    for (String clazz : fornames) {
      // !TODO potential bug
      if (s instanceof JAssignStmt) {
        LValue lvalue = ((JAssignStmt) s).getLeftOp();
        ClassConstant cc = JavaJimple.getInstance().newClassConstant(dot2slashStyle(clazz));
        ret.add(new JAssignStmt(lvalue, cc, StmtPositionInfo.getNoStmtPositionInfo()));
      }
    }
    return ret;
  }

  public static String dot2slashStyle(String clazz) {
    String x = clazz.replace('.', '/');
    return "L" + x + ";";
  }

  @Override
  protected Collection<Stmt> transformClassNewInstance(Stmt stmt) {
    // <java.lang.Class: java.lang.Object newInstance()>
    if (!(stmt instanceof JAssignStmt)) {
      return Collections.emptySet();
    }
    LValue lvalue = ((JAssignStmt) stmt).getLeftOp();
    Map<Stmt, Set<String>> classNewInstances =
        reflectionMap.getOrDefault(ReflectionKind.ClassNewInstance, Collections.emptyMap());
    Collection<String> classNames = classNewInstances.get(stmt);
    if (classNames == null) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    for (String clsName : classNames) {
      SootClass cls = ptaScene.getSootClass(clsName);
      MethodSubSignature initSubSig =
          JavaIdentifierFactory.getInstance().parseMethodSubSignature("void <init>()");
      Optional<? extends SootMethod> omthd = cls.getMethod(initSubSig);
      if (omthd.isPresent()) {
        JNewExpr newExpr = new JNewExpr(cls.getType());
        ret.add(new JAssignStmt(lvalue, newExpr, StmtPositionInfo.getNoStmtPositionInfo()));
        SootMethod constructor = omthd.get();
        ret.add(
            new JInvokeStmt(
                new JSpecialInvokeExpr(
                    (Local) lvalue, constructor.getSignature(), Collections.emptyList()),
                StmtPositionInfo.getNoStmtPositionInfo()));
      }
    }
    return ret;
  }

  @Override
  protected Collection<Stmt> transformContructorNewInstance(Stmt stmt) {
    // <java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>
    if (!(stmt instanceof JAssignStmt)) {
      return Collections.emptySet();
    }

    LValue lvalue = ((JAssignStmt) stmt).getLeftOp();
    Map<Stmt, Set<String>> constructorNewInstances =
        reflectionMap.getOrDefault(ReflectionKind.ConstructorNewInstance, Collections.emptyMap());
    Collection<String> constructorSignatures = constructorNewInstances.get(stmt);
    if (constructorSignatures == null) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
    Value args = invokeExpr.getArg(0);
    JArrayRef arrayRef =
        JavaJimple.getInstance().newArrayRef((Local) args, IntConstant.getInstance(0));

    Local arg = Jimple.newLocal("intermediate/" + arrayRef, jlobjClassType);
    ret.add(new JAssignStmt(arg, arrayRef, StmtPositionInfo.getNoStmtPositionInfo()));
    for (String constructorSignature : constructorSignatures) {
      SootMethod constructor = ptaScene.getMethod(constructorSignature);
      JNewExpr newExpr = new JNewExpr(constructor.getDeclaringClassType());
      ret.add(new JAssignStmt(lvalue, newExpr, StmtPositionInfo.getNoStmtPositionInfo()));
      int argCount = constructor.getParameterCount();
      List<Immediate> mArgs = new ArrayList<>(argCount);
      for (int i = 0; i < argCount; i++) {
        mArgs.add(arg);
      }
      ret.add(
          new JInvokeStmt(
              new JSpecialInvokeExpr((Local) lvalue, constructor.getSignature(), mArgs),
              StmtPositionInfo.getNoStmtPositionInfo()));
    }
    return ret;
  }

  @Override
  protected Collection<Stmt> transformMethodInvoke(Stmt s) {
    // <java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>
    Map<Stmt, Set<String>> methodInvokes =
        reflectionMap.getOrDefault(ReflectionKind.MethodInvoke, Collections.emptyMap());
    Collection<String> methodSignatures = methodInvokes.get(s);
    if (methodSignatures == null) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    AbstractInvokeExpr iie = s.getInvokeExpr();
    Value base = iie.getArg(0);
    Value args = iie.getArg(1);
    Local arg = null;
    if (args.getType() instanceof ArrayType) {
      JArrayRef arrayRef =
          JavaJimple.getInstance().newArrayRef((Local) args, IntConstant.getInstance(0));
      arg = Jimple.newLocal("intermediate/" + arrayRef, jlobjClassType);
      ret.add(new JAssignStmt(arg, arrayRef, StmtPositionInfo.getNoStmtPositionInfo()));
    }

    for (String methodSignature : methodSignatures) {
      SootMethod method = ptaScene.getMethod(methodSignature);
      int argCount = method.getParameterCount();
      List<Immediate> mArgs = new ArrayList<>(argCount);
      for (int i = 0; i < argCount; i++) {
        if (arg != null) {
          mArgs.add(arg);
        } else {
          mArgs.add(NullConstant.getInstance());
        }
      }
      AbstractInvokeExpr ie;
      if (method.isStatic()) {
        assert base instanceof NullConstant;
        ie = new JStaticInvokeExpr(method.getSignature(), mArgs);
      } else {
        assert !(base instanceof NullConstant);
        ie = new JVirtualInvokeExpr((Local) base, method.getSignature(), mArgs);
      }
      if (s instanceof JAssignStmt) {
        LValue lvalue = ((JAssignStmt) s).getLeftOp();
        ret.add(new JAssignStmt(lvalue, ie, StmtPositionInfo.getNoStmtPositionInfo()));
      } else {
        ret.add(new JInvokeStmt(ie, StmtPositionInfo.getNoStmtPositionInfo()));
      }
    }
    return ret;
  }

  @Override
  protected Collection<Stmt> transformFieldSet(Stmt s) {
    // <java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>
    Map<Stmt, Set<String>> fieldSets =
        reflectionMap.getOrDefault(ReflectionKind.FieldSet, Collections.emptyMap());
    Collection<String> fieldSignatures = fieldSets.get(s);
    if (fieldSignatures == null) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    AbstractInvokeExpr iie = s.getInvokeExpr();
    Value base = iie.getArg(0);
    Value rValue = iie.getArg(1);
    for (String fieldSignature : fieldSignatures) {
      FieldSignature fieldSig =
          JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
      SootField field = ptaScene.getView().getField(fieldSig).get();
      JFieldRef fieldRef;
      if (field.isStatic()) {
        assert base instanceof NullConstant;
        fieldRef = Jimple.newStaticFieldRef(field.getSignature());
      } else {
        assert !(base instanceof NullConstant);
        fieldRef = Jimple.newInstanceFieldRef((Local) base, field.getSignature());
      }
      Stmt stmt = new JAssignStmt(fieldRef, rValue, StmtPositionInfo.getNoStmtPositionInfo());
      ret.add(stmt);
    }
    return ret;
  }

  @Override
  protected Collection<Stmt> transformFieldGet(Stmt s) {
    // <java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>
    Map<Stmt, Set<String>> fieldGets =
        reflectionMap.getOrDefault(ReflectionKind.FieldGet, Collections.emptyMap());
    Collection<String> fieldSignatures = fieldGets.get(s);
    if (fieldSignatures == null || !(s instanceof JAssignStmt)) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    LValue lvalue = ((JAssignStmt) s).getLeftOp();
    AbstractInvokeExpr iie = s.getInvokeExpr();
    Value base = iie.getArg(0);
    for (String fieldSignature : fieldSignatures) {
      FieldSignature fieldSig =
          JavaIdentifierFactory.getInstance().parseFieldSignature(fieldSignature);
      SootField field = ptaScene.getView().getField(fieldSig).get();
      JFieldRef fieldRef;
      if (field.isStatic()) {
        assert base instanceof NullConstant;
        fieldRef = Jimple.newStaticFieldRef(field.getSignature());
      } else {
        assert !(base instanceof NullConstant);
        fieldRef = Jimple.newInstanceFieldRef((Local) base, field.getSignature());
      }
      if (fieldRef.getType() instanceof ReferenceType) {
        Stmt stmt = new JAssignStmt(lvalue, fieldRef, StmtPositionInfo.getNoStmtPositionInfo());
        ret.add(stmt);
      }
    }
    return ret;
  }

  @Override
  protected Collection<Stmt> transformArrayNewInstance(Stmt s) {
    // <java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>
    Map<Stmt, Set<String>> mappedToArrayTypes =
        reflectionMap.getOrDefault(ReflectionKind.ArrayNewInstance, Collections.emptyMap());
    Collection<String> arrayTypes = mappedToArrayTypes.get(s);
    if (arrayTypes == null) {
      return Collections.emptySet();
    }

    Collection<Stmt> ret = DataFactory.createSet();
    for (String arrayType : arrayTypes) {
      ArrayType at = (ArrayType) JavaIdentifierFactory.getInstance().getType(arrayType);
      JNewArrayExpr newExpr =
          JavaJimple.getInstance().newNewArrayExpr(at.getElementType(), IntConstant.getInstance(1));
      if (s instanceof JAssignStmt) {
        LValue lvalue = ((JAssignStmt) s).getLeftOp();
        ret.add(new JAssignStmt(lvalue, newExpr, StmtPositionInfo.getNoStmtPositionInfo()));
      }
    }
    return ret;
  }

  @Override
  Collection<Stmt> transformArrayGet(Stmt s) {
    Value base = s.getInvokeExpr().getArg(0);
    if (!(s instanceof JAssignStmt)) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    LValue lvalue = ((JAssignStmt) s).getLeftOp();
    Value arrayRef = null;
    if (base.getType() instanceof ArrayType) {
      arrayRef = JavaJimple.getInstance().newArrayRef((Local) base, IntConstant.getInstance(0));
    } else {
      if (base.getType() == jlobjClassType) {
        Local local = Jimple.newLocal("intermediate/" + base, new ArrayType(jlobjClassType, 1));
        ret.add(new JAssignStmt(local, base, StmtPositionInfo.getNoStmtPositionInfo()));
        arrayRef = JavaJimple.getInstance().newArrayRef(local, IntConstant.getInstance(0));
      }
    }
    if (arrayRef != null) {
      ret.add(new JAssignStmt(lvalue, arrayRef, StmtPositionInfo.getNoStmtPositionInfo()));
    }
    return ret;
  }

  @Override
  Collection<Stmt> transformArraySet(Stmt stmt) {
    AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
    Value base = invokeExpr.getArg(0);
    if (!(base.getType() instanceof ArrayType)) {
      return Collections.emptySet();
    }
    Collection<Stmt> ret = DataFactory.createSet();
    Value from = invokeExpr.getArg(2);
    JArrayRef arrayRef =
        JavaJimple.getInstance().newArrayRef((Local) base, IntConstant.getInstance(0));
    ret.add(new JAssignStmt(arrayRef, from, StmtPositionInfo.getNoStmtPositionInfo()));
    return ret;
  }

  /*
   * parse reflection log generated by Tamiflex.
   * */
  private void parseTamiflexLog(String logFile, boolean verbose) {
    try {
      BufferedReader reader = Files.newBufferedReader(Paths.get(logFile));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] portions = line.split(";", -1);
        if (portions.length < 4) {
          if (verbose) {
            System.out.println("Warning: illegal tamiflex log: " + line);
          }
          continue;
        }
        ReflectionKind kind = ReflectionKind.parse(portions[0]);
        String mappedTarget = portions[1];
        String inClzDotMthdStr = portions[2];
        int lineNumber = portions[3].isEmpty() ? -1 : Integer.parseInt(portions[3]);
        // sanity check.
        if (kind == null) {
          if (verbose) {
            System.out.println("Warning: illegal tamiflex reflection kind: " + portions[0]);
          }
          continue;
        }
        switch (kind) {
          case ClassForName:
            break;
          case ClassNewInstance:
            if (!ptaScene.containsClass(mappedTarget)) {
              if (verbose) {
                System.out.println("Warning: Unknown mapped class for signature: " + mappedTarget);
              }
              continue;
            }
            break;
          case ConstructorNewInstance:
          case MethodInvoke:
            if (!ptaScene.containsMethod(mappedTarget)) {
              if (verbose) {
                System.out.println("Warning: Unknown mapped method for signature: " + mappedTarget);
              }
              continue;
            }
            break;
          case FieldSet:
          case FieldGet:
            if (!ptaScene.containsField(mappedTarget)) {
              if (verbose) {
                System.out.println("Warning: Unknown mapped field for signature: " + mappedTarget);
              }
              continue;
            }
            break;
          case ArrayNewInstance:
            break;
          default:
            if (verbose) {
              System.out.println("Warning: Unsupported reflection kind: " + kind);
            }
            break;
        }
        Collection<Stmt> possibleSourceStmts = inferSourceStmt(inClzDotMthdStr, kind, lineNumber);
        for (Stmt stmt : possibleSourceStmts) {
          reflectionMap
              .computeIfAbsent(kind, m -> DataFactory.createMap())
              .computeIfAbsent(stmt, k -> DataFactory.createSet())
              .add(mappedTarget);
        }
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Collection<SootMethod> inferSourceMethod(String inClzDotMthd) {
    String inClassStr = inClzDotMthd.substring(0, inClzDotMthd.lastIndexOf("."));
    String inMethodStr = inClzDotMthd.substring(inClzDotMthd.lastIndexOf(".") + 1);
    if (!ptaScene.containsClass(inClassStr)) {
      System.out.println("Warning: unknown class \"" + inClassStr + "\" is referenced.");
      return Collections.emptySet();
    }
    SootClass sootClass = ptaScene.getSootClass(inClassStr);
    Set<SootMethod> ret = DataFactory.createSet();
    Set<? extends SootMethod> declMethods = sootClass.getMethods();
    for (SootMethod m : declMethods) {
      if (m.isConcrete() && m.getName().equals(inMethodStr)) {
        ret.add(m);
      }
    }
    return ret;
  }

  private Collection<Stmt> inferSourceStmt(
      String inClzDotMthd, ReflectionKind kind, int lineNumber) {
    Set<Stmt> ret = DataFactory.createSet();
    Set<Stmt> potential = DataFactory.createSet();
    Collection<SootMethod> sourceMethods = inferSourceMethod(inClzDotMthd);
    for (SootMethod sm : sourceMethods) {
      Body body = PTAUtils.getMethodBody(sm);
      for (Stmt stmt : body.getStmts()) {
        if (!stmt.containsInvokeExpr()) {
          continue;
        }
        if (matchReflectionKind(kind, stmt.getInvokeExpr().getMethodSignature().toString())) {
          potential.add(stmt);
        }
      }
    }
    for (Stmt stmt : potential) {
      int firstLine = stmt.getPositionInfo().getStmtPosition().getFirstLine();
      int lastLine = stmt.getPositionInfo().getStmtPosition().getLastLine();
      // !TODO potential bug here
      if (lineNumber < 0 || firstLine <= lineNumber && lineNumber <= lastLine) {
        ret.add(stmt);
      }
    }
    if (ret.isEmpty() && !potential.isEmpty()) {
      System.out.print("Warning: Mismatch between statement and reflection log entry - ");
      System.out.println(kind + ";" + inClzDotMthd + ";" + lineNumber + ";");
      return potential;
    } else {
      return ret;
    }
  }

  private boolean matchReflectionKind(ReflectionKind kind, String methodSig) {
    switch (kind) {
      case ClassForName:
        return methodSig.equals(sigForName) || methodSig.equals(sigForName2);
      case ClassNewInstance:
        return methodSig.equals(sigClassNewInstance);
      case ConstructorNewInstance:
        return methodSig.equals(sigConstructorNewInstance);
      case MethodInvoke:
        return methodSig.equals(sigMethodInvoke);
      case FieldSet:
        return methodSig.equals(sigFieldSet);
      case FieldGet:
        return methodSig.equals(sigFieldGet);
      case ArrayNewInstance:
        return methodSig.equals(sigArrayNewInstance);
      default:
        return false;
    }
  }
}
