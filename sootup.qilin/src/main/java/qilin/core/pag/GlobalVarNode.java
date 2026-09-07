/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.pag;

import qilin.core.context.Context;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.Type;

/**
 * Represents a simple variable node in the pointer assignment graph that is not associated with any
 * particular method invocation. Unlike {@link LocalVarNode}, whose underlying variable (see {@link
 * #getVariable()}) can be several different kinds of synthetic key, a {@code GlobalVarNode}'s
 * variable is always exactly one of {@link FieldSignature} (a static field), {@link
 * StringConstant}, or {@link ClassConstant} — see {@link PAG#makeGlobalVarNode(Object, Type)}. Use
 * {@link #getFieldSignature()}/{@link #getStringConstant()}/{@link #getClassConstant()} instead of
 * casting {@link #getVariable()} yourself.
 *
 * @author Ondrej Lhotak
 */
public class GlobalVarNode extends VarNode {
  public GlobalVarNode(Object variable, Type t) {
    super(variable, t);
  }

  @Override
  public VarNode base() {
    return this;
  }

  @Override
  public SootMethod getMethod() {
    return null;
  }

  /** Returns the underlying static field, or {@code null} if this node represents a constant. */
  public FieldSignature getFieldSignature() {
    return variable instanceof FieldSignature ? (FieldSignature) variable : null;
  }

  /** Returns the underlying string constant, or {@code null} if this node represents a field. */
  public StringConstant getStringConstant() {
    return variable instanceof StringConstant ? (StringConstant) variable : null;
  }

  /** Returns the underlying class constant, or {@code null} if this node represents a field. */
  public ClassConstant getClassConstant() {
    return variable instanceof ClassConstant ? (ClassConstant) variable : null;
  }

  public String toString() {
    return "GlobalVarNode " + getNumber() + " " + variable;
  }

  @Override
  public PagNode parameterize(Parameterizer parameterizer, Context context) {
    return parameterizer.parameterize(this, context);
  }
}
