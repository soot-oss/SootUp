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

import java.util.*;
import qilin.util.JavaTypes;
import qilin.util.queue.ChunkedQueue;
import sootup.callgraph.MethodLookup;
import sootup.callgraph.SignaturePolymorphicMethods;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Resolves virtual calls.
 *
 * @author Ondrej Lhotak
 */
public class VirtualCalls {
  protected View view;
  private final MethodLookup methodLookup;

  public VirtualCalls(View view) {
    this(new MethodLookup(view));
  }

  public VirtualCalls(MethodLookup methodLookup) {
    this.view = methodLookup.getView();
    this.methodLookup = methodLookup;
  }

  public MethodLookup getMethodLookup() {
    return methodLookup;
  }

  public SootMethod resolveSpecial(
      JSpecialInvokeExpr iie, MethodSubSignature subSig, SootMethod container) {
    return resolveSpecial(iie, subSig, container, false);
  }

  public SootMethod resolveSpecial(
      JSpecialInvokeExpr iie, MethodSubSignature subSig, SootMethod container, boolean appOnly) {
    MethodSignature methodSig = iie.getMethodSignature();
    /* cf. JVM spec, invokespecial instruction */
    if (view.getTypeHierarchy()
            .isSubtype(methodSig.getDeclClassType(), container.getDeclaringClassType())
        && container.getDeclaringClassType() != methodSig.getDeclClassType()
        && !methodSig.getName().equals("<init>")
        && !subSig.toString().equals("void <clinit>()")) {
      SootClass cls = view.getClass(container.getDeclaringClassType()).get();
      ClassType superClsType = cls.getSuperclass().get();
      return resolveNonSpecial(superClsType, subSig, appOnly);
    } else {
      Optional<? extends SootMethod> otgt = view.getMethod(methodSig);
      if (otgt.isEmpty()) {
        otgt = SignaturePolymorphicMethods.resolve(view, methodSig);
      }
      if (!otgt.isPresent()) {
        System.out.println(
            "Wrarning: signature " + methodSig + " does not have a concrete method.");
        return null;
      }
      return otgt.get();
    }
  }

  public SootMethod resolveNonSpecial(ClassType t, MethodSubSignature subSig) {
    return resolveNonSpecial(t, subSig, false);
  }

  public SootMethod resolveNonSpecial(ClassType t, MethodSubSignature subSig, boolean appOnly) {
    if (appOnly && view.getClass(t).map(SootClass::isLibraryClass).orElse(true)) {
      return null;
    }
    // superclasses, then default methods, then signature-polymorphic; cached incl. misses
    return methodLookup.findConcrete(t, subSig).orElse(null);
  }

  public void resolve(
      Type t,
      Type declaredType,
      MethodSubSignature subSig,
      SootMethod container,
      ChunkedQueue<SootMethod> targets) {
    resolve(t, declaredType, null, subSig, container, targets);
  }

  public void resolve(
      Type t,
      Type declaredType,
      Type sigType,
      MethodSubSignature subSig,
      SootMethod container,
      ChunkedQueue<SootMethod> targets) {
    resolve(t, declaredType, sigType, subSig, container, targets, false);
  }

  public void resolve(
      Type t,
      Type declaredType,
      Type sigType,
      MethodSubSignature subSig,
      SootMethod container,
      ChunkedQueue<SootMethod> targets,
      boolean appOnly) {
    if (declaredType instanceof ArrayType) {
      declaredType = JavaTypes.OBJECT;
    }
    if (sigType instanceof ArrayType) {
      sigType = JavaTypes.OBJECT;
    }
    if (t instanceof ArrayType) {
      t = JavaTypes.OBJECT;
    }

    if (declaredType != null && !JavaTypes.canStoreType(view, t, declaredType)) {
      return;
    }
    if (sigType != null && !JavaTypes.canStoreType(view, t, sigType)) {
      return;
    }
    if (t instanceof ClassType) {
      SootMethod target = resolveNonSpecial((ClassType) t, subSig, appOnly);
      if (target != null) {
        targets.add(target);
      }
    }
    //        else if (t instanceof AnySubType) {
    //            ClassType base = ((AnySubType) t).getBase();
    //
    //            /*
    //             * Whenever any sub type of a specific type is considered as receiver for a method
    // to call and the base type is an
    //             * interface, calls to existing methods with matching signature (possible
    // implementation of method to call) are also
    //             * added. As Javas' subtyping allows contra-variance for return types and
    // co-variance for parameters when overriding a
    //             * method, these cases are also considered here.
    //             *
    //             * Example: Classes A, B (B sub type of A), interface I with method public A foo(B
    // b); and a class C with method public
    //             * B foo(A a) { ... }. The extended class hierarchy will contain C as possible
    // implementation of I.
    //             *
    //             * Since Java has no multiple inheritance call by signature resolution is only
    // activated if the base is an interface.
    //             */
    //            resolveAnySubType(declaredType, sigType, subSig, container, targets, appOnly,
    // base);
    //        }
    else if (t instanceof NullType) {
    } else {
      throw new RuntimeException("oops " + t);
    }
  }
}
