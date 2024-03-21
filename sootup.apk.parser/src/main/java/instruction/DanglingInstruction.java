package instruction;

import main.DexBody;

public interface DanglingInstruction {
  /**
   * Finalize this instruction taking the successor into consideration.
   *
   * @param body to finalize into
   * @param successor the direct successor of this instruction
   */
  public void finalize(DexBody body, DexLibAbstractInstruction successor);
}
