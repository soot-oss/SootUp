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

import sootup.callgraph.invokedynamic.FunctionalObject;
import sootup.core.model.SootMethod;
import sootup.core.types.Type;

/**
 * An allocation site standing in for the object a {@code LambdaMetafactory}-bootstrapped
 * invokedynamic call site produces (a lambda or method reference). Unlike a plain {@link
 * AllocNode}, its implementation is known statically: calls on it that {@link
 * FunctionalObject#answers answer} the {@link #getFunctionalObject() functional object} dispatch
 * straight to that implementation instead of through the (nonexistent) functional-interface vtable.
 */
public class LambdaAllocNode extends AllocNode {

  private final FunctionalObject functionalObject;

  public LambdaAllocNode(
      Object newExpr, Type type, SootMethod m, FunctionalObject functionalObject) {
    super(newExpr, type, m);
    this.functionalObject = functionalObject;
  }

  public FunctionalObject getFunctionalObject() {
    return functionalObject;
  }
}
