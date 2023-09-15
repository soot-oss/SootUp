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

import qilin.util.DataFactory;
import soot.*;
import soot.jimple.SpecialInvokeExpr;
import soot.util.*;
import soot.util.queue.ChunkedQueue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Resolves virtual calls.
 *
 * @author Ondrej Lhotak
 */
public class VirtualCalls {
    private static volatile VirtualCalls instance = null;
    private final Map<Type, Map<NumberedString, SootMethod>> typeToVtbl = DataFactory.createMap(Scene.v().getTypeNumberer().size());
    protected Map<Type, Set<Type>> baseToSubTypes = DataFactory.createMap();

    private VirtualCalls() {
    }

    public static VirtualCalls v() {
        if (instance == null) {
            synchronized (VirtualCalls.class) {
                if (instance == null) {
                    instance = new VirtualCalls();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public SootMethod resolveSpecial(SpecialInvokeExpr iie, NumberedString subSig, SootMethod container) {
        return resolveSpecial(iie, subSig, container, false);
    }

    public SootMethod resolveSpecial(SpecialInvokeExpr iie, NumberedString subSig, SootMethod container, boolean appOnly) {
        SootMethod target = iie.getMethod();
        /* cf. JVM spec, invokespecial instruction */
        if (Scene.v().getFastHierarchy().canStoreType(container.getDeclaringClass().getType(),
                target.getDeclaringClass().getType())
                && container.getDeclaringClass().getType() != target.getDeclaringClass().getType()
                && !target.getName().equals("<init>")
                && subSig != Scene.v().getSubSigNumberer().findOrAdd("void <clinit>()")) {

            return resolveNonSpecial(container.getDeclaringClass().getSuperclass().getType(), subSig, appOnly);
        } else {
            return target;
        }
    }

    public SootMethod resolveNonSpecial(RefType t, NumberedString subSig) {
        return resolveNonSpecial(t, subSig, false);
    }

    public SootMethod resolveNonSpecial(RefType t, NumberedString subSig, boolean appOnly) {
        Map<NumberedString, SootMethod> vtbl = typeToVtbl.computeIfAbsent(t, k -> DataFactory.createMap(8));
        SootMethod ret = vtbl.get(subSig);
        if (ret != null) {
            return ret;
        }
        SootClass cls = t.getSootClass();
        if (appOnly && cls.isLibraryClass()) {
            return null;
        }

        SootMethod m = cls.getMethodUnsafe(subSig);
        if (m != null) {
            if (!m.isAbstract()) {
                ret = m;
            }
        } else {
            SootClass c = cls.getSuperclassUnsafe();
            if (c != null) {
                ret = resolveNonSpecial(c.getType(), subSig);
            }
        }
        if (ret == null) {
            return null;
        }
        vtbl.put(subSig, ret);
        return ret;
    }

    public void resolve(Type t, Type declaredType, NumberedString subSig, SootMethod container,
                        ChunkedQueue<SootMethod> targets) {
        resolve(t, declaredType, null, subSig, container, targets);
    }

    public void resolve(Type t, Type declaredType, Type sigType, NumberedString subSig, SootMethod container,
                        ChunkedQueue<SootMethod> targets) {
        resolve(t, declaredType, sigType, subSig, container, targets, false);
    }

    public void resolve(Type t, Type declaredType, Type sigType, NumberedString subSig, SootMethod container,
                        ChunkedQueue<SootMethod> targets, boolean appOnly) {
        if (declaredType instanceof ArrayType) {
            declaredType = RefType.v("java.lang.Object");
        }
        if (sigType instanceof ArrayType) {
            sigType = RefType.v("java.lang.Object");
        }
        if (t instanceof ArrayType) {
            t = RefType.v("java.lang.Object");
        }

        if (declaredType != null && !Scene.v().getFastHierarchy().canStoreType(t, declaredType)) {
            return;
        }
        if (sigType != null && !Scene.v().getFastHierarchy().canStoreType(t, sigType)) {
            return;
        }
        if (t instanceof RefType) {
            SootMethod target = resolveNonSpecial((RefType) t, subSig, appOnly);
            if (target != null) {
                targets.add(target);
            }
        } else if (t instanceof AnySubType) {
            RefType base = ((AnySubType) t).getBase();

            /*
             * Whenever any sub type of a specific type is considered as receiver for a method to call and the base type is an
             * interface, calls to existing methods with matching signature (possible implementation of method to call) are also
             * added. As Javas' subtyping allows contra-variance for return types and co-variance for parameters when overriding a
             * method, these cases are also considered here.
             *
             * Example: Classes A, B (B sub type of A), interface I with method public A foo(B b); and a class C with method public
             * B foo(A a) { ... }. The extended class hierarchy will contain C as possible implementation of I.
             *
             * Since Java has no multiple inheritance call by signature resolution is only activated if the base is an interface.
             */
            resolveAnySubType(declaredType, sigType, subSig, container, targets, appOnly, base);
        } else if (t instanceof NullType) {
        } else {
            throw new RuntimeException("oops " + t);
        }
    }

    protected void resolveAnySubType(Type declaredType, Type sigType, NumberedString subSig, SootMethod container,
                                     ChunkedQueue<SootMethod> targets, boolean appOnly, RefType base) {
        {
            Set<Type> subTypes = baseToSubTypes.get(base);
            if (subTypes != null && !subTypes.isEmpty()) {
                for (final Type st : subTypes) {
                    resolve(st, declaredType, sigType, subSig, container, targets, appOnly);
                }
                return;
            }
        }

        Set<Type> newSubTypes = new HashSet<>();
        newSubTypes.add(base);

        LinkedList<SootClass> worklist = new LinkedList<>();
        HashSet<SootClass> workset = new HashSet<>();
        SootClass cl = base.getSootClass();

        if (workset.add(cl)) {
            worklist.add(cl);
        }
        while (!worklist.isEmpty()) {
            cl = worklist.removeFirst();
            if (cl.isInterface()) {
                for (final SootClass c : Scene.v().getFastHierarchy().getAllImplementersOfInterface(cl)) {
                    if (workset.add(c)) {
                        worklist.add(c);
                    }
                }
            } else {
                if (cl.isConcrete()) {
                    resolve(cl.getType(), declaredType, sigType, subSig, container, targets, appOnly);
                    newSubTypes.add(cl.getType());
                }
                for (final SootClass c : Scene.v().getFastHierarchy().getSubclassesOf(cl)) {
                    if (workset.add(c)) {
                        worklist.add(c);
                    }
                }
            }
        }

        baseToSubTypes.computeIfAbsent(base, k -> DataFactory.createSet()).addAll(newSubTypes);
    }

}
