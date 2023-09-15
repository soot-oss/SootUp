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

import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.tagkit.LineNumberTag;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This reflection model handles reflection according to the dynamic traces recorded through Tamiflex.
 */
public class TamiflexModel extends ReflectionModel {

    protected Map<ReflectionKind, Map<Stmt, Set<String>>> reflectionMap;

    public TamiflexModel() {
        reflectionMap = DataFactory.createMap();
        parseTamiflexLog(CoreConfig.v().getAppConfig().REFLECTION_LOG, true);
    }

    @Override
    Collection<Unit> transformClassForName(Stmt s) {
        // <java.lang.Class: java.lang.Class forName(java.lang.String)>
        // <java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> classForNames = reflectionMap.getOrDefault(ReflectionKind.ClassForName, Collections.emptyMap());
        if (classForNames.containsKey(s)) {
            Collection<String> fornames = classForNames.get(s);
            for (String clazz : fornames) {
                Type refType = RefType.v(clazz);
                ClassConstant cc = ClassConstant.fromType(refType);
                if (s instanceof AssignStmt) {
                    Value lvalue = ((AssignStmt) s).getLeftOp();
                    ret.add(new JAssignStmt(lvalue, cc));
                }
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformClassNewInstance(Stmt s) {
        // <java.lang.Class: java.lang.Object newInstance()>
        if (!(s instanceof AssignStmt)) {
            return Collections.emptySet();
        }
        Value lvalue = ((AssignStmt) s).getLeftOp();
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> classNewInstances = reflectionMap.getOrDefault(ReflectionKind.ClassNewInstance, Collections.emptyMap());
        if (classNewInstances.containsKey(s)) {
            Collection<String> classNames = classNewInstances.get(s);
            for (String clsName : classNames) {
                SootClass cls = PTAScene.v().getSootClass(clsName);
                if (cls.declaresMethod(PTAScene.v().getSubSigNumberer().findOrAdd("void <init>()"))) {
                    JNewExpr newExpr = new JNewExpr(cls.getType());
                    ret.add(new JAssignStmt(lvalue, newExpr));
                    SootMethod constructor = cls.getMethod(PTAScene.v().getSubSigNumberer().findOrAdd("void <init>()"));
                    ret.add(new JInvokeStmt(new JSpecialInvokeExpr((Local) lvalue, constructor.makeRef(), Collections.emptyList())));
                }
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformContructorNewInstance(Stmt s) {
        // <java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>
        if (!(s instanceof AssignStmt)) {
            return Collections.emptySet();
        }
        Value lvalue = ((AssignStmt) s).getLeftOp();
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> constructorNewInstances = reflectionMap.getOrDefault(ReflectionKind.ConstructorNewInstance, Collections.emptyMap());
        if (constructorNewInstances.containsKey(s)) {
            Collection<String> constructorSignatures = constructorNewInstances.get(s);
            InvokeExpr iie = s.getInvokeExpr();
            Value args = iie.getArg(0);
            ArrayRef arrayRef = new JArrayRef(args, IntConstant.v(0));
            Value arg = new JimpleLocal("intermediate/" + arrayRef, RefType.v("java.lang.Object"));
            ret.add(new JAssignStmt(arg, arrayRef));
            for (String constructorSignature : constructorSignatures) {
                SootMethod constructor = PTAScene.v().getMethod(constructorSignature);
                SootClass cls = constructor.getDeclaringClass();
                JNewExpr newExpr = new JNewExpr(cls.getType());
                ret.add(new JAssignStmt(lvalue, newExpr));
                int argCount = constructor.getParameterCount();
                List<Value> mArgs = new ArrayList<>(argCount);
                for (int i = 0; i < argCount; i++) {
                    mArgs.add(arg);
                }
                ret.add(new JInvokeStmt(new JSpecialInvokeExpr((Local) lvalue, constructor.makeRef(), mArgs)));
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformMethodInvoke(Stmt s) {
        // <java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> methodInvokes = reflectionMap.getOrDefault(ReflectionKind.MethodInvoke, Collections.emptyMap());
        if (methodInvokes.containsKey(s)) {
            Collection<String> methodSignatures = methodInvokes.get(s);
            InvokeExpr iie = s.getInvokeExpr();
            Value base = iie.getArg(0);
            Value args = iie.getArg(1);
            Value arg = null;
            if (args.getType() instanceof ArrayType) {
                ArrayRef arrayRef = new JArrayRef(args, IntConstant.v(0));
                arg = new JimpleLocal("intermediate/" + arrayRef, RefType.v("java.lang.Object"));
                ret.add(new JAssignStmt(arg, arrayRef));
            }

            for (String methodSignature : methodSignatures) {
                SootMethod method = PTAScene.v().getMethod(methodSignature);
                int argCount = method.getParameterCount();
                List<Value> mArgs = new ArrayList<>(argCount);
                for (int i = 0; i < argCount; i++) {
                    mArgs.add(arg);
                }
                InvokeExpr ie;
                if (method.isStatic()) {
                    assert base instanceof NullConstant;
                    ie = new JStaticInvokeExpr(method.makeRef(), mArgs);
                } else {
                    assert !(base instanceof NullConstant);
                    ie = new JVirtualInvokeExpr(base, method.makeRef(), mArgs);
                }
                if (s instanceof AssignStmt) {
                    Value lvalue = ((AssignStmt) s).getLeftOp();
                    ret.add(new JAssignStmt(lvalue, ie));
                } else {
                    ret.add(new JInvokeStmt(ie));
                }
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformFieldSet(Stmt s) {
        // <java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> fieldSets = reflectionMap.getOrDefault(ReflectionKind.FieldSet, Collections.emptyMap());
        if (fieldSets.containsKey(s)) {
            Collection<String> fieldSignatures = fieldSets.get(s);
            InvokeExpr iie = s.getInvokeExpr();
            Value base = iie.getArg(0);
            Value rValue = iie.getArg(1);
            for (String fieldSignature : fieldSignatures) {
                SootField field = PTAScene.v().getField(fieldSignature).makeRef().resolve();
                FieldRef fieldRef;
                if (field.isStatic()) {
                    assert base instanceof NullConstant;
                    fieldRef = Jimple.v().newStaticFieldRef(field.makeRef());
                } else {
                    assert !(base instanceof NullConstant);
                    fieldRef = new JInstanceFieldRef(base, field.makeRef());
                }
                Stmt stmt = new JAssignStmt(fieldRef, rValue);
                ret.add(stmt);
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformFieldGet(Stmt s) {
        // <java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> fieldGets = reflectionMap.getOrDefault(ReflectionKind.FieldGet, Collections.emptyMap());
        if (fieldGets.containsKey(s) && s instanceof AssignStmt) {
            Collection<String> fieldSignatures = fieldGets.get(s);
            Value lvalue = ((AssignStmt) s).getLeftOp();
            InvokeExpr iie = s.getInvokeExpr();
            Value base = iie.getArg(0);
            for (String fieldSignature : fieldSignatures) {
                SootField field = PTAScene.v().getField(fieldSignature).makeRef().resolve();
                FieldRef fieldRef;
                if (field.isStatic()) {
                    assert base instanceof NullConstant;
                    fieldRef = Jimple.v().newStaticFieldRef(field.makeRef());
                } else {
                    assert !(base instanceof NullConstant);
                    fieldRef = new JInstanceFieldRef(base, field.makeRef());
                }
                if (fieldRef.getType() instanceof RefLikeType) {
                    Stmt stmt = new JAssignStmt(lvalue, fieldRef);
                    ret.add(stmt);
                }
            }
        }
        return ret;
    }

    @Override
    protected Collection<Unit> transformArrayNewInstance(Stmt s) {
        // <java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>
        Collection<Unit> ret = DataFactory.createSet();
        Map<Stmt, Set<String>> mappedToArrayTypes = reflectionMap.getOrDefault(ReflectionKind.ArrayNewInstance, Collections.emptyMap());
        Collection<String> arrayTypes = mappedToArrayTypes.getOrDefault(s, Collections.emptySet());
        for (String arrayType : arrayTypes) {
            ArrayType at = (ArrayType) PTAScene.v().getTypeUnsafe(arrayType, true);
            JNewArrayExpr newExpr = new JNewArrayExpr(at.getElementType(), IntConstant.v(1));
            if (s instanceof AssignStmt) {
                Value lvalue = ((AssignStmt) s).getLeftOp();
                ret.add(new JAssignStmt(lvalue, newExpr));
            }
        }
        return ret;
    }

    @Override
    Collection<Unit> transformArrayGet(Stmt s) {
        Collection<Unit> ret = DataFactory.createSet();
        InvokeExpr iie = s.getInvokeExpr();
        Value base = iie.getArg(0);
        if (s instanceof AssignStmt) {
            Value lvalue = ((AssignStmt) s).getLeftOp();
            Value arrayRef = null;
            if (base.getType() instanceof ArrayType) {
                arrayRef = new JArrayRef(base, IntConstant.v(0));
            } else if (base.getType() == RefType.v("java.lang.Object")) {
                Local local = new JimpleLocal("intermediate/" + base, ArrayType.v(RefType.v("java.lang.Object"), 1));
                ret.add(new JAssignStmt(local, base));
                arrayRef = new JArrayRef(local, IntConstant.v(0));
            }
            if (arrayRef != null) {
                ret.add(new JAssignStmt(lvalue, arrayRef));
            }
        }
        return ret;
    }

    @Override
    Collection<Unit> transformArraySet(Stmt s) {
        Collection<Unit> ret = DataFactory.createSet();
        InvokeExpr iie = s.getInvokeExpr();
        Value base = iie.getArg(0);
        if (base.getType() instanceof ArrayType) {
            Value from = iie.getArg(2);
            Value arrayRef = new JArrayRef(base, IntConstant.v(0));
            ret.add(new JAssignStmt(arrayRef, from));
        }
        return ret;
    }


    /*
     * parse reflection log generated by Tamiflex.
     * */
    private void parseTamiflexLog(String logFile, boolean verbose) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
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
                int lineNumber = portions[3].length() == 0 ? -1 : Integer.parseInt(portions[3]);
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
                        if (!PTAScene.v().containsClass(mappedTarget)) {
                            if (verbose) {
                                System.out.println("Warning: Unknown mapped class for signature: " + mappedTarget);
                            }
                            continue;
                        }
                        break;
                    case ConstructorNewInstance:
                    case MethodInvoke:
                        if (!PTAScene.v().containsMethod(mappedTarget)) {
                            if (verbose) {
                                System.out.println("Warning: Unknown mapped method for signature: " + mappedTarget);
                            }
                            continue;
                        }
                        break;
                    case FieldSet:
                    case FieldGet:
                        if (!PTAScene.v().containsField(mappedTarget)) {
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
                    reflectionMap.computeIfAbsent(kind, m -> DataFactory.createMap()).
                            computeIfAbsent(stmt, k -> DataFactory.createSet()).add(mappedTarget);
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
        if (!PTAScene.v().containsClass(inClassStr)) {
            System.out.println("Warning: unknown class \"" + inClassStr + "\" is referenced.");
        }
        SootClass sootClass = PTAScene.v().getSootClass(inClassStr);
        Set<SootMethod> ret = DataFactory.createSet();
        for (SootMethod m : sootClass.getMethods()) {
            if (m.isConcrete() && m.getName().equals(inMethodStr)) {
                ret.add(m);
            }
        }
        return ret;
    }

    private Collection<Stmt> inferSourceStmt(String inClzDotMthd, ReflectionKind kind, int lineNumber) {
        Set<Stmt> ret = DataFactory.createSet();
        Set<Stmt> potential = DataFactory.createSet();
        Collection<SootMethod> sourceMethods = inferSourceMethod(inClzDotMthd);
        for (SootMethod sm : sourceMethods) {
            Body body = PTAUtils.getMethodBody(sm);
            for (Unit u : body.getUnits()) {
                if (u instanceof Stmt stmt) {
                    if (stmt.containsInvokeExpr()) {
                        String methodSig = stmt.getInvokeExpr().getMethodRef().getSignature();
                        if (matchReflectionKind(kind, methodSig)) {
                            potential.add(stmt);
                        }
                    }
                }
            }
        }
        for (Stmt stmt : potential) {
            LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
            if (lineNumber < 0 || tag != null && tag.getLineNumber() == lineNumber) {
                ret.add(stmt);
            }
        }
        if (ret.size() == 0 && potential.size() > 0) {
            System.out.print("Warning: Mismatch between statement and reflection log entry - ");
            System.out.println(kind + ";" + inClzDotMthd + ";" + lineNumber + ";");
            return potential;
        } else {
            return ret;
        }
    }

    private boolean matchReflectionKind(ReflectionKind kind, String methodSig) {
        return switch (kind) {
            case ClassForName -> methodSig.equals(sigForName) || methodSig.equals(sigForName2);
            case ClassNewInstance -> methodSig.equals(sigClassNewInstance);
            case ConstructorNewInstance -> methodSig.equals(sigConstructorNewInstance);
            case MethodInvoke -> methodSig.equals(sigMethodInvoke);
            case FieldSet -> methodSig.equals(sigFieldSet);
            case FieldGet -> methodSig.equals(sigFieldGet);
            case ArrayNewInstance -> methodSig.equals(sigArrayNewInstance);
            default -> false;
        };
    }
}
