package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.SwitchLabelElement;
import org.jf.dexlib2.builder.instruction.BuilderPackedSwitchPayload;
import org.jf.dexlib2.builder.instruction.BuilderSparseSwitchPayload;
import sootup.apk.backend.Register;
import sootup.core.jimple.common.stmt.Stmt;

public class SwitchPayload extends AbstractPayload {

  private final int[] keys;
  private final Stmt[] stmts;

  public SwitchPayload(Opcode opcode, int[] keys, Stmt[] stmts) {
    super(opcode, null);
    this.keys = keys;
    this.stmts = stmts;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    if (getOpcode().equals(Opcode.SPARSE_SWITCH)) {
      List<SwitchLabelElement> switchElements = new ArrayList<>();
      for (int i = 0; i < keys.length; i++) {
        switchElements.add(
            new SwitchLabelElement(keys[i], getLabelAssigner().getOrCreateLabel(stmts[i])));
      }
      return new BuilderSparseSwitchPayload(switchElements);

    } else {
      TreeMap<Integer, Stmt> cases = new TreeMap<>();
      for (int i = 0; i < keys.length; i++) {
        cases.put(keys[i], stmts[i]);
      }

      List<Label> switchElements = new ArrayList<>();
      Stmt defaultStmt = stmts[stmts.length - 1];
      for (int key = cases.firstKey(); key <= cases.lastKey(); key++) {
        switchElements.add(
            getLabelAssigner().getOrCreateLabel(cases.getOrDefault(key, defaultStmt)));
      }
      return new BuilderPackedSwitchPayload(keys[0], switchElements);
    }
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}

  @Override
  public void logSmali() {
    if (getOpcode().equals(Opcode.SPARSE_SWITCH)) {
      log.info(".sparse-switch");
      for (int i = 0; i < keys.length; i++) {
        String label = getLabelAssigner().getLabelName(stmts[i]);

        log.info(String.format("    0x%x -> %s", keys[i], label));
      }
      log.info(".end sparse-switch");
    } else if (getOpcode().equals(Opcode.PACKED_SWITCH)) {
      TreeMap<Integer, Stmt> cases = new TreeMap<>();
      for (int i = 0; i < keys.length; i++) {
        cases.put(keys[i], stmts[i]);
      }
      List<String> switchElements = new ArrayList<>();
      Stmt defaultStmt = stmts[stmts.length - 1];
      for (int key = cases.firstKey(); key <= cases.lastKey(); key++) {
        switchElements.add(getLabelAssigner().getLabelName(cases.getOrDefault(key, defaultStmt)));
      }
      log.info(".packed-switch 0x{}", Integer.toHexString(keys[0]));
      for (int i = 0; i < switchElements.size(); i++) {
        log.info("    {}    # {}", switchElements.get(i), keys[0] + i);
      }
      log.info(".end packed-switch");
    }
  }
}
