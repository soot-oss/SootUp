package sootup.apk.backend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.types.Type;

public class RegisterAllocator {

  private int nextRegisterNumber = 0;
  private final DexConstantVisitor dexConstantVisitor;

  private final HashMap<Value, Register> registerMap = new LinkedHashMap<>();
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
      return registerMap.get(local);
    } else {
      Register register = allocateNewRegister(local.getType(), isParameter, false);
      registerMap.put(local, register);
      return register;
    }
  }

  public Register getRegisterForValueWithNewType(Value value, Type type, boolean isParameter) {
    Register register = allocateNewRegister(type, isParameter, false);
    registerMap.put(value, register);
    return register;
  }

  public Register getRegisterForConstant(Constant constant) {
    try {
      Register register = allocateNewRegister(constant.getType(), false, false);
      dexConstantVisitor.setTargetRegister(register);
      constant.accept(dexConstantVisitor);
      return register;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Register getRegisterForImmediate(Immediate immediate, boolean isParameter) {
    if (immediate instanceof Local local) {
      return getRegisterForLocal(local, isParameter);
    } else if (immediate instanceof Constant constant) {
      return getRegisterForConstant(constant);
    }
    throw new RuntimeException("Immediate is neither local nor constant: " + immediate);
  }

  public Register getRegisterForParameter(Immediate immediate) {
    return getRegisterForImmediate(immediate, true);
  }

  public Register getEmptyRegister() {
    return new Register(0, null, false, false);
  }

  protected int getRegisterCount() {
    return registerList.stream().mapToInt(Register::getSize).sum();
  }

  protected List<Register> getRegisters() {
    return registerList;
  }
}
