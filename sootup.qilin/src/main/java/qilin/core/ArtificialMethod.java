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

import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

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
        body.getUnits().add(new JIdentityStmt(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Local getNew(ClassType type) {
        Value newExpr = new JNewExpr(type);
        Local local = getNextLocal(type);
        addAssign(local, newExpr);
        return local;
    }

    protected Immediate getNewArray(ClassType type) {
        Value newExpr = new JNewArrayExpr(type, IntConstant.v(1));
        Local local = getNextLocal(new ArrayType(type, 1));
        addAssign(local, newExpr);
        return local;
    }

    protected Local getNextLocal(Type type) {
        return getLocal(type, localStart++);
    }

    private Local getLocal(Type type, int index) {
        Local local = new JimpleLocal("r" + index, type);
        body.getLocals().add(local);
        return local;
    }

    protected void addReturn(Immediate ret) {
        body.getUnits().add(new JReturnStmt(ret, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Value getStaticFieldRef(String className, String name) {
        return Jimple.v().newStaticFieldRef(RefType.v(className).getSootClass().getFieldByName(name).makeRef());
    }

    protected Value getArrayRef(Value base) {
        return new JArrayRef(base, IntConstant.v(0));
    }

    /**
     * add an instance invocation receiver.sig(args)
     */
    protected void addInvoke(Local receiver, String sig, Immediate ... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Immediate> argsL = Arrays.asList(args);
        AbstractInvokeExpr invoke = methodRef.getDeclaringClass().isInterface() ? new JInterfaceInvokeExpr(receiver, methodRef, argsL)
                : new JVirtualInvokeExpr(receiver, methodRef, argsL);
        body.getUnits().add(new JInvokeStmt(invoke, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    /**
     * add an instance invocation and get the return value rx = receiver.sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(Local receiver, String sig, Immediate... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Immediate> argsL = Arrays.asList(args);
        Value invoke = methodRef.getDeclaringClass().isInterface() ? new JInterfaceInvokeExpr(receiver, methodRef, argsL)
                : new JVirtualInvokeExpr(receiver, methodRef, argsL);
        Value rx = getNextLocal(methodRef.getReturnType());
        addAssign(rx, invoke);
        return rx;
    }

    /**
     * add a static invocation sig(args)
     */
    protected void addInvoke(String sig, Immediate... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Immediate> argsL = Arrays.asList(args);
        body.getUnits().add(new JInvokeStmt(new JStaticInvokeExpr(methodRef, argsL)));
    }

    /**
     * add a static invocation and get the return value rx = sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(String sig, Immediate... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Immediate> argsL = Arrays.asList(args);
        Value rx = getNextLocal(methodRef.getReturnType());
        addAssign(rx, new JStaticInvokeExpr(methodRef, argsL));
        return rx;
    }

    protected void addAssign(Value lValue, Value rValue) {
        body.getUnits().add(new JAssignStmt(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }
}
