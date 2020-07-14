package de.upb.swt.soot.java.bytecode.frontend;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;

// FIXME: [AD] is it reasonable to get rid of it?
final class BranchedInsnInfo {
  /* edge endpoint */
  @Nonnull private final AbstractInsnNode insn;
  /* previous stacks at edge */
  private final LinkedList<Operand[]> prevStacks;
  /* current stack at edge */
  @Nullable private List<Operand> stack;

  BranchedInsnInfo(AbstractInsnNode insn) {
    this.insn = insn;
    this.prevStacks = new LinkedList<>();
    this.stack = new ArrayList<>();
  }

  BranchedInsnInfo(AbstractInsnNode insn, List<Operand> opr) {
    this.insn = insn;
    this.prevStacks = new LinkedList<>();
    this.stack = opr;
  }

  public List<Operand> getStack() {
    return stack;
  }

  public void replaceStack(@Nullable List<Operand> stack) {
    this.stack = new ArrayList<>(stack);
  }

  @Nonnull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  public LinkedList<Operand[]> getPrevStacks() {
    return prevStacks;
  }

  public void addToPrevStack(Operand[] stackss) {
    prevStacks.add(stackss);
  }
}
