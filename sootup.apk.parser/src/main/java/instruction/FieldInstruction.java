package instruction;

import static Util.Util.dottedClassName;

import Util.DexUtil;
import java.util.HashSet;
import java.util.Set;
import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.ConcreteRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;

public abstract class FieldInstruction extends DexLibAbstractInstruction {

  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public FieldInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private JFieldRef getSootFieldRef(FieldReference fieldReference, boolean isStatic) {
    String className = dottedClassName(fieldReference.getDefiningClass());
    FieldSignature fieldSignature =
        new FieldSignature(
            Util.Util.getClassTypeFromClassName(className),
            fieldReference.getName(),
            DexUtil.toSootType(fieldReference.getType(), 0));
    if (isStatic) {
      JStaticFieldRef jStaticFieldRef = new JStaticFieldRef(fieldSignature);
      return jStaticFieldRef;
    } else {
      // TODO : Dont know which local to use here, as of now using null which will throw an error
      // for sure.
      JInstanceFieldRef jInstanceFieldRef = new JInstanceFieldRef(null, fieldSignature);
      return jInstanceFieldRef;
    }
  }

  /**
   * Return a static SootFieldRef for a dexlib FieldReference.
   *
   * @param item the dexlib FieldReference.
   */
  protected JFieldRef getStaticSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, true);
  }

  /**
   * Return a SootFieldRef for a dexlib FieldReference.
   *
   * @param item the dexlib FieldReference.
   */
  protected JFieldRef getSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, false);
  }

  /**
   * Check if the field type equals the type of the value that will be stored in the field. A cast
   * expression has to be introduced for the unequal case.
   *
   * @return assignment statement which hold a cast or not depending on the types of the operation
   */
  protected JAssignStmt getAssignStmt(DexBody body, Local sourceValue, ConcreteRef instanceField) {
    JAssignStmt assign;
    assign =
        Jimple.newAssignStmt(
            sourceValue, instanceField, StmtPositionInfo.createNoStmtPositionInfo());
    return assign;
  }

  /** Return the source register for this instruction. */
  private int sourceRegister() {
    // I hate smali's API ..
    if (instruction instanceof Instruction23x) {
      return ((Instruction23x) instruction).getRegisterA();
    } else if (instruction instanceof Instruction22c) {
      return ((Instruction22c) instruction).getRegisterA();
    } else if (instruction instanceof Instruction21c) {
      return ((Instruction21c) instruction).getRegisterA();
    } else {
      throw new RuntimeException("Instruction is not a instance, array or static op");
    }
  }

  /**
   * Return the target type for put instructions.
   *
   * <p>Putters should override this.
   *
   * @param body the body containing this instruction
   */
  protected Type getTargetType(DexBody body) {
    return UnknownType.getInstance();
  }

  @Override
  public Set<Type> introducedTypes() {
    Set<Type> types = new HashSet<Type>();
    // Aput instructions don't have references
    if (!(instruction instanceof ReferenceInstruction)) {
      return types;
    }

    ReferenceInstruction i = (ReferenceInstruction) instruction;

    FieldReference field = (FieldReference) i.getReference();

    types.add(DexUtil.toSootType(field.getType(), 0));
    types.add(DexUtil.toSootType(field.getDefiningClass(), 0));
    return types;
  }
}
