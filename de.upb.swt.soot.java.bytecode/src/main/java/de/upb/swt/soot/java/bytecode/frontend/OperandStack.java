/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Markus Schmidt and others
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

package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.types.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * This class resembles the stack which the bytecode fills. It is used to convert to jimple with
 * Locals. (stack-machine -> "register" machine model)
 */
public class OperandStack {

  private final AsmMethodSource methodSource;
  private List<Operand> stack;
  public Map<AbstractInsnNode, StackFrame> frames;

  public OperandStack(AsmMethodSource methodSource, int nrInsn) {
    this.methodSource = methodSource;
    frames = new LinkedHashMap<>(nrInsn);
  }

  @Nonnull
  public StackFrame getOrCreateStackframe(@Nonnull AbstractInsnNode insn) {
    StackFrame frame = frames.get(insn);
    if (frame == null) {
      frame = new StackFrame(methodSource);
      frames.put(insn, frame);
    }
    return frame;
  }

  public void push(Operand opr) {
    stack.add(opr);
  }

  public void pushDual(Operand opr) {
    stack.add(AsmMethodSource.DWORD_DUMMY);
    stack.add(opr);
  }

  public Operand peek() {
    return stack.get(stack.size() - 1);
  }

  public void push(Type t, Operand opr) {
    if (AsmUtil.isDWord(t)) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  public Operand pop() {
    if (stack.isEmpty()) {
      throw new RuntimeException("Stack underrun");
    }
    return stack.remove(stack.size() - 1);
  }

  public Operand popDual() {
    Operand o = pop();
    Operand o2 = pop();
    if (o2 != AsmMethodSource.DWORD_DUMMY && o2 != o) {
      throw new AssertionError("Not dummy operand, " + o2.value + " -- " + o.value);
    }
    return o;
  }

  @Nonnull
  public Operand pop(@Nonnull Type t) {
    return AsmUtil.isDWord(t) ? popDual() : pop();
  }

  @Nonnull
  public Operand popLocal(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Local)) {
      l = o.stack = methodSource.newStackLocal();
      methodSource.setStmt(
          o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  public Operand popImmediate(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Local) && !(v instanceof Constant)) {
      l = o.stack = methodSource.newStackLocal();
      methodSource.setStmt(
          o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  public Operand popStackConst(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Constant)) {
      l = o.stack = methodSource.newStackLocal();
      methodSource.setStmt(
          o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  public Operand popLocal() {
    return popLocal(pop());
  }

  @SuppressWarnings("unused")
  @Nonnull
  public Operand popLocalDual() {
    return popLocal(popDual());
  }

  @Nonnull
  public Operand popImmediate() {
    return popImmediate(pop());
  }

  @Nonnull
  public Operand popImmediateDual() {
    return popImmediate(popDual());
  }

  @Nonnull
  public Operand popImmediate(@Nonnull Type t) {
    return AsmUtil.isDWord(t) ? popImmediateDual() : popImmediate();
  }

  @Nonnull
  public Operand popStackConst() {
    return popStackConst(pop());
  }

  @SuppressWarnings("unused")
  @Nonnull
  public Operand popStackConstDual() {
    return popStackConst(popDual());
  }

  public List<Operand> getStack() {
    return stack;
  }

  public void setOperandStack(List<Operand> stack) {
    this.stack = stack;
  }
}
