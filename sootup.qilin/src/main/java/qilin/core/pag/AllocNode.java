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
import qilin.core.context.ContextElement;
import qilin.util.Numberable;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

/**
 * Represents an allocation site node in the pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class AllocNode extends PagNode implements ContextElement, Numberable {
  protected Object newExpr;
  private final SootMethod method;

  public AllocNode(Object newExpr, Type t, SootMethod m) {
    super(t);
    this.method = m;
    this.newExpr = newExpr;
  }

  /**
   * Returns the new expression of this allocation site. This is usually a Jimple {@link Value}
   * (e.g. {@link sootup.core.jimple.common.expr.JNewExpr}, {@link
   * sootup.core.jimple.common.expr.JNewArrayExpr}), but for synthetic allocation sites (heap
   * merging, the root node, ...) it can also be a plain {@link String} label or another sentinel
   * object. Use {@link #getAllocationExpr()} if you only care about the common "real expression"
   * case.
   */
  public Object getNewExpr() {
    return newExpr;
  }

  /**
   * Returns this allocation site's expression as a Jimple {@link Value}, or {@code null} if the
   * underlying object is a synthetic sentinel (e.g. a merged/root heap label) rather than a real
   * expression — see {@link #getNewExpr()}.
   */
  public Value getAllocationExpr() {
    return newExpr instanceof Value ? (Value) newExpr : null;
  }

  public String toString() {
    return "AllocNode " + getNumber() + " " + newExpr + " in method " + method;
  }

  public SootMethod getMethod() {
    return method;
  }

  public AllocNode base() {
    return this;
  }

  /** True if this allocation site is a {@code new T[0]}-shaped, statically-empty array. */
  public boolean isEmptyArray() {
    if (newExpr instanceof JNewArrayExpr nae) {
      Value sizeVal = nae.getSize();
      if (sizeVal instanceof IntConstant size) {
        return size.getValue() == 0;
      }
    }
    return false;
  }

  @Override
  public PagNode parameterize(Parameterizer parameterizer, Context context) {
    return parameterizer.parameterize(this, context);
  }
}
