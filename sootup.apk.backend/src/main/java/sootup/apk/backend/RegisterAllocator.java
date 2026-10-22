package sootup.apk.backend;

import static sootup.apk.backend.Constants.JIMPLE_OBJECT_TYPE;

import java.util.*;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public class RegisterAllocator {

  private int nextRegisterNumber = 0;
  private final DexConstantVisitor dexConstantVisitor;

  private int reservedRegisters;

  private Map<Local, Register> registerMap;
  private final List<Register> registerList = new LinkedList<>();

  private final Map<Class<?>, List<Register>> constantRegisterPools = new HashMap<>();
  private final Map<Class<?>, Integer> constantPoolIndices = new HashMap<>();

  private final Set<Register> lockedRegisters = new HashSet<>();

  public RegisterAllocator(DexConstantVisitor dexConstantVisitor) {
    this.dexConstantVisitor = dexConstantVisitor;
    this.reservedRegisters = 0;
    this.registerMap = new LinkedHashMap<>();
  }

  /**
   * Creates a new register with a unique register number
   *
   * @param type Jimple type of the value stored in the register
   * @param isParameter Is the local a parameter
   * @param isTmp Is the register temporary
   * @return The new register
   */
  private Register allocateNewRegister(Type type, boolean isParameter, boolean isTmp) {
    Register newRegister = new Register(nextRegisterNumber, type, isParameter, isTmp);
    registerList.add(newRegister);
    nextRegisterNumber = newRegister.getNumber() + newRegister.getSize();
    return newRegister;
  }

  /**
   * Creates a temporary register with a specific type
   *
   * @param type The type of the register
   * @return The new register
   */
  public Register getRegisterForType(Type type) {
    return allocateNewRegister(type, false, true);
  }

  /**
   * Returns a register for a SootUp local If the local is already assigned to a register, the
   * register is returned Else a new register is created
   *
   * @param local The SootUp local stored in the register
   * @param isParameter Is the local a method parameter
   * @return The new/reused register
   */
  private Register getRegisterForLocal(Local local, boolean isParameter) {
    if (registerMap.containsKey(local)) {
      return registerMap.get(local);
    } else {
      Register register = allocateNewRegister(local.getType(), isParameter, false);
      registerMap.put(local, register);
      return register;
    }
  }

  /**
   * Assign a SootUp value to a new register, regardless of whether the value is already known by a
   * different register Used in cases where a local is assigned to different types (e.g.,
   * primitive/object) within one method
   *
   * @param value The SootUp value
   * @param type The new type for the register
   * @param isParameter Is the value a method parameter
   * @return The generated register
   */
  public Register getRegisterForValueWithNewType(Value value, Type type, boolean isParameter) {
    Register register = allocateNewRegister(type, isParameter, false);
    if (value instanceof Local local) {
      registerMap.put(local, register);
    }
    return register;
  }

  /**
   * Get a register for a temporary constant Uses a constant pool in order to reuse registers
   *
   * @param constant The SootUp constant
   * @param currentStmt The statement where the constant is needed
   * @return A reusable register
   */
  public Register getRegisterForConstant(Constant constant, Stmt currentStmt) {
    Type type;
    boolean guessed;
    if (constant.getType().toString().equals(JIMPLE_OBJECT_TYPE)) {
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
      Register register = getReusableConstantRegister(constant, type, guessed);

      register.setIsTypeGuessed(guessed);

      dexConstantVisitor.setTargetRegister(register);
      dexConstantVisitor.setCurrentStmt(currentStmt);
      constant.accept(dexConstantVisitor);

      return register;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Gets a register for a SootUp Immediate */
  public Register getRegisterForImmediate(
      Immediate immediate, boolean isParameter, Stmt currentStmt) {
    if (immediate instanceof Local local) {
      return getRegisterForLocal(local, isParameter);
    } else if (immediate instanceof Constant constant) {
      return getRegisterForConstant(constant, currentStmt);
    }
    throw new RuntimeException("Immediate is neither local nor constant: " + immediate);
  }

  /**
   * Reserve a register for a method parameter
   *
   * @param immediate The SootUp method parameter
   * @return The reserved register
   */
  public Register allocateRegisterForParameter(Immediate immediate) {
    return getRegisterForImmediate(immediate, true, null);
  }

  private List<Register> getConstantPool(Constant constant) {
    return constantRegisterPools.computeIfAbsent(constant.getClass(), key -> new LinkedList<>());
  }

  private Register getReusableConstantRegister(Constant constant, Type type, boolean guessed) {

    List<Register> pool = getConstantPool(constant);

    int index = constantPoolIndices.getOrDefault(constant.getClass(), 0);

    while (true) {

      if (index >= pool.size()) {
        Register register = allocateNewRegister(type, false, false);

        register.setIsTypeGuessed(guessed);

        pool.add(register);
        constantPoolIndices.put(constant.getClass(), index + 1);

        return register;
      }

      Register register = pool.get(index);
      index++;

      constantPoolIndices.put(constant.getClass(), index);

      if (lockedRegisters.contains(register)) {
        continue;
      }

      register.setIsTypeGuessed(guessed);

      return register;
    }
  }

  public void lockRegister(Register register) {
    lockedRegisters.add(register);
  }

  protected void resetConstantRegisterPools() {
    constantPoolIndices.clear();
  }

  protected void insertIntoRegisterMap(Local local, Register register) {
    registerMap.put(local, register);
  }

  protected int getRegisterCount() {
    return registerList.stream().mapToInt(Register::getSize).sum() + reservedRegisters;
  }

  protected List<Register> getRegisters() {
    return registerList;
  }

  protected Map<Local, Register> getRegisterMap() {
    return registerMap;
  }

  protected void resetRegisterMap() {
    registerMap = new HashMap<>();
  }

  protected void setRegisterMap(Map<Local, Register> registerMap) {
    this.registerMap = registerMap;
  }

  public void addReservedRegisters(int number) {
    this.reservedRegisters += number;
  }
}
