package instruction;

import Util.DexUtil;
import java.util.HashSet;
import java.util.Set;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.core.types.Type;

public abstract class FilledArrayInstruction extends DexLibAbstractInstruction
    implements DanglingInstruction {
  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public FilledArrayInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public Set<Type> introducedTypes() {
    ReferenceInstruction i = (ReferenceInstruction) instruction;

    Set<Type> types = new HashSet<Type>();
    types.add(DexUtil.toSootType(((TypeReference) i.getReference()).getType(), 0));
    return types;
  }
}
