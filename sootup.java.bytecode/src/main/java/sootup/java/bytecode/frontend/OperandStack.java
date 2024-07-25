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

package sootup.java.bytecode.frontend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.AbstractInsnNode;
import sootup.core.types.Type;

/**
 * This class resembles the stack which the bytecode fills. It is used to convert to jimple with
 * Locals. (stack-machine -&gt; "register" machine model)
 */
public class OperandStack {

  @Nonnull private final AsmMethodSource methodSource;
  private List<Operand> stack;
  @Nonnull public Map<AbstractInsnNode, OperandMerging> mergings;

  public OperandStack(@Nonnull AsmMethodSource methodSource, int nrInsn) {
    this.methodSource = methodSource;
    mergings = new LinkedHashMap<>(nrInsn);
  }

  @Nonnull
  public OperandMerging getOrCreateMerging(@Nonnull AbstractInsnNode insn) {
    OperandMerging merging = this.mergings.get(insn);
    if (merging == null) {
      merging = new OperandMerging(insn, methodSource);
      this.mergings.put(insn, merging);
    }
    return merging;
  }

  public void push(@Nonnull Operand opr) {
    stack.add(opr);
  }

  public void pushDual(@Nonnull Operand opr) {
    stack.add(Operand.DWORD_DUMMY);
    stack.add(opr);
  }

  @Nonnull
  public Operand peek() {
    if (stack.isEmpty()) {
      throw new RuntimeException("Stack underrun");
    }
    return stack.get(stack.size() - 1);
  }

  public void push(@Nonnull Type t, @Nonnull Operand opr) {
    if (AsmUtil.isDWord(t)) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  @Nonnull
  public Operand pop() {
    if (stack.isEmpty()) {
      throw new RuntimeException("Stack underrun");
    }
    return stack.remove(stack.size() - 1);
  }

  @Nonnull
  public Operand popDual() {
    Operand o = pop();
    Operand o2 = pop();
    if (o2 != Operand.DWORD_DUMMY && o2 != o) {
      throw new AssertionError("Not dummy operand, " + o2.value + " -- " + o.value);
    }
    return o;
  }

  @Nonnull
  public Operand pop(@Nonnull Type t) {
    return AsmUtil.isDWord(t) ? popDual() : pop();
  }

  @Nonnull
  public Operand popStackConst() {
    return pop();
  }

  @SuppressWarnings("unused")
  @Nonnull
  public Operand popStackConstDual() {
    return popDual();
  }

  @Nonnull
  public List<Operand> getStack() {
    return stack;
  }

  public void setOperandStack(@Nonnull List<Operand> stack) {
    this.stack = stack;
  }
}
