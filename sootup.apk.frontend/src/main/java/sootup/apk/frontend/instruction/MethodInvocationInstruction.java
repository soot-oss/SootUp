package sootup.apk.frontend.instruction;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import java.util.List;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.instruction.formats.Instruction45cc;
import org.jf.dexlib2.iface.instruction.formats.Instruction4rcc;
import org.jf.dexlib2.iface.reference.MethodReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.Type;

public abstract class MethodInvocationInstruction extends DexLibAbstractInstruction
    implements DanglingInstruction {

  protected AbstractInvokeExpr invocation;
  protected JAssignStmt assignStmt;

  @Override
  public void finalize(DexBody body, DexLibAbstractInstruction successor) {
    if (successor instanceof MoveResultInstruction) {
      assignStmt =
          Jimple.newAssignStmt(
              body.getStoreResultLocal(), invocation, StmtPositionInfo.getNoStmtPositionInfo());
      setStmt(assignStmt);
      body.add(assignStmt);
    } else {
      JInvokeStmt jInvokeStmt =
          Jimple.newInvokeStmt(invocation, StmtPositionInfo.getNoStmtPositionInfo());
      setStmt(jInvokeStmt);
      body.add(jInvokeStmt);
    }
  }

  public MethodInvocationInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  protected void jimplifySpecial(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Local> parameters = buildParameters(body, item.getParameterTypes(), false);
    invocation =
        Jimple.newSpecialInvokeExpr(
            parameters.get(0),
            new MethodSignature(
                DexUtil.getClassTypeFromClassName(item.getDefiningClass()),
                new MethodSubSignature(
                    item.getName(),
                    convertParameterTypes(item.getParameterTypes()),
                    DexUtil.toSootType(item.getReturnType(), 0))),
            buildArgs(parameters.subList(1, parameters.size())));
    body.setDanglingInstruction(this);
  }

  protected void jimplifyStatic(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Local> parameters = buildParameters(body, item.getParameterTypes(), true);
    invocation =
        Jimple.newStaticInvokeExpr(
            new MethodSignature(
                DexUtil.getClassTypeFromClassName(item.getDefiningClass()),
                item.getName(),
                convertParameterTypes(item.getParameterTypes()),
                DexUtil.toSootType(item.getReturnType(), 0)));
    body.setDanglingInstruction(this);
  }

  protected void jimplifyVirtual(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Local> parameters = buildParameters(body, item.getParameterTypes(), false);
    // TODO check isIntertface by someother way
    //        if(sootClass.isInterface()) {
    //            jimplifyInterface(body);
    //            return;
    //        }
    MethodSignature methodSignature =
        new MethodSignature(
            DexUtil.getClassTypeFromClassName(item.getDefiningClass()),
            item.getName(),
            convertParameterTypes(item.getParameterTypes()),
            DexUtil.toSootType(item.getReturnType(), 0));
    invocation =
        Jimple.newVirtualInvokeExpr(
            parameters.get(0),
            methodSignature,
            buildArgs(parameters.subList(1, parameters.size())));
    body.setDanglingInstruction(this);
  }

  protected void jimplifyInterface(DexBody body) {
    MethodReference item = (MethodReference) ((ReferenceInstruction) instruction).getReference();
    List<Local> parameters = buildParameters(body, item.getParameterTypes(), false);
    // TODO check isIntertface by someother way
    //        if(!sootClass.isInterface()) {
    //            jimplifyVirtual(body);
    //            return;
    //        }
    MethodSignature methodSignature =
        new MethodSignature(
            DexUtil.getClassTypeFromClassName(item.getDefiningClass()),
            item.getName(),
            convertParameterTypes(item.getParameterTypes()),
            DexUtil.toSootType(item.getReturnType(), 0));
    invocation =
        Jimple.newInterfaceInvokeExpr(
            parameters.get(0),
            methodSignature,
            buildArgs(parameters.subList(1, parameters.size())));
    body.setDanglingInstruction(this);
  }

  protected List<Type> convertParameterTypes(List<? extends CharSequence> paramTypes) {
    List<Type> parameterTypes = new ArrayList<Type>();
    if (paramTypes != null) {
      for (CharSequence type : paramTypes) {
        parameterTypes.add(DexUtil.toSootType(type.toString(), 0));
      }
    }
    return parameterTypes;
  }

  protected List<Immediate> buildArgs(List<Local> locals) {
    List<Immediate> args = new ArrayList<>();
    args.addAll(locals);
    return args;
  }

  protected List<Local> buildParameters(
      DexBody body, List<? extends CharSequence> paramTypes, boolean isStatic) {
    List<Local> parameters = new ArrayList<Local>();
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
      if (paramTypes != null && isWide(paramTypes.get(j).toString())) {
        i++;
      }
    }
    return parameters;
  }

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
    throw new RuntimeException(
        "Instruction is neither a InvokeInstruction nor a InvokeRangeInstruction");
  }

  public static boolean isWide(String type) {
    char c = type.charAt(0);
    return c == 'J' || c == 'D';
  }
}
