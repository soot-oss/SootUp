package sootup.apk.frontend.instruction;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.apk.frontend.Util.DexUtil;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.ConcreteRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.FieldSignature;

public abstract class FieldInstruction extends DexLibAbstractInstruction {

  private FieldReference fieldReference;

  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public FieldInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private JFieldRef getSootFieldRef(FieldReference fieldReference, boolean isStatic) {
    String className = DexUtil.dottedClassName(fieldReference.getDefiningClass());
    FieldSignature fieldSignature =
        new FieldSignature(
            DexUtil.getClassTypeFromClassName(className),
            fieldReference.getName(),
            DexUtil.toSootType(fieldReference.getType(), 0));
    if (isStatic) {
      return new JStaticFieldRef(fieldSignature);
    } else {
      // TODO : Dont know which local to use here, as of now using null which will throw an error
      // for sure.

      return new JInstanceFieldRef(null, fieldSignature);
    }
  }

  /**
   * Return a static SootFieldRef for a dexlib FieldReference.
   *
   * @param fref the dexlib FieldReference.
   * @return the JFieldRef for the given field Reference
   */
  protected JFieldRef getStaticSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, true);
  }

  /**
   * Return a SootFieldRef for a dexlib FieldReference.
   *
   * @return the JFieldRef for the given field Reference
   * @param fref the dexlib FieldReference.
   */
  protected JFieldRef getSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, false);
  }

  /**
   * Check if the field type equals the type of the value that will be stored in the field. A cast
   * expression has to be introduced for the unequal case.
   *
   * @param sourceValue the local (left value) to be used in the assign statement
   * @param instanceField the reference (right value) to be used in the assign statement
   * @return assignment statement which hold a cast or not depending on the types of the operation
   */
  protected JAssignStmt getAssignStmt(Local sourceValue, ConcreteRef instanceField) {
    JAssignStmt assign;
    assign =
        Jimple.newAssignStmt(sourceValue, instanceField, StmtPositionInfo.getNoStmtPositionInfo());
    return assign;
  }
}
