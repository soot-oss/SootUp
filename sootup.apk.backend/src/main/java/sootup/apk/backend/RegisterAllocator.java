package sootup.apk.backend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.types.Type;

public class RegisterAllocator {

  private int nextRegister = 0;
  private final DexConstantVisitor dexConstantVisitor;

  private final HashMap<Value, Register> registerMap = new LinkedHashMap<>();

  public RegisterAllocator(DexConstantVisitor dexConstantVisitor) {
    this.dexConstantVisitor = dexConstantVisitor;
  }

  private Register allocateNewRegister(Type type) {
    Register newRegister = new Register(nextRegister, type);
    nextRegister = newRegister.getNumber() + newRegister.getSize();
    return newRegister;
  }

  public Register getRegisterForType(Type type) {
    return allocateNewRegister(type);
  }

  private Register getRegisterForValue(Value value) {
    if (registerMap.containsKey(value)) {
      return registerMap.get(value);
    } else {
      Register register = allocateNewRegister(value.getType());
      registerMap.put(value, register);
      return register;
    }
  }

  public Register getRegisterForConstant(Constant constant) {
    try {
      Register register = allocateNewRegister(constant.getType());
      dexConstantVisitor.setTargetRegister(register);
      constant.accept(dexConstantVisitor);
      return register;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Register getRegisterForImmediate(Immediate immediate) {
    if (immediate instanceof Local local) {
      return getRegisterForValue(local);
    } else if (immediate instanceof Constant constant) {
      return getRegisterForConstant(constant);
    }
    throw new RuntimeException("Immediate is neither local nor constant: " + immediate);
  }

  public Register getEmptyRegister() {
    return new Register(0, null);
  }
}
