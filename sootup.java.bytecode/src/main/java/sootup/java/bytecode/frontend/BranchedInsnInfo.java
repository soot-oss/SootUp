package sootup.java.bytecode.frontend;
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
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;

// FIXME: [AD] is it reasonable to get rid of it?
class BranchedInsnInfo {
  /* edge endpoint */
  @Nonnull private final AbstractInsnNode insn;
  /* previous stacks at edge */
  @Nonnull private final LinkedList<Operand[]> prevStacks;
  /* current stack at edge */
  @Nullable private final List<List<Operand>> operandStacks = new ArrayList<>();

  BranchedInsnInfo(@Nonnull AbstractInsnNode insn, @Nonnull List<Operand> operands) {
    this.insn = insn;
    this.prevStacks = new LinkedList<>();
    this.operandStacks.add(operands);
  }

  @Nonnull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  @Nonnull
  public List<List<Operand>> getOperandStacks() {
    return operandStacks;
  }

  public void addOperandStack(@Nullable List<Operand> operandStack) {
    operandStacks.add(operandStack);
  }

  @Nonnull
  public LinkedList<Operand[]> getPrevStacks() {
    return prevStacks;
  }

  public void addToPrevStack(@Nonnull Operand[] stacksOperands) {
    prevStacks.add(stacksOperands);
  }
}
