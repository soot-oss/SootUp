package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.instructions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.MethodHandle.Kind;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.IDalvikTyper;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.Util;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing.DalvikTyper;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import javafx.scene.Scene;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.instruction.formats.Instruction45cc;
import org.jf.dexlib2.iface.instruction.formats.Instruction4rcc;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import sun.jvm.hotspot.debugger.cdbg.RefType;


public abstract class MethodInvocationInstruction extends DexlibAbstractInstruction implements DanglingInstruction {

  // stores the dangling InvokeExpr
  protected AbstractInvokeExpr invocation;
  protected JAssignStmt assign = null;
  private JavaView view = null; // TODO

  public MethodInvocationInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void finalize(DexBody body, DexlibAbstractInstruction successor) {
    // defer final jimplification to move result
    if (successor instanceof MoveResultInstruction) {
      // MoveResultInstruction i = (MoveResultInstruction)successor;
      // i.setExpr(invocation);
      // if (lineNumber != -1)
      // i.setTag(new SourceLineNumberTag(lineNumber));
      assign = Jimple.newAssignStmt(body.getStoreResultLocal(), invocation, StmtPositionInfo.createNoStmtPositionInfo());
      setStmt(assign);
      addTags(assign);
      body.add(assign);
      stmt = assign;
      // this is a invoke statement (the MoveResult had to be the direct successor for an expression)
    } else {
      JInvokeStmt invoke = Jimple.newInvokeStmt(invocation, StmtPositionInfo.createNoStmtPositionInfo());
      setStmt(invoke);
      addTags(invoke);
      body.add(invoke);
      stmt = invoke;
    }

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      // Debug.printDbg(IDalvikTyper.DEBUG, "constraint special invoke: "+ assign);

      if (invocation instanceof AbstractInstanceInvokeExpr) {
        Type t = invocation.getMethodSignature().getDeclClassType();
        DalvikTyper.v().setType(((AbstractInstanceInvokeExpr) invocation).getBase(), t, true);
        // DalvikTyper.v().setObjectType(assign.getLeftOpBox());
      }
      int i = 0;
      for (Type pt : invocation.getMethodSignature().getParameterTypes()) {
        DalvikTyper.v().setType(invocation.getArg(i++), pt, true);
      }
      if (assign != null) {
        DalvikTyper.v().setType(assign.getLeftOp(), invocation.getType(), false);
      }

    }
  }

  @Override
  public Set<Type> introducedTypes() {
    Set<Type> types = new HashSet<Type>();
    MethodReference method = (MethodReference) (((ReferenceInstruction) instruction).getReference());

    types.add(DexType.toSoot(method.getDefiningClass()));
    types.add(DexType.toSoot(method.getReturnType()));
    List<? extends CharSequence> paramTypes = method.getParameterTypes();
    if (paramTypes != null) {
      for (CharSequence type : paramTypes) {
        types.add(DexType.toSoot(type.toString()));
      }
    }

    return types;
  }

  // overriden in InvokeStaticInstruction
  @Override
  boolean isUsedAsFloatingPoint(DexBody body, int register) {
    return isUsedAsFloatingPoint(body, register, false);
  }

  /**
   * Determine if register is used as floating point.
   *
   * Abstraction for static and non-static methods. Non-static methods need to ignore the first parameter (this)
   *
   * @param isStatic
   *          if this method is static
   */
  protected boolean isUsedAsFloatingPoint(DexBody body, int register, boolean isStatic) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<? extends CharSequence> paramTypes = item.getParameterTypes();
    List<Integer> regs = getUsedRegistersNums();
    if (paramTypes == null) {
      return false;
    }

    for (int i = 0, j = 0; i < regs.size(); i++, j++) {
      if (!isStatic && i == 0) {
        j--;
        continue;
      }

      if (regs.get(i) == register && Util.isFloatLike(DexType.toSoot(paramTypes.get(j).toString()))) {
        return true;
      }
      if (DexType.isWide(paramTypes.get(j).toString())) {
        i++;
      }
    }
    return false;
  }

  /**
   * Determine if register is used as object.
   *
   * Abstraction for static and non-static methods. Non-static methods need to ignore the first parameter (this)
   *
   * @param isStatic
   *          if this method is static
   */
  protected boolean isUsedAsObject(DexBody body, int register, boolean isStatic) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<? extends CharSequence> paramTypes = item.getParameterTypes();
    List<Integer> regs = getUsedRegistersNums();
    if (paramTypes == null) {
      return false;
    }

    // we call a method on the register
    if (!isStatic && regs.get(0) == register) {
      return true;
    }

    // we call a method with register as a reftype paramter
    for (int i = 0, j = 0; i < regs.size(); i++, j++) {
      if (!isStatic && i == 0) {
        j--;
        continue;
      }

      if (regs.get(i) == register && (DexType.toSoot(paramTypes.get(j).toString()) instanceof RefType)) {
        return true;
      }
      if (DexType.isWide(paramTypes.get(j).toString())) {
        i++;
      }
    }
    return false;
  }

  /**
   * Return the virtual SootMethodRef for the invoked method.
   *
   */
  protected MethodSignature getVirtualSootMethodRef() {
    return getNormalSootMethodRef(Kind.REF_INVOKE_VIRTUAL);
  }

  /**
   * Return the static SootMethodRef for the invoked method.
   *
   */
  protected MethodSignature getStaticSootMethodRef() {
    return getNormalSootMethodRef(Kind.REF_INVOKE_STATIC);
  }

  /**
   * Return the static SootMethodRef for the invoked method.
   *
   */
  protected MethodSignature getInterfaceSootMethodRef() {
    return getNormalSootMethodRef(Kind.REF_INVOKE_INTERFACE);
  }

  /**
   * Return the SootMethodRef for the invoked method.
   *
   * @param kind
   *          The type of the invocation in terms of Kind
   */
  protected MethodSignature getNormalSootMethodRef(Kind kind) {
    return getSootMethodRef((MethodReference) ((ReferenceInstruction) instruction).getReference(), kind);
  }

  /**
   * Return a SootMethodRef for the given MethodReference dependent on the InvocationType passed in.
   * 
   * @param mItem
   *          The MethodReference included in the invoke instruction
   * @param kind
   *          The type of the invocation in terms of Kind
   * @return A SootMethodRef
   */
  protected MethodSignature getSootMethodRef(MethodReference mItem, Kind kind) {
    return getSootMethodRef(convertClassName(mItem.getDefiningClass(), kind), mItem.getName(), mItem.getReturnType(),
        mItem.getParameterTypes(), kind);
  }

  /**
   * Return a SootMethodRef for the given data.
   * 
   * @param classType
   *          The SootClass that the method is declared in
   * @param name
   *          The name of the method being invoked
   * @param returnType
   *          The return type of the method being invoked
   * @param paramTypes
   *          The parameter types of the method being invoked
   * @param kind
   *          The type of the invocation in terms of Kind
   * @return A SootMethodRef
   */
  protected MethodSignature getSootMethodRef(ClassType classType, String name, String returnType,
                                             List<? extends CharSequence> paramTypes, Kind kind) {
    //return Scene.v().makeMethodRef(classType, name, convertParameterTypes(paramTypes), DexType.toSoot(returnType),
    //    kind == Kind.REF_INVOKE_STATIC);
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    MethodSubSignature subSig = idFactory.getMethodSubSignature(name, idFactory.getType(returnType), idFactory.getTypes((List<String>) paramTypes));
    return JavaIdentifierFactory.getInstance().getMethodSignature(classType, subSig);
  }

  /**
   * Return a SootFieldRef for the given data.
   * 
   * @param mItem
   *          The FieldReference included in the invoke instruction
   * @param kind
   *          The type of the field access in terms of Kind
   * @return A SootFieldRef
   */
  protected FieldSignature getSootFieldRef(FieldReference mItem, Kind kind) {
    return getSootFieldRef(convertClassName(mItem.getDefiningClass(), kind), mItem.getName(), mItem.getType(), kind);
  }

  /**
   * Return a SootFieldRef for the given data.
   * 
   * @param classType
   *          The SootClass that the field is declared in
   * @param name
   *          The name of the field
   * @param type
   *          The type of the field
   * @param kind
   *          The type of the field access in terms of Kind
   * @return A SootFieldRef
   */
  protected FieldSignature getSootFieldRef(ClassType classType, String name, String type, Kind kind) {
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    FieldSubSignature subSig = idFactory.getFieldSubSignature(name, idFactory.getType(type));
    return idFactory.getFieldSignature(classType, subSig);
    //return Scene.v().makeFieldRef(sc, name, DexType.toSoot(type),
    //    kind == Kind.REF_GET_FIELD_STATIC || kind == Kind.REF_PUT_FIELD_STATIC);
  }

  /**
   * Converts a list of dex string parameter types to soot types.
   * 
   * @param paramTypes
   *          The dex parameter types
   * @return The soot parameter types
   */
  protected List<Type> convertParameterTypes(List<? extends CharSequence> paramTypes) {
    List<Type> parameterTypes = new ArrayList<Type>();
    if (paramTypes != null) {
      for (CharSequence type : paramTypes) {
        parameterTypes.add(DexType.toSoot(type.toString()));
      }
    }
    return parameterTypes;
  }

  /**
   * Converts a given string class name into a SootClass.
   * 
   * @param name
   *          The dex string representation of the class
   * @param kind
   *          The Kind of the MethodHandle this class is coming from
   * @return
   */
  protected ClassType convertClassName(String name, Kind kind) {
    if (name.startsWith("[")) {
      name = "java.lang.Object";
    } else {
      name = Util.dottedClassName(name);
    }

    return JavaIdentifierFactory.getInstance().getClassType(name);
    //SootClass sc = SootResolver.v().makeClassRef(name);
//   SootClass sc;
//    sc.isPhantomClass();
//    if (kind == Kind.REF_INVOKE_INTERFACE && sc.isPhantom()) {
//      sc.setModifiers(sc.getModifiers() | Modifier.INTERFACE);
//    }
//    return sc;
  }

  /**
   * Build the local parameters of the invocation. If a method is not static then its first register value is the invoking
   * object and will be skipped when constructing the local parameters.
   * 
   * @param body
   *          the body to build for and into
   * @param paramTypes
   *          the parameter types as strings
   * @param isStatic
   *          if the method is static
   * @return the converted parameters
   */
  protected List<Immediate> buildParameters(DexBody body, List<? extends CharSequence> paramTypes, boolean isStatic) {
    List<Immediate> parameters = new ArrayList<>();
    List<Integer> regs = getUsedRegistersNums();

    // i: index for register
    // j: index for parameter type
    for (int i = 0, j = 0; i < regs.size(); i++, j++) {
      parameters.add(body.getRegisterLocal(regs.get(i)));
      // if method is non-static the first parameter is the instance
      // pointer and has no corresponding parameter type
      if (!isStatic && i == 0) {
        j--;
        continue;
      }
      // If current parameter is wide ignore the next register.
      // No need to increment j as there is one parameter type
      // for those two registers.
      if (paramTypes != null && DexType.isWide(paramTypes.get(j).toString())) {
        i++;
      }
    }
    return parameters;
  }

  /**
   * Return the indices used in this instruction.
   *
   * @return a list of register indices
   */
  protected List<Integer> getUsedRegistersNums() {
    if (instruction instanceof Instruction35c) {
      return getUsedRegistersNums((Instruction35c) instruction);
    } else if (instruction instanceof Instruction3rc) {
      return getUsedRegistersNums((Instruction3rc) instruction);
    } else if (instruction instanceof Instruction45cc) {
      return getUsedRegistersNums((Instruction45cc) instruction);
    } else if (instruction instanceof Instruction4rcc) {
      return getUsedRegistersNums((Instruction4rcc) instruction);
    }
    throw new RuntimeException("Instruction is neither a InvokeInstruction nor a InvokeRangeInstruction");
  }

  /**
   * Executes the "jimplify" operation for a virtual invocation
   */
  protected void jimplifyVirtual(DexBody body) {
    // In some applications, InterfaceInvokes are disguised as VirtualInvokes.
    // We fix this silently
    MethodSignature ref = getVirtualSootMethodRef();
    if (view.getClass(ref.getDeclClassType()).get().isInterface()) {
      jimplifyInterface(body);
      return;
    }

    // This is actually a VirtualInvoke
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Immediate> parameters = buildParameters(body, item.getParameterTypes(), false);
    List<Immediate> args = parameters.subList(1, parameters.size());
    invocation = Jimple.newVirtualInvokeExpr((Local) parameters.get(0), ref, args);
    body.setDanglingInstruction(this);
  }

  /**
   * Executes the "jimplify" operation for an interface invocation
   */
  protected void jimplifyInterface(DexBody body) {
    // In some applications, VirtualInvokes are disguised as InterfaceInvokes.
    // We fix this silently
    MethodSignature ref = getInterfaceSootMethodRef();
    if (!view.getClass(ref.getDeclClassType()).get().isInterface()) {
      jimplifyVirtual(body);
      return;
    }

    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Immediate> parameters = buildParameters(body, item.getParameterTypes(), false);
    List<Immediate> args = parameters.subList(1, parameters.size());

    invocation = Jimple.newInterfaceInvokeExpr((Local) parameters.get(0), ref, args);
    body.setDanglingInstruction(this);
  }

  /**
   * Executes the "jimplify" operation for a special invocation
   */
  protected void jimplifySpecial(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Immediate> parameters = buildParameters(body, item.getParameterTypes(), false);
    List<Immediate> args = parameters.subList(1, parameters.size());
    invocation = Jimple.newSpecialInvokeExpr((Local) parameters.get(0), getVirtualSootMethodRef(), args);
    body.setDanglingInstruction(this);
  }

  /**
   * Executes the "jimplify" operation for a static invocation
   */
  protected void jimplifyStatic(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Immediate> parameters = buildParameters(body, item.getParameterTypes(), true);
    List<Immediate> args = parameters.subList(1, parameters.size());
    invocation
        = Jimple.newStaticInvokeExpr(getStaticSootMethodRef(), args);
    body.setDanglingInstruction(this);
  }

}
