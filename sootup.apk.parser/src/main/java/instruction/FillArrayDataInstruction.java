package instruction;

import Util.DexUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.ArrayPayload;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction31t;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class FillArrayDataInstruction extends PseudoInstruction {

  private static final Logger logger = LoggerFactory.getLogger(FillArrayDataInstruction.class);

  @Override
  public void jimplify(DexBody body) {}

  public FillArrayDataInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void computeDataOffsets(DexBody body) {
    if (!(instruction instanceof Instruction31t)) {
      throw new IllegalArgumentException(
          "Expected Instruction31t but got: " + instruction.getClass());
    }

    Instruction31t fillArrayInstr = (Instruction31t) instruction;
    int destRegister = fillArrayInstr.getRegisterA();
    int offset = fillArrayInstr.getCodeOffset();
    int targetAddress = codeAddress + offset;

    Instruction referenceTable = body.instructionAtAddress(targetAddress).instruction;

    if (!(referenceTable instanceof ArrayPayload)) {
      throw new RuntimeException(
          "Address " + targetAddress + "refers to an invalid PseudoInstruction.");
    }

    ArrayPayload arrayTable = (ArrayPayload) referenceTable;

    Local arrayReference = body.getRegisterLocal(destRegister);
    List<Number> elements = arrayTable.getArrayElements();
    int numElements = elements.size();

    Stmt firstAssign = null;
    for (int i = 0; i < numElements; i++) {
      JArrayRef jArrayRef =
          JavaJimple.getInstance().newArrayRef(arrayReference, IntConstant.getInstance(i));
      NumericConstant element = getArrayElement(elements.get(i), body, destRegister);
      if (element == null) {
        break;
      }
      JAssignStmt jAssignStmt =
          Jimple.newAssignStmt(
              arrayReference, element, StmtPositionInfo.getNoStmtPositionInfo());
      body.add(jAssignStmt);
      if (i == 0) {
        firstAssign = jAssignStmt;
      }
    }
    if (firstAssign == null) { // if numElements == 0. Is it possible?
      firstAssign = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
      body.add(firstAssign);
    }
    setStmt(firstAssign);
  }

  private NumericConstant getArrayElement(Number element, DexBody dexBody, int arrayRegister) {
    List<DexLibAbstractInstruction> instructions = dexBody.instructionsBefore(this);
    Set<Integer> usedRegisters = new HashSet<Integer>();
    usedRegisters.add(arrayRegister);
    Type elementType = null;
    Outer:
    for (DexLibAbstractInstruction i : instructions) {
      if (usedRegisters.isEmpty()) {
        break;
      }
      for (int register : usedRegisters) {
        if (i instanceof NewArrayInstruction) {
          NewArrayInstruction newArrayInstruction = (NewArrayInstruction) i;
          Instruction22c instruction22c = (Instruction22c) newArrayInstruction.instruction;
          if (instruction22c.getRegisterA() == register) {
            ArrayType arrayType =
                (ArrayType)
                    DexUtil.toSootType(
                        ((TypeReference) instruction22c.getReference()).getType(), 0);
            elementType = arrayType.getElementType();
            break Outer;
          }
        }
      }
      // // look for obsolete registers
      // for (int reg : usedRegisters) {
      // if (i.overridesRegister(reg)) {
      // usedRegisters.remove(reg);
      // break; // there can't be more than one obsolete
      // }
      // }
      for (int reg : usedRegisters) {
        int newRegister = i.movesToRegister(reg);
        if (newRegister != -1) {
          usedRegisters.add(newRegister);
          usedRegisters.remove(reg);
          break; // there can't be more than one new
        }
      }
    }

    if (elementType == null) {
      // throw new InternalError("Unable to find array type to type array elements!");
      logger.warn(
          "Unable to find array type to type array elements! Array was not defined! (obfuscated bytecode?)");
      return null;
    }
    NumericConstant value;

    if (elementType instanceof PrimitiveType.BooleanType) {
      value = IntConstant.getInstance(element.intValue());
      IntConstant ic = (IntConstant) value;
      if (ic.getValue() != 0) {
        value = IntConstant.getInstance(1);
      }
    } else if (elementType instanceof PrimitiveType.ByteType) {
      value = IntConstant.getInstance(element.byteValue());
    } else if (elementType instanceof PrimitiveType.CharType
        || elementType instanceof PrimitiveType.ShortType) {
      value = IntConstant.getInstance(element.shortValue());
    } else if (elementType instanceof PrimitiveType.DoubleType) {
      value = DoubleConstant.getInstance(Double.longBitsToDouble(element.longValue()));
    } else if (elementType instanceof PrimitiveType.FloatType) {
      value = FloatConstant.getInstance(Float.intBitsToFloat(element.intValue()));
    } else if (elementType instanceof PrimitiveType.IntType) {
      value = IntConstant.getInstance(element.intValue());
    } else if (elementType instanceof PrimitiveType.LongType) {
      value = LongConstant.getInstance(element.longValue());
    } else {
      throw new RuntimeException(
          "Invalid Array Type occured in FillArrayDataInstruction: " + elementType);
    }
    return value;
  }
}
