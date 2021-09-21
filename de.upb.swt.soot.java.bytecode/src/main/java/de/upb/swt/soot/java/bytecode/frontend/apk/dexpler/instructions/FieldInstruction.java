package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.instructions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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


import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.ref.ConcreteRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexBody;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.DexType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import javafx.scene.Scene;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.jf.dexlib2.iface.reference.FieldReference;
import de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.Util;

import java.util.HashSet;
import java.util.Set;


public abstract class FieldInstruction extends DexlibAbstractInstruction {

  public FieldInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  /**
   * Return a static SootFieldRef for a dexlib FieldReference.
   *
   * @param fref
   *          the dexlib FieldReference.
   */
  protected FieldSignature getStaticSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, true);
  }

  /**
   * Return a SootFieldRef for a dexlib FieldReference.
   *
   * @param fref)
   *          the dexlib FieldReference.
   */
  protected FieldSignature getSootFieldRef(FieldReference fref) {
    return getSootFieldRef(fref, false);
  }

  /**
   * Return a SootFieldRef for a dexlib FieldReference.
   *
   * @param fref)
   *          the dexlib FieldReference.
   * @param isStatic
   *          if the FieldRef should be static
   */
  private FieldSignature getSootFieldRef(FieldReference fref, boolean isStatic) {
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    String className = Util.dottedClassName(fref.getDefiningClass());
    JavaClassType sc = idFactory.getClassType(className);
    FieldSubSignature fieldSubSig = new FieldSubSignature(fref.getName(), idFactory.getType(fref.getType()));
    // TODO: isStatic not needed for signature?
    return JavaIdentifierFactory.getInstance().getFieldSignature(sc, fieldSubSig);
  }

  /**
   * Check if the field type equals the type of the value that will be stored in the field. A cast expression has to be
   * introduced for the unequal case.
   *
   * @return assignment statement which hold a cast or not depending on the types of the operation
   */
  protected JAssignStmt getAssignStmt(DexBody body, Local sourceValue, ConcreteRef instanceField) {
    JAssignStmt assign;
    assign = Jimple.newAssignStmt(instanceField, sourceValue, StmtPositionInfo.createNoStmtPositionInfo());
    return assign;
  }

  @Override
  boolean isUsedAsFloatingPoint(DexBody body, int register) {
    return sourceRegister() == register && Util.isFloatLike(getTargetType(body));
  }

  /**
   * Return the source register for this instruction.
   */
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
   * Putters should override this.
   *
   * @param body
   *          the body containing this instruction
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

    types.add(DexType.toSoot(field.getType()));
    types.add(DexType.toSoot(field.getDefiningClass()));
    return types;
  }
}
