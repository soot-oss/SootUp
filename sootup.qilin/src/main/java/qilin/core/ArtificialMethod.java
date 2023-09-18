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

package qilin.core;

import qilin.util.PTAUtils;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;

import java.util.Arrays;
import java.util.List;

public abstract class ArtificialMethod {
    protected SootMethod method;
    protected Body body;
    protected Local thisLocal;
    protected Local[] paraLocals;
    protected int paraStart;
    protected int localStart;

    protected Value getThis() {
        if (thisLocal == null) {
            ClassType type = method.getDeclaringClassType();
            IdentityRef thisRef = new JThisRef(type);
            thisLocal = getLocal(type, 0);
            addIdentity(thisLocal, thisRef);
        }
        return thisLocal;
    }

    protected Value getPara(int index) {
        Value paraLocal = paraLocals[index];
        if (paraLocal == null) {
            Type type = method.getParameterType(index);
            return getPara(index, type);
        }
        return paraLocal;
    }

    protected Value getPara(int index, Type type) {
        Local paraLocal = paraLocals[index];
        if (paraLocal == null) {
            IdentityRef paraRef = new JParameterRef(type, index);
            paraLocal = getLocal(type, paraStart + index);
            addIdentity(paraLocal, paraRef);
            paraLocals[index] = paraLocal;
        }
        return paraLocal;
    }

    private void addIdentity(Local lValue, IdentityRef rValue) {
        body.getUnits().add(new JIdentityStmt<>(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Local getNew(ClassType type) {
        Value newExpr = new JNewExpr(type);
        Local local = getNextLocal(type);
        addAssign(local, newExpr);
        return local;
    }

    protected Immediate getNewArray(ClassType type) {
        Value newExpr = JavaJimple.getInstance().newNewArrayExpr(type, IntConstant.getInstance(1));
        Local local = getNextLocal(new ArrayType(type, 1));
        addAssign(local, newExpr);
        return local;
    }

    protected Local getNextLocal(Type type) {
        return getLocal(type, localStart++);
    }

    private Local getLocal(Type type, int index) {
        Local local = Jimple.newLocal("r" + index, type);
        body.getLocals().add(local);
        return local;
    }

    protected void addReturn(Immediate ret) {
        body.getUnits().add(new JReturnStmt(ret, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Value getStaticFieldRef(String className, String name) {
        ClassType classType = PTAUtils.getClassType(className);
        SootClass sc = (SootClass) PTAScene.v().getView().getClass(classType).get();
        SootField field = (SootField) sc.getField(name).get();
        return Jimple.newStaticFieldRef(field.getSignature());
    }

    protected Value getArrayRef(Value base) {
        return JavaJimple.getInstance().newArrayRef((Local) base, IntConstant.getInstance(0));
    }

    /**
     * add an instance invocation receiver.sig(args)
     */
    protected void addInvoke(Local receiver, String sig, Immediate ... args) {
        MethodSignature methodSig = JavaIdentifierFactory.getInstance().parseMethodSignature(sig);
        SootMethod method = (SootMethod) PTAScene.v().getView().getMethod(methodSig).get();
        SootClass clazz = (SootClass) PTAScene.v().getView().getClass(method.getDeclaringClassType()).get();
        List<Immediate> argsL = Arrays.asList(args);
        AbstractInvokeExpr invoke = clazz.isInterface() ? Jimple.newInterfaceInvokeExpr(receiver, methodSig, argsL)
                : Jimple.newVirtualInvokeExpr(receiver, methodSig, argsL);
        body.getUnits().add(new JInvokeStmt(invoke, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    /**
     * add an instance invocation and get the return value rx = receiver.sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(Local receiver, String sig, Immediate... args) {
        MethodSignature methodSig = JavaIdentifierFactory.getInstance().parseMethodSignature(sig);
        SootMethod method = (SootMethod) PTAScene.v().getView().getMethod(methodSig).get();
        SootClass clazz = (SootClass) PTAScene.v().getView().getClass(method.getDeclaringClassType()).get();
        List<Immediate> argsL = Arrays.asList(args);
        Value invoke = clazz.isInterface() ? Jimple.newInterfaceInvokeExpr(receiver, methodSig, argsL)
                : Jimple.newVirtualInvokeExpr(receiver, methodSig, argsL);
        Value rx = getNextLocal(method.getReturnType());
        addAssign(rx, invoke);
        return rx;
    }

    /**
     * add a static invocation sig(args)
     */
    protected void addInvoke(String sig, Immediate... args) {
        MethodSignature methodSig = JavaIdentifierFactory.getInstance().parseMethodSignature(sig);
        List<Immediate> argsL = Arrays.asList(args);
        body.getUnits().add(new JInvokeStmt(Jimple.newStaticInvokeExpr(methodSig, argsL), StmtPositionInfo.createNoStmtPositionInfo()));
    }

    /**
     * add a static invocation and get the return value rx = sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(String sig, Immediate... args) {
        MethodSignature methodSig = JavaIdentifierFactory.getInstance().parseMethodSignature(sig);
        List<Immediate> argsL = Arrays.asList(args);
        Value rx = getNextLocal(methodSig.getType());
        addAssign(rx, Jimple.newStaticInvokeExpr(methodSig, argsL));
        return rx;
    }

    protected void addAssign(Value lValue, Value rValue) {
        body.getUnits().add(new JAssignStmt<>(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }
}
