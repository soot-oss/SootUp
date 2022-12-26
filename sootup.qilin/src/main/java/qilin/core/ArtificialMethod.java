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

import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

import java.util.Arrays;
import java.util.List;

public abstract class ArtificialMethod {

    protected IdentifierFactory identifierFactory;
    protected SootMethod method;
    protected Body body;
    protected Value thisLocal;
    protected Local[] paraLocals;
    protected int paraStart;
    protected int localStart;

    protected Value getThis() {
        if (thisLocal == null) {
            ClassType type = method.getDeclaringClassType();
            Value thisRef = new JThisRef(type);
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
        body.getUnits().add(Jimple.newIdentityStmt(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Value getNew(ClassType type) {
        Value newExpr = new JNewExpr(type);
        Value local = getNextLocal(type);
        addAssign(local, newExpr);
        return local;
    }

    protected Value getNewArray(ReferenceType type) {
        Value newExpr = new JNewArrayExpr(type, IntConstant.getInstance(1), identifierFactory);
        Value local = getNextLocal(identifierFactory.getArrayType(type, 1));
        addAssign(local, newExpr);
        return local;
    }

    protected Value getNextLocal(Type type) {
        return getLocal(type, localStart++);
    }

    private Local getLocal(Type type, int index) {
        Local local = Jimple.newLocal("r" + index, type);
        body.getLocals().add(local);
        return local;
    }

    protected void addReturn(Immediate ret) {
        body.getUnits().add(Jimple.newReturnStmt(ret, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    protected Value getStaticFieldRef(String className, String name) {
        return Jimple.newStaticFieldRef(RefType.v(className).getSootClass().getFieldByName(name).makeRef());
    }

    protected Value getArrayRef(Value base) {
        return new JArrayRef((Local) base, IntConstant.getInstance(0), );
    }

    /**
     * add an instance invocation receiver.sig(args)
     */
    protected void addInvoke(Value receiver, String sig, Immediate... args) {
        MethodSignature methodRef = PTAScene.v().getMethod(sig).getSignature();
        List<Immediate> argsL = Arrays.asList(args);
        AbstractInvokeExpr invoke = methodRef.getDeclaringClass().isInterface() ? Jimple.newInterfaceInvokeExpr((Local) receiver, methodRef, argsL)
                : Jimple.newVirtualInvokeExpr((Local) receiver, methodRef, argsL);
        body.getUnits().add(Jimple.newInvokeStmt(invoke, StmtPositionInfo.createNoStmtPositionInfo()));
    }

    /**
     * add an instance invocation and get the return value rx = receiver.sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(Value receiver, String sig, Value... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Value> argsL = Arrays.asList(args);
        Value invoke = methodRef.getDeclaringClass().isInterface() ? new JInterfaceInvokeExpr(receiver, methodRef, argsL)
                : new JVirtualInvokeExpr(receiver, methodRef, argsL);
        Value rx = getNextLocal(methodRef.getReturnType());
        addAssign(rx, invoke);
        return rx;
    }

    /**
     * add a static invocation sig(args)
     */
    protected void addInvoke(String sig, Value... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Value> argsL = Arrays.asList(args);
        body.getUnits().add(new JInvokeStmt(new JStaticInvokeExpr(methodRef, argsL)));
    }

    /**
     * add a static invocation and get the return value rx = sig(args)
     *
     * @return rx
     */
    protected Value getInvoke(String sig, Value... args) {
        MethodSignature msig = identifierFactory.parseMethodSignature(sig);
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Value> argsL = Arrays.asList(args);
        Value rx = getNextLocal(methodRef.getReturnType());
        addAssign(rx, new JStaticInvokeExpr(methodRef, argsL));
        return rx;
    }

    protected void addAssign(Value lValue, Value rValue) {
        body.getUnits().add(Jimple.newAssignStmt(lValue, rValue, StmtPositionInfo.createNoStmtPositionInfo()));
    }
}
