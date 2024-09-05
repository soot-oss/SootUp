package sootup.apk.frontend.instruction;

import sootup.apk.frontend.main.DexBody;

public interface DeferableInstruction {
  /**
   * Jimplify this instruction with the guarantee that every other (non-deferred) instruction has
   * been jimplified.
   *
   * @param body to jimplify into
   */
  public void deferredJimplify(DexBody body);
}
