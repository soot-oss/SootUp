package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction23x;
import sootup.apk.backend.Register;

public class Instruction23x extends ThreeRegisterInstruction {

  public Instruction23x(Opcode opcode, Register registerA, Register registerB, Register registerC) {
    super(opcode, registerA, registerB, registerC);
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction23x(
        getOpcode(),
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        getRegisterC().getNumber());
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of(getRegisterA());
  }

  @Override
  public List<Register> getUseRegisters() {
    return List.of(getRegisterB(), getRegisterC());
  }

  @Override
  public BitSet getIncompatibleRegs() {
    BitSet result = new BitSet(3);
    if (!getRegisterA().fitsShort()) result.set(0);
    if (!getRegisterB().fitsShort()) result.set(1);
    if (!getRegisterC().fitsShort()) result.set(2);
    return result;
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, v{}, v{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        getRegisterC().getNumber());
  }
}
