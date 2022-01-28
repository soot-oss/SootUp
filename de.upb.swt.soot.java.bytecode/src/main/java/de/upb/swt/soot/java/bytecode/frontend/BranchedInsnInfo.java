package de.upb.swt.soot.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Markus Schmidt
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
import de.upb.swt.soot.core.types.ClassType;
import java.util.*;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.AbstractInsnNode;

// FIXME: [AD] is it reasonable to get rid of it?
class BranchedInsnInfo {
  /* Traps associated with the current block */
  @Nonnull private List<ClassType> traps;
  /* edge endpoint */
  @Nonnull private final AbstractInsnNode insn;
  /* previous stacks at edge */
  @Nonnull private final LinkedList<Operand[]> prevStacks;
  /* current stack at edge */
  @Nonnull private List<Operand> operandStack;

  BranchedInsnInfo(
      @Nonnull List<ClassType> traps,
      @Nonnull AbstractInsnNode insn,
      @Nonnull List<Operand> operands) {
    this.traps = new ArrayList<>(traps);
    this.insn = insn;
    this.prevStacks = new LinkedList<>();
    this.operandStack = operands;
  }

  @Nonnull
  AbstractInsnNode getInsn() {
    return insn;
  }

  @Nonnull
  List<Operand> getOperandStack() {
    return operandStack;
  }

  void setOperandStack(@Nonnull List<Operand> operandStack) {
    this.operandStack = new ArrayList<>(operandStack);
  }

  @Nonnull
  List<Operand[]> getPrevStacks() {
    return Collections.unmodifiableList(prevStacks);
  }

  void addToPrevStack(@Nonnull Operand[] stacksOperands) {
    prevStacks.add(stacksOperands);
  }
}
