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

import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;

/**
 * An allocation site standing in for the object a {@code LambdaMetafactory}-bootstrapped
 * invokedynamic call site produces (a lambda or method reference). Unlike a plain {@link
 * AllocNode}, its true implementation is known statically from the bootstrap's {@link MethodHandle}
 * constant - {@link #getTargetMethod()}/{@link #getTargetKind()} let call-dispatch resolve calls on
 * it directly instead of through the (nonexistent) functional-interface vtable.
 */
public class LambdaAllocNode extends AllocNode {

  /** The resolved target of a lambda/method-reference alloc, as read off its bootstrap args. */
  public record Target(MethodSignature method, MethodHandle.Kind kind) {}

  private final MethodSignature targetMethod;
  private final MethodHandle.Kind targetKind;

  public LambdaAllocNode(
      Object newExpr,
      Type type,
      SootMethod m,
      MethodSignature targetMethod,
      MethodHandle.Kind targetKind) {
    super(newExpr, type, m);
    this.targetMethod = targetMethod;
    this.targetKind = targetKind;
  }

  public MethodSignature getTargetMethod() {
    return targetMethod;
  }

  public MethodHandle.Kind getTargetKind() {
    return targetKind;
  }
}
