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

import qilin.core.PTAScene;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class ReflectionModel {
    protected final String sigForName = "<java.lang.Class: java.lang.Class forName(java.lang.String)>";
    protected final String sigForName2 = "<java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>";
    protected final String sigClassNewInstance = "<java.lang.Class: java.lang.Object newInstance()>";
    protected final String sigConstructorNewInstance = "<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>";
    protected final String sigMethodInvoke = "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>";
    protected final String sigFieldSet = "<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>";
    protected final String sigFieldGet = "<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>";
    protected final String sigArrayNewInstance = "<java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>";
    protected final String sigArrayGet = "<java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)>";
    protected final String sigArraySet = "<java.lang.reflect.Array: void set(java.lang.Object,int,java.lang.Object)>";
    protected final String sigReifiedField = "<java.lang.Class: java.lang.reflect.Field getField(java.lang.String)>";
    protected final String sigReifiedDeclaredField = "<java.lang.Class: java.lang.reflect.Field getDeclaredField(java.lang.String)>";
    protected final String sigReifiedFieldArray = "<java.lang.Class: java.lang.reflect.Field[] getFields()>";
    protected final String sigReifiedDeclaredFieldArray = "<java.lang.Class: java.lang.reflect.Field[] getDeclaredFields()>";
    protected final String sigReifiedMethod = "<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>";
    protected final String sigReifiedDeclaredMethod = "<java.lang.Class: java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])>";
    protected final String sigReifiedMethodArray = "<java.lang.Class: java.lang.reflect.Method[] getMethods()>";
    protected final String sigReifiedDeclaredMethodArray = "<java.lang.Class: java.lang.reflect.Method[] getDeclaredMethods()>";

    private Collection<Unit> transform(Stmt s) {
        InvokeExpr ie = s.getInvokeExpr();
        return switch (ie.getMethodRef().getSignature()) {
            case sigForName, sigForName2 -> transformClassForName(s);
            case sigClassNewInstance -> transformClassNewInstance(s);
            case sigConstructorNewInstance -> transformContructorNewInstance(s);
            case sigMethodInvoke -> transformMethodInvoke(s);
            case sigFieldSet -> transformFieldSet(s);
            case sigFieldGet -> transformFieldGet(s);
            case sigArrayNewInstance -> transformArrayNewInstance(s);
            case sigArrayGet -> transformArrayGet(s);
            case sigArraySet -> transformArraySet(s);
            default -> Collections.emptySet();
        };
    }

    /**
     * replace reflection call with appropriate statements
     */
    public void buildReflection(SootMethod m) {
        if (!PTAScene.v().reflectionBuilt.add(m)) {
            return;
        }
        Map<Unit, Collection<Unit>> newUnits = DataFactory.createMap();
        UnitPatchingChain units = PTAUtils.getMethodBody(m).getUnits();
        for (final Unit u : units) {
            final Stmt s = (Stmt) u;
            if (s.containsInvokeExpr()) {
                newUnits.put(u, transform(s));
            }
        }
        for (Unit unit : newUnits.keySet()) {
            units.insertAfter(newUnits.get(unit), unit);
        }
    }

    abstract Collection<Unit> transformClassForName(Stmt s);

    abstract Collection<Unit> transformClassNewInstance(Stmt s);

    abstract Collection<Unit> transformContructorNewInstance(Stmt s);

    abstract Collection<Unit> transformMethodInvoke(Stmt s);

    abstract Collection<Unit> transformFieldSet(Stmt s);

    abstract Collection<Unit> transformFieldGet(Stmt s);

    abstract Collection<Unit> transformArrayNewInstance(Stmt s);

    abstract Collection<Unit> transformArrayGet(Stmt s);

    abstract Collection<Unit> transformArraySet(Stmt s);
}
