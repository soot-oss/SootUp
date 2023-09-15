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

import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import soot.jimple.internal.*;

import java.util.Arrays;
import java.util.List;

public abstract class ArtificialMethod {
    protected SootMethod method;
    protected Body body;
    protected Value thisLocal;
    protected Value[] paraLocals;
    protected int paraStart;
    protected int localStart;

    protected Value getThis() {
        if (thisLocal == null) {
            RefType type = method.getDeclaringClass().getType();
            Value thisRef = new ThisRef(type);
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
        Value paraLocal = paraLocals[index];
        if (paraLocal == null) {
            Value paraRef = new ParameterRef(type, index);
            paraLocal = getLocal(type, paraStart + index);
            addIdentity(paraLocal, paraRef);
            paraLocals[index] = paraLocal;
        }
        return paraLocal;
    }

    private void addIdentity(Value lValue, Value rValue) {
        body.getUnits().add(new JIdentityStmt(lValue, rValue));
    }

    protected Value getNew(RefType type) {
        Value newExpr = new JNewExpr(type);
        Value local = getNextLocal(type);
        addAssign(local, newExpr);
        return local;
    }

    protected Value getNewArray(RefType type) {
        Value newExpr = new JNewArrayExpr(type, IntConstant.v(1));
        Value local = getNextLocal(ArrayType.v(type, 1));
        addAssign(local, newExpr);
        return local;
    }

    protected Value getNextLocal(Type type) {
        return getLocal(type, localStart++);
    }

    private Value getLocal(Type type, int index) {
        Local local = new JimpleLocal("r" + index, type);
        body.getLocals().add(local);
        return local;
    }

    protected void addReturn(Value ret) {
        body.getUnits().add(new JReturnStmt(ret));
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
    protected void addInvoke(Value receiver, String sig, Value... args) {
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Value> argsL = Arrays.asList(args);
        Value invoke = methodRef.getDeclaringClass().isInterface() ? new JInterfaceInvokeExpr(receiver, methodRef, argsL)
                : new JVirtualInvokeExpr(receiver, methodRef, argsL);
        body.getUnits().add(new JInvokeStmt(invoke));
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
        SootMethodRef methodRef = PTAScene.v().getMethod(sig).makeRef();
        List<Value> argsL = Arrays.asList(args);
        Value rx = getNextLocal(methodRef.getReturnType());
        addAssign(rx, new JStaticInvokeExpr(methodRef, argsL));
        return rx;
    }

    protected void addAssign(Value lValue, Value rValue) {
        body.getUnits().add(new JAssignStmt(lValue, rValue));
    }
}
