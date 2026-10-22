package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.DexUtil;
import sootup.apk.backend.LabelAssigner;
import sootup.apk.backend.Register;

public abstract class AbstractInstruction {

  protected static final Logger log = LoggerFactory.getLogger(AbstractInstruction.class);

  private final Opcode opcode;
  private LabelAssigner labelAssigner;
  private List<Register> registers;

  public AbstractInstruction(Opcode opcode, List<Register> registers) {
    this.opcode = opcode;
    this.registers = registers;
  }

  public Opcode getOpcode() {
    return opcode;
  }

  public abstract BuilderInstruction getBuilderInstruction();

  public List<Register> getRegisters() {
    return registers;
  }

  public abstract List<Register> getDefRegisters();

  public abstract List<Register> getUseRegisters();

  public void setRegisters(List<Register> registers) {
    this.registers = registers;
  }

  /** Replaces one register of an instruction by another register */
  public abstract void changeRegister(Register oldRegister, Register newRegister);

  public abstract void logSmali();

  public void setLabelAssigner(LabelAssigner labelAssigner) {
    this.labelAssigner = labelAssigner;
  }

  public LabelAssigner getLabelAssigner() {
    return labelAssigner;
  }

  public BitSet getIncompatibleRegs() {
    return new BitSet(0);
  }

  public boolean hasIncompatibleRegs() {
    return getIncompatibleRegs().cardinality() > 0;
  }

  /**
   * Evaluates how many temporary registers with low register numbers are needed. Counts
   * incompatible registers
   *
   * @return The summed size of incompatible registers
   */
  public int getMinimumRegsNeeded() {
    BitSet incompatible = getIncompatibleRegs();
    return IntStream.range(0, registers.size())
        .filter(incompatible::get)
        .map(i -> DexUtil.getRegisterSizeCount(registers.get(i).getType()))
        .sum();
  }
}
