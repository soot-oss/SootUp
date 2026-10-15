package sootup.apk.backend;

import static sootup.apk.backend.DexExprVisitor.generateMoveInstruction;

import java.util.*;
import java.util.stream.Collectors;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.backend.instructions.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.UnknownType;

public class RegisterAssigner {

  private static class InstructionIterator implements Iterator<AbstractInstruction> {

    private final ListIterator<AbstractInstruction> insnsIterator;
    private final Map<AbstractInstruction, Stmt> insnStmtMap;

    public InstructionIterator(
        List<AbstractInstruction> insns, Map<AbstractInstruction, Stmt> insnStmtMap) {
      this.insnStmtMap = insnStmtMap;
      this.insnsIterator = insns.listIterator();
    }

    @Override
    public boolean hasNext() {
      return insnsIterator.hasNext();
    }

    @Override
    public AbstractInstruction next() {
      return insnsIterator.next();
    }

    public AbstractInstruction previous() {
      return insnsIterator.previous();
    }

    @Override
    public void remove() {
      this.insnsIterator.remove();
    }

    public void add(
        AbstractInstruction element, AbstractInstruction forOriginal, Register newRegister) {

      if (this.insnStmtMap.containsKey(forOriginal)) {
        this.insnStmtMap.put(element, insnStmtMap.get(forOriginal));
      }
      this.insnsIterator.add(element);
    }

    public void set(AbstractInstruction element, AbstractInstruction original) {
      Stmt stmt = insnStmtMap.remove(original);
      if (stmt != null) {
        insnStmtMap.put(element, stmt);
      }
      insnsIterator.set(element);
    }
  }

  private final RegisterAllocator registerAllocator;
  private final DexMethodBuilder dexMethodBuilder;

  public RegisterAssigner(RegisterAllocator registerAllocator, DexMethodBuilder dexMethodBuilder) {
    this.registerAllocator = registerAllocator;
    this.dexMethodBuilder = dexMethodBuilder;
  }

  public void prepareRegisters(
      Collection<List<AbstractInstruction>> allInstructions,
      Map<AbstractInstruction, Stmt> insnsStmtMap) {

    renumParamRegsToHigh();

    List<AbstractInstruction> allInsns =
        allInstructions.stream().flatMap(Collection::stream).collect(Collectors.toList());

    reserveRegisters(allInsns, insnsStmtMap);
  }

  public List<AbstractInstruction> finishRegs(
      List<AbstractInstruction> insns, Map<AbstractInstruction, Stmt> insnsStmtMap) {

    InstructionIterator insnIter = new InstructionIterator(insns, insnsStmtMap);

    while (insnIter.hasNext()) {
      AbstractInstruction oldInsn = insnIter.next();
      oldInsn.logSmali();

      if (oldInsn.hasIncompatibleRegs()) {
        AbstractInstruction fittingInsn = findFittingInsn(oldInsn);

        if (fittingInsn != null) {
          insnIter.set(fittingInsn, oldInsn);
        } else {
          insns = fixIncompatRegs(insns, insnsStmtMap);
        }
      }
    }

    return insns;
  }

  private void renumParamRegsToHigh() {
    List<Register> sortedRegisters =
        registerAllocator.getRegisters().stream()
            .sorted(Comparator.comparing(Register::isParameter))
            .toList();

    int registerIndex = 0;

    for (Register r : sortedRegisters) {
      r.setNumber(registerIndex);
      registerIndex += r.getSize();
    }
  }

  private void reserveRegisters(
      List<AbstractInstruction> insns, Map<AbstractInstruction, Stmt> insnsStmtMap) {
    // reserve registers as long as new ones are needed
    int reservedRegs = 0;
    while (true) {
      int regsNeeded = getRegsNeeded(reservedRegs, insns, insnsStmtMap);
      int regsToReserve = regsNeeded - reservedRegs;
      if (regsToReserve <= 0) {
        break;
      }

      registerAllocator.addReservedRegisters(regsToReserve);

      // "reservation": shift the old regs to higher numbers
      shiftRegs(regsToReserve);
      reservedRegs += regsToReserve;
    }
  }

  private int getRegsNeeded(
      int regsAlreadyReserved,
      List<AbstractInstruction> insns,
      Map<AbstractInstruction, Stmt> insnsStmtMap) {
    int regsNeeded = regsAlreadyReserved; // we only need regs that weren't
    // reserved yet
    for (int i = 0; i < insns.size(); i++) {
      AbstractInstruction insn = insns.get(i);
      Stmt stmt = insnsStmtMap.get(insn);
      // first try to find a better opcode
      AbstractInstruction fittingInsn = findFittingInsn(insn);
      if (fittingInsn != null) {
        // use the fitting instruction and continue with next one
        insns.set(i, fittingInsn);
        insnsStmtMap.put(fittingInsn, stmt);
        // insnsStmtMap.remove(insn);
        continue;
      }
      // no fitting instruction -> save if we need more registers
      int newRegsNeeded = insn.getMinimumRegsNeeded();
      if (newRegsNeeded > regsNeeded) {
        regsNeeded = newRegsNeeded;
      }
    }
    return regsNeeded;
  }

  private void shiftRegs(int shiftAmount) {
    for (Register r : registerAllocator.getRegisters()) {
      r.setNumber(r.getNumber() + shiftAmount);
    }
  }

  public List<AbstractInstruction> fixIncompatRegs(
      List<AbstractInstruction> instructions, Map<AbstractInstruction, Stmt> insnsStmtMap) {

    List<AbstractInstruction> finalList = new ArrayList<>();

    for (int i = 0; i < instructions.size(); i++) {

      AbstractInstruction instruction = instructions.get(i);
      List<Register> registers = instruction.getRegisters();
      BitSet incompatRegs = instruction.getIncompatibleRegs();
      int tmpIndx = 0;
      if (!(instruction instanceof Instruction3rc
          || instruction instanceof Instruction4rcc
          || (instruction instanceof Instruction12x
              && instruction.getOpcode().name.startsWith("move"))
          || instruction instanceof Instruction22x
          || instruction instanceof Instruction32x
          || instruction instanceof Instruction21c
              && instruction.getOpcode().equals(Opcode.CHECK_CAST))) {

        LinkedHashMap<Register, Register> registerHashMap = new LinkedHashMap<>();

        List<Register> defRegisters = new ArrayList<>(instruction.getDefRegisters());
        List<Register> useRegisters = new ArrayList<>(instruction.getUseRegisters());

        for (int index = 0; index < incompatRegs.size(); index++) {

          boolean isIncompatible = incompatRegs.get(index);

          if (!isIncompatible) {
            continue;
          }

          Register r = registers.get(index);
          Register tmpRegister = new Register(tmpIndx, UnknownType.getInstance(), false, true);
          tmpRegister.setType(r.getType());
          tmpRegister.setIsTypeGuessed(r.isTypeGuessed());
          instruction.changeRegister(r, tmpRegister);
          if (useRegisters.contains(r) || r.isParameter()) {
            AbstractInstruction move =
                generateMoveInstruction(tmpRegister, r, r.getType(), false, null, null);
            finalList.add(move);
            insnsStmtMap.put(move, insnsStmtMap.get(instruction));
          }
          registerHashMap.put(tmpRegister, r);
          tmpIndx += DexUtil.isWide(r.getType()) ? 2 : 1;
        }

        finalList.add(instruction);

        if (instruction.getOpcode().name.startsWith("throw")
            || instruction.getOpcode().name.startsWith("return")
            || instruction.getOpcode().name.startsWith("if")
            || instruction.getOpcode().equals(Opcode.PACKED_SWITCH)
            || instruction.getOpcode().equals(Opcode.SPARSE_SWITCH)) {
          registerHashMap.clear();
        }

        if (instructions.size() > i + 1
            && (instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT)
                || instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT_OBJECT)
                || instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT_WIDE))) {
          continue;
        }

        // reverse list of registers in order to insert the targetRegister at the end
        // ensures that the targetRegister is not overwritten
        List<Map.Entry<Register, Register>> reverseRegisters =
            new ArrayList<>(registerHashMap.entrySet());
        Collections.reverse(reverseRegisters);

        // move tmpRegisters back to their original registers
        reverseRegisters.forEach(
            r -> {
              if (defRegisters.contains(r.getValue())) {
                Register tmpRegister = r.getKey();
                Register originalRegister = r.getValue();
                AbstractInstruction move =
                    generateMoveInstruction(
                        originalRegister,
                        tmpRegister,
                        originalRegister.getType(),
                        false,
                        null,
                        null);
                finalList.add(move);
                insnsStmtMap.put(move, insnsStmtMap.get(instruction));
              }
            });

      } else if (instruction instanceof Instruction21c instruction21c
          && instruction.getOpcode().equals(Opcode.CHECK_CAST)) {
        Register tmpRegister = new Register(tmpIndx, UnknownType.getInstance(), false, true);
        Register originalRegister = registers.get(0);
        tmpRegister.setType(originalRegister.getType());
        instruction.changeRegister(originalRegister, tmpRegister);
        AbstractInstruction move =
            generateMoveInstruction(
                tmpRegister, originalRegister, originalRegister.getType(), false, null, null);
        finalList.add(move);
        insnsStmtMap.put(move, insnsStmtMap.get(instruction));
        finalList.add(instruction);
        TypeReference typeReference = (TypeReference) instruction21c.getReference();
        AbstractInstruction move2 =
            generateMoveInstruction(
                originalRegister,
                tmpRegister,
                sootup.apk.frontend.Util.DexUtil.toSootType(
                    typeReference.getType(), 0, dexMethodBuilder.getView().getIdentifierFactory()),
                false,
                null,
                null);
        finalList.add(move2);
        insnsStmtMap.put(move2, insnsStmtMap.get(instruction));
      } else if ((instruction instanceof Instruction12x
              && instruction.getOpcode().name.startsWith("move"))
          || instruction instanceof Instruction22x
          || instruction instanceof Instruction32x) {

        AbstractInstruction move;
        if (instruction.getOpcode().name.startsWith("move-object")) {
          move =
              generateMoveInstructionObject(
                  instruction.getRegisters().get(0), instruction.getRegisters().get(1));
        } else if (instruction.getOpcode().name.startsWith("move-wide")) {
          move =
              generateMoveInstructionWide(
                  instruction.getRegisters().get(0), instruction.getRegisters().get(1));
        } else {
          move =
              generateMoveInstructionDefault(
                  instruction.getRegisters().get(0), instruction.getRegisters().get(1));
        }
        finalList.add(move);
        insnsStmtMap.put(move, insnsStmtMap.get(instruction));
      } else {
        finalList.add(instruction);
      }
    }
    return finalList;
  }

  protected AbstractInstruction generateMoveInstructionObject(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE_OBJECT, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_OBJECT_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_OBJECT_16, targetR, sourceRegister);
    }
  }

  protected AbstractInstruction generateMoveInstructionWide(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE_WIDE, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_WIDE_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_WIDE_16, targetR, sourceRegister);
    }
  }

  protected AbstractInstruction generateMoveInstructionDefault(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_16, targetR, sourceRegister);
    }
  }

  private AbstractInstruction findFittingInsn(AbstractInstruction insn) {
    if (!insn.hasIncompatibleRegs()) {
      return null; // no incompatible regs -> no fitting needed
    }
    // we expect the dex specification to rarely change, so we hard-code the
    // mapping "unfitting -> fitting"
    Opcode opcode = insn.getOpcode();
    if (insn instanceof Instruction11n unfittingInsn && opcode.equals(Opcode.CONST_4)) {
      // const-4 (11n, byteReg) -> const-16 (21s, shortReg)
      if (unfittingInsn.getRegisterA().fitsShort()) {
        return new Instruction21s(
            Opcode.CONST_16, unfittingInsn.getRegisterA(), unfittingInsn.getValue());
      }
    } else if (insn instanceof TwoRegisterInstruction && opcode.name.endsWith("_2ADDR")) {
      // */2addr (12x, byteReg,byteReg) -> * (23x,
      // shortReg,shortReg,shortReg)
      Register regA = ((TwoRegisterInstruction) insn).getRegisterA();
      Register regB = ((TwoRegisterInstruction) insn).getRegisterB();
      if (regA.fitsShort() && regB.fitsShort()) {
        // use new opcode without the "/2addr"
        int oldOpcLength = opcode.name.length();
        String newOpcName = opcode.name.substring(0, oldOpcLength - 6);
        Opcode newOpc = Opcode.valueOf(newOpcName);
        Register regAClone = regA.clone();
        return new Instruction23x(newOpc, regA, regAClone, regB);
      }
    } else if (insn instanceof TwoRegisterInstruction
        && opcode.name.startsWith("move")
        && !opcode.name.startsWith("move-result")) {
      /*
       * move+ (12x, byteReg,byteReg) -> move+/from16 (22x, shortReg,unconstReg) -> move+/16 (32x, unconstReg,unconstReg)
       * where "+" is "", "-object" or "-wide"
       */
      Register regA = ((TwoRegisterInstruction) insn).getRegisterA();
      Register regB = ((TwoRegisterInstruction) insn).getRegisterB();
      if (regA.getNumber() != regB.getNumber()) {
        return generateMoveInstruction(regA, regB, regB.getType(), false, null, null);
      }
    }
    // no fitting insn found
    return null;
  }
}
