package sootup.apk.backend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public class RegisterAllocator {

  private static final Logger log = LoggerFactory.getLogger(RegisterAllocator.class);

  private int nextRegisterNumber = 0;
  private final DexConstantVisitor dexConstantVisitor;

  private HashMap<Local, Register> registerMap = new LinkedHashMap<>();
  private final List<Register> registerList = new LinkedList<>();

  public RegisterAllocator(DexConstantVisitor dexConstantVisitor) {
    this.dexConstantVisitor = dexConstantVisitor;
  }

  private Register allocateNewRegister(Type type, boolean isParameter, boolean isTmp) {
    Register newRegister = new Register(nextRegisterNumber, type, isParameter, isTmp);
    registerList.add(newRegister);
    nextRegisterNumber = newRegister.getNumber() + newRegister.getSize();
    return newRegister;
  }

  public Register getRegisterForType(Type type) {
    return allocateNewRegister(type, false, true);
  }

  private Register getRegisterForLocal(Local local, boolean isParameter) {
    if (registerMap.containsKey(local)) {
      log.info(
          "Local {} present in registerMap {} with type {}",
          local.getName(),
          registerMap.get(local).getNumber(),
          registerMap.get(local).getType());
      return registerMap.get(local);
    } else {
      Register register = allocateNewRegister(local.getType(), isParameter, false);
      registerMap.put(local, register);
      log.info(
          "Local {} not present in registerMap. Allocate new register {} with type {}",
          local.getName(),
          register.getNumber(),
          register.getType());
      return register;
    }
  }

  public Register getRegisterForValueWithNewType(Value value, Type type, boolean isParameter) {
    Register register = allocateNewRegister(type, isParameter, false);
    if (value instanceof Local local) {
      registerMap.put(local, register);
    }
    log.info("Put {} {} into register map", value, register.getNumber());
    return register;
  }

  public Register getRegisterForConstant(Constant constant, Stmt currentStmt) {
    Type type;
    boolean guessed;
    if (constant.getType().toString().equals("java.lang.Object")) {
      guessed = true;
      if (constant instanceof IntConstant) {
        type = PrimitiveType.getInt();
      } else if (constant instanceof FloatConstant) {
        type = PrimitiveType.getFloat();
      } else if (constant instanceof DoubleConstant) {
        type = PrimitiveType.getDouble();
      } else if (constant instanceof LongConstant) {
        type = PrimitiveType.getLong();
      } else if (constant instanceof BooleanConstant) {
        type = PrimitiveType.getBoolean();
      } else {
        type = constant.getType();
      }
    } else {
      type = constant.getType();
      guessed = false;
    }

    try {
      Register register = allocateNewRegister(type, false, false);
      register.setIsTypeGuessed(guessed);
      dexConstantVisitor.setTargetRegister(register);
      dexConstantVisitor.setCurrentStmt(currentStmt);
      constant.accept(dexConstantVisitor);
      return register;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Register getRegisterForImmediate(
      Immediate immediate, boolean isParameter, Stmt currentStmt) {
    if (immediate instanceof Local local) {
      return getRegisterForLocal(local, isParameter);
    } else if (immediate instanceof Constant constant) {
      return getRegisterForConstant(constant, currentStmt);
    }
    throw new RuntimeException("Immediate is neither local nor constant: " + immediate);
  }

  public Register allocateRegisterForParameter(Immediate immediate) {
    return getRegisterForImmediate(immediate, true, null);
  }

  protected void insertIntoRegisterMap(Local local, Register register) {
    registerMap.put(local, register);
  }

  protected int getRegisterCount() {
    return registerList.stream().mapToInt(Register::getSize).sum();
  }

  protected List<Register> getRegisters() {
    return registerList;
  }

  protected HashMap<Local, Register> getRegisterMap() {
    return registerMap;
  }

  protected void resetRegisterMap() {
    registerMap = new HashMap<>();
  }

  protected void setRegisterMap(HashMap<Local, Register> registerMap) {
    this.registerMap = registerMap;
  }
}
